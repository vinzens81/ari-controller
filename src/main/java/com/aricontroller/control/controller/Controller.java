package com.aricontroller.control.controller;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.EffectBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.aricontroller.AriLogger;
import com.aricontroller.control.controller.akkaagnosticlogic.CommandHandler;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.ControllerState;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.ControllerUpdated;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effect;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effect.PublishAriCommand;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Result;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import com.aricontroller.exception.UnsupportedTypeException;
import java.util.Set;

public final class Controller
    extends EventSourcedBehavior<ControllerMessage, ControllerUpdated, ControllerState> {

  private static final AriLogger LOGGER = new AriLogger(Controller.class);

  private final ActorContext<ControllerMessage> context;
  private final ControllerState emptyState;

  private Controller(
      final ActorContext<ControllerMessage> context, final ControllerState emptyState) {
    super(PersistenceId.of("controller", emptyState.getRoutingKey().value()));
    context.setLoggerName("akka." + getClass().getName());
    this.context = context;
    this.emptyState = emptyState;
  }

  public static Behavior<ControllerMessage> create(final RoutingKey routingKey) {
    return create(ControllerState.uninitialised(routingKey));
  }

  public static Behavior<ControllerMessage> create(final ControllerState emptyState) {
    return Behaviors.<ControllerMessage>supervise(
            Behaviors.setup(
                context ->
                    Behaviors.withTimers(timerScheduler -> new Controller(context, emptyState))))
        .onFailure(SupervisorStrategy.resume());
  }

  @Override
  public ControllerState emptyState() {
    return emptyState;
  }

  @Override
  public akka.persistence.typed.javadsl.CommandHandler<
          ControllerMessage, ControllerUpdated, ControllerState>
      commandHandler() {
    return (state, message) -> {
      LOGGER.info(
          state.getRoutingKey(),
          "Processing received message {} while in state {}",
          message.incomingAriMessage(),
          state);

      final Result result =
          CommandHandler.handleIncomingMessage(message.incomingAriMessage(), state);
      LOGGER.info(
          state.getRoutingKey(),
          "Finished processing of {}. Executing external effects {} and persisting new controllerState: {}.",
          message.getClass().getSimpleName(),
          result.effects().effects(),
          result.controllerState());

      final EffectBuilder<ControllerUpdated, ControllerState> effect =
          Effect()
              .persist(new ControllerUpdated(result.controllerState()))
              .thenRun(() -> executeEffects(result.effects().effects(), state.getRoutingKey()))
              .thenRun(
                  () -> {
                    LOGGER.debug(
                        state.getRoutingKey(),
                        "Sending acknowledgement after processing message {} completely.",
                        message.incomingAriMessage().getClass().getSimpleName());
                    message.replyTo().tell(StatusReply.ack());
                  });

      return result.effects().stopSession() ? effect.thenStop() : effect;
    };
  }

  @Override
  public akka.persistence.typed.javadsl.EventHandler<ControllerState, ControllerUpdated>
      eventHandler() {
    return (state, event) -> event.controllerState();
  }

  private void executeEffects(final Set<Effect> effects, final RoutingKey routingKey) {
    for (final Effect effect : effects) {
      if (effect instanceof PublishAriCommand publishAriCommand) {
        EffectExecutor.executePublishAriCommand(publishAriCommand, context, routingKey);
      } else {
        throw new UnsupportedTypeException("Executing external effects.", effect);
      }
    }
  }
}
