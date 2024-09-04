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

import static org.apache.amoro.flink.kafka.testutils.KafkaContainerTest.KAFKA_CONTAINER;
import static org.apache.amoro.flink.kafka.testutils.KafkaContainerTest.readRecordsBytes;
import static org.apache.amoro.flink.shuffle.RowKindUtil.transformFromFlinkRowKind;
import static org.junit.Assert.assertEquals;

import org.apache.amoro.flink.kafka.testutils.KafkaConfigGenerate;
import org.apache.amoro.flink.kafka.testutils.KafkaContainerTest;
import org.apache.amoro.flink.read.source.log.LogSourceHelper;
import org.apache.amoro.flink.read.source.log.kafka.LogKafkaPartitionSplitReader;
import org.apache.amoro.flink.read.source.log.kafka.LogRecordWithRetractInfo;
import org.apache.amoro.flink.shuffle.LogRecordV1;
import org.apache.amoro.flink.write.hidden.kafka.TestBaseLog;
import org.apache.amoro.flink.write.hidden.kafka.TestHiddenLogOperators;
import org.apache.amoro.log.FormatVersion;
import org.apache.amoro.log.LogData;
import org.apache.amoro.log.LogDataJsonDeserialization;
import org.apache.amoro.log.LogDataJsonSerialization;
import org.apache.amoro.utils.IdGenerator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsAddition;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsChange;
import org.apache.flink.connector.kafka.source.metrics.KafkaSourceReaderMetrics;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;
import org.apache.flink.connector.testutils.source.reader.TestingReaderContext;
import org.apache.flink.metrics.groups.SourceReaderMetricGroup;
import org.apache.flink.metrics.groups.UnregisteredMetricsGroup;
import org.apache.flink.table.data.RowData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TestLogKafkaPartitionSplitReader {

  private static final Logger LOG = LoggerFactory.getLogger(TestLogKafkaPartitionSplitReader.class);

  public static final int TOPIC1_STOP_OFFSET = 16;
  public static final int TOPIC2_STOP_OFFSET = 21;
  public static final String TOPIC1 = "topic1";
  public static final String TOPIC2 = "topic2";
  private static Map<Integer, Map<String, KafkaPartitionSplit>> splitsByOwners;
  private static final byte[] JOB_ID = IdGenerator.generateUpstreamId();

  @BeforeClass
  public static void prepare() throws Exception {
    KAFKA_CONTAINER.start();

    Map<TopicPartition, Long> earliestOffsets = new HashMap<>();
    earliestOffsets.put(new TopicPartition(TOPIC1, 0), 0L);
    earliestOffsets.put(new TopicPartition(TOPIC2, 0), 5L);
    splitsByOwners = getSplitsByOwners(earliestOffsets);
  }

  @AfterClass
  public static void shutdown() throws Exception {
    KAFKA_CONTAINER.close();
  }

  @Before
  public void initData() throws Exception {
    // |0 1 2 3 4 5 6 7 8 9 Flip 10 11 12 13 14| 15 16 17 18 19
    write(TOPIC1, 0);
    // 0 0 0 0 0 |5 6 7 8 9 10 11 12 13 14 Flip 15 16 17 18 19| 20 21 22 23 24
    write(TOPIC2, 5);
  }

  @Test
  public void testHandleSplitChangesAndFetch() throws IOException {
    LogKafkaPartitionSplitReader reader = createReader(new Properties());
    assignSplitsAndFetchUntilFinish(reader, 0, 20);
    assignSplitsAndFetchUntilFinish(reader, 1, 20);
  }

  private ProducerRecord<byte[], byte[]> createLogData(
      String topic,
      int i,
      int epicNo,
      boolean flip,
      LogDataJsonSerialization<RowData> serialization) {
    RowData rowData = TestHiddenLogOperators.createRowData(i);
    LogData<RowData> logData =
        new LogRecordV1(
            FormatVersion.FORMAT_VERSION_V1,
            JOB_ID,
            epicNo,
            flip,
            transformFromFlinkRowKind(rowData.getRowKind()),
            rowData);
    byte[] message = serialization.serialize(logData);
    int partition = 0;
    ProducerRecord<byte[], byte[]> producerRecord =
        new ProducerRecord<>(topic, partition, null, null, message);
    return producerRecord;
  }

  private void write(String topic, int offset) throws Exception {
    KafkaProducer producer = KafkaContainerTest.getProducer();
    LogDataJsonSerialization<RowData> serialization =
        new LogDataJsonSerialization<>(TestBaseLog.USER_SCHEMA, LogRecordV1.FIELD_GETTER_FACTORY);
    for (int j = 0; j < offset; j++) {
      producer.send(createLogData(topic, 0, 1, false, serialization));
    }

    int i = offset;
    // 0-4 + offset success
    for (; i < offset + 5; i++) {
      producer.send(createLogData(topic, i, 1, false, serialization));
    }

    // 5-9 + offset fail
    for (; i < offset + 10; i++) {
      producer.send(createLogData(topic, i, 2, false, serialization));
    }

    producer.send(createLogData(topic, i, 1, true, serialization));

    // 10-14 + offset success
    for (; i < offset + 15; i++) {
      producer.send(createLogData(topic, i, 2, false, serialization));
    }

    for (; i < offset + 20; i++) {
      producer.send(createLogData(topic, i, 3, false, serialization));
    }
    printDataInTopic(topic);
  }

  public static void printDataInTopic(String topic) {
    ConsumerRecords<byte[], byte[]> consumerRecords = readRecordsBytes(topic);
    LogDataJsonDeserialization<RowData> deserialization =
        TestBaseLog.createLogDataDeserialization();
    consumerRecords.forEach(
        consumerRecord -> {
          try {
            LOG.info("data in kafka: {}", deserialization.deserialize(consumerRecord.value()));
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  private void assignSplitsAndFetchUntilFinish(
      LogKafkaPartitionSplitReader reader, int readerId, int expectedRecordCount)
      throws IOException {
    Map<String, KafkaPartitionSplit> splits = assignSplits(reader, splitsByOwners.get(readerId));

    Map<String, Integer> numConsumedRecords = new HashMap<>();
    Set<String> finishedSplits = new HashSet<>();
    int flipCount = 0;
    while (finishedSplits.size() < splits.size()) {
      RecordsWithSplitIds<ConsumerRecord<byte[], byte[]>> recordsBySplitIds = reader.fetch();
      String splitId = recordsBySplitIds.nextSplit();
      while (splitId != null) {
        // Collect the records in this split.
        List<LogRecordWithRetractInfo<RowData>> splitFetch = new ArrayList<>();
        ConsumerRecord<byte[], byte[]> record;
        boolean hasFlip = false;
        while ((record = recordsBySplitIds.nextRecordFromSplit()) != null) {
          LOG.info(
              "read: {}, offset: {}",
              ((LogRecordWithRetractInfo) record).getLogData().getActualValue(),
              record.offset());
          if (((LogRecordWithRetractInfo<?>) record).isRetracting()) {
            hasFlip = true;
          }
          splitFetch.add((LogRecordWithRetractInfo<RowData>) record);
        }
        if (hasFlip) {
          flipCount++;
        }
        // verify the consumed records.
        if (verifyConsumed(splits.get(splitId), splitFetch, flipCount)) {
          finishedSplits.add(splitId);
        }
        numConsumedRecords.compute(
            splitId,
            (ignored, recordCount) ->
                recordCount == null ? splitFetch.size() : recordCount + splitFetch.size());
        splitId = recordsBySplitIds.nextSplit();
      }
    }

    // Verify the number of records consumed from each split.
    numConsumedRecords.forEach(
        (splitId, recordCount) -> {
          assertEquals(
              String.format("%s should have %d records.", splits.get(splitId), expectedRecordCount),
              expectedRecordCount,
              (long) recordCount);
        });
  }

  public static Map<Integer, Map<String, KafkaPartitionSplit>> getSplitsByOwners(
      Map<TopicPartition, Long> earliestOffsets) {
    final Map<Integer, Map<String, KafkaPartitionSplit>> splitsByOwners = new HashMap<>();
    splitsByOwners.put(
        0,
        new HashMap<String, KafkaPartitionSplit>() {
          {
            TopicPartition tp = new TopicPartition(TOPIC1, 0);
            put(
                KafkaPartitionSplit.toSplitId(tp),
                new KafkaPartitionSplit(tp, earliestOffsets.get(tp), TOPIC1_STOP_OFFSET));
          }
        });
    splitsByOwners.put(
        1,
        new HashMap<String, KafkaPartitionSplit>() {
          {
            TopicPartition tp = new TopicPartition(TOPIC2, 0);
            put(
                KafkaPartitionSplit.toSplitId(tp),
                new KafkaPartitionSplit(tp, earliestOffsets.get(tp), TOPIC2_STOP_OFFSET));
          }
        });
    return splitsByOwners;
  }

  private Map<String, KafkaPartitionSplit> assignSplits(
      LogKafkaPartitionSplitReader reader, Map<String, KafkaPartitionSplit> splits) {
    SplitsChange<KafkaPartitionSplit> splitsChange =
        new SplitsAddition<>(new ArrayList<>(splits.values()));
    reader.handleSplitsChanges(splitsChange);
    return splits;
  }

  private LogKafkaPartitionSplitReader createReader(Properties additionalProperties) {
    Properties props = KafkaConfigGenerate.getPropertiesWithByteArray();
    props.put("group.id", "test");
    props.put("auto.offset.reset", "earliest");
    if (!additionalProperties.isEmpty()) {
      props.putAll(additionalProperties);
    }
    SourceReaderMetricGroup sourceReaderMetricGroup =
        UnregisteredMetricsGroup.createSourceReaderMetricGroup();
    return new LogKafkaPartitionSplitReader(
        props,
        new TestingReaderContext(new Configuration(), sourceReaderMetricGroup),
        new KafkaSourceReaderMetrics(sourceReaderMetricGroup),
        TestBaseLog.USER_SCHEMA,
        true,
        new LogSourceHelper(),
        "all-kinds");
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
        assertEquals(
            record.offset(), record.getActualValue().getInt(1) + valueOffsetDiffInOrderedRead);
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
