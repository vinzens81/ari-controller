package com.aricontroller.boundary.messaging.ari.inbound;

import com.aricontroller.boundary.messaging.ari.inbound.events.bridge.ChannelEnteredBridgeAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.bridge.ChannelLeftBridgeAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.ChannelHangUpRequestAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.ChannelStateChangeAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.ChannelVarSetAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.DialAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.StasisEndAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.StasisStartAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.playback.PlaybackEndedAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.playback.PlaybackStartedAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.responses.AriResponse;
import java.util.Arrays;

public enum IncomingAriMessageClassMapping {
  STASIS_START(StasisStartAriEvent.class),
  DIAL(DialAriEvent.class),
  STASIS_END(StasisEndAriEvent.class),
  CHANNEL_HANGUP_REQUEST(ChannelHangUpRequestAriEvent.class),
  CHANNEL_STATE_CHANGE(ChannelStateChangeAriEvent.class),
  PLAYBACK_STARTED(PlaybackStartedAriEvent.class),
  PLAYBACK_ENDED(PlaybackEndedAriEvent.class),
  RESPONSE(AriResponse.class),
  CHANNELVARSET(ChannelVarSetAriEvent.class),
  CHANNEL_ENTERED_BRIDGE(ChannelEnteredBridgeAriEvent.class),
  CHANNEL_LEFT_BRIDGE(ChannelLeftBridgeAriEvent.class),
  UNKNOWN(UnknownIncomingAriMessage.class);

  private final Class<? extends IncomingAriMessage> associatedClass;

  IncomingAriMessageClassMapping(final Class<? extends IncomingAriMessage> associatedClass) {
    this.associatedClass = associatedClass;
  }

  public Class<? extends IncomingAriMessage> associatedClass() {
    return associatedClass;
  }

  public static IncomingAriMessageClassMapping fromString(final String candidate) {
    return Arrays.stream(IncomingAriMessageClassMapping.values())
        .filter(value -> value.name().equals(candidate))
        .findFirst()
        .orElse(UNKNOWN);
  }
}
