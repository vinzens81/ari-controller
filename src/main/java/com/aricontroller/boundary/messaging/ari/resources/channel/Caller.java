package com.aricontroller.boundary.messaging.ari.resources.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record Caller(
    @JsonProperty("name") String name, @JsonProperty("number") String number) {}
