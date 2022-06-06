package com.aricontroller.boundary.messaging.ari.resources.bridge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;

public final record BridgeId(String value) implements Serializable {

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public BridgeId {}

  @JsonValue
  public String value() {
    return value;
  }
}
