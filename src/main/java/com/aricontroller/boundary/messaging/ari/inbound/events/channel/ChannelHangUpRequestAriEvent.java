package com.aricontroller.boundary.messaging.ari.inbound.events.channel;

import com.aricontroller.boundary.messaging.ari.resources.channel.Channel;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record ChannelHangUpRequestAriEvent(
    @JsonProperty("channel") Channel channel, @JsonProperty("cause") Integer cause)
    implements AriChannelEvent {}
