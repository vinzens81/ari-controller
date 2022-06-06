package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record AriChannelResponse(
    AriCommandId commandId,
    RoutingKey routingKey,
    ChannelId channelId,
    AriResponseStatus ariResponseStatus)
    implements IncomingAriChannelMessage {}
