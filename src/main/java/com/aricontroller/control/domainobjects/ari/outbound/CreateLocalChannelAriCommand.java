package com.aricontroller.control.domainobjects.ari.outbound;

import com.aricontroller.control.domainobjects.ari.inbound.AriCommandId;
import com.aricontroller.control.domainobjects.shared.ChannelId;

public record CreateLocalChannelAriCommand(
    AriCommandId ariCommandId, ChannelId channelId, ChannelId terminatingChannelId)
    implements AriCommand {}
