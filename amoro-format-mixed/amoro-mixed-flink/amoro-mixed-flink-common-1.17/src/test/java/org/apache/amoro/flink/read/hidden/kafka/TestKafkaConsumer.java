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

package org.apache.amoro.flink.read.hidden.kafka;

import static org.apache.amoro.flink.kafka.testutils.KafkaConfigGenerate.getProperties;
import static org.apache.amoro.flink.kafka.testutils.KafkaConfigGenerate.getPropertiesWithByteArray;
import static org.apache.amoro.flink.kafka.testutils.KafkaContainerTest.KAFKA_CONTAINER;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.amoro.flink.kafka.testutils.KafkaConfigGenerate;
import org.apache.amoro.flink.kafka.testutils.KafkaContainerTest;
import org.apache.amoro.flink.write.hidden.kafka.TestBaseLog;
import org.apache.amoro.flink.write.hidden.kafka.TestHiddenLogOperators;
import org.apache.flink.streaming.connectors.kafka.internals.FlinkKafkaInternalProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestKafkaConsumer extends TestBaseLog {
  private static final Logger LOG = LoggerFactory.getLogger(TestKafkaConsumer.class);

  @BeforeClass
  public static void prepare() throws Exception {
    KAFKA_CONTAINER.start();
  }

  @AfterClass
  public static void shutdown() throws Exception {
    KAFKA_CONTAINER.close();
  }

  @Test
  public void testTransactionalConsume() {
    final String topic = "test-offset-flip";
    FlinkKafkaInternalProducer<String, String> reuse = null;
    final String transactionalIdPrefix = UUID.randomUUID().toString();
    try {
      int numCount = 20;
      Properties properties = new Properties();
      properties.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
      properties = getProperties(KafkaConfigGenerate.getStandardProperties(properties));
      properties.put(TRANSACTIONAL_ID_CONFIG, transactionalIdPrefix + "flip");
      reuse = new FlinkKafkaInternalProducer<>(properties);
      reuse.initTransactions();
      reuse.beginTransaction();
      for (int i = 1; i <= numCount; i++) {
        reuse.send(new ProducerRecord<>(topic, "test-value-" + i));
      }
      reuse.commitTransaction();
      int count = KafkaContainerTest.countAllRecords(topic, properties);
      LOG.info("consumption = {}", count);
      assertThat(count).isEqualTo(numCount);
    } catch (Throwable e) {
      LOG.error("error:", e);
      if (reuse != null) {
        reuse.abortTransaction();
      }
    } finally {
      assert reuse != null;
      reuse.close(Duration.ofMillis(1000));
    }
  }

  @Test
  public void testResetOffset() {
    final int countNum = 20;
    String topicIntern = TestHiddenLogOperators.TOPIC;
    Properties properties = new Properties();
    properties.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
    properties = getPropertiesWithByteArray(KafkaConfigGenerate.getStandardProperties(properties));
    // send
    properties.put(TRANSACTIONAL_ID_CONFIG, "transactionalId1");
    FlinkKafkaInternalProducer<byte[], byte[]> reuse = new FlinkKafkaInternalProducer<>(properties);
    reuse.initTransactions();
    reuse.beginTransaction();
    String[] expects = new String[countNum];
    for (int i = 0; i < countNum; i++) {
      expects[i] = "test-value-" + i;
      reuse.send(new ProducerRecord<>(TestHiddenLogOperators.TOPIC, expects[i].getBytes()));
    }
    reuse.commitTransaction();
    reuse.close(Duration.ofMillis(1000));

    // read all
    properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(properties);
    Set<TopicPartition> topicPartitionList =
        consumer.partitionsFor(topicIntern).stream()
            .map(partitionInfo -> new TopicPartition(topicIntern, partitionInfo.partition()))
            .collect(Collectors.toSet());
    TopicPartition partition0 = topicPartitionList.stream().iterator().next();
    consumer.assign(topicPartitionList);
    consumer.seekToBeginning(consumer.assignment());
    ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(1000));

    int count = consumerRecords.count();
    assertThat(count).isEqualTo(countNum);
    List<String> actual = new ArrayList<>();
    consumerRecords.forEach(consumerRecord -> actual.add(new String(consumerRecord.value())));
    Assertions.assertArrayEquals(expects, actual.toArray(new String[0]));

    // seek
    long seekOffset = 1L;
    consumer.seek(partition0, seekOffset);

    consumerRecords = consumer.poll(Duration.ofMillis(1000));

    count = consumerRecords.count();
    assertThat(count).isEqualTo(countNum - seekOffset);
    List<String> actualSeek = new ArrayList<>();
    consumerRecords.forEach(consumerRecord -> actualSeek.add(new String(consumerRecord.value())));
    String[] expect = Arrays.copyOfRange(expects, (int) seekOffset, countNum);
    Assertions.assertArrayEquals(expect, actualSeek.toArray(new String[0]));
  }
}
