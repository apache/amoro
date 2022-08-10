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

package com.netease.arctic.flink.table;

import com.netease.arctic.flink.util.ArcticUtils;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.TimestampData;

import java.io.Serializable;
import java.util.TimeZone;

/**
 * Generate watermark according to the value in the last field of RowData.
 */
public class ArcticWatermarkGenerator implements WatermarkGenerator<RowData>, Serializable {
  public static final long serialVersionUID = 1L;
  private boolean generateWatermark = false;
  private long lastTs = System.currentTimeMillis();
  private final long watermarkIdleMs;
  private final TimeZone timeZone;

  public ArcticWatermarkGenerator(TimeZone timeZone, long watermarkIdleMs) {
    this.timeZone = timeZone;
    this.watermarkIdleMs = watermarkIdleMs;
  }

  @Override
  public void onEvent(RowData event, long eventTimestamp, WatermarkOutput output) {
    lastTs = System.currentTimeMillis();

    // Timestamp is set in the last field from ArcticRecordEmitter
    TimestampData ts = event.getTimestamp(event.getArity() - 1, 3);
    eventTimestamp = ts.getMillisecond();
    output.emitWatermark(new Watermark(eventTimestamp));
    if (eventTimestamp > Long.MIN_VALUE) {
      generateWatermark = true;
    }
  }

  @Override
  public void onPeriodicEmit(WatermarkOutput output) {
    if (generateWatermark) {
      output.emitWatermark(new Watermark(ArcticUtils.getCurrentTimestampData(timeZone).getMillisecond()));
    } else if (System.currentTimeMillis() - lastTs >= watermarkIdleMs) {
      generateWatermark = true;
    }
  }
}