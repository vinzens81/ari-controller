package com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record HangUpChannelAriCommandBody(@JsonProperty("reason") String reason)
    implements AriCommandBody {
  public HangUpChannelAriCommandBody() {
    this("failure");
  }
}
