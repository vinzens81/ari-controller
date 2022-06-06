package com.aricontroller.boundary.messaging.ari.inbound.events.bridge;

import com.aricontroller.boundary.messaging.ari.resources.bridge.Bridge;
import com.aricontroller.boundary.messaging.ari.resources.channel.Channel;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record ChannelEnteredBridgeAriEvent(
    @JsonProperty("channel") Channel channel, @JsonProperty("bridge") Bridge bridge)
    implements AriBridgeEvent {}
