package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.AsteriskRoutingInfo;

public sealed interface StasisStartAriChannelEvent extends AriChannelEvent
    permits InternalChannelStasisStartAriEvent, StasisStartAriEvent {
  AsteriskRoutingInfo asteriskRoutingInfo();
}
