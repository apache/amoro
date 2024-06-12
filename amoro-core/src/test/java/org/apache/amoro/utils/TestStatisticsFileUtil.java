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

package org.apache.amoro.utils;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestStatisticsFileUtil extends TableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  public TestStatisticsFileUtil(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testWriteAndReadPuffin() {
    UnkeyedTable table =
        getMixedTable().isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    table
        .newAppend()
        .set(MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME_EXIST, "true")
        .set(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true")
        .commit();

    Snapshot snapshot = table.currentSnapshot();
    StructLikeMap<Long> optimizedTime = buildPartitionOptimizedTime();
    StructLikeMap<Long> optimizedSequence = buildPartitionOptimizedSequence();

    StatisticsFileUtil.PartitionDataSerializer<Long> dataSerializer =
        StatisticsFileUtil.createPartitionDataSerializer(table.spec(), Long.class);
    StatisticsFileUtil.Writer writer =
        StatisticsFileUtil.writerBuilder(table)
            .withSnapshotId(snapshot.snapshotId())
            .build()
            .add(MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME, optimizedTime, dataSerializer)
            .add(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE, optimizedSequence, dataSerializer);
    StatisticsFile statisticsFile = writer.complete();
    table.updateStatistics().setStatistics(snapshot.snapshotId(), statisticsFile).commit();

    assertStructLikeEquals(
        optimizedTime, readPartitionData(table, MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME));
    assertStructLikeEquals(
        optimizedSequence, readPartitionData(table, MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE));

    table.newAppend().commit();

    assertStructLikeEquals(
        optimizedTime, readPartitionData(table, MixedTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME));
    assertStructLikeEquals(
        optimizedSequence, readPartitionData(table, MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE));
  }

  private StatisticsFile findValidStatisticFile(Table table, String type) {
    Snapshot latestValidSnapshot =
        MixedTableUtil.findLatestValidSnapshot(
            table, table.currentSnapshot().snapshotId(), MixedTableUtil.isTypeExist(type));
    Preconditions.checkState(latestValidSnapshot != null, "Expect one valid snapshot");
    List<StatisticsFile> statisticsFiles =
        StatisticsFileUtil.getStatisticsFiles(table, latestValidSnapshot.snapshotId(), type);
    Preconditions.checkArgument(statisticsFiles.size() == 1, "Expect one valid statistics file");
    return statisticsFiles.get(0);
  }

  private StructLikeMap<Long> readPartitionData(Table table, String type) {
    StatisticsFileUtil.PartitionDataSerializer<Long> dataSerializer =
        StatisticsFileUtil.createPartitionDataSerializer(table.spec(), Long.class);
    List<StructLikeMap<Long>> result =
        StatisticsFileUtil.reader(table)
            .read(findValidStatisticFile(table, type), type, dataSerializer);
    Assert.assertEquals(1, result.size());
    return result.get(0);
  }

  private void assertStructLikeEquals(StructLikeMap<Long> expected, StructLikeMap<Long> actual) {
    Assert.assertEquals(expected.size(), actual.size());
    for (StructLike structLike : expected.keySet()) {
      Assert.assertEquals(expected.get(structLike), actual.get(structLike));
    }
  }

  private StructLikeMap<Long> buildPartitionOptimizedSequence() {
    PartitionSpec spec = getMixedTable().spec();
    StructLikeMap<Long> result = StructLikeMap.create(spec.partitionType());
    if (spec.isUnpartitioned()) {
      result.put(TablePropertyUtil.EMPTY_STRUCT, 1L);
    } else {
      StructLike partition1 = MixedDataTestHelpers.recordPartition("2022-01-01T12:00:00");
      StructLike partition2 = MixedDataTestHelpers.recordPartition("2022-01-01T12:00:00");
      result.put(partition1, 1L);
      result.put(partition2, 2L);
    }
    return result;
  }

  private StructLikeMap<Long> buildPartitionOptimizedTime() {
    PartitionSpec spec = getMixedTable().spec();
    StructLikeMap<Long> result = StructLikeMap.create(spec.partitionType());
    if (spec.isUnpartitioned()) {
      result.put(TablePropertyUtil.EMPTY_STRUCT, 1000L);
    } else {
      StructLike partition1 = MixedDataTestHelpers.recordPartition("2022-01-01T12:00:00");
      StructLike partition2 = MixedDataTestHelpers.recordPartition("2022-01-01T12:00:00");
      result.put(partition1, 1000L);
      result.put(partition2, 2000L);
    }
    return result;
  }
}
