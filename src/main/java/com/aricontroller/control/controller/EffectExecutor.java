package com.aricontroller.control.controller;

import akka.actor.typed.eventstream.EventStream;
import akka.actor.typed.javadsl.ActorContext;
import com.aricontroller.AriLogger;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effect.PublishAriCommand;
import com.aricontroller.control.domainobjects.publishing.AriCommandPublisherIntent;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public final class EffectExecutor {

  private static final AriLogger LOGGER = new AriLogger(EffectExecutor.class);

  private EffectExecutor() {
    throw new IllegalStateException("Utility class");
  }

  public static void executePublishAriCommand(
      final PublishAriCommand effect, final ActorContext<?> context, final RoutingKey routingKey) {
    context
        .getSystem()
        .eventStream()
        .tell(
            new EventStream.Publish<>(
                new AriCommandPublisherIntent(
                    effect.ariCommandsTopic(), effect.routingKey(), effect.ariCommand())));

    LOGGER.debug(
        routingKey,
        "Publishing AriCommand on topic {}. Command: {}",
        effect.ariCommandsTopic().value(),
        effect.ariCommand());
  }
}
