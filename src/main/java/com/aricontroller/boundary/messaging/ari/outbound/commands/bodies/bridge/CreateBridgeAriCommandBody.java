package com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.bridge;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record CreateBridgeAriCommandBody(@JsonProperty("bridgeId") String bridgeId)
    implements AriCommandBody {}
