package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record DialAriEvent(RoutingKey routingKey, AriChannel ariChannel)
    implements AriChannelEvent {

  @Override
  public ChannelId channelId() {
    return ariChannel().channelId();
  }
}
