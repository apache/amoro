/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.flink.kafka.testutils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.netease.arctic.flink.kafka.testutils.KafkaConfigGenerate.getProperties;
import static com.netease.arctic.flink.kafka.testutils.KafkaConfigGenerate.getPropertiesWithByteArray;
import static com.netease.arctic.flink.kafka.testutils.KafkaUtil.createKafkaContainer;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

@Testcontainers
public interface KafkaContainerTest {
  Logger LOG = LoggerFactory.getLogger(KafkaContainerTest.class);
  String INTER_CONTAINER_KAFKA_ALIAS = "kafka";
  Network NETWORK = Network.newNetwork();
  String KAFKA = "confluentinc/cp-kafka:6.2.2";

  @Container
  KafkaContainer KAFKA_CONTAINER =
      createKafkaContainer(KAFKA, LOG)
          .withEmbeddedZookeeper()
          .withNetwork(NETWORK)
          .withNetworkAliases(INTER_CONTAINER_KAFKA_ALIAS);

  default ConsumerRecords<String, String> readRecords(String topic) {
    Properties properties = getProperties();
    properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
    consumer.assign(
        consumer.partitionsFor(topic).stream()
            .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
            .collect(Collectors.toSet()));
    consumer.seekToBeginning(consumer.assignment());
    return consumer.poll(Duration.ofMillis(1000));
  }

  default ConsumerRecords<byte[], byte[]> readRecordsBytes(String topic) {
    return (ConsumerRecords<byte[], byte[]>) readRecords(topic, getPropertiesWithByteArray());
  }

  default ConsumerRecords<?, ?> readRecords(String topic, Properties properties) {
    properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    KafkaConsumer<?, ?> consumer = new KafkaConsumer<>(properties);
    consumer.assign(
        consumer.partitionsFor(topic).stream()
            .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
            .collect(Collectors.toSet()));
    consumer.seekToBeginning(consumer.assignment());
    return consumer.poll(Duration.ofMillis(1000));
  }

  default void createTopics(int numPartitions, String... topics) {
    List<NewTopic> newTopics =
        Arrays.stream(topics)
            .map(topic -> new NewTopic(topic, numPartitions, (short) 1))
            .collect(Collectors.toList());
    Map<String, Object> params = new HashMap<>();
    params.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
    try (AdminClient admin = AdminClient.create(params)) {
      admin.createTopics(newTopics);
    }
  }

  default void deleteTopics(String... topics) {
    Map<String, Object> params = new HashMap<>();
    params.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
    try (AdminClient admin = AdminClient.create(params)) {
      admin.deleteTopics(Arrays.asList(topics));
    }
  }
}
