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

package com.netease.arctic.flink.read.hybrid.reader;

import com.netease.arctic.flink.read.hybrid.split.ArcticSplitState;
import com.netease.arctic.flink.util.ArcticUtils;
import com.netease.arctic.flink.util.FlinkUtil;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.RecordEmitter;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.data.utils.JoinedRowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.TimeZone;

/**
 * Emitter that emit {@link T} to the next flink operator and update the record offset of {@link T} into split state.
 */
public class ArcticRecordEmitter<T> implements RecordEmitter<ArcticRecordWithOffset<T>, T, ArcticSplitState> {

  public static final Logger LOGGER = LoggerFactory.getLogger(ArcticRecordEmitter.class);

  /**
   * It signifies whether the watermark need to be generated.
   */
  public Optional<Boolean> generateWatermark = Optional.empty();
  private long autoWatermarkInterval;
  private long lastTime = 0L;
  private TimeZone timeZone;
  private transient SourceOutput<T> sourceOutput;

  public ArcticRecordEmitter(Configuration config, ClassLoader classLoader) {
    ExecutionConfig executionConfig = new ExecutionConfig();

    timeZone = FlinkUtil.getLocalTimeZone(config);
    executionConfig.configure(config, classLoader);
    generateWatermark = Optional.of(false);
  }

  @Override
  public void emitRecord(
      ArcticRecordWithOffset<T> element,
      SourceOutput<T> sourceOutput,
      ArcticSplitState split) throws Exception {
    this.sourceOutput = sourceOutput;

    T r = element.record();
    if (!generateWatermark.isPresent()) {
      sourceOutput.collect(element.record());
    } else {
      TimestampData wm = TimestampData.fromEpochMillis(Long.MIN_VALUE);

      if (generateWatermark.get()) {
        wm = ArcticUtils.getCurrentTimestampData(timeZone);
      }
      RowData d = new JoinedRowData((RowData) r, GenericRowData.of(wm));
      sourceOutput.collect((T) d);
    }
    split.updateOffset(new Object[]{element.insertFileOffset(), element.insertRecordOffset(),
        element.deleteFileOffset(), element.deleteRecordOffset()});
  }

  public void generateWatermark(SourceOutput<T> sourceOutput) {
    if (!generateWatermark.isPresent()) {
      return;
    }
    // todo optimize
    if (sourceOutput == null) {
      LOGGER.warn("generateWatermark. But it can not be sent due to there is no data in source before.");
      return;
    }
    Watermark watermark;
    long now = System.currentTimeMillis();
    if (now - lastTime < autoWatermarkInterval) {
      return;
    }

    if (generateWatermark.get()) {
      watermark = new Watermark(now);
      sourceOutput.emitWatermark(watermark);
    } else {
//      watermark = Watermark;
    }
  }

  public void startGenerateWatermark() {
    generateWatermark = Optional.of(true);
  }

}
