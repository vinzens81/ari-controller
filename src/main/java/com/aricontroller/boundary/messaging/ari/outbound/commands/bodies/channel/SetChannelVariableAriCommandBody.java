package com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public final record SetChannelVariableAriCommandBody(
    @JsonProperty("variable") Map<String, String> variables) implements AriCommandBody {}
