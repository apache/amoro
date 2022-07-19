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

package com.netease.arctic.flink.write.hidden.kafka;

import com.netease.arctic.flink.kafka.testutils.KafkaTestBase;
import com.netease.arctic.flink.shuffle.LogRecordV1;
import com.netease.arctic.flink.write.hidden.LogMsgFactory;
import com.netease.arctic.log.LogData;
import com.netease.arctic.log.LogDataJsonSerialization;
import org.apache.flink.streaming.connectors.kafka.internals.FlinkKafkaInternalProducer;
import org.apache.flink.table.data.RowData;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.netease.arctic.flink.kafka.testutils.KafkaConfigGenerate.getProperties;
import static com.netease.arctic.flink.kafka.testutils.KafkaConfigGenerate.getPropertiesWithByteArray;
import static com.netease.arctic.table.TableProperties.LOG_STORE_MESSAGE_TOPIC;
import static org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

public class HiddenKafkaProducerTest extends BaseLogTest {
  private static final Logger LOG = LoggerFactory.getLogger(HiddenKafkaProducerTest.class);
  private static final KafkaTestBase kafkaTestBase = new KafkaTestBase();

  @BeforeClass
  public static void prepare() throws Exception {
    kafkaTestBase.prepare();
  }

  @AfterClass
  public static void shutdown() throws Exception {
    kafkaTestBase.shutDownServices();
  }

  @Ignore
  @Test
  public void testInitTransactionId() {
    final String topic = "test-init-transactions";
    kafkaTestBase.createTestTopic(topic, 1, 1);
    FlinkKafkaInternalProducer<String, String> reuse = null;
    final String transactionalIdPrefix = UUID.randomUUID().toString();
    try {
      int numTransactions = 20;
      for (int i = 1; i <= numTransactions; i++) {
        Properties properties = getProperties(kafkaTestBase.getProperties());
        properties.put(TRANSACTIONAL_ID_CONFIG, transactionalIdPrefix + i);
        reuse = new FlinkKafkaInternalProducer<>(properties);
        reuse.initTransactions();
        reuse.beginTransaction();
        reuse.send(new ProducerRecord<>(topic, "test-value-" + i));
        if (i % 2 == 0) {
          reuse.commitTransaction();
        } else {
          reuse.flush();
          reuse.abortTransaction();
        }

        int count = kafkaTestBase.countAllRecords(topic);
        LOG.info("consumption = {}", count);
        assertThat(count).isEqualTo(i / 2);
      }
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
  public void testLogProducerSendFlip() throws Exception {
    final String topic = "test-recover-transactions";
    int numPartitions = 3;
    kafkaTestBase.createTopics(numPartitions, topic);
    LogData.FieldGetterFactory<RowData> fieldGetterFactory = LogRecordV1.fieldGetterFactory;
    LogDataJsonSerialization<RowData> logDataJsonSerialization = new LogDataJsonSerialization<>(
        checkNotNull(userSchema),
        checkNotNull(fieldGetterFactory));

    Properties properties = getPropertiesWithByteArray(kafkaTestBase.getProperties());
    properties.put(LOG_STORE_MESSAGE_TOPIC, topic);
    LogMsgFactory.Producer<RowData> producer =
        new HiddenKafkaFactory<RowData>().createProducer(
            properties,
            logDataJsonSerialization,
            null);
    producer.open();

    int recoverNum = 3;
    for (int i = 0; i < recoverNum; i++) {
      producer.sendToAllPartitions(FLIP_LOG);
    }
    producer.close();

    int count = kafkaTestBase.countAllRecords(topic);
    assertThat(count).isEqualTo(numPartitions * recoverNum);
  }
}