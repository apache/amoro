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

package com.netease.arctic.utils;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestPuffinUtil extends TableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  public TestPuffinUtil(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testWriteAndReadPuffin() {
    UnkeyedTable table =
        getArcticTable().isKeyedTable()
            ? getArcticTable().asKeyedTable().baseTable()
            : getArcticTable().asUnkeyedTable();
    table.newAppend().commit();

    Snapshot snapshot = table.currentSnapshot();
    StructLikeMap<Long> optimizedTime = buildPartitionOptimizedTime();
    StructLikeMap<Long> optimizedSequence = buildPartitionOptimizedSequence();

    PuffinUtil.PartitionDataSerializer dataSerializer =
        PuffinUtil.createPartitionDataSerializer(table.spec());
    PuffinUtil.Writer writer =
        PuffinUtil.writer(table, snapshot.snapshotId(), snapshot.sequenceNumber())
            .add(ArcticTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME, optimizedTime, dataSerializer)
            .add(ArcticTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE, optimizedSequence, dataSerializer);
    StatisticsFile statisticsFile = writer.complete();
    table.updateStatistics().setStatistics(snapshot.snapshotId(), statisticsFile).commit();

    PuffinUtil.Reader reader = PuffinUtil.reader(table);

    assertStructLikeEquals(
        optimizedTime,
        reader.read(
            findValidStatisticFile(table, ArcticTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME),
            ArcticTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME,
            dataSerializer));
    assertStructLikeEquals(
        optimizedSequence,
        reader.read(
            findValidStatisticFile(table, ArcticTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE),
            ArcticTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE,
            dataSerializer));

    table.newAppend().commit();

    assertStructLikeEquals(
        optimizedTime,
        reader.read(
            findValidStatisticFile(table, ArcticTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME),
            ArcticTableUtil.BLOB_TYPE_BASE_OPTIMIZED_TIME,
            dataSerializer));
    assertStructLikeEquals(
        optimizedSequence,
        reader.read(
            findValidStatisticFile(table, ArcticTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE),
            ArcticTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE,
            dataSerializer));
  }

  private StatisticsFile findValidStatisticFile(UnkeyedTable table, String type) {
    List<StatisticsFile> latestValidStatisticsFiles =
        PuffinUtil.findLatestValidStatisticsFiles(
            table, table.currentSnapshot().snapshotId(), PuffinUtil.containsBlobOfType(type));
    return latestValidStatisticsFiles.get(0);
  }

  private void assertStructLikeEquals(StructLikeMap<Long> expected, StructLikeMap<Long> actual) {
    Assert.assertEquals(expected.size(), actual.size());
    for (StructLike structLike : expected.keySet()) {
      Assert.assertEquals(expected.get(structLike), actual.get(structLike));
    }
  }

  private StructLikeMap<Long> buildPartitionOptimizedSequence() {
    PartitionSpec spec = getArcticTable().spec();
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
    PartitionSpec spec = getArcticTable().spec();
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
