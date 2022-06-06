package com.aricontroller.boundary.messaging.ari.resources.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record Channel(
    @JsonProperty("id") ChannelId id,
    @JsonProperty("state") String state,
    @JsonProperty("caller") Caller caller,
    @JsonProperty("channelvars") ChannelVars channelVars,
    @JsonProperty("dialplan") Dialplan dialplan) {}
