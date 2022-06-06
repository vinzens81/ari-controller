package com.aricontroller.boundary.messaging.ari.inbound;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record AriResource(
    @JsonProperty("type") AriResourceType type, @JsonProperty("id") String id) {}
