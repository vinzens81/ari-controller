package com.aricontroller.control.domainobjects.publishing;

import com.aricontroller.control.domainobjects.ari.outbound.AriCommand;
import com.aricontroller.control.domainobjects.shared.AriCommandsTopic;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public record AriCommandPublisherIntent(
    AriCommandsTopic ariCommandsTopic, RoutingKey routingKey, AriCommand ariCommand)
    implements PublisherIntent {}
