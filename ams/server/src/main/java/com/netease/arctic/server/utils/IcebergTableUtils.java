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

package com.netease.arctic.server.utils;

import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.optimizing.scan.BasicTableSnapshot;
import com.netease.arctic.server.optimizing.scan.KeyedTableSnapshot;
import com.netease.arctic.server.optimizing.scan.TableSnapshot;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TableFileUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.util.StructLikeMap;

import java.util.HashSet;
import java.util.Set;

public class IcebergTableUtils {
  public static long getSnapshotId(UnkeyedTable internalTable, boolean refresh) {
    if (refresh) {
      internalTable.refresh();
    }
    Snapshot currentSnapshot = internalTable.currentSnapshot();
    if (currentSnapshot == null) {
      return ArcticServiceConstants.INVALID_SNAPSHOT_ID;
    } else {
      return currentSnapshot.snapshotId();
    }
  }

  public static TableSnapshot getSnapshot(ArcticTable arcticTable, TableRuntime tableRuntime) {
    tableRuntime.refresh(arcticTable);
    if (arcticTable.isUnkeyedTable()) {
      return new BasicTableSnapshot(tableRuntime.getCurrentSnapshotId());
    } else {
      StructLikeMap<Long> partitionOptimizedSequence =
          TablePropertyUtil.getPartitionOptimizedSequence(arcticTable.asKeyedTable());
      StructLikeMap<Long> legacyPartitionMaxTransactionId =
          TablePropertyUtil.getLegacyPartitionMaxTransactionId(arcticTable.asKeyedTable());
      return new KeyedTableSnapshot(tableRuntime.getCurrentSnapshotId(),
          tableRuntime.getCurrentChangeSnapshotId(),
          partitionOptimizedSequence,
          legacyPartitionMaxTransactionId);
    }
  }

  public static Snapshot getSnapshot(UnkeyedTable internalTable, boolean refresh) {
    if (refresh) {
      internalTable.refresh();
    }
    return internalTable.currentSnapshot();
  }

  public static Set<String> getAllContentFilePath(UnkeyedTable internalTable) {
    Set<String> validFilesPath = new HashSet<>();

    TableEntriesScan manifestReader = TableEntriesScan.builder(internalTable)
        .includeFileContent(FileContent.DATA, FileContent.POSITION_DELETES, FileContent.EQUALITY_DELETES)
        .allEntries().build();
    for (IcebergFileEntry entry : manifestReader.entries()) {
      validFilesPath.add(TableFileUtil.getUriPath(entry.getFile().path().toString()));
    }

    return validFilesPath;
  }
}
