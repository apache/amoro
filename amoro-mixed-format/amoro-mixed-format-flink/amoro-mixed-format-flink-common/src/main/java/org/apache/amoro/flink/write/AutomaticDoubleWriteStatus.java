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

import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.table.MixedTable;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.iceberg.UpdateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

/** This is an automatic logstore writer util class. */
public class AutomaticDoubleWriteStatus implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AutomaticDoubleWriteStatus.class);

  private static final long serialVersionUID = 1L;
  private final MixedFormatTableLoader tableLoader;
  private final AutomaticWriteSpecification specification;
  private MixedTable table;
  private transient boolean shouldDoubleWrite = false;
  private int subtaskId;

  public AutomaticDoubleWriteStatus(
      MixedFormatTableLoader tableLoader, Duration writeLogstoreWatermarkGap) {
    this.tableLoader = tableLoader;
    this.specification = new AutomaticWriteSpecification(writeLogstoreWatermarkGap);
  }

  public void setup(int indexOfThisSubtask) {
    this.subtaskId = indexOfThisSubtask;
  }

  public void open() {
    table = MixedFormatUtils.loadMixedTable(tableLoader);
    sync();
  }

  public boolean isDoubleWrite() {
    return shouldDoubleWrite;
  }

  public void processWatermark(Watermark mark) {
    if (isDoubleWrite()) {
      return;
    }
    if (specification.shouldDoubleWrite(mark.getTimestamp())) {
      shouldDoubleWrite = true;
      LOG.info(
          "processWatermark {}, subTaskId is {}, should double write is true.", mark, subtaskId);
      LOG.info(
          "begin update mixed-format table, set {} to true",
          MixedFormatValidator.LOG_STORE_CATCH_UP.key());
      UpdateProperties updateProperties = table.updateProperties();
      updateProperties.set(MixedFormatValidator.LOG_STORE_CATCH_UP.key(), String.valueOf(true));
      updateProperties.set(
          MixedFormatValidator.LOG_STORE_CATCH_UP_TIMESTAMP.key(),
          String.valueOf(System.currentTimeMillis()));
      updateProperties.remove(MixedFormatValidator.AUTO_EMIT_LOGSTORE_WATERMARK_GAP.key());
      updateProperties.commit();
      LOG.info("end update mixed-format table.");
    }
  }

  public void sync() {
    table.refresh();
    Map<String, String> properties = table.properties();
    shouldDoubleWrite =
        !properties.containsKey(MixedFormatValidator.AUTO_EMIT_LOGSTORE_WATERMARK_GAP.key());
    LOG.info(
        "AutomaticDoubleWriteStatus sync, subTaskId: {}, should double write: {}",
        subtaskId,
        shouldDoubleWrite);
  }
}
