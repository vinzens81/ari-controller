package com.aricontroller.boundary.messaging.inbox;

import static akka.stream.Attributes.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.AskPattern;
import akka.kafka.ConsumerMessage.CommittableMessage;
import akka.kafka.ConsumerMessage.CommittableOffset;
import akka.pattern.StatusReply;
import akka.stream.ActorAttributes;
import akka.stream.Materializer;
import akka.stream.Supervision;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.aricontroller.AriLogger;
import com.aricontroller.control.controller.ControllerMessage;
import com.aricontroller.control.domainobjects.InboxPartitionId;
import com.aricontroller.control.domainobjects.ari.inbound.IncomingAriMessage;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import com.aricontroller.control.routing.ControllerParentMessage;
import com.aricontroller.control.routing.ControllerParentMessage.ResolveController;
import com.aricontroller.control.routing.ResolveControllerReply;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

public final class ControllerInboxReceiver {
  private static final AriLogger LOGGER = new AriLogger(ControllerInboxReceiver.class);
  private static final Pattern LINEBREAK_PATTERN = Pattern.compile("\r*\n*");
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\s+");

  private ControllerInboxReceiver() {
    throw new IllegalStateException("Utility class");
  }

  public static void run(
      final Materializer materializer,
      final Source<CommittableMessage<byte[], String>, NotUsed> source,
      final Sink<CommittableOffset, NotUsed> sink,
      final ActorRef<ControllerParentMessage> controllerParent) {

    source
        .mapAsync(
            1,
            committableMessage -> {
              final String routingKey =
                  new String(committableMessage.record().key(), Charset.defaultCharset());
              LOGGER.debug(
                  new RoutingKey(routingKey),
                  "Received Kafka message with offset {} on partition {} with key: {} and value: {}",
                  committableMessage.record().offset(),
                  committableMessage.record().partition(),
                  routingKey,
                  getSingleLineStringValue(committableMessage));

              final IncomingAriMessage incomingAriMessage;
              try {
                incomingAriMessage =
                    InboxUnmarshaller.unmarshall(routingKey, committableMessage.record().value());
              } catch (Exception e) {
                LOGGER.warn(
                    new RoutingKey(routingKey),
                    "Unable to deserialize inbox message: {}",
                    e.getMessage());
                return completedFuture(committableMessage.committableOffset());
              }

              final Scheduler scheduler = Adapter.toTyped(materializer.system()).scheduler();

              final InboxPartitionId inboxPartitionId =
                  new InboxPartitionId(String.valueOf(committableMessage.record().partition()));
              final CompletionStage<ActorRef<ControllerMessage>> controllerFetched =
                  resolveController(
                      controllerParent,
                      incomingAriMessage.routingKey(),
                      inboxPartitionId,
                      scheduler);

              final CompletionStage<StatusReply<Done>> messageProcessed =
                  controllerFetched.thenCompose(
                      controller -> sendMessage(incomingAriMessage, scheduler, controller));

              return messageProcessed.thenApply(ignored -> committableMessage.committableOffset());
            })
        .log("message processed")
        .withAttributes(createLogLevels(logLevelOff(), logLevelInfo(), logLevelError()))
        .to(sink)
        .withAttributes(
            ActorAttributes.withSupervisionStrategy(
                error -> (Supervision.Directive) Supervision.resume()))
        .run(materializer);
  }

  private static CompletionStage<ActorRef<ControllerMessage>> resolveController(
      final ActorRef<ControllerParentMessage> controllerParent,
      final RoutingKey routingKey,
      final InboxPartitionId inboxPartitionId,
      final Scheduler scheduler) {
    return AskPattern.<ControllerParentMessage, StatusReply<ResolveControllerReply>>ask(
            controllerParent,
            replyTo -> new ResolveController(routingKey, inboxPartitionId, replyTo),
            Duration.ofSeconds(1),
            scheduler)
        .thenCompose(
            (StatusReply<ResolveControllerReply> reply) -> {
              if (reply.isError()) {
                return failedFuture(new IllegalStateException(reply.getError()));
              }

              return completedFuture(reply.getValue().controller());
            });
  }

  private static CompletionStage<StatusReply<Done>> sendMessage(
      final IncomingAriMessage ariMessage,
      final Scheduler scheduler,
      final ActorRef<ControllerMessage> controller) {
    return AskPattern.ask(
        controller,
        replyTo -> new ControllerMessage(ariMessage, replyTo),
        Duration.ofSeconds(1),
        scheduler);
  }

  private static String getSingleLineStringValue(
      final CommittableMessage<byte[], String> committableMessage) {
    final String withoutLinebreaks =
        LINEBREAK_PATTERN.matcher(committableMessage.record().value()).replaceAll("");

    return WHITESPACE_PATTERN.matcher(withoutLinebreaks).replaceAll(" ");
  }
}
