package com.aricontroller.boundary.messaging.ari.resources.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record ChannelVars(@JsonProperty("TARGET") String target) {}
