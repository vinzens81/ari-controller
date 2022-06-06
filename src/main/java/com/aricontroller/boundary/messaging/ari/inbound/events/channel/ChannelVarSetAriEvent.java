package com.aricontroller.boundary.messaging.ari.inbound.events.channel;

import com.aricontroller.boundary.messaging.ari.resources.channel.Channel;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record ChannelVarSetAriEvent(
    @JsonProperty("channel") Channel channel,
    @JsonProperty("variable") String variable,
    @JsonProperty("value") String value)
    implements AriChannelEvent {}
