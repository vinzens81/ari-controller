package com.aricontroller.control.routing;

import static com.aricontroller.AriLogger.LogMarker.INFRASTRUCTURE;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.pattern.StatusReply;
import com.aricontroller.AriLogger;
import com.aricontroller.control.controller.ControllerMessage;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import com.aricontroller.control.routing.ControllerParentMessage.ResolveController;
import com.aricontroller.control.routing.ControllerParentMessage.WakeUpControllerInstances;
import java.util.Optional;
import java.util.function.Function;

public final class ControllerParent extends AbstractBehavior<ControllerParentMessage> {
  private static final AriLogger LOGGER = new AriLogger(ControllerParent.class);

  private final Function<RoutingKey, Behavior<ControllerMessage>> controllerBehaviorCreator;

  private ControllerParent(
      final ActorContext<ControllerParentMessage> context,
      final Function<RoutingKey, Behavior<ControllerMessage>> controllerBehaviorCreator) {
    super(context);
    context.setLoggerName("akka." + getClass().getName());
    this.controllerBehaviorCreator = controllerBehaviorCreator;
  }

  private static String buildControllerInstanceName(final RoutingKey routingKey) {
    return String.format("controller-%s", routingKey.value());
  }

  public static Behavior<ControllerParentMessage> create(
      final Function<RoutingKey, Behavior<ControllerMessage>> controllerBehaviorCreator) {
    return Behaviors.supervise(
            Behaviors.<ControllerParentMessage>setup(
                context -> new ControllerParent(context, controllerBehaviorCreator)))
        .onFailure(SupervisorStrategy.resume());
  }

  @Override
  public Receive<ControllerParentMessage> createReceive() {
    final ReceiveBuilder<ControllerParentMessage> builder = newReceiveBuilder();

    builder
        .onMessage(ResolveController.class, this::onResolveController)
        .onMessage(WakeUpControllerInstances.class, this::onWakeUpControllerInstances)
        .onAnyMessage(ControllerParent::onUnhandledMessage);

    return builder.build();
  }

  private Behavior<ControllerParentMessage> onResolveController(final ResolveController msg) {
    findCachedOrCreateNewController(msg.routingKey(), msg.replyTo());

    return Behaviors.same();
  }

  private Behavior<ControllerParentMessage> onWakeUpControllerInstances(
      final WakeUpControllerInstances msg) {
    msg.routingKeys().forEach(this::wakeUpControllerInstance);

    return Behaviors.same();
  }

  private static Behavior<ControllerParentMessage> onUnhandledMessage(
      final ControllerParentMessage message) {
    LOGGER.error(INFRASTRUCTURE, "Unhandled ControllerParentMessage: {}", message);

    return Behaviors.same();
  }

  private void findCachedOrCreateNewController(
      final RoutingKey routingKey, final ActorRef<StatusReply<ResolveControllerReply>> replyTo) {
    final Optional<ActorRef<ControllerMessage>> aliveInstance =
        getAliveControllerInstance(routingKey);

    if (aliveInstance.isPresent()) {
      replyTo.tell(StatusReply.success(new ResolveControllerReply(aliveInstance.get())));
    } else {
      final ActorRef<ControllerMessage> controllerRef =
          getNewOrSleepingControllerInstance(routingKey);
      replyTo.tell(StatusReply.success(new ResolveControllerReply(controllerRef)));
    }
  }

  private Optional<ActorRef<ControllerMessage>> getAliveControllerInstance(
      final RoutingKey routingKey) {
    return getContext()
        .getChild(buildControllerInstanceName(routingKey))
        .map(ActorRef::unsafeUpcast);
  }

  private ActorRef<ControllerMessage> getNewOrSleepingControllerInstance(
      final RoutingKey routingKey) {
    final Behavior<ControllerMessage> behavior = controllerBehaviorCreator.apply(routingKey);
    final String name = buildControllerInstanceName(routingKey);

    return getContext().spawn(behavior, name);
  }

  private void wakeUpControllerInstance(final RoutingKey routingKey) {
    if (getAliveControllerInstance(routingKey).isEmpty()) {
      final Behavior<ControllerMessage> behavior = controllerBehaviorCreator.apply(routingKey);
      getContext().spawn(behavior, buildControllerInstanceName(routingKey));
    }
  }
}
