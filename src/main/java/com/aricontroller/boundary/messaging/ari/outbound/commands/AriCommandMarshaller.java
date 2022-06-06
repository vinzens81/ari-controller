package com.aricontroller.boundary.messaging.ari.outbound.commands;

import static com.aricontroller.boundary.messaging.ari.Constants.CHANNEL_VARIABLE_CALL_CONTEXT;
import static com.aricontroller.boundary.messaging.ari.Constants.CHANNEL_VARIABLE_TARGET;
import static com.aricontroller.boundary.messaging.ari.Constants.CONTEXT_NAME;
import static com.aricontroller.boundary.messaging.ari.Constants.INTERNAL_CHANNEL_TARGET;

import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.bridge.AddChannelToBridgeAriCommandBody;
import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.bridge.CreateBridgeAriCommandBody;
import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel.CreateLocalChannelAriCommandBody;
import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel.HangUpChannelAriCommandBody;
import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel.PlaySoundOnChannelAriCommandBody;
import com.aricontroller.boundary.messaging.ari.outbound.commands.bodies.channel.SetChannelVariableAriCommandBody;
import com.aricontroller.control.domainobjects.ari.inbound.AriCommandId;
import com.aricontroller.control.domainobjects.ari.outbound.AddChannelToBridgeAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.AnswerChannelAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.CreateBridgeAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.CreateLocalChannelAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.DeleteBridgeAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.DialChannelAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.HangupChannelAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.PlaySoundAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.RemoveChannelFromBridgeAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.SetChannelVariableAriCommand;
import com.aricontroller.control.domainobjects.shared.RoutingKey;
import com.aricontroller.exception.UnsupportedTypeException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;

public final class AriCommandMarshaller {

  private static final ObjectWriter writer = createWriter();

  private static final String CHANNEL_VARIABLE_SINGLE_INHERITANCE_PREFIX = "_";

  private AriCommandMarshaller() {
    throw new IllegalStateException("Utility class");
  }

