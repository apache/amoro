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

package org.apache.amoro.flink.table;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.flink.api.common.operators.MailboxExecutor;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.JavaSerializer;
import org.apache.flink.runtime.state.StateInitializationContext;
import org.apache.flink.runtime.state.StateSnapshotContext;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.api.operators.StreamSourceContexts;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.runtime.tasks.ProcessingTimeService;
import org.apache.flink.table.data.RowData;
import org.apache.flink.util.function.ThrowingRunnable;
import org.apache.iceberg.flink.source.FlinkInputFormat;
import org.apache.iceberg.flink.source.FlinkInputSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;

/** Minimal reader operator to avoid depending on Iceberg's non-public StreamingReaderOperator. */
public class UnkeyedStreamingReaderOperator extends AbstractStreamOperator<RowData>
    implements OneInputStreamOperator<FlinkInputSplit, RowData> {

  private static final Logger LOG = LoggerFactory.getLogger(UnkeyedStreamingReaderOperator.class);

  private final MailboxExecutor executor;
  private FlinkInputFormat format;
  private transient SourceFunction.SourceContext<RowData> sourceContext;
  private transient ListState<FlinkInputSplit> inputSplitsState;
  private transient Queue<FlinkInputSplit> splits;
  private transient SplitState currentSplitState;

  public UnkeyedStreamingReaderOperator(
      FlinkInputFormat format, ProcessingTimeService timeService, MailboxExecutor mailboxExecutor) {
    this.format = Preconditions.checkNotNull(format, "The InputFormat should not be null.");
    this.processingTimeService = timeService;
    this.executor =
        Preconditions.checkNotNull(mailboxExecutor, "The mailboxExecutor should not be null.");
  }

  @Override
  public void initializeState(StateInitializationContext context) throws Exception {
    super.initializeState(context);
    inputSplitsState =
        context
            .getOperatorStateStore()
            .getListState(new ListStateDescriptor<>("splits", new JavaSerializer<>()));
    currentSplitState = SplitState.IDLE;
    splits = Lists.newLinkedList();
    if (context.isRestored()) {
      int taskIdx = getRuntimeContext().getIndexOfThisSubtask();
      LOG.info("Restoring state for the {} (taskIdx: {}).", getClass().getSimpleName(), taskIdx);
      for (FlinkInputSplit split : inputSplitsState.get()) {
        splits.add(split);
      }
    }

    sourceContext =
        StreamSourceContexts.getSourceContext(
            getOperatorConfig().getTimeCharacteristic(),
            getProcessingTimeService(),
            new Object(),
            output,
            getRuntimeContext().getExecutionConfig().getAutoWatermarkInterval(),
            -1L,
            true);
    enqueueProcessSplits();
  }

  @Override
  public void snapshotState(StateSnapshotContext context) throws Exception {
    super.snapshotState(context);
    inputSplitsState.clear();
    inputSplitsState.addAll(Lists.newArrayList(splits));
  }

  @Override
  public void processElement(StreamRecord<FlinkInputSplit> element) {
    splits.add(element.getValue());
    enqueueProcessSplits();
  }

  private void enqueueProcessSplits() {
    if (currentSplitState == SplitState.IDLE && !splits.isEmpty()) {
      currentSplitState = SplitState.RUNNING;
      executor.execute(
          (ThrowingRunnable<IOException>) this::processSplits, getClass().getSimpleName());
    }
  }

  private void processSplits() throws IOException {
    FlinkInputSplit split = splits.poll();
    if (split == null) {
      currentSplitState = SplitState.IDLE;
      return;
    }

    format.open(split);
    try {
      RowData next = null;
      while (!format.reachedEnd()) {
        next = format.nextRecord(next);
        sourceContext.collect(next);
      }
    } finally {
      currentSplitState = SplitState.IDLE;
      format.close();
    }
    enqueueProcessSplits();
  }

  @Override
  public void processWatermark(Watermark mark) {}

  @Override
  public void close() throws Exception {
    super.close();
    if (format != null) {
      format.close();
      format.closeInputFormat();
      format = null;
    }
    sourceContext = null;
  }

  @Override
  public void finish() throws Exception {
    super.finish();
    output.close();
    if (sourceContext != null) {
      sourceContext.emitWatermark(Watermark.MAX_WATERMARK);
      sourceContext.close();
      sourceContext = null;
    }
  }

  private enum SplitState {
    IDLE,
    RUNNING
  }
}
