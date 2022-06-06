package com.aricontroller.control.routing;

import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.aricontroller.control.domainobjects.InboxPartitionId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import java.util.Set;

public interface ControllerParentMessage {
  record ResolveController(
      RoutingKey routingKey,
      InboxPartitionId inboxPartitionId,
      ActorRef<StatusReply<ResolveControllerReply>> replyTo)
      implements ControllerParentMessage {}

  record WakeUpControllerInstances(Set<RoutingKey> routingKeys)
      implements ControllerParentMessage {}
}
