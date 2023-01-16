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

package com.netease.arctic.flink.read.source.log.pulsar;

import com.netease.arctic.flink.read.source.log.kafka.LogKafkaPartitionSplit;
import com.netease.arctic.log.LogData;
import org.apache.flink.connector.pulsar.source.reader.message.PulsarMessage;
import org.apache.pulsar.client.api.MessageId;

public class LogRecordPulsarWithRetractInfo<T> extends PulsarMessage<T> {

  /**
   * Denote reader is in retracting read mode.
   * In this mode, data would be read in reverse order and opposite RowKind.
   */
  private final boolean retracting;
  /**
   * @see LogKafkaPartitionSplit#retractStopOffset
   */
  private final Long retractStoppingOffset;
  /**
   * @see LogKafkaPartitionSplit#revertStartOffset
   */
  private final Long revertStartingOffset;
  /**
   * @see LogKafkaPartitionSplit#retractingEpicNo
   */
  private final Long retractingEpicNo;
  /**
   * Data in source, whose {@link LogData#getActualValue()} is the value in log-store.
   */
  private final LogData<T> logData;
  private final T valueToBeSent;

  public LogRecordPulsarWithRetractInfo(MessageId id, long eventTime,
                                        boolean retracting,
                                        Long retractStoppingOffset,
                                        Long revertStartingOffset,
                                        Long retractingEpicNo,
                                        LogData<T> logData,
                                        T valueToBeSent) {
    super(id, null, eventTime);
    this.retracting = retracting;
    this.retractStoppingOffset = retractStoppingOffset;
    this.revertStartingOffset = revertStartingOffset;
    this.retractingEpicNo = retractingEpicNo;
    this.logData = logData;
    this.valueToBeSent = valueToBeSent;
  }

  public static <T> LogRecordPulsarWithRetractInfo<T> ofRetract(MessageId id, long eventTime,
                                                                Long retractStoppingOffset,
                                                                Long revertStartingOffset,
                                                                Long retractingEpicNo,
                                                                LogData<T> logData,
                                                                T valueToBeSent) {
    return new LogRecordPulsarWithRetractInfo<>(id, eventTime, true, retractStoppingOffset,
        revertStartingOffset, retractingEpicNo, logData, valueToBeSent);
  }

  public static <T> LogRecordPulsarWithRetractInfo<T> of(MessageId id, long eventTime,
                                                         LogData<T> logData) {
    return new LogRecordPulsarWithRetractInfo<>(id, eventTime, false, null,
        null, null, logData, logData.getActualValue());
  }

  public boolean isRetracting() {
    return retracting;
  }

  public Long getRetractStoppingOffset() {
    return retractStoppingOffset;
  }

  public Long getRevertStartingOffset() {
    return revertStartingOffset;
  }

  public LogData<T> getLogData() {
    return logData;
  }

  public Long getRetractingEpicNo() {
    return retractingEpicNo;
  }

  public T getValueToBeSent() {
    return valueToBeSent;
  }

  @Override
  public String toString() {
    return "LogMsgWithRetractInfo{" +
        "retracting=" + retracting +
        ", retractStoppingOffset=" + retractStoppingOffset +
        ", revertStartingOffset=" + revertStartingOffset +
        ", retractingEpicNo=" + retractingEpicNo +
        ", logData=" + logData +
        ", actualValue=" + valueToBeSent +
        '}';
  }
}