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

package org.apache.amoro.server.process.paimon;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.process.executor.LocalExecutionEngine;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.utils.JacksonUtil;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.Snapshot;
import org.apache.paimon.table.FileStoreTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaimonTableMetaSyncProcess extends TableProcess implements LocalProcess {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonTableMetaSyncProcess.class);
  private static final MetadataPersistence PERSISTENCE = new MetadataPersistence();

  public PaimonTableMetaSyncProcess(org.apache.amoro.TableRuntime tableRuntime) {
    super(tableRuntime, new LocalExecutionEngine());
  }

  @Override
  public void run() {
    AmoroTable<?> amoroTable = tableRuntime.loadTable();
    Object originalTable = amoroTable.originalTable();
    if (!(originalTable instanceof FileStoreTable)) {
      LOG.warn(
          "Skip table metadata sync for {}, unsupported table type {}",
          tableRuntime.getTableIdentifier(),
          originalTable == null ? "null" : originalTable.getClass().getName());
      return;
    }

    FileStoreTable fileStoreTable = (FileStoreTable) originalTable;
    try {
      // Trigger Paimon lazy refresh before extracting the latest snapshot and metadata.
      amoroTable.currentSnapshot();

      TableMetadata metadata = buildTableMetadata(amoroTable, fileStoreTable);
      PERSISTENCE.persist(tableRuntime.getTableIdentifier().getId(), metadata);
      LOG.info("Synced table metadata for {}", tableRuntime.getTableIdentifier());
    } catch (Throwable t) {
      LOG.error("Failed to sync table metadata for {}", tableRuntime.getTableIdentifier(), t);
      throw t;
    }
  }

  @Override
  public String tag() {
    return LocalExecutionEngine.TABLE_META_SYNC_POOL;
  }

  @Override
  public Action getAction() {
    return PaimonActions.SYNC_TABLE_META;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, String> getSummary() {
    return Collections.emptyMap();
  }

  private TableMetadata buildTableMetadata(
      AmoroTable<?> amoroTable, FileStoreTable fileStoreTable) {
    String tableLocation = fileStoreTable.location().toString();
    String baseLocation = fileStoreTable.store().pathFactory().dataFilePath().toString();
    String changeLocation = null;
    try {
      changeLocation = fileStoreTable.store().changelogManager().changelogDirectory().toString();
    } catch (Exception e) {
      LOG.debug("No changelog directory for {}", tableRuntime.getTableIdentifier());
    }

    String primaryKey =
        fileStoreTable.primaryKeys().isEmpty()
            ? null
            : String.join(",", fileStoreTable.primaryKeys());
    String schemaJson = JacksonUtil.toJSONString(extractSchema(fileStoreTable));
    Snapshot snapshot = fileStoreTable.store().snapshotManager().latestSnapshot();
    String snapshotId = snapshot == null ? null : String.valueOf(snapshot.id());
    CoreOptions coreOptions = CoreOptions.fromMap(fileStoreTable.options());

    return TableMetadata.fromExternalTable(
        tableRuntime.getTableIdentifier(),
        tableLocation,
        baseLocation,
        changeLocation,
        primaryKey,
        amoroTable.properties(),
        schemaJson,
        fileStoreTable.bucketMode().toString(),
        coreOptions.bucket(),
        snapshotId,
        fileStoreTable.comment().orElse(null));
  }

  private List<AMSColumnInfo> extractSchema(FileStoreTable fileStoreTable) {
    return fileStoreTable.rowType().getFields().stream()
        .map(
            field ->
                new AMSColumnInfo(
                    field.name(),
                    field.type().asSQLString(),
                    !field.type().isNullable(),
                    field.description()))
        .collect(Collectors.toList());
  }

  private static class MetadataPersistence extends PersistentBase {

    private void persist(long tableId, TableMetadata tableMetadata) {
      Long updated =
          updateAs(TableMetaMapper.class, mapper -> mapper.updateTableMeta(tableId, tableMetadata));
      if (updated == 0L) {
        doAs(TableMetaMapper.class, mapper -> mapper.insertTableMeta(tableMetadata));
      }
    }
  }
}
