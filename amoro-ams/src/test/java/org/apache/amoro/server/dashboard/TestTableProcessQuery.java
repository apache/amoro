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

package org.apache.amoro.server.dashboard;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class TestTableProcessQuery extends PersistentBase {
  private final DerbyPersistence persistence = new DerbyPersistence();
  private final ServerTableIdentifier table =
      ServerTableIdentifier.of("catalog", "db", "tbl", TableFormat.PAIMON);
  private final ServerTableIdentifier otherTable =
      ServerTableIdentifier.of("catalog", "db", "other", TableFormat.PAIMON);
  private final String action = PaimonActions.EXPIRE_SNAPSHOTS.getName();

  @AfterEach
  void cleanup() {
    persistence.clearTable(table.getId());
    persistence.clearTable(otherTable.getId());
  }

  @Test
  void cleanupQueryPreservesResultsAndLimitsEveryQueryToTargetTable() {
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(table));
    insert(table.getId(), 11, action, ProcessStatus.SUCCESS);
    insert(table.getId(), 12, action, ProcessStatus.FAILED);
    insert(table.getId(), 13, "MINOR", ProcessStatus.SUCCESS);
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(otherTable));
    insert(otherTable.getId(), 14, action, ProcessStatus.SUCCESS);
    TableProcessQuery query = new TableProcessQuery();
    Pair<List<OptimizingProcessInfo>, Integer> page =
        query.list(table, List.of(action), "  ", null, 1, 0);
    Assertions.assertEquals(2, page.getRight());
    OptimizingProcessInfo result = page.getLeft().get(0);
    Assertions.assertEquals("12", result.getProcessId());
    Assertions.assertFalse(asDashboardJson(result).containsKey("amsCancellationSupported"));
    Assertions.assertEquals("catalog", result.getCatalogName());
    Assertions.assertEquals("db", result.getDbName());
    Assertions.assertEquals("tbl", result.getTableName());
    Assertions.assertEquals(Map.of("outcome", "noop"), result.getSummary());
    Assertions.assertEquals(0, result.getTotalTasks());
    Assertions.assertNotNull(result.getInputFiles());
    Assertions.assertNotNull(result.getOutputFiles());
    Assertions.assertEquals(
        "11",
        query
            .list(table, List.of(action), null, ProcessStatus.SUCCESS, 10, 0)
            .getLeft()
            .get(0)
            .getProcessId());
    Assertions.assertEquals(
        0, query.list(table, Collections.emptyList(), null, null, 10, 0).getRight());
    Assertions.assertEquals(
        0, query.list(table, List.of(action), "unknown", null, 10, 0).getRight());
    Assertions.assertEquals(0, query.list(table, List.of(action), null, null, 10, 50).getRight());
  }

  @Test
  void serverRoutingRetainsIcebergTypePriorityAndOptimizationMetrics() {
    table.setFormat(TableFormat.ICEBERG);
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(table));
    insert(table.getId(), 11, action, ProcessStatus.SUCCESS);
    Map<String, String> metrics =
        Map.of(
            MetricsSummary.INPUT_DATA_FILES,
            "4",
            MetricsSummary.INPUT_DATA_SIZE,
            "40",
            MetricsSummary.OUTPUT_DATA_FILES,
            "1",
            MetricsSummary.OUTPUT_DATA_SIZE,
            "10");
    doAs(
        TableProcessMapper.class,
        mapper ->
            mapper.insertProcess(
                table.getId(),
                12L,
                "",
                ProcessStatus.SUCCESS,
                "MINOR",
                "default",
                "local",
                0,
                1000L,
                Map.of(),
                metrics));
    AmoroTable<?> iceberg = Mockito.mock(AmoroTable.class);
    Mockito.when(iceberg.id()).thenReturn(TableIdentifier.of("catalog", "db", "tbl"));
    Mockito.when(iceberg.format()).thenReturn(TableFormat.ICEBERG);
    CatalogManager catalogs = Mockito.mock(CatalogManager.class);
    ServerCatalog catalog = Mockito.mock(ServerCatalog.class);
    Mockito.when(catalogs.getServerCatalog("catalog")).thenReturn(catalog);
    Mockito.doReturn(iceberg).when(catalog).loadTable("db", "tbl");
    ServerTableDescriptor descriptor =
        new ServerTableDescriptor(catalogs, Mockito.mock(TableManager.class), new Configurations());
    org.apache.amoro.api.TableIdentifier id = iceberg.id().buildTableIdentifier();
    OptimizingProcessInfo cleanup =
        descriptor.getOptimizingProcessesInfo(id, null, "CLEANUP", null, 10, 0).getLeft().get(0);
    Assertions.assertEquals("11", cleanup.getProcessId());
    Assertions.assertFalse(asDashboardJson(cleanup).containsKey("amsCancellationSupported"));
    for (String category : new String[] {"CLEANUP", "OPTIMIZING", null}) {
      OptimizingProcessInfo minor =
          descriptor
              .getOptimizingProcessesInfo(id, "MINOR", category, null, 10, 0)
              .getLeft()
              .get(0);
      Assertions.assertEquals("12", minor.getProcessId());
      Assertions.assertEquals(4, minor.getInputFiles().getFileCnt());
      Assertions.assertEquals(10L, minor.getOutputFiles().getTotalSize());
    }
    Assertions.assertEquals(
        0, descriptor.getOptimizingProcessesInfo(id, "unknown", "CLEANUP", null, 10, 0).getRight());
    Assertions.assertEquals(
        0, descriptor.getOptimizingProcessesInfo(id, null, null, null, 10, 0).getRight());
    Assertions.assertEquals(
        0, descriptor.getOptimizingProcessesInfo(id, null, "unknown", null, 10, 0).getRight());
    Mockito.when(iceberg.id()).thenReturn(TableIdentifier.of("catalog", "db", "unregistered"));
    Assertions.assertEquals(
        0, descriptor.getOptimizingProcessesInfo(id, null, "CLEANUP", null, 10, 0).getRight());
    Mockito.when(catalog.loadTable("db", "tbl"))
        .thenThrow(new IllegalStateException("table unavailable"));
    Assertions.assertThrows(
        IllegalStateException.class,
        () -> descriptor.getOptimizingProcessesInfo(id, null, "CLEANUP", null, 10, 0));
  }

  @Test
  void cleanupHistoryComesFromTheRegisteredDescriptor(@TempDir Path plugins) throws Exception {
    Path services = Files.createDirectories(plugins.resolve("META-INF/services"));
    Files.writeString(
        services.resolve(FormatTableDescriptor.class.getName()), NativeHistory.class.getName());
    // Deliberately use the same action and a different record in the AMS store.
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(table));
    insert(table.getId(), 11, action, ProcessStatus.SUCCESS);
    AmoroTable<?> nativeTable = Mockito.mock(AmoroTable.class);
    Mockito.when(nativeTable.id()).thenReturn(TableIdentifier.of("catalog", "db", "tbl"));
    Mockito.when(nativeTable.format()).thenReturn(TableFormat.PAIMON);
    CatalogManager catalogs = Mockito.mock(CatalogManager.class);
    ServerCatalog catalog = Mockito.mock(ServerCatalog.class);
    Mockito.when(catalogs.getServerCatalog("catalog")).thenReturn(catalog);
    Mockito.doReturn(nativeTable).when(catalog).loadTable("db", "tbl");

    Thread thread = Thread.currentThread();
    ClassLoader original = thread.getContextClassLoader();
    try (URLClassLoader loader =
        new URLClassLoader(new URL[] {plugins.toUri().toURL()}, original)) {
      thread.setContextClassLoader(loader);
      ServerTableDescriptor server =
          new ServerTableDescriptor(
              catalogs, Mockito.mock(TableManager.class), new Configurations());
      for (String type : new String[] {null, action}) {
        Pair<List<OptimizingProcessInfo>, Integer> history =
            server.getOptimizingProcessesInfo(
                nativeTable.id().buildTableIdentifier(), type, "CLEANUP", null, 10, 0);
        Assertions.assertEquals(1, history.getRight());
        OptimizingProcessInfo entry = history.getLeft().get(0);
        Assertions.assertEquals("native-record", entry.getProcessId());
        Assertions.assertEquals(action, entry.getOptimizingType());
        Assertions.assertEquals(ProcessStatus.RUNNING, entry.getStatus());
        Assertions.assertFalse(asDashboardJson(entry).containsKey("amsCancellationSupported"));
      }
    } finally {
      thread.setContextClassLoader(original);
    }
  }

  public static class NativeHistory extends MixedAndIcebergTableDescriptor {
    @Override
    public List<TableFormat> supportFormat() {
      return List.of(TableFormat.PAIMON);
    }

    @Override
    public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
        AmoroTable<?> table,
        String type,
        String category,
        ProcessStatus status,
        int limit,
        int offset) {
      OptimizingProcessInfo entry = new OptimizingProcessInfo();
      entry.setProcessId("native-record");
      entry.setOptimizingType(PaimonActions.EXPIRE_SNAPSHOTS.getName());
      entry.setStatus(ProcessStatus.RUNNING);
      return Pair.of(List.of(entry), 1);
    }
  }

  private Map<?, ?> asDashboardJson(OptimizingProcessInfo info) {
    JavalinJsonMapper json = JavalinJsonMapper.createDefaultJsonMapper();
    return json.fromJsonString(json.toJsonString(info), Map.class);
  }

  private void insert(long tableId, long processId, String type, ProcessStatus status) {
    doAs(
        TableProcessMapper.class,
        mapper ->
            mapper.insertProcess(
                tableId,
                processId,
                "",
                status,
                type,
                "default",
                "local",
                0,
                1000L,
                Collections.emptyMap(),
                Map.of("outcome", "noop")));
  }
}
