package com.aricontroller.control.controller.akkaagnosticlogic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aricontroller.control.controller.akkaagnosticlogic.shared.ControllerState;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effect.PublishAriCommand;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effects;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Result;
import com.aricontroller.control.domainobjects.ari.Language;
import com.aricontroller.control.domainobjects.ari.Sound;
import com.aricontroller.control.domainobjects.ari.inbound.AriChannel;
import com.aricontroller.control.domainobjects.ari.inbound.AriChannelState;
import com.aricontroller.control.domainobjects.ari.inbound.ChannelStateChangeAriEvent;
import com.aricontroller.control.domainobjects.ari.inbound.StasisStartAriEvent;
import com.aricontroller.control.domainobjects.ari.outbound.AnswerChannelAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.AriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.PlaySoundAriCommand;
import com.aricontroller.control.domainobjects.shared.*;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class CommandHandlerTest {

  private static final RoutingKey ROUTING_KEY = new RoutingKey("aRoutingKey");
  private static final AriCommandsTopic COMMANDS_TOPIC = new AriCommandsTopic("commandsTopic");
  private static final AsteriskRoutingInfo ASTERISK_ROUTING_INFO =
      new AsteriskRoutingInfo(COMMANDS_TOPIC);
  private static final ChannelId CHANNEL_ID = new ChannelId("aChannelId");

  @Test
  void handleStasisStartAriEvent() {
    final ControllerState controllerState = ControllerState.uninitialised(ROUTING_KEY);
    final StasisStartAriEvent stasisStart =
        new StasisStartAriEvent(
            ROUTING_KEY,
            ASTERISK_ROUTING_INFO,
            new AriChannel(CHANNEL_ID, AriChannelState.RING),
            new PhoneNumber("someDestinationNumber"));

    final Result result = CommandHandler.handleIncomingMessage(stasisStart, controllerState);

    final var actualAnswerCommand =
        extractSinglePublishedAriCommand(result, AnswerChannelAriCommand.class);
    final var expectedState =
        ControllerState.initialized(ROUTING_KEY, ASTERISK_ROUTING_INFO).withChannel(CHANNEL_ID);
    final var expectedEffects =
        Effects.of(
            new PublishAriCommand(
                new AnswerChannelAriCommand(actualAnswerCommand.ariCommandId(), CHANNEL_ID),
                COMMANDS_TOPIC,
                ROUTING_KEY));
    assertEquals(new Result(expectedState, expectedEffects), result);
  }

  @Test
  void handleChannelStateChangeAriEvent() {
    final ControllerState controllerState =
        ControllerState.initialized(ROUTING_KEY, ASTERISK_ROUTING_INFO);
    final ChannelStateChangeAriEvent channelStateChange =
        new ChannelStateChangeAriEvent(ROUTING_KEY, new AriChannel(CHANNEL_ID, AriChannelState.UP));

    final Result result = CommandHandler.handleIncomingMessage(channelStateChange, controllerState);

    final var actualPlaySoundCommand =
        extractSinglePublishedAriCommand(result, PlaySoundAriCommand.class);
    final var expectedState = ControllerState.initialized(ROUTING_KEY, ASTERISK_ROUTING_INFO);
    final var expectedEffects =
        Effects.of(
            new PublishAriCommand(
                new PlaySoundAriCommand(
                    actualPlaySoundCommand.ariCommandId(),
                    CHANNEL_ID,
                    actualPlaySoundCommand.playbackId(),
                    new Language("en"),
                    new Sound("tt-monkeys", "")),
                COMMANDS_TOPIC,
                ROUTING_KEY));
    assertEquals(new Result(expectedState, expectedEffects), result);
  }

  private static <T extends AriCommand> T extractSinglePublishedAriCommand(
      final Result result, Class<T> commandType) {
    final List<T> candidates =
        result.effects().effects().stream()
            .flatMap(
                effect ->
                    effect instanceof PublishAriCommand publishAriCommand
                        ? Stream.of(publishAriCommand.ariCommand())
                        : Stream.empty())
            .flatMap(
                command ->
                    commandType.isInstance(command)
                        ? Stream.of(commandType.cast(command))
                        : Stream.empty())
            .toList();

    return switch (candidates.size()) {
      case 1 -> candidates.get(0);
      case 0 -> throw new AssertionFailedError(
          "Result does not contain any published ARI command of type %s"
              .formatted(commandType.getSimpleName()));
      default -> throw new AssertionFailedError(
          "Result does contain more than 1 published ARI command of type %s"
              .formatted(commandType.getSimpleName()));
    };
  }
}
