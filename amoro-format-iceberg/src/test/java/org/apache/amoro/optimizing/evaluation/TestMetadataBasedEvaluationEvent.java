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

package org.apache.amoro.optimizing.evaluation;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.io.writer.RecordWithAction;
import org.apache.amoro.optimizing.plan.CommonPartitionEvaluator;
import org.apache.amoro.optimizing.plan.MixedIcebergPartitionPlan;
import org.apache.amoro.optimizing.plan.PartitionEvaluator;
import org.apache.amoro.optimizing.scan.IcebergTableFileScanHelper;
import org.apache.amoro.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.optimizing.scan.UnkeyedTableFileScanHelper;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMetadataBasedEvaluationEvent extends TableTestBase {

  public static final Schema TABLE_SCHEMA =
      new Schema(
          Lists.newArrayList(
              Types.NestedField.required(1, "id", Types.IntegerType.get()),
              Types.NestedField.required(2, "name", Types.StringType.get()),
              Types.NestedField.required(3, "ts", Types.LongType.get()),
              Types.NestedField.required(4, "op_time", Types.TimestampType.withoutZone())),
          Sets.newHashSet(1, 2, 3, 4));
  public static final PartitionSpec SPEC =
      PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build();

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(
            new BasicCatalogTestHelper(TableFormat.ICEBERG),
            new BasicTableTestHelper(TABLE_SCHEMA, true, SPEC)),
        Arguments.of(
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)));
  }

  public void initData() throws IOException {
    if (getMixedTable().isKeyedTable()) {
      writeBaseStore(getMixedTable().asKeyedTable(), initRecords(1, 4, 0, "2022-01-01T12:00:00"));
      writeBaseStore(getMixedTable().asKeyedTable(), initRecords(5, 8, 0, "2022-01-01T12:00:00"));

    } else {
      write(getMixedTable().asUnkeyedTable(), initRecords(1, "aaa", 0, 1, ChangeAction.INSERT));
      write(getMixedTable().asUnkeyedTable(), initRecords(2, "bbb", 0, 1, ChangeAction.INSERT));
    }
  }

  private List<RecordWithAction> initRecords(
      int id, String name, long ts, int day, ChangeAction action) {
    ImmutableList.Builder<RecordWithAction> builder = ImmutableList.builder();
    builder.add(
        new RecordWithAction(
            MixedDataTestHelpers.createRecord(
                id, name, ts, String.format("2022-01-%02dT12:00:00", day)),
            action));

    return builder.build();
  }

  private List<Record> initRecords(int from, int to, long ts, String opTime) {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = from; i <= to; i++) {
      builder.add(tableTestHelper().generateTestRecord(i, i + "", ts, opTime));
    }

    return builder.build();
  }

  private void write(UnkeyedTable table, List<RecordWithAction> list) throws IOException {
    WriteResult result = IcebergDataTestHelpers.delta(table, list);

    RowDelta rowDelta = table.newRowDelta();
    Arrays.stream(result.dataFiles()).forEach(rowDelta::addRows);
    Arrays.stream(result.deleteFiles()).forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  private void writeBaseStore(KeyedTable keyedTable, List<Record> records) {
    List<DataFile> baseFiles =
        tableTestHelper()
            .writeBaseStore(keyedTable, keyedTable.beginTransaction(""), records, false);
    AppendFiles baseAppend = keyedTable.baseTable().newAppend();
    baseFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
  }

  private void writeChangeStore(KeyedTable keyedTable, List<DataFile> dataFiles) {
    AppendFiles appendFiles = keyedTable.changeTable().newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  public void addChangeStoreData() throws IOException {
    MixedTable mixedTable = getMixedTable();
    if (mixedTable.isUnkeyedTable()) {
      write(mixedTable.asUnkeyedTable(), initRecords(1, "aaa", 0, 1, ChangeAction.DELETE));
      write(mixedTable.asUnkeyedTable(), initRecords(2, "ccc", 0, 1, ChangeAction.UPDATE_AFTER));
    } else {
      writeChangeStore(
          mixedTable.asKeyedTable(),
          tableTestHelper()
              .writeChangeStore(
                  mixedTable.asKeyedTable(),
                  mixedTable.asKeyedTable().beginTransaction(""),
                  ChangeAction.INSERT,
                  initRecords(1, 2, 0, "2022-01-01T12:00:00"),
                  false));
      writeChangeStore(
          mixedTable.asKeyedTable(),
          tableTestHelper()
              .writeChangeStore(
                  mixedTable.asKeyedTable(),
                  mixedTable.asKeyedTable().beginTransaction(""),
                  ChangeAction.INSERT,
                  initRecords(3, 8, 0, "2022-01-01T12:00:00"),
                  false));
      writeChangeStore(
          mixedTable.asKeyedTable(),
          tableTestHelper()
              .writeChangeStore(
                  mixedTable.asKeyedTable(),
                  mixedTable.asKeyedTable().beginTransaction(""),
                  ChangeAction.DELETE,
                  initRecords(1, 1, 0, "2022-01-01T12:00:00"),
                  false));
      writeChangeStore(
          mixedTable.asKeyedTable(),
          tableTestHelper()
              .writeChangeStore(
                  mixedTable.asKeyedTable(),
                  mixedTable.asKeyedTable().beginTransaction(""),
                  ChangeAction.DELETE,
                  initRecords(2, 2, 0, "2022-01-01T12:00:00"),
                  false));
      writeChangeStore(
          mixedTable.asKeyedTable(),
          tableTestHelper()
              .writeChangeStore(
                  mixedTable.asKeyedTable(),
                  mixedTable.asKeyedTable().beginTransaction(""),
                  ChangeAction.DELETE,
                  initRecords(3, 4, 0, "2022-01-01T12:00:00"),
                  false));
    }
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void test_evaluating_metadataBasedTriggerEnabled(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    OptimizingConfig config = getDefaultOptimizingConfig();
    Assertions.assertFalse(config.isMetadataBasedTriggerEnabled());

    config.setEvaluationFallbackInterval(Long.MAX_VALUE);
    Assertions.assertTrue(config.isMetadataBasedTriggerEnabled());
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void test_evaluating_emptyTable(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    // Temporarily set self-optimizing.evaluation.fallback-interval to Long.MAX_VALUE to prevent
    // triggering due to reaching the fallback interval.
    OptimizingConfig config =
        getDefaultOptimizingConfig().setEvaluationFallbackInterval(Long.MAX_VALUE);
    MixedTable table = getMixedTable();

    // Verify the empty table stats
    TableStatsProvider.BasicFileStats stats =
        MixedAndIcebergTableStatsProvider.INSTANCE.collect(table);
    Assertions.assertEquals(0, stats.dataFileCnt);
    Assertions.assertEquals(0, stats.totalFileSize);
    Assertions.assertEquals(0, stats.deleteFileCnt);

    // Verify the empty table evaluation
    Assertions.assertTrue(config.isMetadataBasedTriggerEnabled());
    Assertions.assertFalse(MetadataBasedEvaluationEvent.isReachFallbackInterval(config, 0L));
    Assertions.assertFalse(MetadataBasedEvaluationEvent.isEvaluatingNecessary(config, table, 0L));
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void test_evaluating_nonEmptyTable(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    initData();
    OptimizingConfig config = getDefaultOptimizingConfig();
    MixedTable table = getMixedTable();

    // Verify the nonEmpty table stats
    TableStatsProvider.BasicFileStats stats =
        MixedAndIcebergTableStatsProvider.INSTANCE.collect(table);
    Assertions.assertTrue(stats.dataFileCnt > 0);
    Assertions.assertTrue(stats.totalFileSize > 0);
    Assertions.assertEquals(0, stats.deleteFileCnt);

    // 1. Test for metadata-based trigger disabled.
    config.setEvaluationFallbackInterval(-1);
    Assertions.assertFalse(config.isMetadataBasedTriggerEnabled());

    // 2. Test for metadata-based trigger enabled.
    // Temporarily set self-optimizing.evaluation.fallback-interval to Long.MAX_VALUE to prevent
    // triggering due to reaching the fallback interval.
    config.setEvaluationFallbackInterval(Long.MAX_VALUE);
    Assertions.assertTrue(config.isMetadataBasedTriggerEnabled());
    Assertions.assertFalse(MetadataBasedEvaluationEvent.isReachFallbackInterval(config, 0L));

    // 2.1 Test for evaluating pendingInput necessary.
    config.setTargetSize(134217728);
    Assertions.assertTrue(MetadataBasedEvaluationEvent.isEvaluatingNecessary(config, table, 0L));

    // 2.2 Test for evaluating pendingInput not necessary
    // Temporarily set self-optimizing.target-size to a lower value than the average file size
    config.setTargetSize(100);
    Assertions.assertFalse(MetadataBasedEvaluationEvent.isEvaluatingNecessary(config, table, 0L));

    // 2.3 Test for evaluating pendingInput necessary because the fallback interval has been
    // reached.
    config.setEvaluationFallbackInterval(0L);
    Assertions.assertTrue(config.isMetadataBasedTriggerEnabled());
    Assertions.assertTrue(MetadataBasedEvaluationEvent.isEvaluatingNecessary(config, table, 0L));
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void test_evaluating_pendingInput_nonEmptyTable(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    initData();
    // Set metadata-based trigger enabled and fallback interval not reached.
    OptimizingConfig config =
        getDefaultOptimizingConfig().setEvaluationFallbackInterval(Long.MAX_VALUE);
    MixedTable table = getMixedTable();

    // 1. Test file size square error sum updates during partition plans initialization using
    // default mse tolerance (=0) , expecting no updates.
    TableFileScanHelper tableFileScanHelper = initTableFileScanHelper();
    Map<String, PartitionEvaluator> partitionPlanMap =
        initPartitionPlans(tableFileScanHelper, config);

    Assertions.assertEquals(1, partitionPlanMap.size());

    long sizeSquaredErrorSum =
        ((CommonPartitionEvaluator) new ArrayList<>(partitionPlanMap.values()).get(0))
            .getFileSizeSquaredErrorSum();
    Assertions.assertEquals(0L, sizeSquaredErrorSum);

    List<PartitionEvaluator> necessaryPartitions =
        partitionPlanMap.values().stream()
            .filter(PartitionEvaluator::isNecessary)
            .collect(Collectors.toList());
    Assertions.assertEquals(1, necessaryPartitions.size());

    // 2. Set mse tolerance > 0 to enabled file size square error sum update during initializing
    // Partition plans.
    config.setEvaluationMseTolerance(120000000);
    partitionPlanMap = initPartitionPlans(tableFileScanHelper, config);
    Assertions.assertEquals(1, partitionPlanMap.size());

    sizeSquaredErrorSum =
        ((CommonPartitionEvaluator) new ArrayList<>(partitionPlanMap.values()).get(0))
            .getFileSizeSquaredErrorSum();
    Assertions.assertTrue(sizeSquaredErrorSum > 0);

    necessaryPartitions =
        partitionPlanMap.values().stream()
            .filter(PartitionEvaluator::isNecessary)
            .collect(Collectors.toList());
    Assertions.assertEquals(0, necessaryPartitions.size());

    // 3. Test the file size variance updated after adding change data.
    addChangeStoreData();
    partitionPlanMap = initPartitionPlans(initTableFileScanHelper(), config);
    Assertions.assertEquals(1, partitionPlanMap.size());

    long sizeSquaredErrorSumUpdated1 =
        ((CommonPartitionEvaluator) new ArrayList<>(partitionPlanMap.values()).get(0))
            .getFileSizeSquaredErrorSum();
    Assertions.assertNotEquals(sizeSquaredErrorSum, sizeSquaredErrorSumUpdated1);

    necessaryPartitions =
        partitionPlanMap.values().stream()
            .filter(PartitionEvaluator::isNecessary)
            .collect(Collectors.toList());
    Assertions.assertEquals(0, necessaryPartitions.size());

    // 4. Set mse tolerance smaller for partitions to test necessary pending.
    config.setEvaluationMseTolerance(100000000);
    partitionPlanMap = initPartitionPlans(initTableFileScanHelper(), config);

    long sizeSquaredErrorSumUpdated2 =
        ((CommonPartitionEvaluator) new ArrayList<>(partitionPlanMap.values()).get(0))
            .getFileSizeSquaredErrorSum();
    Assertions.assertEquals(sizeSquaredErrorSumUpdated2, sizeSquaredErrorSumUpdated1);

    necessaryPartitions =
        partitionPlanMap.values().stream()
            .filter(PartitionEvaluator::isNecessary)
            .collect(Collectors.toList());
    Assertions.assertEquals(1, necessaryPartitions.size());
  }

  private TableFileScanHelper initTableFileScanHelper() {
    MixedTable mixedTable = getMixedTable();
    TableFileScanHelper tableFileScanHelper;
    if (TableFormat.ICEBERG.equals(mixedTable.format())) {
      tableFileScanHelper =
          new IcebergTableFileScanHelper(
              mixedTable.asUnkeyedTable(),
              mixedTable.asUnkeyedTable().currentSnapshot().snapshotId());
    } else {
      if (mixedTable.isUnkeyedTable()) {
        tableFileScanHelper =
            new UnkeyedTableFileScanHelper(
                mixedTable.asUnkeyedTable(),
                mixedTable.asUnkeyedTable().currentSnapshot().snapshotId());
      } else {
        Snapshot currentSnapshot = mixedTable.asKeyedTable().baseTable().currentSnapshot();
        Snapshot changeSnapshot = mixedTable.asKeyedTable().changeTable().currentSnapshot();

        tableFileScanHelper =
            new KeyedTableFileScanHelper(
                mixedTable.asKeyedTable(),
                new KeyedTableSnapshot(
                    currentSnapshot != null
                        ? currentSnapshot.snapshotId()
                        : Constants.INVALID_SNAPSHOT_ID,
                    changeSnapshot != null
                        ? changeSnapshot.snapshotId()
                        : Constants.INVALID_SNAPSHOT_ID));
      }
    }
    tableFileScanHelper.withPartitionFilter(Expressions.alwaysTrue());

    return tableFileScanHelper;
  }

  private Map<String, PartitionEvaluator> initPartitionPlans(
      TableFileScanHelper tableFileScanHelper, OptimizingConfig config) {
    Map<String, PartitionEvaluator> partitionPlanMap = Maps.newHashMap();

    long count = 0;
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      for (TableFileScanHelper.FileScanResult fileScanResult : results) {
        PartitionSpec partitionSpec = tableFileScanHelper.getSpec(fileScanResult.file().specId());
        StructLike partition = fileScanResult.file().partition();
        String partitionPath = partitionSpec.partitionToPath(partition);
        PartitionEvaluator evaluator =
            partitionPlanMap.computeIfAbsent(
                partitionPath,
                ignore -> buildEvaluator(Pair.of(partitionSpec.specId(), partition), config));
        evaluator.addFile(fileScanResult.file(), fileScanResult.deleteFiles());
        count++;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return partitionPlanMap;
  }

  private PartitionEvaluator buildEvaluator(
      Pair<Integer, StructLike> partition, OptimizingConfig config) {
    if (getMixedTable().isUnkeyedTable()) {
      return new CommonPartitionEvaluator(
          ServerTableIdentifier.of(TableTestHelper.TEST_TABLE_ID, TableFormat.ICEBERG),
          config,
          partition,
          System.currentTimeMillis(),
          0L,
          0L,
          0L);
    } else {
      Map<String, String> partitionProperties =
          TablePropertyUtil.getPartitionProperties(getMixedTable(), partition.second());
      return new MixedIcebergPartitionPlan.MixedIcebergPartitionEvaluator(
          ServerTableIdentifier.of(TableTestHelper.TEST_TABLE_ID, TableFormat.MIXED_ICEBERG),
          config,
          partition,
          partitionProperties,
          System.currentTimeMillis(),
          getMixedTable().isKeyedTable(),
          0L,
          0L,
          0L);
    }
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void test_setFileSizeMSETolerance(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    OptimizingConfig config = new OptimizingConfig();
    Assertions.assertEquals(0, config.getEvaluationMseTolerance());

    config.setEvaluationMseTolerance(1000);
    Assertions.assertEquals(1000, config.getEvaluationMseTolerance());

    config.setEvaluationMseTolerance(140000000);
    Assertions.assertEquals(140000000, config.getEvaluationMseTolerance());
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testBasicStatsAccept(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    MixedAndIcebergTableStatsProvider.BasicFileStats stats =
        new MixedAndIcebergTableStatsProvider.BasicFileStats();
    // Initial state should be zeros
    Assertions.assertEquals(0, stats.deleteFileCnt);
    Assertions.assertEquals(0, stats.dataFileCnt);
    Assertions.assertEquals(0, stats.totalFileSize);
    // Create first summary map
    Map<String, String> summary1 = new HashMap<>();
    summary1.put("total-delete-files", "5");
    summary1.put("total-data-files", "10");
    summary1.put("total-files-size", "1024");
    stats.accept(summary1);
    Assertions.assertEquals(5, stats.deleteFileCnt);
    Assertions.assertEquals(10, stats.dataFileCnt);
    Assertions.assertEquals(1024, stats.totalFileSize);
    // Create second summary map to test accumulation
    Map<String, String> summary2 = new HashMap<>();
    summary2.put("total-delete-files", "3");
    summary2.put("total-data-files", "7");
    summary2.put("total-files-size", "2048");
    stats.accept(summary2);
    // Values should be accumulated
    Assertions.assertEquals(8, stats.deleteFileCnt); // 5 + 3
    Assertions.assertEquals(17, stats.dataFileCnt); // 10 + 7
    Assertions.assertEquals(3072, stats.totalFileSize); // 1024 + 2048
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testBasicTableStatsAcceptWithMissingProperties(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    MixedAndIcebergTableStatsProvider.BasicFileStats stats =
        new MixedAndIcebergTableStatsProvider.BasicFileStats();
    // Summary map with missing properties should use default values
    Map<String, String> summary = new HashMap<>();
    // Only provide one property, others should default to 0
    summary.put("total-data-files", "15");
    stats.accept(summary);
    Assertions.assertEquals(0, stats.deleteFileCnt); // default value
    Assertions.assertEquals(15, stats.dataFileCnt);
    Assertions.assertEquals(0, stats.totalFileSize); // default value
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testBasicTableStatsAcceptWithInvalidValues(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
    MixedAndIcebergTableStatsProvider.BasicFileStats stats =
        new MixedAndIcebergTableStatsProvider.BasicFileStats();
    // Summary map with invalid numeric values should use default values
    Map<String, String> summary = new HashMap<>();
    summary.put("total-delete-files", "invalid");
    summary.put("total-data-files", "20");
    summary.put("total-files-size", "not-a-number");

    // Invalid values should throw NumberFormatException
    Assertions.assertThrows(NumberFormatException.class, () -> stats.accept(summary));
  }

  OptimizingConfig getDefaultOptimizingConfig() {
    return new OptimizingConfig()
        .setEnabled(TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT)
        .setAllowPartialCommit(TableProperties.SELF_OPTIMIZING_ALLOW_PARTIAL_COMMIT_DEFAULT)
        .setMaxExecuteRetryCount(TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT)
        .setOptimizerGroup(TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT)
        .setFragmentRatio(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT)
        .setMinTargetSizeRatio(TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT)
        .setMaxFileCount(TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT)
        .setOpenFileCost(TableProperties.SPLIT_OPEN_FILE_COST_DEFAULT)
        .setTargetSize(TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT)
        .setMaxTaskSize(TableProperties.SELF_OPTIMIZING_MAX_TASK_SIZE_DEFAULT)
        .setTargetQuota(TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT)
        .setMinorLeastFileCount(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT_DEFAULT)
        .setMinorLeastInterval(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL_DEFAULT)
        .setMajorDuplicateRatio(
            TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO_DEFAULT)
        .setFullTriggerInterval(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL_DEFAULT)
        .setFullRewriteAllFiles(TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES_DEFAULT)
        .setFilter(TableProperties.SELF_OPTIMIZING_FILTER_DEFAULT)
        .setBaseHashBucket(TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT)
        .setBaseRefreshInterval(TableProperties.BASE_REFRESH_INTERVAL_DEFAULT)
        .setHiveRefreshInterval(HiveTableProperties.REFRESH_HIVE_INTERVAL_DEFAULT)
        .setMinPlanInterval(TableProperties.SELF_OPTIMIZING_MIN_PLAN_INTERVAL_DEFAULT)
        .setEvaluationFallbackInterval(
            TableProperties.SELF_OPTIMIZING_EVALUATION_FALLBACK_INTERVAL_DEFAULT)
        .setEvaluationMseTolerance(
            TableProperties.SELF_OPTIMIZING_EVALUATION_FILE_SIZE_MSE_TOLERANCE_DEFAULT);
  }
}
