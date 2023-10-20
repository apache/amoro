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

package com.netease.arctic.server.dashboard;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.server.catalog.ServerCatalog;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.OptimizingProcessInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import com.netease.arctic.server.dashboard.utils.FilesStatisticsBuilder;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingProcessMeta;
import com.netease.arctic.server.optimizing.OptimizingTaskMeta;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.apache.paimon.AbstractFileStore;
import org.apache.paimon.Snapshot;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.manifest.ManifestFile;
import org.apache.paimon.manifest.ManifestFileMeta;
import org.apache.paimon.manifest.ManifestList;
import org.apache.paimon.table.FileStoreTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerTableDescriptor extends PersistentBase {

  private final Map<TableFormat, FormatTableDescriptor> formatDescriptorMap = new HashMap<>();

  private final TableService tableService;

  public ServerTableDescriptor(TableService tableService) {
    this.tableService = tableService;
    FormatTableDescriptor[] formatTableDescriptors = new FormatTableDescriptor[]{
        new MixedAndIcebergTableDescriptor(),
        new PaimonTableDescriptor()
    };
    for (FormatTableDescriptor formatTableDescriptor : formatTableDescriptors) {
      for (TableFormat format : formatTableDescriptor.supportFormat()) {
        formatDescriptorMap.put(format, formatTableDescriptor);
      }
    }
  }

  public ServerTableMeta getTableDetail(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableDetail(amoroTable);
  }

  public List<TransactionsOfTable> getTransactions(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTransactions(amoroTable);
  }

  public List<PartitionFileBaseInfo> getTransactionDetail(TableIdentifier tableIdentifier, long transactionId) {
    AmoroTable<?> amoroTable = loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTransactionDetail(amoroTable, transactionId);
  }

  public List<DDLInfo> getTableOperations(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableOperations(amoroTable);
  }

  public List<PartitionBaseInfo> getTablePartition(TableIdentifier tableIdentifier) {
    AmoroTable<?> amoroTable = loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTablePartitions(amoroTable);
  }

  public List<PartitionFileBaseInfo> getTableFile(TableIdentifier tableIdentifier, String partition) {
    AmoroTable<?> amoroTable = loadTable(tableIdentifier);
    FormatTableDescriptor formatTableDescriptor = formatDescriptorMap.get(amoroTable.format());
    return formatTableDescriptor.getTableFiles(amoroTable, partition);
  }

  public List<OptimizingProcessMeta> getOptimizingProcesses(String catalog, String db, String table) {
    return getAs(
        OptimizingMapper.class,
        mapper -> mapper.selectOptimizingProcesses(catalog, db, table));
  }

  public List<OptimizingTaskMeta> getOptimizingTasks(List<OptimizingProcessMeta> processMetaList) {
    if (CollectionUtils.isEmpty(processMetaList)) {
      return Collections.emptyList();
    }
    List<Long> processIds = processMetaList.stream()
        .map(OptimizingProcessMeta::getProcessId).collect(Collectors.toList());
    return getAs(
        OptimizingMapper.class,
        mapper -> mapper.selectOptimizeTaskMetas(processIds));
  }

  private AmoroTable<?> loadTable(TableIdentifier identifier) {
    ServerCatalog catalog = tableService.getServerCatalog(identifier.getCatalog());
    return catalog.loadTable(identifier.getDatabase(), identifier.getTableName());
  }

  public List<OptimizingProcessInfo> getPaimonOptimizingProcesses(
      AmoroTable<?> amoroTable, ServerTableIdentifier tableIdentifier) {
    // Temporary solution for Paimon. TODO: Get compaction info from Paimon compaction task
    List<OptimizingProcessInfo> processInfoList = new ArrayList<>();
    FileStoreTable fileStoreTable = (FileStoreTable) amoroTable.originalTable();
    AbstractFileStore<?> store = (AbstractFileStore<?>) fileStoreTable.store();
    ServerTableIdentifier tableIdentifierWithTableId = getAs(TableMetaMapper.class,
        mapper -> mapper.selectTableIdentifier(tableIdentifier.getCatalog(),
            tableIdentifier.getDatabase(),
            tableIdentifier.getTableName()));
    try {
      Streams.stream(store.snapshotManager().snapshots())
          .filter(s -> s.commitKind() == Snapshot.CommitKind.COMPACT)
          .forEach(s -> {
            OptimizingProcessInfo optimizingProcessInfo = new OptimizingProcessInfo();
            optimizingProcessInfo.setProcessId(s.id());
            optimizingProcessInfo.setTableId(tableIdentifierWithTableId.getId());
            optimizingProcessInfo.setCatalogName(tableIdentifierWithTableId.getCatalog());
            optimizingProcessInfo.setDbName(tableIdentifierWithTableId.getDatabase());
            optimizingProcessInfo.setTableName(tableIdentifierWithTableId.getTableName());
            optimizingProcessInfo.setStatus(OptimizingProcess.Status.SUCCESS);
            optimizingProcessInfo.setFinishTime(s.timeMillis());
            FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
            FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
            ManifestFile manifestFile = store.manifestFileFactory().create();
            ManifestList manifestList = store.manifestListFactory().create();
            List<ManifestFileMeta> manifestFileMetas = s.deltaManifests(manifestList);
            for (ManifestFileMeta manifestFileMeta : manifestFileMetas) {
              List<ManifestEntry> compactManifestEntries = manifestFile.read(manifestFileMeta.fileName());
              for (ManifestEntry compactManifestEntry : compactManifestEntries) {
                if (compactManifestEntry.kind() == FileKind.DELETE) {
                  inputBuilder.addFile(compactManifestEntry.file().fileSize());
                } else {
                  outputBuilder.addFile(compactManifestEntry.file().fileSize());
                }
              }
            }
            optimizingProcessInfo.setInputFiles(inputBuilder.build());
            optimizingProcessInfo.setOutputFiles(outputBuilder.build());
            processInfoList.add(optimizingProcessInfo);
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return processInfoList;
  }
}
