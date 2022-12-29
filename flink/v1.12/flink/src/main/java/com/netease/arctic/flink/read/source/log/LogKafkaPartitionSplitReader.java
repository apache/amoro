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

package com.netease.arctic.flink.read.source.log;

import com.netease.arctic.flink.read.internals.KafkaPartitionSplitReader;
import com.netease.arctic.flink.shuffle.LogRecordV1;
import com.netease.arctic.log.LogData;
import com.netease.arctic.log.LogDataJsonDeserialization;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializer;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.iceberg.Schema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static com.netease.arctic.flink.read.source.log.LogSourceHelper.checkMagicNum;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.LOG_CONSUMER_CHANGELOG_MODE_APPEND_ONLY;

public class LogKafkaPartitionSplitReader extends KafkaPartitionSplitReader<RowData> {

  private static final Logger LOG = LoggerFactory.getLogger(LogKafkaPartitionSplitReader.class);

  private final LogDataJsonDeserialization<RowData> logDataJsonDeserialization;
  private final LogSourceHelper logReadHelper;
  private final boolean logRetractionEnable;
  private final boolean logConsumerAppendOnly;

  public LogKafkaPartitionSplitReader(Properties props, KafkaRecordDeserializer<RowData> deserializationSchema,
                                      int subtaskId,
                                      Schema schema,
                                      boolean logRetractionEnable,
                                      LogSourceHelper logReadHelper,
                                      String logConsumerChangelogMode) {
    super(props, deserializationSchema, subtaskId);

    this.logDataJsonDeserialization = new LogDataJsonDeserialization<>(
        schema,
        LogRecordV1.factory,
        LogRecordV1.arrayFactory,
        LogRecordV1.mapFactory
    );
    this.logRetractionEnable = logRetractionEnable;
    this.logReadHelper = logReadHelper;
    this.logConsumerAppendOnly = LOG_CONSUMER_CHANGELOG_MODE_APPEND_ONLY.equalsIgnoreCase(logConsumerChangelogMode);
  }

  public static int RETRACT_SIZE = 500;
  public static long RETRACT_FETCH_MAX_ROUND = 5;

  @Override
  public RecordsWithSplitIds<LogRecordWithRetractInfo<RowData>> fetch() throws IOException {
    KafkaPartitionSplitRecords<LogRecordWithRetractInfo<RowData>> recordsBySplits =
        new KafkaPartitionSplitRecords<>();

    Set<TopicPartition> retractTps;
    if (logRetractionEnable && !(retractTps = logReadHelper.getRetractTopicPartitions()).isEmpty()) {
      readReversely(retractTps, recordsBySplits);
    } else {
      readNormal(recordsBySplits);
    }

    recordsBySplits.prepareForRead();
    return recordsBySplits;
  }

  private void readNormal(KafkaPartitionSplitRecords<LogRecordWithRetractInfo<RowData>> recordsBySplits)
      throws IOException {
    ConsumerRecords<byte[], byte[]> consumerRecords;
    try {
      consumerRecords = consumer.poll(Duration.ofMillis(POLL_TIMEOUT));
    } catch (WakeupException we) {
      return;
    }

    List<TopicPartition> finishedPartitions = new ArrayList<>();

    for (TopicPartition tp : consumerRecords.partitions()) {
      long stoppingOffset = getStoppingOffset(tp);
      String splitId = tp.toString();
      Collection<LogRecordWithRetractInfo<RowData>> recordsForSplit =
          recordsBySplits.recordsForSplit(splitId);
      for (ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords.records(tp)) {
        // Stop consuming from this partition if the offsets has reached the stopping
        // offset.
        // Note that there are two cases, either case finishes a split:
        // 1. After processing a record with offset of "stoppingOffset - 1". The split
        // reader
        //    should not continue fetching because the record with stoppingOffset may not
        // exist.
        // 2. Before processing a record whose offset is greater than or equals to the
        // stopping
        //    offset. This should only happens when case 1 was not met due to log compaction
        // or
        //    log retention.
        // Case 2 is handled here. Case 1 is handled after the record is processed.
        if (consumerRecord.offset() >= stoppingOffset) {
          finishSplitAtRecord(
              tp,
              stoppingOffset,
              consumerRecord.offset(),
              finishedPartitions,
              recordsBySplits);
          break;
        }

        byte[] value = consumerRecord.value();
        boolean magicFormat = checkMagicNum(value);
        if (!magicFormat) {
          throw new UnsupportedOperationException(
              "Can't deserialize arctic log queue message due to it does not contain magic number.");
        }

        LogData<RowData> logData = logDataJsonDeserialization.deserialize(value);
        if (!logData.getFlip() && filterByRowKind(logData.getActualValue())) {
          LOG.info(
              "filter the rowData, because of logConsumerAppendOnly is true, and rowData={}.",
              logData.getActualValue());
          continue;
        }

        // Finish the split because there might not be any message after this point.
        // Keep polling
        // will just block forever.
        if (consumerRecord.offset() == stoppingOffset - 1) {
          finishSplitAtRecord(
              tp,
              stoppingOffset,
              consumerRecord.offset(),
              finishedPartitions,
              recordsBySplits);
        }

        final long currentOffset = consumerRecord.offset();

        if (logData.getFlip()) {
          if (logRetractionEnable) {
            logReadHelper.startRetracting(tp, logData.getUpstreamId(), logData.getEpicNo(),
                currentOffset + 1);
            break;
          } else {
            continue;
          }
        }

        if (logRetractionEnable) {
          logReadHelper.initialEpicStartOffsetIfEmpty(tp, logData.getUpstreamId(), logData.getEpicNo(), currentOffset);
        }
        recordsForSplit.add(LogRecordWithRetractInfo.of(consumerRecord, logData));
      }
    }
    // Unassign the partitions that has finished.
    if (!finishedPartitions.isEmpty()) {
      unassignPartitions(finishedPartitions);
    }
  }

