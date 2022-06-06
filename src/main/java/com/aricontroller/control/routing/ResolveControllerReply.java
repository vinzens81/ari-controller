package com.aricontroller.control.routing;

import akka.actor.typed.ActorRef;
import com.aricontroller.control.controller.ControllerMessage;

public record ResolveControllerReply(ActorRef<ControllerMessage> controller) {}
