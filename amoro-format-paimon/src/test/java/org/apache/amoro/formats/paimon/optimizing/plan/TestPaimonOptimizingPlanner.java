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

package org.apache.amoro.formats.paimon.optimizing.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DisplayName("PaimonOptimizingPlanner")
public class TestPaimonOptimizingPlanner {

  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  private static void writeRecords(Table table, List<GenericRow> rowsInOneCommit) throws Exception {
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite()) {
      for (GenericRow row : rowsInOneCommit) {
        write.write(row);
      }
      List<CommitMessage> messages = write.prepareCommit();
      try (BatchTableCommit commit = builder.newCommit()) {
        commit.commit(messages);
      }
    }
  }

  private static PaimonTable wrap(Table table, String name) {
    return new PaimonTable(TableIdentifier.of("test_catalog", "db1", name), table);
  }

  private static Table createAppendOnlyTable(
      Catalog catalog, String tableName, Map<String, String> extraOptions) throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "-1");
    for (Map.Entry<String, String> e : extraOptions.entrySet()) {
      builder.option(e.getKey(), e.getValue());
    }
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return catalog.getTable(id);
  }

  @Test
  @DisplayName("Append-only BUCKET_UNAWARE table with many small commits is picked up")
  void testUnawareTablePicksUpSmallFiles(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    // Force a tiny target-file-size so every commit produces a "small" file candidate.
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_small", opts);
    for (int i = 0; i < 5; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("name-" + i))));
    }
    PaimonTable paimonTable =
        wrap(catalog.getTable(Identifier.create("db1", "t_small")), "t_small");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 100L /* tableId */, 1L /* processId */, 1.0, 1024);
    assertTrue(planner.isNecessary(), "Small-file-heavy table should need compaction");

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertNotNull(result);
    assertFalse(result.getTasks().isEmpty(), "plan() must produce at least one task");
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    for (PaimonCompactionTask t : result.getTasks()) {
      assertEquals(100L, t.getTableId());
      assertNotNull(t.getInput());
      assertNotNull(t.getInput().getTaskBytes());
      assertTrue(t.getInput().getTaskBytes().length > 0);
      assertEquals(
          PaimonCompactionExecutorFactory.class.getName(),
          t.getProperties().get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL));
    }
  }

  @Test
  @DisplayName("Primary-key (HASH_FIXED) table is skipped without throwing")
  void testPrimaryKeyTableSkipped(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    catalog.createDatabase("db1", true);
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .primaryKey("id")
            .option("bucket", "2")
            .build();
    Identifier id = Identifier.create("db1", "t_pk");
    catalog.createTable(id, schema, true);
    PaimonTable paimonTable = wrap(catalog.getTable(id), "t_pk");

    PaimonOptimizingPlanner planner = new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 1024);
    assertFalse(planner.isNecessary());
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertNotNull(result);
    assertTrue(result.getTasks().isEmpty());
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    assertNull(planner.getCommitUser());
  }

  @Test
  @DisplayName("Empty append table (no snapshot) is skipped")
  void testEmptyTableSkipped(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createAppendOnlyTable(catalog, "t_empty", new HashMap<>());
    PaimonTable paimonTable = wrap(table, "t_empty");

    PaimonOptimizingPlanner planner = new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 1024);
    assertFalse(planner.isNecessary());
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("Plan metadata (processId/planTime/targetSnapshotId/commitUser) is populated")
  void testPlanResultMetadata(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_meta", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    PaimonTable paimonTable = wrap(catalog.getTable(Identifier.create("db1", "t_meta")), "t_meta");

    long before = System.currentTimeMillis();
    PaimonOptimizingPlanner planner = new PaimonOptimizingPlanner(paimonTable, 77L, 9L, 2.0, 4096);
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    long after = System.currentTimeMillis();

    assertEquals(9L, result.getProcessId());
    assertEquals(9L, planner.getProcessId());
    assertTrue(result.getPlanTime() >= before && result.getPlanTime() <= after);
    assertTrue(result.getTargetSnapshotId() >= 1L);
    assertEquals(-1L, result.getTargetChangeSnapshotId());
    assertNotNull(planner.getCommitUser());
    assertFalse(planner.getCommitUser().isEmpty());
  }

  @Test
  @DisplayName("commitUser stays stable across repeated plan() calls for the same Planner instance")
  void testCommitUserStable(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_user", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    PaimonTable paimonTable = wrap(catalog.getTable(Identifier.create("db1", "t_user")), "t_user");

    PaimonOptimizingPlanner planner = new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 1024);
    OptimizingPlanResult<PaimonCompactionTask> r1 = planner.plan();
    OptimizingPlanResult<PaimonCompactionTask> r2 = planner.plan();
    assertFalse(r1.getTasks().isEmpty());
    assertFalse(r2.getTasks().isEmpty());
    String commitUser = planner.getCommitUser();
    for (PaimonCompactionTask t : r1.getTasks()) {
      assertEquals(commitUser, t.getInput().getCommitUser());
    }
    for (PaimonCompactionTask t : r2.getTasks()) {
      assertEquals(commitUser, t.getInput().getCommitUser());
    }
  }
}
