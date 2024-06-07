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

package org.apache.amoro.data;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestUpsertPushDown extends TableTestBase {

  public TestUpsertPushDown(PartitionSpec partitionSpec) {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            BasicTableTestHelper.TABLE_SCHEMA,
            BasicTableTestHelper.PRIMARY_KEY_SPEC,
            partitionSpec,
            buildTableProperties()));
  }

  private static Map<String, String> buildTableProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(TableProperties.UPSERT_ENABLED, "true");
    return properties;
  }

  @Parameterized.Parameters(name = "spec = {0}")
  public static Object[] parameters() {
    return new Object[] {
      PartitionSpec.unpartitioned(),
      BasicTableTestHelper.SPEC,
      PartitionSpec.builderFor(BasicTableTestHelper.TABLE_SCHEMA)
          .day("op_time")
          .identity("ts")
          .build()
    };
  }

  @Before
  public void initChangeStoreData() {
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(),
        1L,
        ChangeAction.DELETE,
        writeRecords(1, "aaa", 0, 1),
        false);
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(),
        2L,
        ChangeAction.UPDATE_AFTER,
        writeRecords(1, "aaa", 0, 1),
        false);
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(),
        3L,
        ChangeAction.DELETE,
        writeRecords(2, "bbb", 0, 2),
        false);
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(),
        3L,
        ChangeAction.UPDATE_AFTER,
        writeRecords(2, "bbb", 0, 2),
        false);
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(),
        4L,
        ChangeAction.DELETE,
        writeRecords(2, "ccc", 0, 2),
        false);
    MixedDataTestHelpers.writeAndCommitChangeStore(
        getMixedTable().asKeyedTable(),
        5L,
        ChangeAction.UPDATE_AFTER,
        writeRecords(2, "ccc", 0, 2),
        false);
  }

  @Test
  public void testReadKeyedTableWithoutFilter() {
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), Expressions.alwaysTrue());
    Assert.assertEquals(records.size(), 2);
    Assert.assertTrue(recordToNameList(records).containsAll(Arrays.asList("aaa", "ccc")));
  }

  @Test
  public void testReadKeyedTableWithPartitionAndColumnFilter() {
    Assume.assumeTrue(isPartitionedTable());
    Expression partitionAndColumnFilter =
        Expressions.and(
            Expressions.and(
                Expressions.notNull("op_time"),
                Expressions.equal("op_time", "2022-01-02T12:00:00")),
            Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb")));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), partitionAndColumnFilter);
    // Scan from change store only filter partition column expression, so record(name=ccc) is still
    // returned.
    Assert.assertEquals(records.size(), 1);
    Assert.assertTrue(recordToNameList(records).contains("ccc"));
  }

  @Test
  public void testReadKeyedTableWithPartitionOrColumnFilter() {
    Assume.assumeTrue(isPartitionedTable());
    Expression partitionOrColumnFilter =
        Expressions.or(
            Expressions.and(
                Expressions.notNull("op_time"),
                Expressions.equal("op_time", "2022-01-02T12:00:00")),
            Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb")));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), partitionOrColumnFilter);
    Assert.assertEquals(records.size(), 2);
    Assert.assertTrue(recordToNameList(records).containsAll(Arrays.asList("aaa", "ccc")));
  }

  @Test
  public void testReadKeyedTableWithPartitionFilter() {
    Assume.assumeTrue(isPartitionedTable());
    Expression partitionFilter =
        Expressions.and(
            Expressions.notNull("op_time"), Expressions.equal("op_time", "2022-01-02T12:00:00"));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(getMixedTable().asKeyedTable(), partitionFilter);
    Assert.assertEquals(records.size(), 1);
    Assert.assertTrue(recordToNameList(records).contains("ccc"));
  }

  @Test
  public void testReadKeyedTableWithColumnFilter() {
    Expression columnFilter =
        Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb"));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(getMixedTable().asKeyedTable(), columnFilter);
    Assert.assertEquals(records.size(), 2);
    Assert.assertTrue(recordToNameList(records).containsAll(Arrays.asList("aaa", "ccc")));
  }

  @Test
  public void testReadKeyedTableWithGreaterPartitionAndColumnFilter() {
    Assume.assumeTrue(isPartitionedTable());
    Expression greaterPartitionAndColumnFilter =
        Expressions.and(
            Expressions.and(
                Expressions.notNull("op_time"),
                Expressions.greaterThan("op_time", "2022-01-01T12:00:00")),
            Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb")));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), greaterPartitionAndColumnFilter);
    Assert.assertEquals(records.size(), 1);
    Assert.assertTrue(recordToNameList(records).contains("ccc"));
  }

  private List<Record> writeRecords(int id, String name, long ts, int day) {

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(
        MixedDataTestHelpers.createRecord(
            id, name, ts, String.format("2022-01-%02dT12:00:00", day)));

    return builder.build();
  }

  private List<String> recordToNameList(List<Record> list) {
    return list.stream().map(r -> r.getField("name").toString()).collect(Collectors.toList());
  }
}
