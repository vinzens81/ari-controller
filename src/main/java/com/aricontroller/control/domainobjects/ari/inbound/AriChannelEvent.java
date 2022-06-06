package com.aricontroller.control.domainobjects.ari.inbound;

public sealed interface AriChannelEvent extends IncomingAriChannelMessage
    permits ChannelHangupRequestAriEvent,
        ChannelStateChangeAriEvent,
        ChannelVarSetAriEvent,
        DialAriEvent,
        StasisEndAriEvent,
        StasisStartAriChannelEvent {}
