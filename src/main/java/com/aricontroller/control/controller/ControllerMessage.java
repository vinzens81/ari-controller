package com.aricontroller.control.controller;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.aricontroller.control.domainobjects.ari.inbound.IncomingAriMessage;

public record ControllerMessage(
    IncomingAriMessage incomingAriMessage, ActorRef<StatusReply<Done>> replyTo) {}
