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

package org.apache.amoro.spark.test.suites.sql;

import org.apache.amoro.TableFormat;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.mixed.SparkSQLProperties;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.extensions.EnableCatalogSelect;
import org.apache.amoro.spark.test.utils.DataComparator;
import org.apache.amoro.spark.test.utils.ExpectResultUtil;
import org.apache.amoro.spark.test.utils.RecordGenerator;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.amoro.table.MetadataColumns;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test 1. upsert not enabled test 2. upsert enabled test 3. upsert source duplicate check test 4.
 * upsert optimize write
 */
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestInsertIntoSQL extends MixedTableTestBase {

  static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()),
          Types.NestedField.required(3, "pt", Types.StringType.get()));

  static final PrimaryKeySpec ID_PRIMARY_KEY_SPEC =
      PrimaryKeySpec.builderFor(SCHEMA).addColumn("id").build();

  static final PartitionSpec PT_SPEC = PartitionSpec.builderFor(SCHEMA).identity("pt").build();

  List<Record> base =
      Lists.newArrayList(
          RecordGenerator.newRecord(SCHEMA, 1, "aaa", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 2, "bbb", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 3, "ccc", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 4, "ddd", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 5, "eee", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 6, "fff", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 7, "ggg", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 8, "hhh", "BBB"));
  List<Record> source =
      Lists.newArrayList(
          RecordGenerator.newRecord(SCHEMA, 1, "xxx", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 2, "xxx", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 7, "xxx", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 8, "xxx", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 9, "xxx", "CCC"),
          RecordGenerator.newRecord(SCHEMA, 10, "xxx", "CCC"));
  List<Record> duplicateSource =
      Lists.newArrayList(
          RecordGenerator.newRecord(SCHEMA, 1, "xxx", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 2, "xxx", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 2, "xxx", "BBB"));

  Comparator<Record> pkComparator = Comparator.comparing(r -> r.get(0, Integer.class));

  Comparator<Record> dataComparator = Comparator.comparing(r -> r.get(1, String.class));

  Comparator<Record> changeActionComparator =
      Comparator.comparing(r -> (String) r.getField(MetadataColumns.CHANGE_ACTION_NAME));
  Comparator<Record> comparator = pkComparator.thenComparing(dataComparator);

  public static Stream<Arguments> testNoUpsert() {
    return Stream.of(
            Arguments.of(MIXED_HIVE, SCHEMA, ID_PRIMARY_KEY_SPEC, PT_SPEC),
            Arguments.of(MIXED_HIVE, SCHEMA, NO_PRIMARY_KEY, PT_SPEC),
            Arguments.of(MIXED_HIVE, SCHEMA, ID_PRIMARY_KEY_SPEC, UNPARTITIONED),
            Arguments.of(MIXED_HIVE, SCHEMA, NO_PRIMARY_KEY, UNPARTITIONED),
            Arguments.of(MIXED_ICEBERG, SCHEMA, ID_PRIMARY_KEY_SPEC, PT_SPEC),
            Arguments.of(MIXED_ICEBERG, SCHEMA, NO_PRIMARY_KEY, PT_SPEC),
            Arguments.of(MIXED_ICEBERG, SCHEMA, ID_PRIMARY_KEY_SPEC, UNPARTITIONED),
            Arguments.of(MIXED_ICEBERG, SCHEMA, NO_PRIMARY_KEY, UNPARTITIONED))
        .flatMap(
            e -> {
              List parquet = Lists.newArrayList(e.get());
              parquet.add(FileFormat.PARQUET);
              List orc = Lists.newArrayList(e.get());
              orc.add(FileFormat.ORC);
              return Stream.of(Arguments.of(parquet.toArray()), Arguments.of(orc.toArray()));
            });
  }

  @DisplayName("TestSQL: INSERT INTO table without upsert")
  @ParameterizedTest
  @MethodSource
  public void testNoUpsert(
      TableFormat format,
      Schema schema,
      PrimaryKeySpec keySpec,
      PartitionSpec ptSpec,
      FileFormat fileFormat) {
    MixedTable table =
        createTarget(
            schema,
            tableBuilder ->
                tableBuilder
                    .withPrimaryKeySpec(keySpec)
                    .withProperty(TableProperties.UPSERT_ENABLED, "false")
                    .withProperty(TableProperties.CHANGE_FILE_FORMAT, fileFormat.name())
                    .withProperty(TableProperties.BASE_FILE_FORMAT, fileFormat.name())
                    .withPartitionSpec(ptSpec));

    createViewSource(schema, source);

    TestTableUtil.writeToBase(table, base);
    sql("INSERT INTO " + target() + " SELECT * FROM " + source());
    table.refresh();

    // mor result
    List<Record> results = TestTableUtil.tableRecords(table);
    List<Record> expects = Lists.newArrayList();
    expects.addAll(base);
    expects.addAll(source);

    DataComparator.build(expects, results).ignoreOrder(comparator).assertRecordsEqual();
  }

  public static Stream<Arguments> testUpsert() {
    return Stream.of(
            Arguments.of(MIXED_HIVE, SCHEMA, ID_PRIMARY_KEY_SPEC, PT_SPEC),
            Arguments.of(MIXED_HIVE, SCHEMA, NO_PRIMARY_KEY, PT_SPEC),
            Arguments.of(MIXED_HIVE, SCHEMA, ID_PRIMARY_KEY_SPEC, UNPARTITIONED),
            Arguments.of(MIXED_HIVE, SCHEMA, NO_PRIMARY_KEY, UNPARTITIONED),
            Arguments.of(MIXED_ICEBERG, SCHEMA, ID_PRIMARY_KEY_SPEC, PT_SPEC),
            Arguments.of(MIXED_ICEBERG, SCHEMA, NO_PRIMARY_KEY, PT_SPEC),
            Arguments.of(MIXED_ICEBERG, SCHEMA, ID_PRIMARY_KEY_SPEC, UNPARTITIONED),
            Arguments.of(MIXED_ICEBERG, SCHEMA, NO_PRIMARY_KEY, UNPARTITIONED))
        .flatMap(
            e -> {
              List parquet = Lists.newArrayList(e.get());
              parquet.add(FileFormat.PARQUET);
              List orc = Lists.newArrayList(e.get());
              orc.add(FileFormat.ORC);
              return Stream.of(Arguments.of(parquet.toArray()), Arguments.of(orc.toArray()));
            });
  }

  @DisplayName("TestSQL: INSERT INTO table with upsert enabled")
  @ParameterizedTest
  @MethodSource
  public void testUpsert(
      TableFormat format,
      Schema schema,
      PrimaryKeySpec keySpec,
      PartitionSpec ptSpec,
      FileFormat fileFormat) {
    MixedTable table =
        createTarget(
            schema,
            tableBuilder ->
                tableBuilder
                    .withPrimaryKeySpec(keySpec)
                    .withProperty(TableProperties.UPSERT_ENABLED, "true")
                    .withProperty(TableProperties.CHANGE_FILE_FORMAT, fileFormat.name())
                    .withProperty(TableProperties.BASE_FILE_FORMAT, fileFormat.name())
                    .withPartitionSpec(ptSpec));
    createViewSource(schema, source);

    TestTableUtil.writeToBase(table, base);
    sql("INSERT INTO " + target() + " SELECT * FROM " + source());

    List<Record> expects;
    if (keySpec.primaryKeyExisted()) {
      expects = ExpectResultUtil.upsertResult(base, source, r -> r.get(0, Integer.class));
    } else {
      expects = Lists.newArrayList(base);
      expects.addAll(source);
    }

    table.refresh();
    List<Record> results = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, results).ignoreOrder(comparator).assertRecordsEqual();

    if (table.isKeyedTable()) {
      List<Record> deletes =
          ExpectResultUtil.upsertDeletes(base, source, r -> r.get(0, Integer.class));

      List<Record> expectChanges =
          deletes.stream()
              .map(
                  r ->
                      TestTableUtil.extendMetadataValue(
                          r, MetadataColumns.CHANGE_ACTION_FIELD, ChangeAction.DELETE.name()))
              .collect(Collectors.toList());

      source.stream()
          .map(
              r ->
                  TestTableUtil.extendMetadataValue(
                      r, MetadataColumns.CHANGE_ACTION_FIELD, ChangeAction.INSERT.name()))
          .forEach(expectChanges::add);

      List<Record> changes = TestTableUtil.changeRecordsWithAction(table.asKeyedTable());

      DataComparator.build(expectChanges, changes)
          .ignoreOrder(pkComparator.thenComparing(changeActionComparator))
          .assertRecordsEqual();
    }
  }

  public static Stream<Arguments> testDuplicateSourceCheck() {
    return Stream.of(
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, true, true),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, true, false),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, false, false),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, true, true),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, true, false),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, false, false));
  }

  @DisplayName("TestSQL: INSERT INTO duplicate source check")
  @ParameterizedTest(name = "{index} {0} {1} source-is-duplicate: {2} expect-exception: {3}")
  @MethodSource
  public void testDuplicateSourceCheck(
      TableFormat format,
      PrimaryKeySpec keySpec,
      boolean duplicateSource,
      boolean expectException) {
    spark().conf().set(SparkSQLProperties.CHECK_SOURCE_DUPLICATES_ENABLE, "true");
    createTarget(SCHEMA, tableBuilder -> tableBuilder.withPrimaryKeySpec(keySpec));

    List<Record> source = duplicateSource ? this.duplicateSource : this.source;
    createViewSource(SCHEMA, source);

    boolean getException = false;

    try {
      sql("INSERT INTO " + target() + " SELECT * FROM " + source());
    } catch (Exception e) {
      getException = true;
    }
    Assertions.assertEquals(expectException, getException, "expect exception assert failed.");
  }
}
