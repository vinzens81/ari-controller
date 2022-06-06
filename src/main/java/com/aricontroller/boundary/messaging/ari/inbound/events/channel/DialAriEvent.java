package com.aricontroller.boundary.messaging.ari.inbound.events.channel;

import com.aricontroller.boundary.messaging.ari.resources.channel.Peer;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
We currently map this to a ChannelEvent in the Domain Layer.
In the current ARI version (13) the peer and channel object
transmitted via ARI look identical and as of now we do not see
the need to have a `peer` object.
*/
public final record DialAriEvent(@JsonProperty("peer") Peer peer) implements PeerAriEvent {}
