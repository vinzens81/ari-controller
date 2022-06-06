package com.aricontroller.boundary.messaging.ari.inbound;

import com.aricontroller.boundary.messaging.inbox.InboxMessageEnvelope;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonDeserialize()
public final record IncomingAriMessageJsonEnvelope(
    @JsonProperty("type") IncomingAriMessageClassMapping type,
    @JsonProperty("commandsTopic") String commandsTopic,
    @JsonProperty("commandId") String commandId,
    @JsonProperty("callContext") String callContext,
    @JsonProperty("resources") List<AriResource> resources,
    @JsonProperty("payload") JsonNode payload)
    implements InboxMessageEnvelope {}
