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
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.table.BasicTableSnapshot;
import com.netease.arctic.server.table.KeyedTableSnapshot;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableSnapshot;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import com.netease.arctic.utils.TableFileUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.util.LocationUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class IcebergTableUtil {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergTableUtil.class);

  public static long getSnapshotId(Table table, boolean refresh) {
    Snapshot currentSnapshot = getSnapshot(table, refresh);
    if (currentSnapshot == null) {
      return ArcticServiceConstants.INVALID_SNAPSHOT_ID;
    } else {
      return currentSnapshot.snapshotId();
    }
  }

  public static TableSnapshot getSnapshot(ArcticTable arcticTable, TableRuntime tableRuntime) {
    if (arcticTable.isUnkeyedTable()) {
      return new BasicTableSnapshot(tableRuntime.getCurrentSnapshotId());
    } else {
      StructLikeMap<Long> partitionOptimizedSequence =
          TablePropertyUtil.getPartitionOptimizedSequence(arcticTable.asKeyedTable());
      StructLikeMap<Long> legacyPartitionMaxTransactionId =
          TablePropertyUtil.getLegacyPartitionMaxTransactionId(arcticTable.asKeyedTable());
      return new KeyedTableSnapshot(
          tableRuntime.getCurrentSnapshotId(),
          tableRuntime.getCurrentChangeSnapshotId(),
          partitionOptimizedSequence,
          legacyPartitionMaxTransactionId);
    }
  }

  public static Snapshot getSnapshot(Table table, boolean refresh) {
    if (refresh) {
      table.refresh();
    }
    return table.currentSnapshot();
  }

  public static Set<String> getAllContentFilePath(Table internalTable) {
    Set<String> validFilesPath = new HashSet<>();

    TableEntriesScan entriesScan =
        TableEntriesScan.builder(internalTable)
            .includeFileContent(
                FileContent.DATA, FileContent.POSITION_DELETES, FileContent.EQUALITY_DELETES)
            .allEntries()
            .build();
    try (CloseableIterable<IcebergFileEntry> entries = entriesScan.entries()) {
      for (IcebergFileEntry entry : entries) {
        validFilesPath.add(TableFileUtil.getUriPath(entry.getFile().path().toString()));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return validFilesPath;
  }

  public static Set<DeleteFile> getDanglingDeleteFiles(Table internalTable) {
    if (internalTable.currentSnapshot() == null) {
      return Collections.emptySet();
    }
    Set<String> deleteFilesPath = new HashSet<>();
    TableScan tableScan = internalTable.newScan();
    try (CloseableIterable<FileScanTask> fileScanTasks = tableScan.planFiles()) {
      for (FileScanTask fileScanTask : fileScanTasks) {
        for (DeleteFile delete : fileScanTask.deletes()) {
          deleteFilesPath.add(delete.path().toString());
        }
      }
    } catch (IOException e) {
      LOG.error("table scan plan files error", e);
      return Collections.emptySet();
    }

    Set<DeleteFile> danglingDeleteFiles = new HashSet<>();
    TableEntriesScan entriesScan =
        TableEntriesScan.builder(internalTable)
            .useSnapshot(internalTable.currentSnapshot().snapshotId())
            .includeFileContent(FileContent.EQUALITY_DELETES, FileContent.POSITION_DELETES)
            .build();

    for (IcebergFileEntry entry : entriesScan.entries()) {
      ContentFile<?> file = entry.getFile();
      String path = file.path().toString();
      if (!deleteFilesPath.contains(path)) {
        danglingDeleteFiles.add((DeleteFile) file);
      }
    }

    return danglingDeleteFiles;
  }


}
