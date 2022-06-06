package com.aricontroller.boundary.messaging.ari.inbound.events.playback;

import com.aricontroller.boundary.messaging.ari.inbound.events.AriEvent;
import com.aricontroller.boundary.messaging.ari.resources.playback.Playback;

public interface AriPlaybackEvent extends AriEvent {
  Playback playback();
}
