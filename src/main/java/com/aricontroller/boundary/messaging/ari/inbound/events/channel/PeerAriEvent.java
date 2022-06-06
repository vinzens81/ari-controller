package com.aricontroller.boundary.messaging.ari.inbound.events.channel;

import com.aricontroller.boundary.messaging.ari.inbound.events.AriEvent;
import com.aricontroller.boundary.messaging.ari.resources.channel.Peer;

public interface PeerAriEvent extends AriEvent {
  Peer peer();
}
