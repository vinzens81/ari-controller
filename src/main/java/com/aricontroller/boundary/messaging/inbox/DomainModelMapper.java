package com.aricontroller.boundary.messaging.inbox;

import static com.aricontroller.boundary.messaging.ari.Constants.INTERNAL_CHANNEL_TARGET;

import com.aricontroller.boundary.messaging.ari.inbound.AriResourceType;
import com.aricontroller.boundary.messaging.ari.inbound.CommandsTopic;
import com.aricontroller.boundary.messaging.ari.inbound.events.bridge.ChannelEnteredBridgeAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.bridge.ChannelLeftBridgeAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.ChannelHangUpRequestAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.ChannelStateChangeAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.ChannelVarSetAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.DialAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.StasisEndAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.channel.StasisStartAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.playback.PlaybackEndedAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.events.playback.PlaybackStartedAriEvent;
import com.aricontroller.boundary.messaging.ari.inbound.responses.AriResponseWithCommandId;
import com.aricontroller.boundary.messaging.ari.resources.channel.Channel;
import com.aricontroller.control.domainobjects.ari.inbound.*;
import com.aricontroller.control.domainobjects.shared.AriCommandsTopic;
import com.aricontroller.control.domainobjects.shared.AsteriskRoutingInfo;
import com.aricontroller.control.domainobjects.shared.BridgeId;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.PhoneNumber;
import com.aricontroller.control.domainobjects.shared.PlaybackId;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public final class DomainModelMapper {

  private static final int HTTP_OK_CODE = 200;
  private static final int HTTP_NO_CONTENT_CODE = 204;
  private static final int HTTP_BAD_REQUEST = 400;
  private static final int HTTP_NOT_FOUND = 404;
  private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

  private DomainModelMapper() {
    throw new IllegalStateException("Utility class");
  }

  static AriChannelEvent convertAriStasisStartEvent(
      final StasisStartAriEvent event,
      final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey,
      final CommandsTopic commandsTopic) {

    if (INTERNAL_CHANNEL_TARGET.equals(event.channel().channelVars().target())) {
      return new InternalChannelStasisStartAriEvent(
          new RoutingKey(routingKey.value()),
          new AsteriskRoutingInfo(new AriCommandsTopic(commandsTopic.value())),
          convertChannel(event.channel()));
    }

    return new com.aricontroller.control.domainobjects.ari.inbound.StasisStartAriEvent(
        new RoutingKey(routingKey.value()),
        new AsteriskRoutingInfo(new AriCommandsTopic(commandsTopic.value())),
        convertChannel(event.channel()),
        new PhoneNumber(event.channel().channelVars().target()));
  }

  public static com.aricontroller.control.domainobjects.ari.inbound.StasisEndAriEvent
      convertAriStasisEndEvent(
          StasisEndAriEvent event,
          com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.StasisEndAriEvent(
        new RoutingKey(routingKey.value()), convertChannel(event.channel()));
  }

  static com.aricontroller.control.domainobjects.ari.inbound.ChannelVarSetAriEvent
      convertAriChannelVarsetEvent(
          final ChannelVarSetAriEvent channelVarSetAriEvent,
          final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.ChannelVarSetAriEvent(
        new RoutingKey(routingKey.value()),
        convertChannel(channelVarSetAriEvent.channel()),
        channelVarSetAriEvent.variable(),
        channelVarSetAriEvent.value());
  }

  static com.aricontroller.control.domainobjects.ari.inbound.PlaybackStartedAriEvent
      convertAriPlaybackStartedEvent(
          final PlaybackStartedAriEvent playEvent,
          final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.PlaybackStartedAriEvent(
        new RoutingKey(routingKey.value()), new PlaybackId(playEvent.playback().id().value()));
  }

  static com.aricontroller.control.domainobjects.ari.inbound.PlaybackEndedAriEvent
      convertAriPlaybackEndedEvent(
          final PlaybackEndedAriEvent playEvent,
          final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.PlaybackEndedAriEvent(
        new RoutingKey(routingKey.value()), new PlaybackId(playEvent.playback().id().value()));
  }

  static com.aricontroller.control.domainobjects.ari.inbound.ChannelStateChangeAriEvent
      convertAriChannelStateChangeEvent(
          final ChannelStateChangeAriEvent stateChangeEvent,
          final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.ChannelStateChangeAriEvent(
        new RoutingKey(routingKey.value()), convertChannel(stateChangeEvent.channel()));
  }

  public static ChannelHangupRequestAriEvent convertAriChannelHangupRequested(
      final ChannelHangUpRequestAriEvent channelHangupEvent,
      final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new ChannelHangupRequestAriEvent(
        new RoutingKey(routingKey.value()), convertChannel(channelHangupEvent.channel()));
  }

  public static com.aricontroller.control.domainobjects.ari.inbound.ChannelEnteredBridgeAriEvent
      convertAriChannelEnteredBridge(
          ChannelEnteredBridgeAriEvent event,
          com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.ChannelEnteredBridgeAriEvent(
        new RoutingKey(routingKey.value()),
        new ChannelId(event.channel().id().value()),
        new BridgeId(event.bridge().id().value()));
  }

  public static com.aricontroller.control.domainobjects.ari.inbound.ChannelLeftBridgeAriEvent
      convertAriChannelLeftBridge(
          ChannelLeftBridgeAriEvent event,
          com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.ChannelLeftBridgeAriEvent(
        new RoutingKey(routingKey.value()),
        new ChannelId(event.channel().id().value()),
        new BridgeId(event.bridge().id().value()));
  }

  public static com.aricontroller.control.domainobjects.ari.inbound.DialAriEvent convertAriDial(
      DialAriEvent event, com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    return new com.aricontroller.control.domainobjects.ari.inbound.DialAriEvent(
        new RoutingKey(routingKey.value()),
        new AriChannel(
            new ChannelId(event.peer().id().value()), convertChannelState(event.peer().state())));
  }

  public static AriChannelResponse convertAriChannelResponse(
      final AriResponseWithCommandId event,
      final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    final AriCommandId commandId = new AriCommandId(event.commandId().value());
    final ChannelId channelId =
        event.resources().stream()
            .filter(r -> r.type() == AriResourceType.CHANNEL)
            .findFirst()
            .map(resource -> new ChannelId(resource.id()))
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        String.format(
                            "Unable to convert response '%s' to channel response", event)));

    return new AriChannelResponse(
        commandId,
        new RoutingKey(routingKey.value()),
        channelId,
        convertResponseCode(event.responseCode()));
  }

  public static AriBridgeResponse convertAriBridgeResponse(
      final AriResponseWithCommandId event,
      final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    final AriCommandId commandId = new AriCommandId(event.commandId().value());
    final BridgeId bridgeId =
        event.resources().stream()
            .filter(r -> r.type() == AriResourceType.BRIDGE)
            .findFirst()
            .map(resource -> new BridgeId(resource.id()))
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        String.format(
                            "Unable to convert response '%s' to bridge response", event)));

    return new AriBridgeResponse(
        commandId,
        new RoutingKey(routingKey.value()),
        bridgeId,
        convertResponseCode(event.responseCode()));
  }

  private static AriChannel convertChannel(final Channel channel) {
    return new AriChannel(
        new ChannelId(channel.id().value()), convertChannelState(channel.state()));
  }

  private static AriChannelState convertChannelState(final String channelState) {
    final Map<String, AriChannelState> channelStateValues = new HashMap<>();
    channelStateValues.put("Down", AriChannelState.DOWN);
    channelStateValues.put("Rsrved", AriChannelState.RSRVED);
    channelStateValues.put("OffHook", AriChannelState.OFFHOOK);
    channelStateValues.put("Dialing", AriChannelState.DIALING);
    channelStateValues.put("Ring", AriChannelState.RING);
    channelStateValues.put("Ringing", AriChannelState.RINGING);
    channelStateValues.put("Up", AriChannelState.UP);
    channelStateValues.put("Busy", AriChannelState.BUSY);
    channelStateValues.put("Dialing Offhook", AriChannelState.DIALING_OFFHOOK);
    channelStateValues.put("Pre-ring", AriChannelState.PRE_RING);
    channelStateValues.put("Unknown", AriChannelState.UNKNOWN);

    return channelStateValues.entrySet().stream()
        .filter(value -> StringUtils.equalsIgnoreCase(value.getKey(), channelState))
        .findFirst()
        .map(Map.Entry::getValue)
        .orElse(
            AriChannelState.UNKNOWN); // TODO: find out if we rather want to drop the message here
  }

  private static AriResponseStatus convertResponseCode(final int responseCode) {
    return switch (responseCode) {
      case HTTP_OK_CODE, HTTP_NO_CONTENT_CODE -> AriResponseStatus.SUCCESS;
      case HTTP_BAD_REQUEST -> AriResponseStatus.BAD_REQUEST;
      case HTTP_NOT_FOUND -> AriResponseStatus.RESOURCE_NOT_FOUND;
      case HTTP_UNPROCESSABLE_ENTITY -> AriResponseStatus.UNPROCESSABLE_ENTITY;
      default -> AriResponseStatus.FAILURE;
    };
  }
}
