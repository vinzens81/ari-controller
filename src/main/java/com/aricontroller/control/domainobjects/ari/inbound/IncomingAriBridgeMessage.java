package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.BridgeId;

public sealed interface IncomingAriBridgeMessage extends IncomingAriMessage
    permits AriBridgeEvent, AriBridgeResponse {
  BridgeId bridgeId();
}
