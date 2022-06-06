package com.aricontroller.control.domainobjects.ari.inbound;

public sealed interface AriBridgeEvent extends IncomingAriBridgeMessage
    permits ChannelEnteredBridgeAriEvent, ChannelLeftBridgeAriEvent {}
