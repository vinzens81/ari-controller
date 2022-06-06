package com.aricontroller.blackbox;

import static com.aricontroller.blackbox.MessageTemplates.channelStateChangedAriEvent;
import static com.aricontroller.blackbox.MessageTemplates.stasisStartAriEvent;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import akka.actor.typed.ActorSystem;
import com.aricontroller.Orchestrator;
import com.aricontroller.control.controller.Controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

public class BlackboxTest {
  private static final String ARI_COMMANDS_TOPIC = "ari-commands";
  private static final String INBOX_TOPIC = "controller-inbox";
  private static final String ROUTING_KEY = "theRoutingKey";
  private static final String CHANNEL_ID = "theChannelId";
  private static final String CHANNEL_NAME = "theChannelName";
  private static final Duration EXPECTED_MESSAGE_TIMEOUT = Duration.ofSeconds(5);

  private static final Logger LOGGER = LoggerFactory.getLogger(BlackboxTest.class);
  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  private static KafkaContainer kafka;
  private static KafkaProducer<String, String> kafkaProducer;
  private static KafkaConsumer<String, String> kafkaConsumer;
  private static ActorSystem<Void> app;

  @Test
  void happyPath() throws IOException {
    kafkaProducer.send(stasisStart());
    assertIsAnswerChannelAriCommand(expectNextAriCommand());

    kafkaProducer.send(channelStateChanged());
    assertIsPlayAudioAriCommand(expectNextAriCommand());

    kafkaProducer.send(channelStateChanged());
  }

  @BeforeAll
  static void setup() {
    initKafka();
    initApp();
  }

  @AfterAll
  static void teardown() {
    kafkaProducer.close();
    app.terminate();
    kafka.close();
  }

  private static void initKafka() {
    kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.1.1"));
    kafka.start();

    kafkaProducer =
        new KafkaProducer<>(
            Map.of(
                BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers(),
                CLIENT_ID_CONFIG,
                "BlackboxTestProducer",
                KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class,
                VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class));

    kafkaConsumer =
        new KafkaConsumer<>(
            Map.of(
                BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers(),
                GROUP_ID_CONFIG,
                "BlackboxTestConsumer",
                KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,
                VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class));
    kafkaConsumer.subscribe(List.of(ARI_COMMANDS_TOPIC));
  }

  private static void initApp() {
    System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafka.getBootstrapServers());
    app = ActorSystem.create(Orchestrator.create(Controller::create), "orchestrator");
  }

  private ConsumerRecord<String, String> expectNextAriCommand() {
    return Awaitility.await()
        .atMost(EXPECTED_MESSAGE_TIMEOUT)
        .until(
            () ->
                StreamSupport.stream(kafkaConsumer.poll(Duration.ofMillis(50)).spliterator(), false)
                    .findFirst(),
            Optional::isPresent)
        .orElseThrow();
  }

  private void assertIsAnswerChannelAriCommand(final ConsumerRecord<String, String> command)
      throws JsonProcessingException {
    final JsonNode payload = JSON_MAPPER.readTree(command.value());
    assertAriCommandMetadata(payload, "POST");
    assertEquals(
        "/channels/%s/answer".formatted(CHANNEL_ID), payload.at("/ariCommand/url").asText());
  }

  private void assertIsPlayAudioAriCommand(final ConsumerRecord<String, String> command)
      throws JsonProcessingException {
    final JsonNode payload = JSON_MAPPER.readTree(command.value());
    assertAriCommandMetadata(payload, "POST");

    final String actualUrl = payload.at("/ariCommand/url").asText();
    final String expectedUrlPrefix = "/channels/%s/play/".formatted(CHANNEL_ID);
    assertTrue(actualUrl.startsWith(expectedUrlPrefix));

    final String actualPlayId = actualUrl.substring(expectedUrlPrefix.length());
    assertTrue(actualPlayId.matches("^[a-zA-Z\\d-]+$"));

    assertEquals("sound:tt-monkeys", payload.at("/ariCommand/body/media").asText());
    assertEquals("en", payload.at("/ariCommand/body/lang").asText());
  }

  private void assertAriCommandMetadata(final JsonNode payload, final String method) {
    assertEquals(ROUTING_KEY, payload.at("/callContext").asText());
    assertTrue(isNotBlank(payload.at("/commandId").asText()));
    assertEquals(method, payload.at("/ariCommand/method").asText());
  }

  private static ProducerRecord<String, String> stasisStart() {
    return new ProducerRecord<>(
        INBOX_TOPIC,
        ROUTING_KEY,
        stasisStartAriEvent(ROUTING_KEY, CHANNEL_ID, CHANNEL_NAME, ARI_COMMANDS_TOPIC));
  }

  private static ProducerRecord<String, String> channelStateChanged() {
    return new ProducerRecord<>(
        INBOX_TOPIC,
        ROUTING_KEY,
        channelStateChangedAriEvent(ROUTING_KEY, CHANNEL_ID, CHANNEL_NAME, ARI_COMMANDS_TOPIC));
  }
}
