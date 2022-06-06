package com.aricontroller.control.domainobjects.shared;

import java.io.Serializable;
import java.util.UUID;

public final record PlaybackId(String value) implements Serializable {
  public static PlaybackId generate() {
    return new PlaybackId(UUID.randomUUID().toString());
  }
}
