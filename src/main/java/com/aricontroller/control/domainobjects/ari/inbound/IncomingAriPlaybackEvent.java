package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.PlaybackId;

public sealed interface IncomingAriPlaybackEvent extends IncomingAriMessage
    permits PlaybackEndedAriEvent, PlaybackStartedAriEvent {
  PlaybackId playbackId();
}
