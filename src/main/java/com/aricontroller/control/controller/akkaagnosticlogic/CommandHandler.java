package com.aricontroller.control.controller.akkaagnosticlogic;

import com.aricontroller.AriLogger;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.ControllerState;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effect.PublishAriCommand;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Effects;
import com.aricontroller.control.controller.akkaagnosticlogic.shared.Result;
import com.aricontroller.control.domainobjects.ari.Language;
import com.aricontroller.control.domainobjects.ari.Sound;
import com.aricontroller.control.domainobjects.ari.inbound.*;
import com.aricontroller.control.domainobjects.ari.outbound.AnswerChannelAriCommand;
import com.aricontroller.control.domainobjects.ari.outbound.PlaySoundAriCommand;
import com.aricontroller.control.domainobjects.shared.ChannelId;
import com.aricontroller.control.domainobjects.shared.PlaybackId;

public final class CommandHandler {

  private static final AriLogger LOGGER = new AriLogger(CommandHandler.class);

  private CommandHandler() {
    throw new IllegalStateException("Utility class");
  }

  public static Result handleIncomingMessage(
      final IncomingAriMessage incomingAriMessage, final ControllerState controllerState) {
    LOGGER.debug(
        incomingAriMessage.routingKey(),
        "Handling incoming ARI message {} in state: {}",
        incomingAriMessage,
        controllerState);
    return switch (incomingAriMessage) {
      case IncomingAriChannelMessage channelMessage -> handleChannelEvent(
          controllerState, channelMessage);
      case IncomingAriPlaybackEvent playbackEvent -> handlePlaybackEvent(
          controllerState, playbackEvent);
      case IncomingAriBridgeMessage bridgeMessage -> {
        LOGGER.info(
            controllerState.getRoutingKey(),
            "Ignoring message of type {} because handling was not implemented",
            bridgeMessage.getClass().getSimpleName());
        yield new Result(controllerState, Effects.empty());
      }
    };
  }

  private static Result handleChannelEvent(
      final ControllerState controllerState, final IncomingAriChannelMessage message) {
    return switch (message) {
      case StasisStartAriChannelEvent event -> handleStasisStart(controllerState, event);
      case ChannelStateChangeAriEvent event -> handleStateChange(controllerState, event);
      case ChannelHangupRequestAriEvent event -> handleHangup(controllerState, event);
      case ChannelVarSetAriEvent event -> handleChannelVarset(controllerState, event);
      case DialAriEvent event -> handleDial(controllerState, event);
      case StasisEndAriEvent event -> handleStasisEndEvent(controllerState, event);
      case AriChannelResponse response -> handleChannelResponse(controllerState, response);
    };
  }

  private static Result handlePlaybackEvent(
      final ControllerState controllerState, final IncomingAriPlaybackEvent event) {
    return switch (event) {
      case PlaybackStartedAriEvent playbackStarted -> {
        LOGGER.info(
            controllerState.getRoutingKey(),
            "Playback {} started",
            playbackStarted.playbackId().value());
        yield new Result(controllerState, Effects.empty());
      }
      case PlaybackEndedAriEvent playbackEnded -> {
        LOGGER.info(
            controllerState.getRoutingKey(),
            "Playback {} stopped",
            playbackEnded.playbackId().value());
        yield new Result(controllerState, Effects.empty());
      }
    };
  }

  private static Result handleStasisStart(
      final ControllerState controllerState, final StasisStartAriChannelEvent event) {
    LOGGER.info(
        controllerState.getRoutingKey(),
        "Received a StasisStart for channel {}",
        event.channelId().value());

    final Effects effects =
        Effects.of(
            new PublishAriCommand(
                new AnswerChannelAriCommand(AriCommandId.generate(), event.channelId()),
                event.asteriskRoutingInfo().ariCommandsTopic(),
                event.routingKey()));

    final ControllerState nextState =
        controllerState.withRoutingInfo(event.asteriskRoutingInfo()).withChannel(event.channelId());

    return new Result(nextState, effects);
  }

  private static Result handleStateChange(
      final ControllerState controllerState, final ChannelStateChangeAriEvent event) {
    if (!event.ariChannel().state().equals(AriChannelState.UP)) {
      return new Result(controllerState, Effects.empty());
    }

    final ChannelId channelId = event.ariChannel().channelId();
    LOGGER.info(
        controllerState.getRoutingKey(),
        "Channel {} is up, playback tt-monkeys",
        channelId.value());

    final PlaySoundAriCommand playback =
        new PlaySoundAriCommand(
            AriCommandId.generate(),
            channelId,
            PlaybackId.generate(),
            new Language("en"),
            new Sound("tt-monkeys", ""));
    final Effects effects =
        Effects.of(
            new PublishAriCommand(
                playback,
                controllerState.getAsteriskRoutingInfo().ariCommandsTopic(),
                controllerState.getRoutingKey()));

    return new Result(controllerState, effects);
  }

  private static Result handleHangup(
      final ControllerState controllerState, final ChannelHangupRequestAriEvent event) {
    LOGGER.info(
        controllerState.getRoutingKey(),
        "Call of channel {} was hung up, stopping controller session",
        event.channelId().value());

    return new Result(controllerState, Effects.empty().withStopSession());
  }

  private static Result handleChannelVarset(
      final ControllerState controllerState, final ChannelVarSetAriEvent channelVarSet) {
    LOGGER.info(
        controllerState.getRoutingKey(),
        "Channel var {} was set to {} on channel {}",
        channelVarSet.variable(),
        channelVarSet.value(),
        channelVarSet.channelId().value());

    return new Result(controllerState, Effects.empty());
  }

  private static Result handleDial(final ControllerState controllerState, final DialAriEvent dial) {
    LOGGER.info(dial.routingKey(), "Received dial event on channel {}", dial.channelId().value());

    return new Result(controllerState, Effects.empty());
  }

  private static Result handleStasisEndEvent(
      final ControllerState controllerState, final StasisEndAriEvent stasisEnd) {
    LOGGER.info(
        stasisEnd.routingKey(),
        "Received stasis end event for channel {}",
        stasisEnd.channelId().value());

    return new Result(controllerState, Effects.empty());
  }

  private static Result handleChannelResponse(
      final ControllerState controllerState, final AriChannelResponse response) {
    LOGGER.info(
        controllerState.getRoutingKey(),
        "Ignoring ARI channel response {} because handling was not implemented yet",
        response);

    return new Result(controllerState, Effects.empty());
  }
}
