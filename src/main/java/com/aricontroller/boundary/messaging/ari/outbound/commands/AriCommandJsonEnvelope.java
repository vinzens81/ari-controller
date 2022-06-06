package com.aricontroller.boundary.messaging.ari.outbound.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record AriCommandJsonEnvelope(
    @JsonProperty("ariCommand") AriCommand ariCommand,
    @JsonProperty("callContext") String callContext,
    @JsonProperty("commandId") String commandId) {}
