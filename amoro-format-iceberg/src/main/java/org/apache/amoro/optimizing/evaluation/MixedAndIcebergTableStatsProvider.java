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

package org.apache.amoro.optimizing.evaluation;

import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

public class MixedAndIcebergTableStatsProvider extends TableStatsProvider {

  public static final MixedAndIcebergTableStatsProvider INSTANCE =
      new MixedAndIcebergTableStatsProvider();

  private MixedAndIcebergTableStatsProvider() {}

  @Override
  public BasicFileStats collect(MixedTable table) {
    BasicFileStats stats = new BasicFileStats();
    if (table.isUnkeyedTable()) {
      acceptSnapshotIfPresent(stats, table.asUnkeyedTable().currentSnapshot());
    } else {
      acceptSnapshotIfPresent(stats, table.asKeyedTable().baseTable().currentSnapshot());
      acceptSnapshotIfPresent(stats, table.asKeyedTable().changeTable().currentSnapshot());
    }
    return stats;
  }

  private void acceptSnapshotIfPresent(BasicFileStats stats, Snapshot snapshot) {
    if (snapshot != null) {
      stats.accept(snapshot.summary());
    }
  }

  public static class BasicFileStats extends TableStatsProvider.BasicFileStats {
    public void accept(Map<String, String> summary) {
      deleteFileCnt +=
          PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DELETE_FILES_PROP, 0);
      dataFileCnt += PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
      totalFileSize +=
          PropertyUtil.propertyAsLong(summary, SnapshotSummary.TOTAL_FILE_SIZE_PROP, 0);
    }
  }
}
