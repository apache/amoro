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
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class TestTableProcessQuery extends PersistentBase {
  private final Persistence persistence = new Persistence();
  private final ServerTableIdentifier table =
      ServerTableIdentifier.of(1L, "catalog", "db", "tbl", TableFormat.PAIMON);
  private final String action = PaimonActions.EXPIRE_SNAPSHOTS.getName();

  @AfterEach
  void cleanup() {
    persistence.clear();
  }

  @Test
  void cleanupQueryPreservesResultsAndLimitsEveryQueryToTargetTable() {
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(table));
    insert(table.getId(), 11, action, ProcessStatus.SUCCESS);
    insert(table.getId(), 12, action, ProcessStatus.FAILED);
    insert(table.getId(), 13, "MINOR", ProcessStatus.SUCCESS);
    insert(table.getId() + 1, 14, action, ProcessStatus.SUCCESS);
    TableProcessQuery query = new TableProcessQuery();
    Pair<List<OptimizingProcessInfo>, Integer> page =
        query.list(table, List.of(action), "  ", null, 1, 0);
    Assertions.assertEquals(2, page.getRight());
    OptimizingProcessInfo result = page.getLeft().get(0);
    Assertions.assertEquals("12", result.getProcessId());
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
    Assertions.assertEquals(
        "11",
        descriptor
            .getOptimizingProcessesInfo(id, null, "CLEANUP", null, 10, 0)
            .getLeft()
            .get(0)
            .getProcessId());
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

  private static class Persistence extends DerbyPersistence {
    void clear() {
      super.after();
    }
  }
}
