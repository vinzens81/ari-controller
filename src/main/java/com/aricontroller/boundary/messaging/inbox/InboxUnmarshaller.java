package com.aricontroller.boundary.messaging.inbox;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.aricontroller.boundary.messaging.ari.inbound.AriResourceType;
import com.aricontroller.boundary.messaging.ari.inbound.CommandsTopic;
import com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessageJsonEnvelope;
import com.aricontroller.boundary.messaging.ari.inbound.UnknownIncomingAriMessage;
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
import com.aricontroller.boundary.messaging.ari.inbound.responses.AriResponse;
import com.aricontroller.boundary.messaging.ari.inbound.responses.AriResponseWithCommandId;
import com.aricontroller.boundary.messaging.ari.outbound.commands.AriCommandId;
import com.aricontroller.control.domainobjects.ari.inbound.IncomingAriMessage;
import com.aricontroller.exception.UnsupportedTypeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;

public final class InboxUnmarshaller {
  private static final ObjectMapper OBJECT_MAPPER =
      JsonMapper.builder()
          .configure(FAIL_ON_NULL_CREATOR_PROPERTIES, false)
          .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
          .build();

  private InboxUnmarshaller() {
    throw new IllegalStateException("Utility class");
  }

  static IncomingAriMessage unmarshall(final String routingKey, final String payload)
      throws IOException {
    try {
      final InboxMessageEnvelope envelope =
          OBJECT_MAPPER.readValue(payload, InboxMessageEnvelope.class);

      if (envelope instanceof IncomingAriMessageJsonEnvelope incomingAriMessageJsonEnvelope) {
        final IncomingAriMessageData incomingAriMessageData =
            parseAsAriMessage(incomingAriMessageJsonEnvelope);
        validateIncomingAriMessage(incomingAriMessageData);
        return convertIncomingAriMessage(incomingAriMessageData);
      }
    } catch (JsonProcessingException | InvalidInboxMessageDataException e) {
      throw new InvalidInboxMessageException(payload, e);
    }

    throw new IOException("Unable to unmarshall inbox record payload: " + payload);
  }

  private static IncomingAriMessageData parseAsAriMessage(
      final IncomingAriMessageJsonEnvelope incomingAriMessageJsonEnvelope) throws IOException {
    final Class<? extends com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessage>
        msgTypeClass = incomingAriMessageJsonEnvelope.type().associatedClass();

    final com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessage incomingAriMessage =
        OBJECT_MAPPER.readerFor(msgTypeClass).readValue(incomingAriMessageJsonEnvelope.payload());

    if (incomingAriMessage instanceof UnknownIncomingAriMessage) {
      throw new IllegalArgumentException(
          "unknown ari message of type: " + incomingAriMessageJsonEnvelope.type());
    }

    final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey =
        new com.aricontroller.boundary.messaging.ari.inbound.RoutingKey(
            incomingAriMessageJsonEnvelope.callContext());
    final CommandsTopic commandsTopic =
        new CommandsTopic(incomingAriMessageJsonEnvelope.commandsTopic());

    if (incomingAriMessage instanceof AriResponse ariResponse) {
      final AriCommandId commandId =
          AriCommandId.fromString(incomingAriMessageJsonEnvelope.commandId())
              .orElseThrow(() -> new IllegalStateException("CommandId missing in AriResponse."));
      return new IncomingAriMessageData(
          new AriResponseWithCommandId(
              commandId,
              ariResponse.body(),
              ariResponse.statusCode(),
              incomingAriMessageJsonEnvelope.resources()),
          routingKey,
          commandsTopic);
    }

    return new IncomingAriMessageData(incomingAriMessage, routingKey, commandsTopic);
  }

  private static void validateIncomingAriMessage(
      final IncomingAriMessageData incomingAriMessageData) throws InvalidInboxMessageDataException {
    if (incomingAriMessageData.routingKey() == null
        || isBlank(incomingAriMessageData.routingKey().value())) {
      throw new InvalidInboxMessageDataException("Routing key missing");
    }

    if (incomingAriMessageData.commandsTopic() == null
        || isBlank(incomingAriMessageData.commandsTopic().value())) {
      throw new InvalidInboxMessageDataException("Commands topic missing");
    }
  }

