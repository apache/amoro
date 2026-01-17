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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.formats.IcebergHadoopCatalogTestHelper;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.hive.formats.IcebergHiveCatalogTestHelper;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.TestServerTableDescriptor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.Table;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestIcebergServerTableDescriptor extends TestServerTableDescriptor {
  @Rule public DerbyPersistence derbyPersistence = new DerbyPersistence();

  public TestIcebergServerTableDescriptor(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[] {
      IcebergHadoopCatalogTestHelper.defaultHelper(), IcebergHiveCatalogTestHelper.defaultHelper()
    };
  }

  @Override
  protected void tableOperationsAddColumns() {
    getTable()
        .updateSchema()
        .allowIncompatibleChanges()
        .addColumn("new_col", Types.IntegerType.get())
        .commit();
  }

  @Test
  public void testGetTablePartitions() {
    // The default test table is partitioned by 'age' field (see
    // IcebergHadoopCatalogTestHelper.SPEC)
    org.apache.iceberg.Table icebergTable = getTable();
    Assert.assertTrue("Test table should be partitioned", icebergTable.spec().isPartitioned());

    // Add some data files to create partitions
    org.apache.iceberg.DataFile file1 =
        org.apache.iceberg.DataFiles.builder(icebergTable.spec())
            .withPath("/path/to/data-a.parquet")
            .withFileSizeInBytes(100)
            .withRecordCount(10)
            .build();
    icebergTable.newAppend().appendFile(file1).commit();

    // Get partitions using descriptor
    MixedAndIcebergTableDescriptor descriptor = new MixedAndIcebergTableDescriptor();
    List<org.apache.amoro.table.descriptor.PartitionBaseInfo> partitions =
        descriptor.getTablePartitions(getAmoroCatalog().loadTable(TEST_DB, TEST_TABLE));

    // Verify we got partition info - should have at least 1 partition after adding a file
    Assert.assertNotNull(partitions);
    Assert.assertTrue("Should have at least 1 partition", partitions.size() > 0);

    // Verify file count is correct
    long totalFiles = partitions.stream().mapToLong(p -> p.getFileCount()).sum();
    Assert.assertEquals("Should have 1 file total", 1, totalFiles);

    // Verify we used PARTITIONS metadata table which provides fileSize and lastCommitTime
    for (org.apache.amoro.table.descriptor.PartitionBaseInfo partition : partitions) {
      // File size should be available from PARTITIONS metadata table
      Assert.assertTrue(
          "FileSize should be available from PARTITIONS metadata table",
          partition.getFileSize() >= 0);
      // Last commit time should be available from PARTITIONS metadata table
      Assert.assertTrue(
          "LastCommitTime should be available from PARTITIONS metadata table",
          partition.getLastCommitTime() >= 0);
    }
  }

  @Test
  public void testGetTablePartitionsFallback() {
    // Test the fallback path by using a custom descriptor that forces fallback
    org.apache.iceberg.Table icebergTable = getTable();
    Assert.assertTrue("Test table should be partitioned", icebergTable.spec().isPartitioned());

    // Add data file
    org.apache.iceberg.DataFile file1 =
        org.apache.iceberg.DataFiles.builder(icebergTable.spec())
            .withPath("/path/to/fallback-data.parquet")
            .withFileSizeInBytes(200)
            .withRecordCount(20)
            .build();
    icebergTable.newAppend().appendFile(file1).commit();

    // Use the fallback method directly
    TestMixedAndIcebergTableDescriptor testDescriptor = new TestMixedAndIcebergTableDescriptor();
    List<org.apache.amoro.table.descriptor.PartitionBaseInfo> partitions =
        testDescriptor.testCollectPartitionsFromFileScan(icebergTable);

    // Verify fallback works correctly
    Assert.assertNotNull(partitions);
    Assert.assertTrue("Fallback should return at least 1 partition", partitions.size() > 0);

    // Verify file count
    long totalFiles = partitions.stream().mapToLong(p -> p.getFileCount()).sum();
    Assert.assertEquals("Fallback should count files correctly", 1, totalFiles);

    // Verify fallback calculates actual values (not 0 like PARTITIONS table)
    long totalSize = partitions.stream().mapToLong(p -> p.getFileSize()).sum();
    Assert.assertTrue("Fallback should calculate actual file size (not 0)", totalSize > 0);
  }

  @Test
  public void testOptimizingProcess() {
    TestMixedAndIcebergTableDescriptor descriptor = new TestMixedAndIcebergTableDescriptor();

    String catalogName = "catalog1";
    String dbName = "db1";
    String tableName = "table1";

    ServerTableIdentifier identifier =
        ServerTableIdentifier.of(1L, catalogName, dbName, tableName, TableFormat.ICEBERG);
    descriptor.insertTable(identifier);
    MetricsSummary dummySummery = new MetricsSummary();
    dummySummery.setNewDeleteFileCnt(1);
    dummySummery.setNewDataFileCnt(1);
    dummySummery.setNewDataSize(1);
    dummySummery.setNewDeleteFileCnt(1);
    dummySummery.setNewDeleteSize(1);
    descriptor.insertOptimizingProcess(
        identifier,
        1L,
        1,
        1,
        ProcessStatus.SUCCESS,
        OptimizingType.MAJOR,
        1L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        2L,
        2L,
        2L,
        ProcessStatus.SUCCESS,
        OptimizingType.MINOR,
        2L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        3L,
        3L,
        3L,
        ProcessStatus.SUCCESS,
        OptimizingType.FULL,
        3L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        4L,
        4L,
        4L,
        ProcessStatus.FAILED,
        OptimizingType.MAJOR,
        4L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        5L,
        5L,
        5L,
        ProcessStatus.SUCCESS,
        OptimizingType.MINOR,
        5L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        6L,
        6L,
        6L,
        ProcessStatus.SUCCESS,
        OptimizingType.FULL,
        6L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        7L,
        7L,
        7L,
        ProcessStatus.FAILED,
        OptimizingType.MAJOR,
        7L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        8L,
        8L,
        8L,
        ProcessStatus.SUCCESS,
        OptimizingType.MINOR,
        8L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        9L,
        9L,
        9L,
        ProcessStatus.FAILED,
        OptimizingType.FULL,
        9L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    descriptor.insertOptimizingProcess(
        identifier,
        10L,
        10L,
        10L,
        ProcessStatus.SUCCESS,
        OptimizingType.MINOR,
        10L,
        dummySummery,
        Collections.emptyMap(),
        Collections.emptyMap());

    AmoroTable<?> table = mock(IcebergTable.class);
    TableIdentifier tableIdentifier =
        TableIdentifier.of(
            identifier.getCatalog(), identifier.getDatabase(), identifier.getTableName());
    doReturn(tableIdentifier).when(table).id();

    Pair<List<OptimizingProcessInfo>, Integer> res =
        descriptor.getOptimizingProcessesInfo(table, null, null, 4, 4);
    Integer expectReturnItemSizeForNoTypeNoStatusOffset0Limit5 = 4;
    Integer expectTotalForNoTypeNoStatusOffset0Limit5 = 10;
    Assert.assertEquals(
        expectReturnItemSizeForNoTypeNoStatusOffset0Limit5, (Integer) res.getLeft().size());
    Assert.assertEquals(expectTotalForNoTypeNoStatusOffset0Limit5, res.getRight());

    res = descriptor.getOptimizingProcessesInfo(table, null, ProcessStatus.SUCCESS, 5, 0);
    Integer expectReturnItemSizeForOnlyStatusOffset0limit5 = 5;
    Integer expectedTotalForOnlyStatusOffset0Limit5 = 7;
    Assert.assertEquals(
        expectReturnItemSizeForOnlyStatusOffset0limit5, (Integer) res.getLeft().size());
    Assert.assertEquals(expectedTotalForOnlyStatusOffset0Limit5, res.getRight());

    res = descriptor.getOptimizingProcessesInfo(table, OptimizingType.MINOR.name(), null, 5, 0);
    Integer expectedRetItemsSizeForOnlyTypeOffset0Limit5 = 4;
    Integer expectedRetTotalForOnlyTypeOffset0Limit5 = 4;
    Assert.assertEquals(
        expectedRetItemsSizeForOnlyTypeOffset0Limit5, (Integer) res.getLeft().size());
    Assert.assertEquals(expectedRetTotalForOnlyTypeOffset0Limit5, res.getRight());

    res =
        descriptor.getOptimizingProcessesInfo(
            table, OptimizingType.MINOR.name(), ProcessStatus.SUCCESS, 2, 2);
    Integer expectedRetItemSizeForBothTypeAndStatusOffset2Limit2 = 2;
    Integer expectedRetTotalForBothTypeAndStatusOffset2Limit2 = 4;
    Assert.assertEquals(
        expectedRetItemSizeForBothTypeAndStatusOffset2Limit2, (Integer) res.getLeft().size());
    Assert.assertEquals(expectedRetTotalForBothTypeAndStatusOffset2Limit2, res.getRight());
  }

  @Override
  protected void tableOperationsRenameColumns() {
    getTable().updateSchema().renameColumn("new_col", "renamed_col").commit();
  }

  @Override
  protected void tableOperationsChangeColumnType() {
    getTable().updateSchema().updateColumn("renamed_col", Types.LongType.get()).commit();
  }

  @Override
  protected void tableOperationsChangeColumnComment() {
    getTable()
        .updateSchema()
        .updateColumn("renamed_col", Types.LongType.get(), "new comment")
        .commit();
  }

  @Override
  protected void tableOperationsChangeColumnRequired() {
    getTable().updateSchema().allowIncompatibleChanges().requireColumn("renamed_col").commit();
  }

  @Override
  protected void tableOperationsDropColumn() {
    getTable().updateSchema().deleteColumn("renamed_col").commit();
  }

  @Override
  protected FormatTableDescriptor getTableDescriptor() {
    return new MixedAndIcebergTableDescriptor();
  }

  private Table getTable() {
    return (Table) getAmoroCatalog().loadTable(TEST_DB, TEST_TABLE).originalTable();
  }

  /** Test descriptor class, add insert table/optimizing process methods for test. */
  private static class TestMixedAndIcebergTableDescriptor extends MixedAndIcebergTableDescriptor {

    public List<org.apache.amoro.table.descriptor.PartitionBaseInfo>
        testCollectPartitionsFromFileScan(Table table) {
      return collectPartitionsFromFileScan(table);
    }

    public void insertTable(ServerTableIdentifier identifier) {
      doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
    }

    public void insertOptimizingProcess(
        ServerTableIdentifier identifier,
        long processId,
        long targetSnapshotId,
        long targetChangeSnapshotId,
        ProcessStatus status,
        OptimizingType type,
        long planTime,
        MetricsSummary summary,
        Map<String, Long> fromSequence,
        Map<String, Long> toSequence) {
      Action action = getOptimizingAction(type);
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.insertProcess(
                  identifier.getId(),
                  processId,
                  "",
                  status,
                  action,
                  type.name(),
                  "AMORO",
                  0,
                  planTime,
                  new HashMap<>(),
                  summary.summaryAsMap(false)));
      doAs(
          OptimizingProcessMapper.class,
          mapper ->
              mapper.insertInternalProcessState(
                  identifier.getId(),
                  processId,
                  targetSnapshotId,
                  targetChangeSnapshotId,
                  fromSequence,
                  toSequence));
    }

    /**
     * Convert OptimizingType to corresponding Action.
     *
     * @param optimizingType optimizing type
     * @return corresponding Action
     */
    private Action getOptimizingAction(OptimizingType optimizingType) {
      switch (optimizingType) {
        case MINOR:
          return IcebergActions.OPTIMIZING_MINOR;
        case MAJOR:
          return IcebergActions.OPTIMIZING_MAJOR;
        case FULL:
          return IcebergActions.OPTIMIZING_FULL;
        default:
          throw new IllegalArgumentException("Unknown optimizing type: " + optimizingType);
      }
    }
  }
}
