package com.aricontroller.boundary.messaging.ari.resources.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final record ChannelId(String value) {

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public ChannelId {}

  @Override
  @JsonValue
  public String value() {
    return value;
  }
}
