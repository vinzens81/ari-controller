package com.aricontroller.boundary.messaging.ari.resources.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record Dialplan(
    @JsonProperty("priority") int priority,
    @JsonProperty("exten") String exten,
    @JsonProperty("context") String context) {}
