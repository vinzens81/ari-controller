package com.aricontroller.boundary.messaging.ari.inbound.events.bridge;

import com.aricontroller.boundary.messaging.ari.inbound.events.AriEvent;
import com.aricontroller.boundary.messaging.ari.resources.bridge.Bridge;

public interface AriBridgeEvent extends AriEvent {
  Bridge bridge();
}
