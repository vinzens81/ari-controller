package com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.bridge;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record AddChannelToBridgeAriCommandBody(@JsonProperty("channel") String channel)
    implements AriCommandBody {}
