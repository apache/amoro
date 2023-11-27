/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Table;

/**
 * API for maintaining table.
 *
 * <p>Includes: clean content files, clean metadata, clean dangling delete files, expire snapshots,
 * auto create tags.
 */
// TODO TableMaintainer should not be in this optimizing.xxx package.
public interface TableMaintainer {

  /** Clean table orphan files. Includes: data files, metadata files, dangling delete files. */
  void cleanOrphanFiles(TableRuntime tableRuntime);

  /**
   * Expire snapshots. The optimizing based on the snapshot that the current table relies on will
   * not expire according to TableRuntime.
   */
  void expireSnapshots(TableRuntime tableRuntime);

  /**
   * Expire historical data based on the expiration field, and data that exceeds the retention
   * period will be purged
   *
   * @param tableRuntime TableRuntime
   */
  void expireData(TableRuntime tableRuntime);

  /** Auto create tags for table. */
  void autoCreateTags(TableRuntime tableRuntime);

  static TableMaintainer ofTable(AmoroTable<?> amoroTable) {
    TableFormat format = amoroTable.format();
    if (format == TableFormat.MIXED_HIVE || format == TableFormat.MIXED_ICEBERG) {
      return new MixedTableMaintainer((ArcticTable) amoroTable.originalTable());
    } else if (format == TableFormat.ICEBERG) {
      return new IcebergTableMaintainer((Table) amoroTable.originalTable());
    } else {
      throw new RuntimeException("Unsupported table type" + amoroTable.originalTable().getClass());
    }
  }
}
