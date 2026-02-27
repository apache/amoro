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
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestUnkeyedExpressionUtil extends TableTestBase {

  public TestUnkeyedExpressionUtil(TableFormat tableFormat, PartitionSpec partitionSpec) {
    super(
        new BasicCatalogTestHelper(tableFormat),
        new BasicTableTestHelper(TABLE_SCHEMA, false, partitionSpec));
  }

  public static final Schema TABLE_SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name", Types.StringType.get()),
          Types.NestedField.required(3, "ts", Types.LongType.get()),
          Types.NestedField.optional(4, "op_time", Types.TimestampType.withoutZone()));

  @Override
  protected MixedTable getMixedTable() {
    return super.getMixedTable();
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).identity("op_time").build()},
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).bucket("name", 2).build()},
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).truncate("ts", 10).build()},
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).year("op_time").build()},
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).month("op_time").build()},
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build()},
      {TableFormat.ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).hour("op_time").build()},
      {TableFormat.ICEBERG, PartitionSpec.unpartitioned()},
      {
        TableFormat.MIXED_ICEBERG,
        PartitionSpec.builderFor(TABLE_SCHEMA).identity("op_time").build()
      },
      {TableFormat.MIXED_ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).bucket("name", 2).build()},
      {
        TableFormat.MIXED_ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).truncate("ts", 10).build()
      },
      {TableFormat.MIXED_ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).year("op_time").build()},
      {TableFormat.MIXED_ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).month("op_time").build()},
      {TableFormat.MIXED_ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build()},
      {TableFormat.MIXED_ICEBERG, PartitionSpec.builderFor(TABLE_SCHEMA).hour("op_time").build()},
      {TableFormat.MIXED_ICEBERG, PartitionSpec.unpartitioned()}
    };
  }

  @Test
  public void testUnkeyedConvertPartitionStructLikeToDataFilter() throws IOException {
    Assume.assumeTrue(getMixedTable().isUnkeyedTable());
    UnkeyedTable table = getMixedTable().asUnkeyedTable();
    ArrayList<Record> records =
        Lists.newArrayList(
            // hash("111") = -210118348, hash("222") = -699778209
            tableTestHelper().generateTestRecord(1, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(2, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(3, "222", 11, null),
            tableTestHelper().generateTestRecord(4, "222", 11, null));
    // 2 files for partition table, 1 file for unpartition table
    DataFile[] dataFiles = IcebergDataTestHelpers.insert(table, records).dataFiles();
    AppendFiles appendFiles = table.newAppend();
    for (DataFile dataFile : dataFiles) {
      appendFiles.appendFile(dataFile);
    }
    appendFiles.commit();
    for (DataFile dataFile : dataFiles) {
      Expression partitionFilter =
          ExpressionUtil.convertPartitionDataToDataFilter(
              getMixedTable(), dataFile.specId(), Sets.newHashSet(dataFile.partition()));
      assertPlanHalfWithPartitionFilter(partitionFilter);
    }
  }

  private void assertPlanHalfWithPartitionFilter(Expression partitionFilter) {
    // plan all
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    try (CloseableIterable<FileScanTask> fileScanTasks =
        getMixedTable().asUnkeyedTable().newScan().planFiles()) {
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
        getMixedTable().asUnkeyedTable().newScan().filter(partitionFilter).planFiles()) {
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
