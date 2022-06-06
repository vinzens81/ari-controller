package com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel;

import static com.aricontroller.boundary.messaging.ari.Constants.APP_NAME;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public final record CreateLocalChannelAriCommandBody(
    @JsonProperty("endpoint") String endpoint,
    @JsonProperty("channelId") String channelId,
    @JsonProperty("otherChannelId") String otherChannelId,
    @JsonProperty("variables") Map<String, String> variables)
    implements AriCommandBody {

  public String getApp() {
    return APP_NAME;
  }
}
