package com.aricontroller.boundary.messaging.publish;

import static com.aricontroller.AriLogger.LogMarker.INFRASTRUCTURE;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.Signal;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.eventstream.EventStream.Subscribe;
import akka.actor.typed.eventstream.EventStream.Unsubscribe;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Producer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.aricontroller.AriLogger;
import com.aricontroller.boundary.messaging.ari.outbound.commands.AriCommandMarshaller;
import com.aricontroller.control.domainobjects.publishing.AriCommandPublisherIntent;
import com.aricontroller.control.domainobjects.publishing.PublisherIntent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaJsonPublisher extends AbstractBehavior<PublisherIntent> {

  private static final AriLogger LOGGER = new AriLogger(KafkaJsonPublisher.class);

  private static final Config DEFAULT_KAFKA_PRODUCER_CONFIG =
      ConfigFactory.load().getConfig(ProducerSettings.configPath());
  private static final Config KAFKA_CONFIG = ConfigFactory.load().getConfig("kafka");
  private static final String BOOTSTRAP_SERVERS = KAFKA_CONFIG.getString("bootstrap-servers");

  private static final Duration MIN_SINK_BACKOFF_DURATION = Duration.ofSeconds(1);
  private static final Duration MAX_SINK_BACKOFF_DURATION = Duration.ofSeconds(30);
  private static final double SINK_BACKOFF_RANDOM_FACTOR = .1;
  private static final int MAX_SINK_SUPERVISION_RESTARTS = 5;
  private static final Duration SINK_BACKOFF_RESTART_TIMEOUT = Duration.ofSeconds(10);

  private static final byte[] IGNORED_RECORD_KEY = "none".getBytes(Charset.defaultCharset());

  private final ActorRef<ProducerRecord<byte[], String>> kafkaSinkActorRef;

  private KafkaJsonPublisher(
      final ActorContext<PublisherIntent> context,
      final Sink<ProducerRecord<byte[], String>, ?> sink) {
    super(context);
    context.setLoggerName("akka." + getClass().getName());
    context
        .getSystem()
        .eventStream()
        .tell(new Subscribe<>(PublisherIntent.class, context.getSelf()));

    kafkaSinkActorRef = startKafkaPublisherPipeline(context, sink);
    LOGGER.debug(INFRASTRUCTURE, "Successfully started Kafka Publisher Pipeline.");

    context.watch(kafkaSinkActorRef);
  }

  public static Behavior<PublisherIntent> create() {
    return create(createKafkaProducerSink());
  }

  public static Behavior<PublisherIntent> create(
      final Sink<ProducerRecord<byte[], String>, ?> sink) {
    return Behaviors.supervise(
            Behaviors.<PublisherIntent>setup(
                actorContext -> new KafkaJsonPublisher(actorContext, sink)))
        .onFailure(
            SupervisorStrategy.restartWithBackoff(
                    MIN_SINK_BACKOFF_DURATION,
                    MAX_SINK_BACKOFF_DURATION,
                    SINK_BACKOFF_RANDOM_FACTOR)
                .withMaxRestarts(MAX_SINK_SUPERVISION_RESTARTS)
                .withResetBackoffAfter(SINK_BACKOFF_RESTART_TIMEOUT));
  }

  private static ProducerRecord<byte[], String> getAriCommandPublisherProducerRecord(
      final AriCommandPublisherIntent ariCommandPublisherIntent) {
    return new ProducerRecord<>(
        ariCommandPublisherIntent.ariCommandsTopic().value(),
        IGNORED_RECORD_KEY,
        AriCommandMarshaller.marshall(
            ariCommandPublisherIntent.routingKey(),
            ariCommandPublisherIntent.ariCommand().ariCommandId(),
            ariCommandPublisherIntent.ariCommand()));
  }

  private static Sink<ProducerRecord<byte[], String>, CompletionStage<Done>>
      createKafkaProducerSink() {
    final ProducerSettings<byte[], String> settings =
        ProducerSettings.create(
                DEFAULT_KAFKA_PRODUCER_CONFIG, new ByteArraySerializer(), new StringSerializer())
            .withBootstrapServers(BOOTSTRAP_SERVERS);

    return Producer.plainSink(settings);
  }

  private static ActorRef<ProducerRecord<byte[], String>> startKafkaPublisherPipeline(
      final ActorContext<?> context, final Sink<ProducerRecord<byte[], String>, ?> sink) {

    final Source<ProducerRecord<byte[], String>, akka.actor.ActorRef> source =
        Source.actorRef(
            elem -> Optional.empty(), elem -> Optional.empty(), 100, OverflowStrategy.fail());
    final RunnableGraph<akka.actor.ActorRef> pipeline = source.to(sink);
    final akka.actor.ActorRef sourceActor = pipeline.run(Materializer.createMaterializer(context));

    return Adapter.toTyped(sourceActor);
  }

  @Override
  public Receive<PublisherIntent> createReceive() {
    return newReceiveBuilder()
        .onMessage(PublisherIntent.class, this::onReceive)
        .onSignal(PostStop.class, this::cleanup)
        .onSignal(PreRestart.class, this::cleanup)
        .build();
  }

  private Behavior<PublisherIntent> onReceive(final PublisherIntent publisherIntent) {

    ProducerRecord<byte[], String> producerRecord = null;

    if (publisherIntent instanceof AriCommandPublisherIntent ariCommandPublisherIntent) {
      producerRecord = getAriCommandPublisherProducerRecord(ariCommandPublisherIntent);
    }

    LOGGER.debug(publisherIntent.routingKey(), "Going to produce record: {}", producerRecord);
    kafkaSinkActorRef.tell(producerRecord);

    return this;
  }

  private Behavior<PublisherIntent> cleanup(Signal signal) {
    getContext().getSystem().eventStream().tell(new Unsubscribe<>(getContext().getSelf()));
    LOGGER.debug(INFRASTRUCTURE, "Cleanup: unsubscribed from event stream.");

    return this;
  }
}
