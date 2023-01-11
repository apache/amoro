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

package com.netease.arctic.flink.read.hidden.pulsar;

import com.netease.arctic.flink.read.source.log.LogSourceHelper;
import com.netease.arctic.flink.read.source.log.kafka.LogRecordWithRetractInfo;
import com.netease.arctic.flink.read.source.log.pulsar.LogPulsarOrderedPartitionSplitReader;
import com.netease.arctic.flink.shuffle.LogRecordV1;
import com.netease.arctic.flink.util.pulsar.PulsarTestEnvironment;
import com.netease.arctic.flink.util.pulsar.runtime.PulsarRuntime;
import com.netease.arctic.flink.util.pulsar.runtime.PulsarRuntimeOperator;
import com.netease.arctic.log.FormatVersion;
import com.netease.arctic.log.LogData;
import com.netease.arctic.log.LogDataJsonDeserialization;
import com.netease.arctic.log.LogDataJsonSerialization;
import com.netease.arctic.utils.IdGenerator;
import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsAddition;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;
import org.apache.flink.connector.pulsar.common.config.PulsarConfigBuilder;
import org.apache.flink.connector.pulsar.source.config.SourceConfiguration;
import org.apache.flink.connector.pulsar.source.enumerator.cursor.StopCursor;
import org.apache.flink.connector.pulsar.source.enumerator.topic.TopicPartition;
import org.apache.flink.connector.pulsar.source.reader.message.PulsarMessage;
import org.apache.flink.connector.pulsar.source.split.PulsarPartitionSplit;
import org.apache.flink.table.data.RowData;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static com.netease.arctic.flink.shuffle.RowKindUtil.transformFromFlinkRowKind;
import static com.netease.arctic.flink.write.hidden.kafka.BaseLogTest.createLogDataDeserialization;
import static com.netease.arctic.flink.write.hidden.kafka.BaseLogTest.userSchema;
import static com.netease.arctic.flink.write.hidden.kafka.HiddenLogOperatorsTest.createRowData;
import static java.util.Collections.singletonList;
import static org.apache.flink.connector.pulsar.common.config.PulsarOptions.PULSAR_ADMIN_URL;
import static org.apache.flink.connector.pulsar.common.config.PulsarOptions.PULSAR_SERVICE_URL;
import static org.apache.flink.connector.pulsar.source.PulsarSourceOptions.PULSAR_SUBSCRIPTION_NAME;
import static org.apache.flink.connector.pulsar.source.config.PulsarSourceConfigUtils.SOURCE_CONFIG_VALIDATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class LogPulsarPartitionSplitReaderTest {

  private static final Logger LOG = LoggerFactory.getLogger(LogPulsarPartitionSplitReaderTest.class);
  @ClassRule
  public static PulsarTestEnvironment environment = new PulsarTestEnvironment(PulsarRuntime.mock());
  public static final String TOPIC1 = "topic1";
  public static final int DEFAULT_SIZE = 21;
  private static final byte[] JOB_ID = IdGenerator.generateUpstreamId();

  @Before
  public void initData() throws Exception {
    // |0 1 2 3 4 5 6 7 8 9 Flip 10 11 12 13 14| 15 16 17 18 19
    write(TOPIC1, 0);
  }

  public PulsarRuntimeOperator op() {
    return environment.operator();
  }

  @Test
  public void testHandleSplitChangesAndFetch() throws IOException {
    LogPulsarOrderedPartitionSplitReader reader = createReader(null, false);

    handleSplit(reader, TOPIC1, 0, MessageId.earliest);
    fetchedMessages(reader, DEFAULT_SIZE, true);
  }

  private byte[] createLogData(int i, int epicNo, boolean flip, LogDataJsonSerialization<RowData> serialization) {
    RowData rowData = createRowData(i);
    LogData<RowData> logData = new LogRecordV1(
        FormatVersion.FORMAT_VERSION_V1,
        JOB_ID,
        epicNo,
        flip,
        transformFromFlinkRowKind(rowData.getRowKind()),
        rowData
    );
    return serialization.serialize(logData);
  }

  private void write(String topic, int offset) {
    Collection<byte[]> data = new ArrayList<>(DEFAULT_SIZE + offset);

    LogDataJsonSerialization<RowData> serialization =
        new LogDataJsonSerialization<>(userSchema, LogRecordV1.fieldGetterFactory);
    for (int j = 0; j < offset; j++) {
      data.add(createLogData(0, 1, false, serialization));
    }

    int i = offset, batch = 5;
    // 0-4 + offset success
    for (; i < offset + batch; i++) {
      data.add(createLogData(i, 1, false, serialization));
    }

    // 5-9 + offset fail
    for (; i < offset + batch * 2; i++) {
      data.add(createLogData(i, 2, false, serialization));
    }

    data.add(createLogData(i, 1, true, serialization));

    // 10-14 + offset success
    for (; i < offset + batch * 3; i++) {
      data.add(createLogData(i, 2, false, serialization));
    }

    for (; i < offset + batch * 4; i++) {
      data.add(createLogData(i, 3, false, serialization));
    }
    op().setupTopic(topic, Schema.BYTES, () -> data.iterator().next(), data.size());
    printDataInTopic(topic);
  }

  public void printDataInTopic(String topic) {
    List<Message<byte[]>> consumerRecords = op().receiveAllMessages(topic, Schema.BYTES, Duration.ofSeconds(10));
    LogDataJsonDeserialization<RowData> deserialization = createLogDataDeserialization();
    consumerRecords.forEach(consumerRecord -> {
      try {
        LOG.info("data in pulsar: {}, msgId: {}", deserialization.deserialize(consumerRecord.getData()),
            consumerRecord.getMessageId());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  protected List<PulsarMessage<RowData>> fetchedMessages(
      LogPulsarOrderedPartitionSplitReader splitReader, int expectedCount, boolean verify) {
    return fetchedMessages(
        splitReader, expectedCount, verify, Boundedness.CONTINUOUS_UNBOUNDED);
  }

  private List<PulsarMessage<RowData>> fetchedMessages(
      LogPulsarOrderedPartitionSplitReader splitReader,
      int expectedCount,
      boolean verify,
      Boundedness boundedness) {
    List<PulsarMessage<RowData>> messages = new ArrayList<>(expectedCount);
    List<String> finishedSplits = new ArrayList<>();
    for (int i = 0; i < 3; ) {
      try {
        RecordsWithSplitIds<PulsarMessage<RowData>> recordsBySplitIds = splitReader.fetch();
        if (recordsBySplitIds.nextSplit() != null) {
          // Collect the records in this split.
          PulsarMessage<RowData> record;
          while ((record = recordsBySplitIds.nextRecordFromSplit()) != null) {
            messages.add(record);
          }
          finishedSplits.addAll(recordsBySplitIds.finishedSplits());
        } else {
          i++;
        }
      } catch (IOException e) {
        i++;
      }
      sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
    if (verify) {
      assertThat(messages).as("We should fetch the expected size").hasSize(expectedCount);
      if (boundedness == Boundedness.CONTINUOUS_UNBOUNDED) {
        assertThat(finishedSplits).as("Split should not be marked as finished").isEmpty();
      } else {
        assertThat(finishedSplits).as("Split should be marked as finished").hasSize(1);
      }
    }

    return messages;
  }

  protected void handleSplit(
      LogPulsarOrderedPartitionSplitReader reader, String topicName, int partitionId) {
    handleSplit(reader, topicName, partitionId, null);
  }

  protected void handleSplit(
      LogPulsarOrderedPartitionSplitReader reader,
      String topicName,
      int partitionId,
      MessageId startPosition) {
    TopicPartition partition = new TopicPartition(topicName, partitionId);
    PulsarPartitionSplit split =
        new PulsarPartitionSplit(partition, StopCursor.never(), startPosition, null);
    SplitsAddition<PulsarPartitionSplit> addition = new SplitsAddition<>(singletonList(split));
    reader.handleSplitsChanges(addition);
  }

  private LogPulsarOrderedPartitionSplitReader createReader(Configuration conf, boolean logRetractionEnable) {
    PulsarClient pulsarClient = op().client();
    PulsarAdmin pulsarAdmin = op().admin();
    PulsarConfigBuilder configBuilder = new PulsarConfigBuilder();

    if (conf != null) {
      configBuilder.set(conf);
    }
    configBuilder.set(PULSAR_SERVICE_URL, op().serviceUrl());
    configBuilder.set(PULSAR_ADMIN_URL, op().adminUrl());
    configBuilder.set(PULSAR_SUBSCRIPTION_NAME, "test-split-reader");
    
    SourceConfiguration sourceConfiguration =
        configBuilder.build(SOURCE_CONFIG_VALIDATOR, SourceConfiguration::new);

    String logConsumerChangelogMode = "all-kinds";
    
    LogSourceHelper logReadHelper = logRetractionEnable ? new LogSourceHelper() : null;
    return new LogPulsarOrderedPartitionSplitReader(
        pulsarClient,
        pulsarAdmin,
        sourceConfiguration,
        null,
        userSchema,
        false,
        logReadHelper,
        logConsumerChangelogMode);
  }

  private boolean verifyConsumed(
      final KafkaPartitionSplit split,
      final Collection<LogRecordWithRetractInfo<RowData>> consumed,
      final int valueOffsetDiffInOrderedRead) {
    long currentOffset = -1;

    for (LogRecordWithRetractInfo<RowData> record : consumed) {
      if (record.isRetracting()) {
        assertEquals(record.offset(), record.getActualValue().getInt(1));
      } else {
        assertEquals(record.offset(),
            record.getActualValue().getInt(1) + valueOffsetDiffInOrderedRead);
      }

      currentOffset = Math.max(currentOffset, record.offset());
    }
    if (split.getStoppingOffset().isPresent()) {
      return currentOffset == split.getStoppingOffset().get() - 1;
    } else {
      return false;
    }
  }

}
