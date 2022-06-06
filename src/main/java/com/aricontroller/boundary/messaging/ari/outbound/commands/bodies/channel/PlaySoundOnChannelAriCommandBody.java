package com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.AriCommandBody;
import com.fasterxml.jackson.annotation.JsonProperty;

public final record PlaySoundOnChannelAriCommandBody(
    @JsonProperty("media") String media, @JsonProperty("lang") String lang)
    implements AriCommandBody {}
