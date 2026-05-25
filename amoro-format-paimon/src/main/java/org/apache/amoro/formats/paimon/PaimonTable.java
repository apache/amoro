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
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.Snapshot;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.operation.FileStoreScan;
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

public class PaimonTable implements AmoroTable<Table>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(PaimonTable.class);

  private final TableIdentifier tableIdentifier;

  private final Table table;

  private final Map<String, String> catalogProperties;

  public PaimonTable(TableIdentifier tableIdentifier, Table table) {
    this(tableIdentifier, table, ImmutableMap.of());
  }

  public PaimonTable(
      TableIdentifier tableIdentifier, Table table, Map<String, String> catalogProperties) {
    this.tableIdentifier = tableIdentifier;
    this.table = table;
    this.catalogProperties =
        catalogProperties == null ? ImmutableMap.of() : ImmutableMap.copyOf(catalogProperties);
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
    if (!(table instanceof DataTable)) {
      return null;
    }

    Snapshot snapshot = ((DataTable) table).snapshotManager().latestSnapshot();
    return snapshot == null ? null : new PaimonSnapshot(snapshot);
  }

  @Override
  public Optional<PendingInputResult> evaluatePendingInput(
      OptimizationContext context, int maxPendingPartitions) {
    PaimonPendingInput pendingInput = collectPaimonPendingInput();
    return Optional.of(new PendingInputResult(pendingInput, true));
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
