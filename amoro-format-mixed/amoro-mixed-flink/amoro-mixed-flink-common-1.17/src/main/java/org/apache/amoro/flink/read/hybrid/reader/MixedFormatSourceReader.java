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

package org.apache.amoro.flink.read.hybrid.reader;

import org.apache.amoro.flink.read.MixedFormatSource;
import org.apache.amoro.flink.read.hybrid.enumerator.InitializationFinishedEvent;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplitState;
import org.apache.amoro.flink.read.hybrid.split.SplitRequestEvent;
import org.apache.amoro.flink.util.FlinkClassReflectionUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkOutputMultiplexer;
import org.apache.flink.api.connector.source.ReaderOutput;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.SingleThreadMultiplexSourceReaderBase;
import org.apache.flink.core.io.InputStatus;
import org.apache.flink.streaming.api.operators.source.ProgressiveTimestampsAndWatermarks;
import org.apache.flink.streaming.api.operators.source.SourceOutputWithWatermarks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Mixed-format source reader that is created by a {@link
 * MixedFormatSource#createReader(SourceReaderContext)}.
 */
public class MixedFormatSourceReader<T>
    extends SingleThreadMultiplexSourceReaderBase<
        MixedFormatRecordWithOffset<T>, T, MixedFormatSplit, MixedFormatSplitState> {

  public static final Logger LOGGER = LoggerFactory.getLogger(MixedFormatSourceReader.class);

  public ReaderOutput<T> output;
  /** SourceEvents may be received before this#pollNext. */
  private volatile boolean maxWatermarkToBeEmitted = false;

  public MixedFormatSourceReader(
      ReaderFunction<T> readerFunction,
      Configuration config,
      SourceReaderContext context,
      boolean populateRowTime) {
    super(
        () -> new HybridSplitReader<>(readerFunction, context),
        new MixedFormatRecordEmitter<T>(populateRowTime),
        config,
        context);
  }

  @Override
  public void start() {
    // We request a split only if we did not get splits during the checkpoint restore.
    // Otherwise, reader restarts will keep requesting more and more splits.
    if (getNumberOfCurrentlyAssignedSplits() == 0) {
      requestSplit(Collections.emptyList());
    }
    context.sendSourceEventToCoordinator(ReaderStartedEvent.INSTANCE);
  }

  @Override
  protected void onSplitFinished(Map<String, MixedFormatSplitState> finishedSplitIds) {
    requestSplit(Lists.newArrayList(finishedSplitIds.keySet()));
  }

  @Override
  protected MixedFormatSplitState initializedState(MixedFormatSplit split) {
    return new MixedFormatSplitState(split);
  }

  @Override
  protected MixedFormatSplit toSplitType(String splitId, MixedFormatSplitState splitState) {
    return splitState.toSourceSplit();
  }

  private void requestSplit(Collection<String> finishedSplitIds) {
    context.sendSourceEventToCoordinator(new SplitRequestEvent(finishedSplitIds));
  }

  @Override
  public void handleSourceEvents(SourceEvent sourceEvent) {
    if (!(sourceEvent instanceof InitializationFinishedEvent)) {
      return;
    }
    LOGGER.info("receive InitializationFinishedEvent");
    maxWatermarkToBeEmitted = true;
    emitWatermarkIfNeeded();
  }

  private void emitWatermarkIfNeeded() {
    if (this.output == null || !maxWatermarkToBeEmitted) {
      return;
    }
    LOGGER.info("emit watermark");
    output.emitWatermark(new Watermark(Long.MAX_VALUE));
    maxWatermarkToBeEmitted = false;
  }

  @Override
  public InputStatus pollNext(ReaderOutput<T> output) throws Exception {
    this.output = output;
    emitWatermarkIfNeeded();
    return super.pollNext(wrapOutput(output));
  }

  public ReaderOutput<T> wrapOutput(ReaderOutput<T> output) {
    if (!(output instanceof SourceOutputWithWatermarks)) {
      return output;
    }
    return new MixedFormatReaderOutput<>(output);
  }

  /**
   * There is a case that the watermark in {@link WatermarkOutputMultiplexer.OutputState} has been
   * updated, but watermark has not been emitted for that when {@link
   * WatermarkOutputMultiplexer#onPeriodicEmit} called, the outputState has been removed by {@link
   * WatermarkOutputMultiplexer#unregisterOutput(String)} after split finished. Wrap {@link
   * ReaderOutput} to call {@link
   * ProgressiveTimestampsAndWatermarks.SplitLocalOutputs#emitPeriodicWatermark()} when split
   * finishes.
   */
  static class MixedFormatReaderOutput<T> implements ReaderOutput<T> {

    private final ReaderOutput<T> internal;

    public MixedFormatReaderOutput(ReaderOutput<T> readerOutput) {
      Preconditions.checkArgument(
          readerOutput instanceof SourceOutputWithWatermarks,
          "readerOutput should be SourceOutputWithWatermarks, but was %s",
          readerOutput.getClass());
      this.internal = readerOutput;
    }

    @Override
    public void collect(T record) {
      internal.collect(record);
    }

    @Override
    public void collect(T record, long timestamp) {
      internal.collect(record, timestamp);
    }

    @Override
    public void emitWatermark(Watermark watermark) {
      internal.emitWatermark(watermark);
    }

    @Override
    public void markIdle() {
      internal.markIdle();
    }

    @Override
    public void markActive() {
      internal.markActive();
    }

    @Override
    public SourceOutput<T> createOutputForSplit(String splitId) {
      return internal.createOutputForSplit(splitId);
    }

    @Override
    public void releaseOutputForSplit(String splitId) {
      Object splitLocalOutput = FlinkClassReflectionUtil.getSplitLocalOutput(internal);
      FlinkClassReflectionUtil.emitPeriodWatermark(splitLocalOutput);
      internal.releaseOutputForSplit(splitId);
    }
  }
}