  /**
   * read reversely in retracting mode
   */
  private void readReversely(Set<TopicPartition> retractTps,
                             KafkaPartitionSplitRecords<LogRecordWithRetractInfo<RowData>> recordsBySplits)
      throws IOException {
    Set<TopicPartition> origin = consumer.assignment();
    consumer.assign(retractTps);

    // stop in current offsets, the msg in the offset would be read
    Map<TopicPartition, Long> stoppingOffsetsFromConsumer = new HashMap<>();
    for (TopicPartition tp : retractTps) {
      // the next poll offset
      long offset = consumer.position(tp);
      stoppingOffsetsFromConsumer.put(tp, offset - 1);
      long startFrom = offset - RETRACT_SIZE;
      if (startFrom < 0) {
        startFrom = 0;
      }
      consumer.seek(tp, startFrom);
    }
    Map<TopicPartition, List<ConsumerRecord<byte[], byte[]>>> records = pollToDesignatedPositions(stoppingOffsetsFromConsumer);

    Set<TopicPartition> finishRetract = new HashSet<>();
    for (Map.Entry<TopicPartition, List<ConsumerRecord<byte[], byte[]>>> entry : records.entrySet()) {
      TopicPartition tp = entry.getKey();
      List<ConsumerRecord<byte[], byte[]>> consumerRecords = entry.getValue();

      long stoppingOffsetFromConsumer = stoppingOffsetsFromConsumer.get(tp);
      LogSourceHelper.EpicRetractingInfo retractingInfo = logReadHelper.getRetractInfo(tp);
      // stoppingOffsetFromConsumer is the offset queried from consumer, it may be larger than flip offset because 
      // kafka poll batch records every time.
      // revertStartingOffset is the offset after flip, so it should minus 2 to get the offset before flip.
      long stoppingOffset = Math.min(stoppingOffsetFromConsumer, retractingInfo.getRevertStartingOffset() - 2);
      int startIndex = findIndexOfOffset(consumerRecords, stoppingOffset);

      String splitId = tp.toString();
      Collection<LogRecordWithRetractInfo<RowData>> recordsForSplit = recordsBySplits.recordsForSplit(splitId);

      for (int i = startIndex; i >= 0; i--) {
        ConsumerRecord<byte[], byte[]> r = consumerRecords.get(i);

        if (r.offset() < retractingInfo.getRetractStoppingOffset()) {
          finishRetract.add(tp);
          break;
        }
        LogData<RowData> logData = logDataJsonDeserialization.deserialize(r.value());

        if (!Objects.equals(logData.getUpstreamId(), retractingInfo.getUpstreamId()) ||
            logData.getEpicNo() <= retractingInfo.getEpicNo()) {
          LOG.debug("won't retract other job or the success ckp epic data, upstreamId: {}, epicNo: {}",
              logData.getUpstreamId(), logData.getEpicNo());
        } else {
          RowData actualValue = logReadHelper.turnRowKind(logData.getActualValue());
          recordsForSplit.add(LogRecordWithRetractInfo.ofRetract(
              r, retractingInfo.getRetractStoppingOffset(), retractingInfo.getRevertStartingOffset(),
              retractingInfo.getEpicNo(), logData, actualValue
          ));
        }

        if (r.offset() == retractingInfo.getRetractStoppingOffset()) {
          finishRetract.add(tp);
          break;
        }
      }
    }

    suspendRetracting(finishRetract);
    consumer.assign(origin);
  }