  public static String marshall(
      final RoutingKey routingKey,
      final AriCommandId ariCommandId,
      final com.aricontroller.control.domainobjects.ari.outbound.AriCommand ariCommand) {

    final AriCommandJsonEnvelope envelope =
        new AriCommandJsonEnvelope(
            translateToRestCommand(ariCommand, routingKey),
            routingKey.value(),
            ariCommandId.value());

    try {
      return writer.writeValueAsString(envelope);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static AriCommand translateToRestCommand(
      com.aricontroller.control.domainobjects.ari.outbound.AriCommand modelAriCommand,
      final RoutingKey routingKey) {
    if (modelAriCommand instanceof AnswerChannelAriCommand command) {
      return createAnswerChannelAriCommand(command);
    }
    if (modelAriCommand instanceof HangupChannelAriCommand command) {
      return createChannelHangupAriCommand(command);
    }
    if (modelAriCommand instanceof PlaySoundAriCommand command) {
      return createPlaySoundOnChannelCommand(command);
    }
    if (modelAriCommand instanceof CreateBridgeAriCommand command) {
      return createCreateBridgeAriCommand(command);
    }
    if (modelAriCommand instanceof AddChannelToBridgeAriCommand command) {
      return createAddChannelToBridgeAriCommand(command);
    }
    if (modelAriCommand instanceof RemoveChannelFromBridgeAriCommand command) {
      return createRemoveChannelFromBridgeAriCommand(command);
    }
    if (modelAriCommand instanceof DeleteBridgeAriCommand command) {
      return createDeleteBridgeAriCommand(command);
    }
    if (modelAriCommand instanceof CreateLocalChannelAriCommand command) {
      return createCreateLocalChannelAriCommand(command, routingKey);
    }
    if (modelAriCommand instanceof DialChannelAriCommand command) {
      return createDialChannelAriCommand(command);
    }

    if (modelAriCommand instanceof SetChannelVariableAriCommand command) {
      return createSetChannelVariableAriCommand(command);
    }

    throw new UnsupportedTypeException(modelAriCommand);
  }

  private static AriCommand createSetChannelVariableAriCommand(
      final SetChannelVariableAriCommand command) {
    return new AriCommand(
        "POST",
        String.format("/channels/%s/variable", command.channelId().value()),
        new SetChannelVariableAriCommandBody(command.variables()));
  }

  private static AriCommand createPlaySoundOnChannelCommand(final PlaySoundAriCommand command) {
    String media;
    if (command.sound().folder().length() > 0) {
      media = String.format("sound:%s/%s", command.sound().folder(), command.sound().name());
    } else {
      media = String.format("sound:%s", command.sound().name());
    }

    final PlaySoundOnChannelAriCommandBody body =
        new PlaySoundOnChannelAriCommandBody(media, command.language().value());

    return new AriCommand(
        "POST",
        String.format(
            "/channels/%s/play/%s", command.channelId().value(), command.playbackId().value()),
        body);
  }

  private static AriCommand createChannelHangupAriCommand(final HangupChannelAriCommand command) {
    return new AriCommand(
        "DELETE",
        String.format("/channels/%s", command.channelId().value()),
        new HangUpChannelAriCommandBody());
  }

  private static AriCommand createAnswerChannelAriCommand(final AnswerChannelAriCommand command) {
    return AriCommand.withEmptyBody(
        "POST", String.format("/channels/%s/answer", command.channelId().value()));
  }

  private static AriCommand createAddChannelToBridgeAriCommand(
      final AddChannelToBridgeAriCommand command) {
    return new AriCommand(
        "POST",
        String.format("/bridges/%s/addChannel", command.bridgeId().value()),
        new AddChannelToBridgeAriCommandBody(command.channelId().value()));
  }

  private static AriCommand createRemoveChannelFromBridgeAriCommand(
      final RemoveChannelFromBridgeAriCommand command) {
    return new AriCommand(
        "POST",
        String.format("/bridges/%s/removeChannel", command.bridgeId().value()),
        new AddChannelToBridgeAriCommandBody(command.channelId().value()));
  }

  private static AriCommand createCreateBridgeAriCommand(final CreateBridgeAriCommand command) {
    return new AriCommand(
        "POST", "/bridges", new CreateBridgeAriCommandBody(command.bridgeId().value()));
  }

  private static AriCommand createCreateLocalChannelAriCommand(
      final CreateLocalChannelAriCommand command, final RoutingKey routingKey) {
    return new AriCommand(
        "POST",
        "/channels/create",
        new CreateLocalChannelAriCommandBody(
            "local/%s@%s".formatted(INTERNAL_CHANNEL_TARGET, CONTEXT_NAME),
            command.channelId().value(),
            command.terminatingChannelId().value(),
            Map.of(
                withInheritancePrefix(CHANNEL_VARIABLE_CALL_CONTEXT),
                routingKey.value(),
                withInheritancePrefix(CHANNEL_VARIABLE_TARGET),
                INTERNAL_CHANNEL_TARGET)));
  }

  private static String withInheritancePrefix(final String channelVariableName) {
    return "%s%s".formatted(CHANNEL_VARIABLE_SINGLE_INHERITANCE_PREFIX, channelVariableName);
  }

  private static AriCommand createDialChannelAriCommand(final DialChannelAriCommand command) {
    return AriCommand.withEmptyBody(
        "POST", "/channels/%s/dial".formatted(command.channelId().value()));
  }

  private static AriCommand createDeleteBridgeAriCommand(final DeleteBridgeAriCommand command) {
    return AriCommand.withEmptyBody(
        "DELETE", String.format("/bridges/%s", command.bridgeId().value()));
  }

  private static ObjectWriter createWriter() {
    return JsonMapper.builder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build()
        .writerFor(new TypeReference<AriCommandJsonEnvelope>() {});
  }
}
