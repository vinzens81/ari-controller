package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.BridgeId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record AriBridgeResponse(
    AriCommandId commandId,
    RoutingKey routingKey,
    BridgeId bridgeId,
    AriResponseStatus ariResponseStatus)
    implements IncomingAriBridgeMessage {}
