package com.aricontroller.control.domainobjects.ari.inbound;

import com.aricontroller.control.domainobjects.shared.ChannelId;
import java.io.Serializable;

public record AriChannel(ChannelId channelId, AriChannelState state) implements Serializable {}
