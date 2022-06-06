package com.aricontroller.control.domainobjects.ari.outbound;

import com.aricontroller.control.domainobjects.ari.inbound.AriCommandId;
import com.aricontroller.control.domainobjects.shared.BridgeId;

public record DeleteBridgeAriCommand(AriCommandId ariCommandId, BridgeId bridgeId)
    implements AriCommand {}
