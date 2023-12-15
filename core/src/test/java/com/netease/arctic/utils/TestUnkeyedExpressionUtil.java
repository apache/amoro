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

import static com.netease.arctic.BasicTableTestHelper.SPEC;
import static com.netease.arctic.BasicTableTestHelper.TABLE_SCHEMA;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.io.IcebergDataTestHelpers;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestUnkeyedExpressionUtil extends TableTestBase {

  private static final Logger logger = LoggerFactory.getLogger(TestUnkeyedExpressionUtil.class);

  public TestUnkeyedExpressionUtil(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).identity("op_time").build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).bucket("name", 2).build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).truncate("ts", 10).build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).year("op_time").build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).month("op_time").build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false,
            // 'day' transform on 'op_time'
            SPEC)
      },
      {
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).hour("op_time").build())
      },
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)},
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).identity("op_time").build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).bucket("name", 2).build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).truncate("ts", 10).build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).year("op_time").build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).month("op_time").build())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false,
            // 'day' transform on 'op_time'
            SPEC)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            false, PartitionSpec.builderFor(TABLE_SCHEMA).hour("op_time").build())
      },
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  @Test
  public void testUnkeyedConvertPartitionStructLikeToDataFilter() throws IOException {
    Assume.assumeTrue(getArcticTable().isUnkeyedTable());
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    logger.info("Partition Spec:" + getArcticTable().spec());
    ArrayList<Record> records =
        Lists.newArrayList(
            // hash("111") = -210118348, hash("222") = -699778209
            tableTestHelper().generateTestRecord(1, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(2, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(3, "222", 11, "2022-02-02T02:00:00"),
            tableTestHelper().generateTestRecord(4, "222", 11, "2022-02-02T02:00:00"));
    // 2 files for partition table, 1 file for unpartition table
    DataFile[] dataFiles = IcebergDataTestHelpers.insert(table, records).dataFiles();
    AppendFiles appendFiles = table.newAppend();
    for (DataFile dataFile : dataFiles) {
      appendFiles.appendFile(dataFile);
    }
    appendFiles.commit();
    // identity: opTime=2021-01-01T01:00:00; bucket: id_bucket=0;
    // truncate: ts_trunc=0; year: op_time_year=2021; month: op_time_month=2021-01;
    // day: op_time_day=2021-01-01; hour: op_time_hour=2021-01-01-01
    DataFile sampleFile = dataFiles[0];
    Expression partitionFilter =
        ExpressionUtil.convertPartitionDataToDataFilter(
            getArcticTable(), sampleFile.specId(), Sets.newHashSet(sampleFile.partition()));
    assertPlanHalfWithPartitionFilter(partitionFilter);
  }

  private void assertPlanHalfWithPartitionFilter(Expression partitionFilter) {
    // plan all
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    try (CloseableIterable<FileScanTask> fileScanTasks =
        getArcticTable().asUnkeyedTable().newScan().planFiles()) {
      for (FileScanTask fileScanTask : fileScanTasks) {
        baseDataFiles.add(fileScanTask.file());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (isPartitionedTable()) {
      Assert.assertEquals(2, baseDataFiles.size());
    } else {
      Assert.assertEquals(1, baseDataFiles.size());
    }
    baseDataFiles.clear();

    // plan with partition filter
    try (CloseableIterable<FileScanTask> fileScanTasks =
        getArcticTable().asUnkeyedTable().newScan().filter(partitionFilter).planFiles()) {
      for (FileScanTask fileScanTask : fileScanTasks) {
        baseDataFiles.add(fileScanTask.file());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (isPartitionedTable()) {
      Assert.assertEquals(1, baseDataFiles.size());
    } else {
      Assert.assertEquals(1, baseDataFiles.size());
    }
  }
}
