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
import org.apache.flink.api.connector.source.SourceOutput;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.source.reader.RecordEmitter;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.data.utils.JoinedRowData;
import org.apache.flink.util.Preconditions;
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
   * It signifies whether the ProcessingTimestamp need to be set into RowData.
   * If it's null, RowData won't be changed.
   * If it's false, RowData would be added a field, which values LONG.MIN_VALUE.
   * If it's true, RowData would be added a field, which values Processing Time.
   */
  public Boolean generateProcessingTimestamp;
  private final TimeZone timeZone;

  public ArcticRecordEmitter(Configuration config, ClassLoader classLoader, boolean generateWatermarkTimestamp) {
    ExecutionConfig executionConfig = new ExecutionConfig();

    timeZone = FlinkUtil.getLocalTimeZone(config);
    executionConfig.configure(config, classLoader);

    this.generateProcessingTimestamp = generateWatermarkTimestamp ? false : null;
  }

  @Override
  public void emitRecord(
      ArcticRecordWithOffset<T> element,
      SourceOutput<T> sourceOutput,
      ArcticSplitState split) throws Exception {
    T record = element.record();
    if (generateProcessingTimestamp == null) {
      sourceOutput.collect(record);
    } else {
      TimestampData rowTime = TimestampData.fromEpochMillis(Long.MIN_VALUE);

      if (generateProcessingTimestamp) {
        rowTime = ArcticUtils.getCurrentTimestampData(timeZone);
      }
      Preconditions.checkArgument(record instanceof RowData,
          "Custom watermark strategy doesn't support %s, except RowData for now.",
          record.getClass());
      RowData rowData = new JoinedRowData((RowData) record, GenericRowData.of(rowTime));
      sourceOutput.collect((T) rowData);
    }
    split.updateOffset(new Object[]{element.insertFileOffset(), element.insertRecordOffset(),
        element.deleteFileOffset(), element.deleteRecordOffset()});
  }

  public void startGenerateTimestamp() {
    generateProcessingTimestamp = true;
  }

}
