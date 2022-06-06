package com.aricontroller.control.domainobjects.ari.outbound;

import com.aricontroller.control.domainobjects.ari.inbound.AriCommandId;
import com.aricontroller.control.domainobjects.shared.BridgeId;
import com.aricontroller.control.domainobjects.shared.ChannelId;

public record RemoveChannelFromBridgeAriCommand(
    AriCommandId ariCommandId, BridgeId bridgeId, ChannelId channelId) implements AriCommand {}
