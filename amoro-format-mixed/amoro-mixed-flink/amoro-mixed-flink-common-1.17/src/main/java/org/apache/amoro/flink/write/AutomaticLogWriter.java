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

package org.apache.amoro.flink.write;

import org.apache.amoro.flink.shuffle.ShuffleHelper;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.write.hidden.HiddenLogWriter;
import org.apache.amoro.flink.write.hidden.LogMsgFactory;
import org.apache.amoro.log.LogData;
import org.apache.flink.runtime.state.StateInitializationContext;
import org.apache.flink.runtime.state.StateSnapshotContext;
import org.apache.flink.streaming.api.graph.StreamConfig;
import org.apache.flink.streaming.api.operators.Output;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.runtime.tasks.StreamTask;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;

import java.time.Duration;
import java.util.Properties;

/**
 * This is an automatic logstore writer util class. It will write logstore when the system current
 * timestamp is greater than the watermark of all subtasks plus the {@link
 * MixedFormatValidator#AUTO_EMIT_LOGSTORE_WATERMARK_GAP} value.
 */
public class AutomaticLogWriter extends MixedFormatLogWriter {
  private final AutomaticDoubleWriteStatus status;
  private final MixedFormatLogWriter mixedFormatLogWriter;

  public AutomaticLogWriter(
      Schema schema,
      Properties producerConfig,
      String topic,
      LogMsgFactory<RowData> factory,
      LogData.FieldGetterFactory<RowData> fieldGetterFactory,
      byte[] jobId,
      ShuffleHelper helper,
      MixedFormatTableLoader tableLoader,
      Duration writeLogstoreWatermarkGap) {
    this.mixedFormatLogWriter =
        new HiddenLogWriter(
            schema, producerConfig, topic, factory, fieldGetterFactory, jobId, helper);
    this.status = new AutomaticDoubleWriteStatus(tableLoader, writeLogstoreWatermarkGap);
  }

  @Override
  public void setup(
      StreamTask<?, ?> containingTask, StreamConfig config, Output<StreamRecord<RowData>> output) {
    super.setup(containingTask, config, output);
    mixedFormatLogWriter.setup(containingTask, config, output);
    status.setup(getRuntimeContext().getIndexOfThisSubtask());
  }

  @Override
  public void initializeState(StateInitializationContext context) throws Exception {
    super.initializeState(context);
    mixedFormatLogWriter.initializeState(context);
  }

  @Override
  public void open() throws Exception {
    super.open();
    mixedFormatLogWriter.open();
    status.open();
  }

  @Override
  public void processElement(StreamRecord<RowData> element) throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.processElement(element);
    }
  }

  @Override
  public void processWatermark(Watermark mark) throws Exception {
    status.processWatermark(mark);
    super.processWatermark(mark);
  }

  @Override
  public void prepareSnapshotPreBarrier(long checkpointId) throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.prepareSnapshotPreBarrier(checkpointId);
    } else {
      status.sync();
    }
  }

  @Override
  public void snapshotState(StateSnapshotContext context) throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.snapshotState(context);
    }
  }

  @Override
  public void notifyCheckpointComplete(long checkpointId) throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.notifyCheckpointComplete(checkpointId);
    }
  }

  @Override
  public void notifyCheckpointAborted(long checkpointId) throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.notifyCheckpointAborted(checkpointId);
    }
  }

  @Override
  public void close() throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.close();
    }
  }

  @Override
  public void endInput() throws Exception {
    if (status.isDoubleWrite()) {
      mixedFormatLogWriter.endInput();
    }
  }
}
