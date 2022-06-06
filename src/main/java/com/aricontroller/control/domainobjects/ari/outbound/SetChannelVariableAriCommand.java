package com.aricontroller.control.domainobjects.ari.outbound;

import com.aricontroller.control.domainobjects.ari.inbound.AriCommandId;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import java.util.Map;

public record SetChannelVariableAriCommand(
    AriCommandId ariCommandId, ChannelId channelId, Map<String, String> variables)
    implements AriCommand {}
