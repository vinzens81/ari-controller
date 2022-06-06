package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.AsteriskRoutingInfo;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record InternalChannelStasisStartAriEvent(
    RoutingKey routingKey, AsteriskRoutingInfo asteriskRoutingInfo, AriChannel channel)
    implements StasisStartAriChannelEvent {

  public ChannelId channelId() {
    return channel.channelId();
  }
}
