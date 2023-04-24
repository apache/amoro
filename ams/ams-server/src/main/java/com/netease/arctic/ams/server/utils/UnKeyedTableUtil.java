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

package com.netease.arctic.ams.server.utils;

import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: It's better to refactor UnKeyedTableUtil to IcebergTableUtil.
 */
public class UnKeyedTableUtil {
  private static final Logger LOG = LoggerFactory.getLogger(UnKeyedTableUtil.class);

  public static long getSnapshotId(Table internalTable) {
    internalTable.refresh();
    Snapshot currentSnapshot = internalTable.currentSnapshot();
    if (currentSnapshot == null) {
      return TableOptimizeRuntime.INVALID_SNAPSHOT_ID;
    } else {
      return currentSnapshot.snapshotId();
    }
  }

  public static Snapshot getCurrentSnapshot(Table internalTable) {
    internalTable.refresh();
    return internalTable.currentSnapshot();
  }

  public static Set<String> getAllContentFilePath(Table internalTable) {
    Set<String> validFilesPath = new HashSet<>();

    TableEntriesScan manifestReader = TableEntriesScan.builder(internalTable)
        .includeFileContent(FileContent.DATA, FileContent.POSITION_DELETES, FileContent.EQUALITY_DELETES)
        .allEntries().build();
    for (IcebergFileEntry entry : manifestReader.entries()) {
      validFilesPath.add(TableFileUtils.getUriPath(entry.getFile().path().toString()));
    }

    return validFilesPath;
  }

  private static String metadataTableName(String tableName, MetadataTableType type) {
    return tableName + (tableName.contains("/") ? "#" : ".") + type;
  }
}
