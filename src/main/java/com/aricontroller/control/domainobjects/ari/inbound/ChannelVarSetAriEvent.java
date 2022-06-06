package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record ChannelVarSetAriEvent(
    RoutingKey routingKey, AriChannel ariChannel, String variable, String value)
    implements AriChannelEvent {

  @Override
  public ChannelId channelId() {
    return ariChannel.channelId();
  }
}
