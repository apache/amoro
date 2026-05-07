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
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class TestKeyedExpressionUtil extends TableTestBase {

  public static final Schema TABLE_SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name", Types.StringType.get()),
          Types.NestedField.required(3, "ts", Types.LongType.get()),
          Types.NestedField.optional(4, "op_time", Types.TimestampType.withoutZone()));

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).identity("op_time").build()),
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).bucket("name", 2).build()),
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).truncate("ts", 10).build()),
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).year("op_time").build()),
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).month("op_time").build()),
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build()),
        Arguments.of(PartitionSpec.builderFor(TABLE_SCHEMA).hour("op_time").build()),
        Arguments.of(PartitionSpec.unpartitioned()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("parameters")
  public void testKeyedConvertPartitionStructLikeToDataFilter(PartitionSpec partitionSpec)
      throws IOException {
    setupTable(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(TABLE_SCHEMA, true, partitionSpec));
    Assumptions.assumeTrue(isKeyedTable());
    ArrayList<Record> baseStoreRecords =
        Lists.newArrayList(
            // hash("111") = -210118348, hash("222") = -699778209
            tableTestHelper().generateTestRecord(1, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(2, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(3, "222", 11, null),
            tableTestHelper().generateTestRecord(4, "222", 11, null));
    ArrayList<Record> changeStoreRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(5, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(6, "111", 1, "2021-01-01T01:00:00"),
            tableTestHelper().generateTestRecord(7, "222", 11, null),
            tableTestHelper().generateTestRecord(8, "222", 11, null));
    // 4 files
    List<DataFile> baseStoreFiles =
        MixedDataTestHelpers.writeAndCommitBaseStore(getMixedTable(), 1L, baseStoreRecords, true);
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(), 2L, ChangeAction.INSERT, changeStoreRecords, true);
    for (DataFile baseStoreFile : baseStoreFiles) {
      Expression partitionFilter =
          ExpressionUtil.convertPartitionDataToDataFilter(
              getMixedTable(), baseStoreFile.specId(), Sets.newHashSet(baseStoreFile.partition()));
      assertPlanHalfWithPartitionFilter(partitionFilter);
    }
  }

  private void assertPlanHalfWithPartitionFilter(Expression partitionFilter) {
    // plan all
    Set<DataFile> baseDataFiles = Sets.newHashSet();
    Set<DataFile> insertFiles = Sets.newHashSet();
    try (CloseableIterable<CombinedScanTask> it =
        getMixedTable().asKeyedTable().newScan().planTasks()) {
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
    baseDataFiles.clear();
    insertFiles.clear();
    try (CloseableIterable<CombinedScanTask> it =
        getMixedTable().asKeyedTable().newScan().planTasks()) {
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

    Assertions.assertEquals(4, baseDataFiles.size());
    Assertions.assertEquals(4, insertFiles.size());
    baseDataFiles.clear();
    insertFiles.clear();

    // plan with partition filter
    try (CloseableIterable<CombinedScanTask> it =
        getMixedTable().asKeyedTable().newScan().filter(partitionFilter).planTasks()) {
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
      Assertions.assertEquals(2, baseDataFiles.size());
      Assertions.assertEquals(2, insertFiles.size());
    } else {
      Assertions.assertEquals(4, baseDataFiles.size());
      Assertions.assertEquals(4, insertFiles.size());
    }
  }
}
