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

package org.apache.amoro.formats.paimon;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyOptions;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.Snapshot;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.operation.FileStoreScan;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.BucketMode;
import org.apache.paimon.table.DataTable;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class PaimonTable implements AmoroTable<Table>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(PaimonTable.class);

  private final TableIdentifier tableIdentifier;

  private final Table table;

  private final Map<String, String> catalogProperties;

  private transient TableMetaStore tableMetaStore;

  public PaimonTable(TableIdentifier tableIdentifier, Table table) {
    this(tableIdentifier, table, ImmutableMap.of());
  }

  public PaimonTable(
      TableIdentifier tableIdentifier, Table table, Map<String, String> catalogProperties) {
    this(tableIdentifier, table, catalogProperties, null);
  }

  public PaimonTable(
      TableIdentifier tableIdentifier,
      Table table,
      Map<String, String> catalogProperties,
      TableMetaStore tableMetaStore) {
    this.tableIdentifier = tableIdentifier;
    this.table = table;
    this.catalogProperties =
        catalogProperties == null ? ImmutableMap.of() : ImmutableMap.copyOf(catalogProperties);
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public TableIdentifier id() {
    return tableIdentifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }

  @Override
  public Map<String, String> properties() {
    return CatalogUtil.mergeCatalogPropertiesToTable(table.options(), catalogProperties);
  }

  @Override
  public Table originalTable() {
    return table;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    return doAs(
        () -> {
          if (!(table instanceof DataTable)) {
            return null;
          }

          Snapshot snapshot = ((DataTable) table).snapshotManager().latestSnapshot();
          return snapshot == null ? null : new PaimonSnapshot(snapshot);
        });
  }

  @Override
  public Optional<PendingInputResult> evaluatePendingInput(
      OptimizationContext context, int maxPendingPartitions) {
    return doAs(
        () -> {
          boolean appendBucketUnaware = isAppendBucketUnawareTable();
          PaimonPendingInput pendingInput =
              appendBucketUnaware ? collectPaimonPendingInput() : new PaimonPendingInput();
          boolean selfOptimizingEnabled = isSelfOptimizingEnabled(context);
          boolean optimizingNecessary =
              selfOptimizingEnabled && (appendBucketUnaware || isPrimaryKeyHashOptimizingEnabled());
          return Optional.of(new PendingInputResult(pendingInput, optimizingNecessary));
        });
  }

  public <T> T doAs(Callable<T> callable) {
    if (tableMetaStore == null) {
      return call(callable);
    }
    return tableMetaStore.doAs(callable);
  }

  private <T> T call(Callable<T> callable) {
    try {
      return callable.call();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Run with Paimon table authentication context failed.", e);
    }
  }

  private boolean isSelfOptimizingEnabled(OptimizationContext context) {
    if (context == null) {
      return true;
    }
    OptimizingConfig config = context.getOptimizingConfig();
    return config == null || config.isEnabled();
  }

  private boolean isAppendBucketUnawareTable() {
    if (!(table instanceof AppendOnlyFileStoreTable)) {
      return false;
    }
    AppendOnlyFileStoreTable appendOnlyTable = (AppendOnlyFileStoreTable) table;
    return appendOnlyTable.bucketMode() == BucketMode.BUCKET_UNAWARE;
  }

  private boolean isPrimaryKeyHashOptimizingEnabled() {
    if (!(table instanceof FileStoreTable) || table instanceof AppendOnlyFileStoreTable) {
      return false;
    }
    FileStoreTable fileStoreTable = (FileStoreTable) table;
    if (fileStoreTable.primaryKeys() == null || fileStoreTable.primaryKeys().isEmpty()) {
      return false;
    }
    if (fileStoreTable.bucketMode() != BucketMode.HASH_FIXED
        && fileStoreTable.bucketMode() != BucketMode.HASH_DYNAMIC) {
      return false;
    }
    try {
      return PaimonPrimaryKeyOptions.from(fileStoreTable.options()).enabled();
    } catch (RuntimeException e) {
      LOG.warn(
          "Paimon primary-key optimizing options are invalid for table [{}], skip pending input.",
          tableIdentifier,
          e);
      return false;
    }
  }

  private PaimonPendingInput collectPaimonPendingInput() {
    if (!(table instanceof FileStoreTable)) {
      LOG.warn(
          "Expected FileStoreTable but got {}, returning empty pending input",
          table.getClass().getName());
      return new PaimonPendingInput();
    }
    FileStoreTable fileStoreTable = (FileStoreTable) table;
    FileStoreScan scan = fileStoreTable.store().newScan();
    List<ManifestEntry> entries = scan.plan().files(FileKind.ADD);

    long targetFileSize = CoreOptions.fromMap(fileStoreTable.options()).targetFileSize(false);
    Map<BinaryRow, List<DataFileMeta>> partitionFiles = new HashMap<>();

    int dataFileCount = 0;
    long dataFileSize = 0;
    long dataRecordCount = 0;
    int smallFileCount = 0;
    long smallFileSize = 0;
    int fileWithDeleteCount = 0;
    long deleteRecordCount = 0;

    for (ManifestEntry entry : entries) {
      DataFileMeta file = entry.file();
      dataFileCount++;
      dataFileSize += file.fileSize();
      dataRecordCount += file.rowCount();

      if (file.fileSize() < targetFileSize) {
        smallFileCount++;
        smallFileSize += file.fileSize();
      }

      if (file.deleteRowCount().isPresent() && file.deleteRowCount().get() > 0) {
        fileWithDeleteCount++;
        deleteRecordCount += file.deleteRowCount().get();
      }

      partitionFiles.computeIfAbsent(entry.partition(), k -> new ArrayList<>()).add(file);
    }

    int partitionCount = partitionFiles.size();
    int healthScore =
        PaimonPendingInput.computeHealthScore(
            dataFileCount,
            dataFileSize,
            smallFileCount,
            smallFileSize,
            dataRecordCount,
            deleteRecordCount,
            partitionFiles);

    return new PaimonPendingInput(
        dataFileCount,
        dataFileSize,
        dataRecordCount,
        smallFileCount,
        smallFileSize,
        partitionCount,
        fileWithDeleteCount,
        deleteRecordCount,
        healthScore);
  }
}
