package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.ChannelId;

public sealed interface IncomingAriChannelMessage extends IncomingAriMessage
    permits AriChannelEvent, AriChannelResponse {
  ChannelId channelId();
}
