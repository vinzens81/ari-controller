package com.aricontroller.boundary.messaging.ari.resources.playback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record Playback(@JsonProperty("id") PlaybackId id) {
  @JsonCreator
  public Playback(@JsonProperty("id") final PlaybackId id) {
    this.id = id;
  }
}
