package com.aricontroller.boundary.messaging.ari.outbound.commands;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record AriCommand(
    @JsonProperty("method") String method,
    @JsonProperty("url") String url,
    @JsonProperty("body") AriCommandBody body) {
  AriCommand(String method, String url) {
    this(method, url, null);
  }

  public static AriCommand withEmptyBody(String method, String url) {
    return new AriCommand(method, url);
  }
}
