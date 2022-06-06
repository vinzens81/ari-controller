package com.aricontroller.control.domainobjects.shared;

import java.io.Serializable;

public final record AsteriskRoutingInfo(AriCommandsTopic ariCommandsTopic)
    implements Serializable {}
