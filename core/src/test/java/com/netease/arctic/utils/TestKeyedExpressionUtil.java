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

import static com.netease.arctic.BasicTableTestHelper.TABLE_SCHEMA;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.scan.CombinedScanTask;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestKeyedExpressionUtil extends TableTestBase {

  public TestKeyedExpressionUtil(PartitionSpec partitionSpec) {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, partitionSpec));
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[][] {
      {PartitionSpec.builderFor(TABLE_SCHEMA).identity("op_time").build()},
      {PartitionSpec.builderFor(TABLE_SCHEMA).bucket("name", 2).build()},
      {PartitionSpec.builderFor(TABLE_SCHEMA).truncate("ts", 10).build()},
      {PartitionSpec.builderFor(TABLE_SCHEMA).year("op_time").build()},
      {PartitionSpec.builderFor(TABLE_SCHEMA).month("op_time").build()},
      {PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build()},
      {PartitionSpec.builderFor(TABLE_SCHEMA).hour("op_time").build()},
      {PartitionSpec.unpartitioned()}
    };
  }

  @Test
  public void testKeyedConvertPartitionStructLikeToDataFilter() {
    Assume.assumeTrue(isKeyedTable());
    ArrayList<Record> baseStoreRecords =
        Lists.newArrayList(
            // hash("111") = -210118348, hash("222") = -699778209
            tableTestHelper().generateTestRecord(1, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(2, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(3, "222", 11, "2022-02-02T02:00:00"),
            tableTestHelper().generateTestRecord(4, "222", 11, "2022-02-02T02:00:00"));
    ArrayList<Record> changeStoreRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(5, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(6, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(7, "222", 11, "2022-02-02T02:00:00"),
            tableTestHelper().generateTestRecord(8, "222", 11, "2022-02-02T02:00:00"));
    // 4 files
    List<DataFile> baseStoreFiles =
        MixedDataTestHelpers.writeAndCommitBaseStore(getArcticTable(), 1L, baseStoreRecords, true);
    // identity: opTime=2021-01-01T01:00:00; bucket: id_bucket=0;
    // truncate: ts_trunc=0; year: op_time_year=2021; month: op_time_month=2021-01;
    // day: op_time_day=2021-01-01; hour: op_time_hour=2021-01-01-01
    DataFile sampleFile = baseStoreFiles.get(0);

    MixedDataTestHelpers.writeAndCommitChangeStore(
        getArcticTable().asKeyedTable(), 2L, ChangeAction.INSERT, changeStoreRecords, true);

    Expression partitionFilter =
        ExpressionUtil.convertPartitionDataToDataFilter(
            getArcticTable(), sampleFile.specId(), Sets.newHashSet(sampleFile.partition()));
    assertPlanHalfWithPartitionFilter(partitionFilter);
  }

  private void assertPlanHalfWithPartitionFilter(Expression partitionFilter) {
    // plan all
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    Set<DataFile> insertFiles = Sets.newHashSet();
    try (CloseableIterable<CombinedScanTask> it =
        getArcticTable().asKeyedTable().newScan().planTasks()) {
      it.forEach(
          cst ->
              cst.tasks()
                  .forEach(
                      t -> {
                        t.baseTasks().forEach(fileTask -> baseDataFiles.add(fileTask.file()));
                        t.insertTasks().forEach(fileTask -> insertFiles.add(fileTask.file()));
                      }));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Assert.assertEquals(4, baseDataFiles.size());
    Assert.assertEquals(4, insertFiles.size());
    baseDataFiles.clear();
    insertFiles.clear();

    // plan with partition filter
    try (CloseableIterable<CombinedScanTask> it =
        getArcticTable().asKeyedTable().newScan().filter(partitionFilter).planTasks()) {
      it.forEach(
          cst ->
              cst.tasks()
                  .forEach(
                      t -> {
                        t.baseTasks().forEach(fileTask -> baseDataFiles.add(fileTask.file()));
                        t.insertTasks().forEach(fileTask -> insertFiles.add(fileTask.file()));
                      }));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (isPartitionedTable()) {
      Assert.assertEquals(2, baseDataFiles.size());
      Assert.assertEquals(2, insertFiles.size());
    } else {
      Assert.assertEquals(4, baseDataFiles.size());
      Assert.assertEquals(4, insertFiles.size());
    }
  }
}
