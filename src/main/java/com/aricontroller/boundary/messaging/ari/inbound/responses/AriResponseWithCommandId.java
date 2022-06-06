package com.aricontroller.boundary.messaging.ari.inbound.responses;

import com.aricontroller.boundary.messaging.ari.inbound.AriResource;
import com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessage;
import com.aricontroller.boundary.messaging.ari.outbound.commands.AriCommandId;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public final record AriResponseWithCommandId(
    AriCommandId commandId, JsonNode body, int responseCode, List<AriResource> resources)
    implements IncomingAriMessage {}
