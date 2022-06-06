package com.aricontroller.boundary.messaging.ari.resources.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record Peer(
    @JsonProperty("id") ChannelId id,
    @JsonProperty("state") String state,
    @JsonProperty("caller") Caller caller) {}
