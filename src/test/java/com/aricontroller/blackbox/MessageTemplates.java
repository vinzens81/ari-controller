package com.aricontroller.blackbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MessageTemplates {

  private MessageTemplates() {
    throw new IllegalStateException("Utility class");
  }

  public static String stasisStartAriEvent(
      final String routingKey,
      final String channelId,
      final String channelName,
      final String ariCommandsTopic) {
    return loadJson("messagetemplates/StasisStartAriEvent.json")
        .replaceAll("#ROUTING_KEY#", routingKey)
        .replaceAll("#CHANNEL_ID#", channelId)
        .replaceAll("#CHANNEL_NAME#", channelName)
        .replaceAll("#COMMANDS_TOPIC#", ariCommandsTopic);
  }

  public static String channelStateChangedAriEvent(
      final String routingKey,
      final String channelId,
      final String channelName,
      final String ariCommandsTopic) {
    return loadJson("messagetemplates/ChannelStateChangeAriEvent.json")
        .replaceAll("#ROUTING_KEY#", routingKey)
        .replaceAll("#CHANNEL_ID#", channelId)
        .replaceAll("#CHANNEL_NAME#", channelName)
        .replaceAll("#COMMANDS_TOPIC#", ariCommandsTopic);
  }

  public static String channelHangupRequestEvent(
      final String routingKey, final String channelEndId, final String ariCommandsTopic) {
    return loadJson("messagetemplates/ChannelHangupRequestEvent.json")
        .replaceAll("#ROUTING_KEY#", routingKey)
        .replaceAll("#CHANNEL_END_ID#", channelEndId)
        .replaceAll("#COMMANDS_TOPIC#", ariCommandsTopic);
  }

  private static String loadJson(final String resourceName) {
    final ClassLoader classLoader = MessageTemplates.class.getClassLoader();
    final File file = new File(classLoader.getResource(resourceName).getFile());
    try {
      return new String(Files.readAllBytes(file.toPath()));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
