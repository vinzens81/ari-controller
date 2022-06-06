package com.aricontroller.control.controller.akkaagnosticlogic.shared;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.aricontroller.control.controller.akkaagnosticlogic.shared.errors.ControllerStateDataNotFoundException;
import com.aricontroller.control.domainobjects.shared.AsteriskRoutingInfo;
import com.aricontroller.control.domainobjects.shared.BridgeId;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final class ControllerState implements Serializable {
  private final RoutingKey routingKey;
  private final AsteriskRoutingInfo asteriskRoutingInfo;
  private final Set<ChannelId> channelIdList;
  private final Set<BridgeId> bridgeIdList;

  private ControllerState(
      final RoutingKey routingKey,
      final AsteriskRoutingInfo asteriskRoutingInfo,
      final Set<ChannelId> channelIdList,
      final Set<BridgeId> bridgeIdList) {

    requireNonNull(routingKey, "RoutingKey cannot be empty.");

    this.routingKey = routingKey;
    this.asteriskRoutingInfo = asteriskRoutingInfo;
    this.channelIdList = Set.copyOf(channelIdList);
    this.bridgeIdList = Set.copyOf(bridgeIdList);
  }

  public static ControllerState initialized(
      final RoutingKey routingKey, final AsteriskRoutingInfo asteriskRoutingInfo) {
    return new ControllerState(routingKey, asteriskRoutingInfo, new HashSet<>(), new HashSet<>());
  }

  public static ControllerState uninitialised(final RoutingKey routingKey) {
    return new ControllerState(routingKey, null, new HashSet<>(), new HashSet<>());
  }

  public ControllerState withRoutingInfo(final AsteriskRoutingInfo asteriskRoutingInfo) {
    return new ControllerState(routingKey, asteriskRoutingInfo, channelIdList, bridgeIdList);
  }

  public ControllerState withChannel(final ChannelId endId) {
    final Set<ChannelId> newChannelId = new HashSet<>(channelIdList);
    newChannelId.add(endId);
    return new ControllerState(routingKey, asteriskRoutingInfo, newChannelId, bridgeIdList);
  }

  public ControllerState removeChannel(final ChannelId endId) {
    final Set<ChannelId> newChannelId = new HashSet<>(channelIdList);
    newChannelId.remove(endId);
    return new ControllerState(routingKey, asteriskRoutingInfo, newChannelId, bridgeIdList);
  }

  public ControllerState withBridge(final BridgeId bridgeId) {
    final Set<BridgeId> newBridgeList = new HashSet<>(bridgeIdList);
    newBridgeList.add(bridgeId);
    return new ControllerState(routingKey, asteriskRoutingInfo, channelIdList, newBridgeList);
  }

  public ControllerState removeBridge(final BridgeId bridgeId) {
    final Set<BridgeId> newBridgeList = new HashSet<>(bridgeIdList);
    newBridgeList.remove(bridgeId);
    return new ControllerState(routingKey, asteriskRoutingInfo, channelIdList, newBridgeList);
  }

  public Set<BridgeId> getBridgeIdList() {
    return this.bridgeIdList;
  }

  public RoutingKey getRoutingKey() {
    return routingKey;
  }

  public AsteriskRoutingInfo getAsteriskRoutingInfo() {
    if (asteriskRoutingInfo == null) {
      throw new ControllerStateDataNotFoundException(
          "Unable to get asteriskRoutingInfo from uninitialized controller state");
    }
    return asteriskRoutingInfo;
  }

  @Override
  public int hashCode() {
    return reflectionHashCode(this);
  }

  @Override
  public boolean equals(final Object o) {
    return reflectionEquals(this, o);
  }

  @Override
  public String toString() {
    return reflectionToString(this, SHORT_PREFIX_STYLE);
  }
}
