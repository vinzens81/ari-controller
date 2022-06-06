package com.aricontroller.boundary.messaging.ari.resources.playback;

import com.aricontroller.boundary.messaging.ari.resources.AriResourceId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final record PlaybackId(String value) implements AriResourceId {

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public PlaybackId {}

  @Override
  @JsonValue
  public String value() {
    return value;
  }
}
