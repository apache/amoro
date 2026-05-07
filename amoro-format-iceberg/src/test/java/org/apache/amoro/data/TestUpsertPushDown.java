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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUpsertPushDown extends TableTestBase {

  private static Map<String, String> buildTableProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(TableProperties.UPSERT_ENABLED, "true");
    return properties;
  }

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(PartitionSpec.unpartitioned()),
        Arguments.of(BasicTableTestHelper.SPEC),
        Arguments.of(
            PartitionSpec.builderFor(BasicTableTestHelper.TABLE_SCHEMA)
                .day("op_time")
                .identity("ts")
                .build()));
  }

  private void prepareTable(PartitionSpec partitionSpec) throws IOException {
    setupTable(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            BasicTableTestHelper.TABLE_SCHEMA,
            BasicTableTestHelper.PRIMARY_KEY_SPEC,
            partitionSpec,
            buildTableProperties()));
    initChangeStoreData();
  }

  private void initChangeStoreData() {
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

  @ParameterizedTest(name = "spec = {0}")
  @MethodSource("parameters")
  public void testReadKeyedTableWithoutFilter(PartitionSpec partitionSpec) throws IOException {
    prepareTable(partitionSpec);
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), Expressions.alwaysTrue());
    Assertions.assertEquals(records.size(), 2);
    Assertions.assertTrue(recordToNameList(records).containsAll(Arrays.asList("aaa", "ccc")));
  }

  @ParameterizedTest(name = "spec = {0}")
  @MethodSource("parameters")
  public void testReadKeyedTableWithPartitionAndColumnFilter(PartitionSpec partitionSpec)
      throws IOException {
    prepareTable(partitionSpec);
    Assumptions.assumeTrue(isPartitionedTable());
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
    Assertions.assertEquals(records.size(), 1);
    Assertions.assertTrue(recordToNameList(records).contains("ccc"));
  }

  @ParameterizedTest(name = "spec = {0}")
  @MethodSource("parameters")
  public void testReadKeyedTableWithPartitionOrColumnFilter(PartitionSpec partitionSpec)
      throws IOException {
    prepareTable(partitionSpec);
    Assumptions.assumeTrue(isPartitionedTable());
    Expression partitionOrColumnFilter =
        Expressions.or(
            Expressions.and(
                Expressions.notNull("op_time"),
                Expressions.equal("op_time", "2022-01-02T12:00:00")),
            Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb")));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), partitionOrColumnFilter);
    Assertions.assertEquals(records.size(), 2);
    Assertions.assertTrue(recordToNameList(records).containsAll(Arrays.asList("aaa", "ccc")));
  }

  @ParameterizedTest(name = "spec = {0}")
  @MethodSource("parameters")
  public void testReadKeyedTableWithPartitionFilter(PartitionSpec partitionSpec)
      throws IOException {
    prepareTable(partitionSpec);
    Assumptions.assumeTrue(isPartitionedTable());
    Expression partitionFilter =
        Expressions.and(
            Expressions.notNull("op_time"), Expressions.equal("op_time", "2022-01-02T12:00:00"));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(getMixedTable().asKeyedTable(), partitionFilter);
    Assertions.assertEquals(records.size(), 1);
    Assertions.assertTrue(recordToNameList(records).contains("ccc"));
  }

  @ParameterizedTest(name = "spec = {0}")
  @MethodSource("parameters")
  public void testReadKeyedTableWithColumnFilter(PartitionSpec partitionSpec) throws IOException {
    prepareTable(partitionSpec);
    Expression columnFilter =
        Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb"));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(getMixedTable().asKeyedTable(), columnFilter);
    Assertions.assertEquals(records.size(), 2);
    Assertions.assertTrue(recordToNameList(records).containsAll(Arrays.asList("aaa", "ccc")));
  }

  @ParameterizedTest(name = "spec = {0}")
  @MethodSource("parameters")
  public void testReadKeyedTableWithGreaterPartitionAndColumnFilter(PartitionSpec partitionSpec)
      throws IOException {
    prepareTable(partitionSpec);
    Assumptions.assumeTrue(isPartitionedTable());
    Expression greaterPartitionAndColumnFilter =
        Expressions.and(
            Expressions.and(
                Expressions.notNull("op_time"),
                Expressions.greaterThan("op_time", "2022-01-01T12:00:00")),
            Expressions.and(Expressions.notNull("name"), Expressions.equal("name", "bbb")));
    List<Record> records =
        MixedDataTestHelpers.readKeyedTable(
            getMixedTable().asKeyedTable(), greaterPartitionAndColumnFilter);
    Assertions.assertEquals(records.size(), 1);
    Assertions.assertTrue(recordToNameList(records).contains("ccc"));
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