  private void suspendRetracting(Set<TopicPartition> finishRetract) {
    revertConsumer(finishRetract);
    logReadHelper.suspendRetracting(finishRetract);
  }

  /**
   * revert consumer to original offset after flip
   */
  public void revertConsumer(Set<TopicPartition> finishRetract) {
    for (TopicPartition tp : finishRetract) {
      LogSourceHelper.EpicRetractingInfo retractingInfo = logReadHelper.getRetractInfo(tp);
      long revert = retractingInfo.getRevertStartingOffset();
      consumer.seek(tp, revert);
    }
  }

  /**
   * @param records should be in order of kafka.
   * @param offset  Kafka offset
   * @return the index in records
   */
  private int findIndexOfOffset(List<ConsumerRecord<byte[], byte[]>> records, long offset) {
    int last = records.size() - 1;
    int idx = Math.min(RETRACT_SIZE, last);

    long diff = -1;
    while (idx >= 0 && idx <= last && (diff = records.get(idx).offset() - offset) != 0) {
      if (diff > 0) {
        idx--;
      } else {
        idx++;
      }
    }
    if (diff == 0) {
      LOG.debug("start index is: {}", idx);
      return idx;
    }
    LOG.info("topic: {}, partition: {}, records' offset range: [{}, {}], need to find: {}",
        records.get(0).topic(), records.get(0).partition(),
        records.get(0).offset(), records.get(last).offset(), offset);
    throw new IllegalStateException("can not find offset in records");
  }

  /**
   * @param stoppingOffsets the stopping offset is the position which should be read.
   * @return value in map may contain some useless records. It should be filtered.
   */
  private Map<TopicPartition, List<ConsumerRecord<byte[], byte[]>>> pollToDesignatedPositions(
      Map<TopicPartition, Long> stoppingOffsets) {
    ConsumerRecords<byte[], byte[]> consumerRecords;
    try {
      consumerRecords = consumer.poll(Duration.ofMillis(POLL_TIMEOUT));
    } catch (WakeupException we) {
      LOG.error("consume reversely error");
      return Collections.EMPTY_MAP;
    }

    Map<TopicPartition, List<ConsumerRecord<byte[], byte[]>>> recordsForTps = new HashMap<>();

    int unfinished = stoppingOffsets.size();
    int round = 0;

    Set<TopicPartition> unfinishedTps = new HashSet<>();
    while (unfinished > 0 && round++ < RETRACT_FETCH_MAX_ROUND) {
      unfinishedTps.clear();

      for (TopicPartition tp : consumerRecords.partitions()) {
        recordsForTps.putIfAbsent(tp, new ArrayList<>(RETRACT_SIZE));
        List<ConsumerRecord<byte[], byte[]>> records = recordsForTps.get(tp);

        records.addAll(consumerRecords.records(tp));

        long stoppingOffset = stoppingOffsets.get(tp);
        if (records.get(records.size() - 1).offset() >= stoppingOffset) {
          unfinished--;
          LOG.info("reach the stopping offset. stopping offset: {}, tp: {}. data size:{}", stoppingOffset, tp,
              records.size());
        } else {
          unfinishedTps.add(tp);
        }
      }
      if (unfinished == 0) {
        break;
      }
      consumer.assign(unfinishedTps);
    }

    if (unfinished > 0) {
      LOG.error("can not poll msg to designated positions. unfinished: {}", unfinishedTps);
      for (TopicPartition tp : unfinishedTps) {
        List<ConsumerRecord<byte[], byte[]>> records = recordsForTps.get(tp);
        LOG.info("tp: {}, polled offset:{}, stopping offset: {}", tp, records.get(records.size() - 1).offset(),
            stoppingOffsets.get(tp));
      }
      throw new UnsupportedOperationException("poll msg reversely error");
    }

    return recordsForTps;
  }

  /**
   * filter the rowData only works during
   * {@link com.netease.arctic.flink.table.descriptors.ArcticValidator#ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE}
   * is false and
   * {@link com.netease.arctic.flink.table.descriptors.ArcticValidator#ARCTIC_LOG_CONSUMER_CHANGELOG_MODE}
   * is {@link com.netease.arctic.flink.table.descriptors.ArcticValidator#LOG_CONSUMER_CHANGELOG_MODE_APPEND_ONLY} and
   * rowData.rowKind != INSERT
   *
   * @param rowData the judged data
   * @return true means should be filtered.
   */
  boolean filterByRowKind(RowData rowData) {
    return !logRetractionEnable && logConsumerAppendOnly && !rowData.getRowKind().equals(RowKind.INSERT);
  }

}