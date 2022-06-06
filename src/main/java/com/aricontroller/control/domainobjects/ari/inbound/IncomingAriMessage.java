package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.RoutingKey;

public sealed interface IncomingAriMessage
    permits IncomingAriBridgeMessage, IncomingAriChannelMessage, IncomingAriPlaybackEvent {
  RoutingKey routingKey();
}
