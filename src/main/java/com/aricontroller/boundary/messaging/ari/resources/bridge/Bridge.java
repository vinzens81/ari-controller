package com.aricontroller.boundary.messaging.ari.resources.bridge;

import com.fasterxml.jackson.annotation.JsonProperty;

public final record Bridge(@JsonProperty("id") BridgeId id) {}
