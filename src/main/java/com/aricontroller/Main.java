package com.aricontroller;

import static com.aricontroller.AriLogger.LogMarker.INFRASTRUCTURE;

import akka.actor.typed.ActorSystem;
import com.aricontroller.control.controller.Controller;

public final class Main {
  private static final AriLogger LOGGER = new AriLogger(Main.class);

  public static void main(String[] args) {
    LOGGER.info(INFRASTRUCTURE, "Application is starting.");
    ActorSystem.create(Orchestrator.create(Controller::create), "orchestrator");
  }
}