  private static com.aricontroller.control.domainobjects.ari.inbound.IncomingAriMessage
      convertIncomingAriMessage(final IncomingAriMessageData incomingAriMessageData) {
    final com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessage incomingAriMessage =
        incomingAriMessageData.incomingAriMessage();
    final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey =
        incomingAriMessageData.routingKey();
    final CommandsTopic commandsTopic = incomingAriMessageData.commandsTopic();

    if (incomingAriMessage instanceof StasisStartAriEvent message) {
      return DomainModelMapper.convertAriStasisStartEvent(message, routingKey, commandsTopic);
    }
    if (incomingAriMessage instanceof StasisEndAriEvent message) {
      return DomainModelMapper.convertAriStasisEndEvent(message, routingKey);
    }
    if (incomingAriMessage instanceof ChannelVarSetAriEvent message) {
      return DomainModelMapper.convertAriChannelVarsetEvent(message, routingKey);
    }
    if (incomingAriMessage instanceof ChannelStateChangeAriEvent message) {
      return DomainModelMapper.convertAriChannelStateChangeEvent(message, routingKey);
    }
    if (incomingAriMessage instanceof ChannelEnteredBridgeAriEvent message) {
      return DomainModelMapper.convertAriChannelEnteredBridge(message, routingKey);
    }
    if (incomingAriMessage instanceof ChannelLeftBridgeAriEvent message) {
      return DomainModelMapper.convertAriChannelLeftBridge(message, routingKey);
    }
    if (incomingAriMessage instanceof ChannelHangUpRequestAriEvent message) {
      return DomainModelMapper.convertAriChannelHangupRequested(message, routingKey);
    }
    if (incomingAriMessage instanceof DialAriEvent message) {
      return DomainModelMapper.convertAriDial(message, routingKey);
    }
    if (incomingAriMessage instanceof PlaybackStartedAriEvent message) {
      return DomainModelMapper.convertAriPlaybackStartedEvent(message, routingKey);
    }
    if (incomingAriMessage instanceof PlaybackEndedAriEvent message) {
      return DomainModelMapper.convertAriPlaybackEndedEvent(message, routingKey);
    }
    if (incomingAriMessage instanceof AriResponseWithCommandId message) {
      return convertAriResponse(message, routingKey);
    }

    throw new UnsupportedTypeException(incomingAriMessage);
  }

  private static com.aricontroller.control.domainobjects.ari.inbound.IncomingAriMessage
      convertAriResponse(
          final AriResponseWithCommandId message,
          final com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey) {
    final boolean isBridgeCommand =
        message.resources().stream().anyMatch(s -> s.type() == AriResourceType.BRIDGE);
    if (isBridgeCommand) {
      return DomainModelMapper.convertAriBridgeResponse(message, routingKey);
    }

    final boolean isChannelCommand =
        message.resources().stream().anyMatch(s -> s.type() == AriResourceType.CHANNEL);
    if (isChannelCommand) {
      return DomainModelMapper.convertAriChannelResponse(message, routingKey);
    }
    throw new UnsupportedTypeException("converting ARI response to domain object.", message);
  }

  private static record IncomingAriMessageData(
      com.aricontroller.boundary.messaging.ari.inbound.IncomingAriMessage incomingAriMessage,
      com.aricontroller.boundary.messaging.ari.inbound.RoutingKey routingKey,
      CommandsTopic commandsTopic) {}

  private static class InvalidInboxMessageDataException extends Exception {
    public InvalidInboxMessageDataException(final String message) {
      super(message);
    }
  }

  public static class InvalidInboxMessageException extends RuntimeException {
    public InvalidInboxMessageException(final Object inboxMessage, final Exception error) {
      super("Unable to unmarshall inbox message '%s': %s".formatted(inboxMessage, error));
    }
  }
}
