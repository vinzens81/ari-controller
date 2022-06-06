package com.aricontroller.boundary.messaging.ari.inbound.events.playback;

import com.aricontroller.boundary.messaging.ari.resources.playback.Playback;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record PlaybackStartedAriEvent(@JsonProperty("playback") Playback playback)
    implements AriPlaybackEvent {}
