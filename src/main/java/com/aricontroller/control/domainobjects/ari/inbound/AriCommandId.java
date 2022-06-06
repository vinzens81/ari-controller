package com.aricontroller.control.domainobjects.ari.inbound;

import java.io.Serializable;
import java.util.UUID;

public record AriCommandId(String value) implements Serializable {
  public static AriCommandId generate() {
    return new AriCommandId(UUID.randomUUID().toString());
  }
}
