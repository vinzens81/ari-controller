package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.BridgeId;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record ChannelEnteredBridgeAriEvent(
    RoutingKey routingKey, ChannelId channelId, BridgeId bridgeId) implements AriBridgeEvent {}
