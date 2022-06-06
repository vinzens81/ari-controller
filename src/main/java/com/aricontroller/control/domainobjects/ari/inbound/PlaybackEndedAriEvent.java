package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.PlaybackId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record PlaybackEndedAriEvent(RoutingKey routingKey, PlaybackId playback)
    implements IncomingAriPlaybackEvent {

  @Override
  public PlaybackId playbackId() {
    return playback;
  }
}
