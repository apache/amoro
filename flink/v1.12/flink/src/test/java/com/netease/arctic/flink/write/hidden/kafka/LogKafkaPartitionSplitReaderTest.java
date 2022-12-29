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

import com.netease.arctic.flink.kafka.testutils.KafkaConfigGenerate;
import com.netease.arctic.flink.read.source.log.LogSourceHelper;
import com.netease.arctic.flink.read.source.log.LogKafkaPartitionSplitReader;
import com.netease.arctic.flink.read.source.log.LogRecordWithRetractInfo;
import com.netease.arctic.flink.util.OneInputStreamOperatorInternTest;
import com.netease.arctic.utils.IdGenerator;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsAddition;
import org.apache.flink.connector.base.source.reader.splitreader.SplitsChange;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;
import org.apache.flink.runtime.checkpoint.OperatorSubtaskState;
import org.apache.flink.table.data.RowData;
import org.apache.kafka.common.TopicPartition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.netease.arctic.flink.kafka.testutils.KafkaContainerTest.KAFKA_CONTAINER;
import static com.netease.arctic.flink.write.hidden.kafka.BaseLogTest.userSchema;
import static com.netease.arctic.flink.write.hidden.kafka.HiddenLogOperatorsTest.createProducer;
import static com.netease.arctic.flink.write.hidden.kafka.HiddenLogOperatorsTest.createRowData;
import static org.junit.Assert.assertEquals;

public class LogKafkaPartitionSplitReaderTest {

  public static final int TOPIC1_STOP_OFFSET = 17;
  public static final int TOPIC2_STOP_OFFSET = 22;
  public static final String TOPIC1 = "topic1";
  public static final String TOPIC2 = "topic2";
  private static Map<Integer, Map<String, KafkaPartitionSplit>> splitsByOwners;

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

  private void write(String topic, int offset) throws Exception {
    OperatorSubtaskState state0;
    byte[] jobId = IdGenerator.generateUpstreamId();
    int i = offset;
    OneInputStreamOperatorInternTest<RowData, RowData> harness0 =
        createProducer(1, 0, jobId, topic);
    harness0.setup();
    harness0.initializeEmptyState();
    harness0.open();

    for (int j = 0; j < offset; j++) {
      harness0.processElement(createRowData(0), 0);
    }

    // 0-4 + offset success
    for (; i < offset + 5; i++) {
      harness0.processElement(createRowData(i), 0);
    }

    // chp-1 success.
    state0 = harness0.snapshot(1, 0);
    // 5-9 + offset fail
    for (; i < offset + 10; i++) {
      harness0.processElement(createRowData(i), 1);
    }

    OneInputStreamOperatorInternTest<RowData, RowData> harness1 =
        createProducer(1, 0, jobId, 1L, topic);
    harness1.setup();
    harness1.initializeState(state0);
    harness1.open();

    // 10-14 + offset success
    for (; i < offset + 15; i++) {
      harness1.processElement(createRowData(i), 2);
    }
    harness1.snapshot(2, 2);

    for (; i < offset + 20; i++) {
      harness1.processElement(createRowData(i), 3);
    }
  }

  private void assignSplitsAndFetchUntilFinish(
      LogKafkaPartitionSplitReader reader, int readerId, int expectedRecordCount) throws IOException {
    Map<String, KafkaPartitionSplit> splits =
        assignSplits(reader, splitsByOwners.get(readerId));

    Map<String, Integer> numConsumedRecords = new HashMap<>();
    Set<String> finishedSplits = new HashSet<>();
    int flipCount = 0;
    while (finishedSplits.size() < splits.size()) {
      RecordsWithSplitIds<LogRecordWithRetractInfo<RowData>> recordsBySplitIds = reader.fetch();
      String splitId = recordsBySplitIds.nextSplit();
      while (splitId != null) {
        // Collect the records in this split.
        List<LogRecordWithRetractInfo<RowData>> splitFetch = new ArrayList<>();
        LogRecordWithRetractInfo<RowData> record;
        while ((record = recordsBySplitIds.nextRecordFromSplit()) != null) {
          splitFetch.add(record);
        }

        // verify the consumed records.
        if (verifyConsumed(splits.get(splitId), splitFetch, flipCount)) {
          finishedSplits.add(splitId);
        }
        numConsumedRecords.compute(
            splitId,
            (ignored, recordCount) ->
                recordCount == null
                    ? splitFetch.size()
                    : recordCount + splitFetch.size());
        splitId = recordsBySplitIds.nextSplit();
      }
      flipCount++;
    }

    // Verify the number of records consumed from each split.
    numConsumedRecords.forEach(
        (splitId, recordCount) -> {
          assertEquals(
              String.format(
                  "%s should have %d records.",
                  splits.get(splitId), expectedRecordCount),
              expectedRecordCount,
              (long) recordCount);
        });
  }

  public static Map<Integer, Map<String, KafkaPartitionSplit>> getSplitsByOwners(
      Map<TopicPartition, Long> earliestOffsets) {
    final Map<Integer, Map<String, KafkaPartitionSplit>> splitsByOwners = new HashMap<>();
    splitsByOwners.put(0, new HashMap<String, KafkaPartitionSplit>() {{
      TopicPartition tp = new TopicPartition(TOPIC1, 0);
      put(KafkaPartitionSplit.toSplitId(tp), new KafkaPartitionSplit(tp, earliestOffsets.get(tp), TOPIC1_STOP_OFFSET));
    }});
    splitsByOwners.put(1, new HashMap<String, KafkaPartitionSplit>() {{
      TopicPartition tp = new TopicPartition(TOPIC2, 0);
      put(KafkaPartitionSplit.toSplitId(tp), new KafkaPartitionSplit(tp, earliestOffsets.get(tp), TOPIC2_STOP_OFFSET));
    }});
    return splitsByOwners;
  }

  private Map<String, KafkaPartitionSplit> assignSplits(
      LogKafkaPartitionSplitReader reader, Map<String, KafkaPartitionSplit> splits) {
    SplitsChange<KafkaPartitionSplit> splitsChange =
        new SplitsAddition<>(new ArrayList<>(splits.values()));
    reader.handleSplitsChanges(splitsChange);
    return splits;
  }

  private LogKafkaPartitionSplitReader createReader(
      Properties additionalProperties) {
    Properties props = KafkaConfigGenerate.getPropertiesWithByteArray();
    props.put("group.id", "test");
    props.put("auto.offset.reset", "earliest");
    if (!additionalProperties.isEmpty()) {
      props.putAll(additionalProperties);
    }
    return new LogKafkaPartitionSplitReader(
        props,
        null,
        0,
        userSchema,
        true,
        new LogSourceHelper(),
        "all-kinds"
    );
  }

  private boolean verifyConsumed(
      final KafkaPartitionSplit split,
      final Collection<LogRecordWithRetractInfo<RowData>> consumed,
      final int valueOffsetDiffInOrderedRead) {
    long currentOffset = -1;

    for (LogRecordWithRetractInfo<RowData> record : consumed) {
      if (record.isRetracting()) {
        assertEquals(record.getConsumerRecord().offset(), record.getActualValue().getInt(1));
      } else {
        assertEquals(record.getConsumerRecord().offset(),
            record.getActualValue().getInt(1) + valueOffsetDiffInOrderedRead);
      }

      currentOffset = Math.max(currentOffset, record.getConsumerRecord().offset());
    }
    if (split.getStoppingOffset().isPresent()) {
      return currentOffset == split.getStoppingOffset().get() - 1;
    } else {
      return false;
    }
  }

}
