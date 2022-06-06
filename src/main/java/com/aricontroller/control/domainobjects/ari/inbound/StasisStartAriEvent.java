package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.AsteriskRoutingInfo;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.PhoneNumber;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record StasisStartAriEvent(
    RoutingKey routingKey,
    AsteriskRoutingInfo asteriskRoutingInfo,
    AriChannel channel,
    PhoneNumber targetNumber)
    implements StasisStartAriChannelEvent {

  @Override
  public ChannelId channelId() {
    return channel.channelId();
  }
}
