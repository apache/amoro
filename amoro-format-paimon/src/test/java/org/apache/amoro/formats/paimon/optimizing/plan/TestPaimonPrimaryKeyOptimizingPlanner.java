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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyOptions;
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
import org.apache.paimon.table.BucketMode;
import org.apache.paimon.table.FileStoreTable;
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

@DisplayName("PaimonPrimaryKeyOptimizingPlanner")
class TestPaimonPrimaryKeyOptimizingPlanner {

  @Test
  @DisplayName("HASH_FIXED MINOR uses effective minor threshold")
  void hashFixedMinorUsesEffectiveMinorThreshold(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_fixed_minor", options);
    writeCommits(catalog.getTable(id), 3);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig().setMinorLeastFileCount(99),
                runtimeOptions("num-sorted-run.compaction-trigger", "3"))
            .plan();

    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
    assertTaskMetadata(result);
    for (PaimonPrimaryKeyCompactionTask task : result.getTasks()) {
      assertEquals(OptimizingType.MINOR, task.getInput().getOptimizingType());
      assertFalse(task.getInput().isFullCompaction());
      assertEquals(
          PaimonPrimaryKeyCompactionExecutorFactory.class.getName(),
          task.getProperties().get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL));
    }
  }

  @Test
  @DisplayName("HASH_FIXED MAJOR overrides MINOR")
  void hashFixedMajorOverridesMinor(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_fixed_major", options);
    writeCommits(catalog.getTable(id), 3);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig(),
                runtimeOptions(
                    "num-sorted-run.compaction-trigger",
                    "2",
                    PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD,
                    "3"))
            .plan();

    assertEquals(OptimizingType.MAJOR, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
    for (PaimonPrimaryKeyCompactionTask task : result.getTasks()) {
      assertEquals(OptimizingType.MAJOR, task.getInput().getOptimizingType());
      assertTrue(task.getInput().isFullCompaction());
    }
  }

  @Test
  @DisplayName("HASH_DYNAMIC uses the same planner logic")
  void hashDynamicUsesSamePlannerLogic(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "-1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_dynamic_minor", options);
    writeCommits(catalog.getTable(id), 2, 0);
    assertEquals(BucketMode.HASH_DYNAMIC, ((FileStoreTable) catalog.getTable(id)).bucketMode());

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig().setMinorLeastFileCount(99),
                runtimeOptions("num-sorted-run.compaction-trigger", "2"))
            .plan();

    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
    assertFalse(result.getTasks().get(0).getInput().isFullCompaction());
  }

  @Test
  @DisplayName("non-empty filter returns empty plan")
  void nonEmptyFilterReturnsEmptyPlan(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_filter", options);
    writeCommits(catalog.getTable(id), 2);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(catalog, id, defaultConfig().setFilter("id = 1")).plan();

    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("FULL without partition idle time returns empty plan")
  void fullWithoutPartitionIdleTimeReturnsEmptyPlan(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    Identifier id = createPrimaryKeyTable(catalog, "t_full_no_idle", options);
    writeCommits(catalog.getTable(id), 1);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(catalog, id, defaultConfig().setMinorLeastFileCount(99).setFullTriggerInterval(1))
            .plan();

    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("FULL with PT0S idle time plans idle buckets")
  void fullWithZeroIdleTimePlansIdleBuckets(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put(PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "PT0S");
    Identifier id = createPrimaryKeyTable(catalog, "t_full_idle", options);
    writeCommits(catalog.getTable(id), 1);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(catalog, id, defaultConfig().setMinorLeastFileCount(99).setFullTriggerInterval(1))
            .plan();

    assertEquals(OptimizingType.FULL, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
    assertTaskMetadata(result);
    for (PaimonPrimaryKeyCompactionTask task : result.getTasks()) {
      assertEquals(OptimizingType.FULL, task.getInput().getOptimizingType());
      assertTrue(task.getInput().isFullCompaction());
    }
  }

  @Test
  @DisplayName("MAX_BUCKETS_PER_TASK=1 puts at most one unit in each task")
  void maxBucketsPerTaskOnePacksOneUnitPerTask(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    options.put(PaimonPrimaryKeyOptions.MAX_BUCKETS_PER_TASK, "1");
    Identifier id = createPartitionedPrimaryKeyTable(catalog, "t_pack_one", options);
    writePartitionCommits(catalog.getTable(id), "p1", 2);
    writePartitionCommits(catalog.getTable(id), "p2", 2);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig(),
                runtimeOptions("num-sorted-run.compaction-trigger", "2"))
            .plan();

    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    assertTrue(result.getTasks().size() >= 2);
    for (PaimonPrimaryKeyCompactionTask task : result.getTasks()) {
      assertEquals(1, task.getInput().getUnits().size());
    }
  }

  @Test
  @DisplayName("partition idle time filters MINOR candidates")
  void partitionIdleTimeFiltersMinorCandidates(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    options.put(PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "PT999999H");
    Identifier id = createPartitionedPrimaryKeyTable(catalog, "t_idle_minor", options);
    writePartitionCommits(catalog.getTable(id), "p1", 2);
    writePartitionCommits(catalog.getTable(id), "p2", 2);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig(),
                runtimeOptions("num-sorted-run.compaction-trigger", "2"))
            .plan();

    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("partition idle time filters MAJOR candidates")
  void partitionIdleTimeFiltersMajorCandidates(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    options.put(PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "PT999999H");
    Identifier id = createPartitionedPrimaryKeyTable(catalog, "t_idle_major", options);
    writePartitionCommits(catalog.getTable(id), "p1", 3);
    writePartitionCommits(catalog.getTable(id), "p2", 3);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig(),
                runtimeOptions(
                    "num-sorted-run.compaction-trigger",
                    "2",
                    PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD,
                    "3"))
            .plan();

    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("minor interval throttles MINOR planning")
  void minorIntervalThrottlesMinorPlanning(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_minor_interval", options);
    writeCommits(catalog.getTable(id), 2);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig().setMinorLeastInterval(Integer.MAX_VALUE).setFullTriggerInterval(1),
                runtimeOptions("num-sorted-run.compaction-trigger", "2"),
                System.currentTimeMillis(),
                0L)
            .plan();

    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("MAJOR ignores minor interval under high pressure")
  void majorIgnoresMinorIntervalWhenHighPressure(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_major_interval", options);
    writeCommits(catalog.getTable(id), 3);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig().setMinorLeastInterval(Integer.MAX_VALUE),
                runtimeOptions(
                    "num-sorted-run.compaction-trigger",
                    "2",
                    PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD,
                    "3"),
                System.currentTimeMillis(),
                0L)
            .plan();

    assertEquals(OptimizingType.MAJOR, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("private major threshold smaller than minor returns empty plan")
  void privateMajorThresholdSmallerThanMinorReturnsEmptyPlan(@TempDir Path warehouse)
      throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("bucket", "1");
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_bad_major_threshold", options);
    writeCommits(catalog.getTable(id), 3);

    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                defaultConfig(),
                runtimeOptions(
                    "num-sorted-run.compaction-trigger",
                    "3",
                    PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD,
                    "2"))
            .plan();

    assertTrue(result.getTasks().isEmpty());
  }

  private static PaimonPrimaryKeyOptimizingPlanner planner(
      Catalog catalog, Identifier id, OptimizingConfig config) throws Exception {
    return planner(catalog, id, config, Collections.emptyMap());
  }

  private static PaimonPrimaryKeyOptimizingPlanner planner(
      Catalog catalog, Identifier id, OptimizingConfig config, Map<String, String> runtimeOptions)
      throws Exception {
    return planner(catalog, id, config, runtimeOptions, 0L, 0L);
  }

  private static PaimonPrimaryKeyOptimizingPlanner planner(
      Catalog catalog,
      Identifier id,
      OptimizingConfig config,
      Map<String, String> runtimeOptions,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime)
      throws Exception {
    PaimonTable table = wrap(catalog.getTable(id).copy(runtimeOptions), id.getObjectName());
    return new PaimonPrimaryKeyOptimizingPlanner(
        table,
        100L,
        7L,
        4.0,
        64L * 1024 * 1024,
        config,
        lastMinorOptimizingTime,
        0L,
        lastFullOptimizingTime,
        null);
  }

  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  private static Identifier createPrimaryKeyTable(
      Catalog catalog, String tableName, Map<String, String> options) throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .primaryKey("id");
    options.forEach(builder::option);
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return id;
  }

  private static Identifier createPartitionedPrimaryKeyTable(
      Catalog catalog, String tableName, Map<String, String> options) throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .column("dt", DataTypes.STRING())
            .partitionKeys("dt")
            .primaryKey("dt", "id");
    options.forEach(builder::option);
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return id;
  }

  private static void writeCommits(Table table, int count) throws Exception {
    writeCommits(table, count, null);
  }

  private static void writeCommits(Table table, int count, Integer bucket) throws Exception {
    for (int i = 0; i < count; i++) {
      writeRecords(
          table,
          Collections.singletonList(GenericRow.of(i, BinaryString.fromString("name-" + i))),
          bucket);
    }
  }

  private static void writePartitionCommits(Table table, String partition, int count)
      throws Exception {
    for (int i = 0; i < count; i++) {
      writeRecords(
          table,
          Collections.singletonList(
              GenericRow.of(
                  i,
                  BinaryString.fromString(partition + "-" + i),
                  BinaryString.fromString(partition))),
          null);
    }
  }

  private static void writeRecords(Table table, List<GenericRow> rowsInOneCommit) throws Exception {
    writeRecords(table, rowsInOneCommit, null);
  }

  private static void writeRecords(Table table, List<GenericRow> rowsInOneCommit, Integer bucket)
      throws Exception {
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite()) {
      for (GenericRow row : rowsInOneCommit) {
        if (bucket == null) {
          write.write(row);
        } else {
          write.write(row, bucket);
        }
      }
      List<CommitMessage> messages = write.prepareCommit();
      try (BatchTableCommit commit = builder.newCommit()) {
        commit.commit(messages);
      }
    }
  }

  private static Map<String, String> primaryKeyOptions() {
    Map<String, String> options = new HashMap<>();
    options.put(PaimonPrimaryKeyOptions.ENABLED, "true");
    return options;
  }

  private static Map<String, String> runtimeOptions(String... keyValues) {
    Map<String, String> options = new HashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      options.put(keyValues[i], keyValues[i + 1]);
    }
    return options;
  }

  private static PaimonTable wrap(Table table, String name) {
    return new PaimonTable(TableIdentifier.of("test_catalog", "db1", name), table);
  }

  private static OptimizingConfig defaultConfig() {
    return new OptimizingConfig()
        .setEnabled(true)
        .setMinorLeastFileCount(2)
        .setMinorLeastInterval(0)
        .setFullTriggerInterval(-1)
        .setFullRewriteAllFiles(false)
        .setMaxTaskSize(64L * 1024 * 1024);
  }

  private static void assertTaskMetadata(
      OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result) {
    assertEquals(7L, result.getProcessId());
    assertTrue(result.getPlanTime() > 0);
    assertTrue(result.getTargetSnapshotId() > 0);
    assertEquals(-1L, result.getTargetChangeSnapshotId());
    for (PaimonPrimaryKeyCompactionTask task : result.getTasks()) {
      assertNotNull(task.getInput().getCommitUser());
      assertEquals(7L, task.getInput().getCommitIdentifier());
      assertEquals(result.getTargetSnapshotId(), task.getInput().getTargetSnapshotId());
    }
  }
}
