package com.aricontroller;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.concat;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static com.aricontroller.AriLogger.LogMarker.INFRASTRUCTURE;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.ChildFailed;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.kafka.CommitterSettings;
import akka.kafka.ConsumerMessage.CommittableMessage;
import akka.kafka.ConsumerMessage.CommittableOffset;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Committer;
import akka.kafka.javadsl.Consumer;
import akka.stream.Materializer;
import akka.stream.RestartSettings;
import akka.stream.javadsl.RestartSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.aricontroller.boundary.messaging.inbox.ControllerInboxReceiver;
import com.aricontroller.boundary.messaging.publish.KafkaJsonPublisher;
import com.aricontroller.control.controller.ControllerMessage;
import com.aricontroller.control.domainobjects.publishing.PublisherIntent;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import com.aricontroller.control.routing.ControllerParent;
import com.aricontroller.control.routing.ControllerParentMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.time.Duration;
import java.util.function.Function;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public final class Orchestrator {

  public static final String CONTROLLER_PARENT_ACTOR_NAME = "controllerParent";
  private static final AriLogger LOGGER = new AriLogger(Orchestrator.class);
  private static final Duration MIN_SOURCE_BACKOFF_DURATION = Duration.ofSeconds(5);
  private static final Duration MAX_SOURCE_BACKOFF_DURATION = Duration.ofSeconds(10);
  private static final double SOURCE_BACKOFF_RANDOM_FACTOR = .2;
  private static final Duration MIN_SUPERVISION_BACKOFF_DURATION = Duration.ofSeconds(1);
  private static final Duration MAX_SUPERVISION_BACKOFF_DURATION = Duration.ofSeconds(30);
  private static final double SUPERVISION_BACKOFF_RANDOM_FACTOR = .1;
  private static final int MAX_SUPERVISION_RESTARTS = 3;

  private Orchestrator() {
    throw new IllegalStateException("Utility class");
  }

  public static Behavior<Void> create(
      final Function<RoutingKey, Behavior<ControllerMessage>> controllerBehaviorSupplier) {
    return Behaviors.supervise(
            Behaviors.<Void>setup(
                context -> {
                  context.setLoggerName("akka." + Orchestrator.class.getName());
                  try {
                    spawnPublishers(context);

                    final ActorRef<ControllerParentMessage> controllerParent =
                        spawnControllerParent(context, controllerBehaviorSupplier);

                    runHttpServer(context);

                    final Source<CommittableMessage<byte[], String>, NotUsed> source =
                        createControllerInboxSource(context.getSystem(), controllerParent);

                    final Sink<CommittableOffset, NotUsed> sink = createSink(context);

                    return create(source, sink, controllerParent);
                  } catch (Exception e) {
                    LOGGER.error(
                        INFRASTRUCTURE,
                        "Failed to start mandatory subsystems with {}; shutting down ...",
                        e.getLocalizedMessage(),
                        e);
                    throw new IllegalStateException("shutting down");
                  }
                }))
        .onFailure(
            SupervisorStrategy.restartWithBackoff(
                    MIN_SUPERVISION_BACKOFF_DURATION,
                    MAX_SUPERVISION_BACKOFF_DURATION,
                    SUPERVISION_BACKOFF_RANDOM_FACTOR)
                .withMaxRestarts(
                    MAX_SUPERVISION_RESTARTS) // we want to ultimately die here so systemd can
            // restart the whole service
            );
  }

  public static Behavior<Void> create(
      final Source<CommittableMessage<byte[], String>, NotUsed> inboxSource,
      final Sink<CommittableOffset, NotUsed> sink,
      final ActorRef<ControllerParentMessage> controllerParent) {
    return Behaviors.setup(
        context -> {
          runControllerInboxProcessingPipeline(context, inboxSource, sink, controllerParent);

          return createBehavior();
        });
  }

  private static Behavior<Void> createBehavior() {
    return Behaviors.receiveSignal(
        (ctx, signal) -> {
          if (signal instanceof ChildFailed childFailed) {
            throw new IllegalStateException(
                "Child " + childFailed.getRef().path() + " failed", childFailed.getCause());
          }
          return Behaviors.same();
        });
  }

  private static ActorRef<ControllerParentMessage> spawnControllerParent(
      final ActorContext<Void> context,
      final Function<RoutingKey, Behavior<ControllerMessage>> controllerBehaviorSupplier) {
    final ActorRef<ControllerParentMessage> controllerParent =
        context.spawn(
            ControllerParent.create(controllerBehaviorSupplier), CONTROLLER_PARENT_ACTOR_NAME);
    LOGGER.debug(INFRASTRUCTURE, "Successfully spawned controller parent.");
    return controllerParent;
  }

  private static Source<CommittableMessage<byte[], String>, NotUsed> createControllerInboxSource(
      final ActorSystem<?> system, final ActorRef<ControllerParentMessage> controllerParent) {
    final Config kafkaConfig = ConfigFactory.load().getConfig("kafka");
    final String bootstrapServers = kafkaConfig.getString("bootstrap-servers");
    final String groupId = kafkaConfig.getString("consumer-group");
    final String topic = kafkaConfig.getString("inbox-topic");

    final ConsumerSettings<byte[], String> consumerSettings =
        ConsumerSettings.create(system, new ByteArrayDeserializer(), new StringDeserializer())
            .withBootstrapServers(bootstrapServers)
            .withGroupId(groupId)
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    Source<CommittableMessage<byte[], String>, NotUsed> source =
        RestartSource.withBackoff(
            RestartSettings.create(
                MIN_SOURCE_BACKOFF_DURATION,
                MAX_SOURCE_BACKOFF_DURATION,
                SOURCE_BACKOFF_RANDOM_FACTOR),
            () -> Consumer.committableSource(consumerSettings, Subscriptions.topics(topic)));

    LOGGER.debug(INFRASTRUCTURE, "Successfully created ControllerInboxSource.");

    return source;
  }

  private static Sink<CommittableOffset, NotUsed> createSink(final ActorContext<Void> context) {
    final Sink<CommittableOffset, NotUsed> sink =
        Committer.<CommittableOffset>sink(CommitterSettings.apply(context.getSystem()))
            .mapMaterializedValue(unused -> NotUsed.notUsed());

    LOGGER.debug(INFRASTRUCTURE, "Successfully created Sink.");
    return sink;
  }

  private static void spawnPublishers(final ActorContext<?> context) {
    final ActorRef<PublisherIntent> kafkaJsonPublisher =
        context.spawn(KafkaJsonPublisher.create(), KafkaJsonPublisher.class.getSimpleName());
    context.watch(kafkaJsonPublisher);

    LOGGER.debug(INFRASTRUCTURE, "Successfully spawned KafkaPublishers.");
  }

  private static void runControllerInboxProcessingPipeline(
      final ActorContext<?> context,
      final Source<CommittableMessage<byte[], String>, NotUsed> source,
      final Sink<CommittableOffset, NotUsed> sink,
      final ActorRef<ControllerParentMessage> controllerParent) {
    try {

      final Materializer materializer = Materializer.createMaterializer(context);
      context.watch(Adapter.toTyped(materializer.supervisor()));

      ControllerInboxReceiver.run(materializer, source, sink, controllerParent);

      LOGGER.debug(INFRASTRUCTURE, "Successfully started inbox pipeline.");
    } catch (final Exception e) {
      throw new IllegalStateException(
          "Failed to start mandatory subsystems with {}, shutting down.", e);
    }
  }

  private static void runHttpServer(final ActorContext<?> context) {
    final Route livenessRoute = get(() -> complete(StatusCodes.OK));
    Http.get(context.getSystem())
        .newServerAt(
            ConfigFactory.load().getString("health.http-interface"),
            ConfigFactory.load().getInt("health.http-port"))
        .bind(
            concat(
                pathPrefix(
                    "health", () -> concat(path("alive", () -> livenessRoute), livenessRoute))));
    LOGGER.debug(INFRASTRUCTURE, "Successfully started Health HTTP Server.");
  }
}
