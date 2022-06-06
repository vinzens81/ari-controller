package com.aricontroller.boundary.messaging.ari.inbound.responses;

import com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public final record AriResponse(
    @JsonProperty("status_code") Integer statusCode, @JsonProperty("body") JsonNode body)
    implements IncomingAriMessage {}
