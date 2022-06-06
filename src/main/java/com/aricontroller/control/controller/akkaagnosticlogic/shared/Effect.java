package com.aricontroller.control.controller.akkaagnosticlogic.shared;

import com.aricontroller.control.domainobjects.ari.outbound.AriCommand;
import com.aricontroller.control.domainobjects.shared.AriCommandsTopic;
import com.aricontroller.control.domainobjects.shared.RoutingKey;

public interface Effect {

  record PublishAriCommand(
      AriCommand ariCommand, AriCommandsTopic ariCommandsTopic, RoutingKey routingKey)
      implements Effect {}
}
