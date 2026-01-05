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
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.mixed.SparkSQLProperties;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.extensions.EnableCatalogSelect;
import org.apache.amoro.spark.test.utils.Asserts;
import org.apache.amoro.spark.test.utils.DataComparator;
import org.apache.amoro.spark.test.utils.ExpectResultUtil;
import org.apache.amoro.spark.test.utils.RecordGenerator;
import org.apache.amoro.spark.test.utils.TableFiles;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 1. dynamic insert overwrite 2. static insert overwrite 3. for un-partitioned table 3. duplicate
 * check for insert overwrite 4. optimize write is work for insert overwrite
 */
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestInsertOverwriteSQL extends MixedTableTestBase {

  static final String OVERWRITE_MODE_KEY = "spark.sql.sources.partitionOverwriteMode";
  static final String DYNAMIC = "DYNAMIC";
  static final String STATIC = "STATIC";

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
          RecordGenerator.newRecord(SCHEMA, 3, "ccc", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 4, "ddd", "BBB"),
          RecordGenerator.newRecord(SCHEMA, 5, "eee", "CCC"),
          RecordGenerator.newRecord(SCHEMA, 6, "fff", "CCC"));

  List<Record> change =
      Lists.newArrayList(
          RecordGenerator.newRecord(SCHEMA, 7, "ggg", "DDD"),
          RecordGenerator.newRecord(SCHEMA, 8, "hhh", "DDD"),
          RecordGenerator.newRecord(SCHEMA, 9, "jjj", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 10, "kkk", "AAA"));

  List<Record> source =
      Lists.newArrayList(
          RecordGenerator.newRecord(SCHEMA, 1, "xxx", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 2, "xxx", "AAA"),
          RecordGenerator.newRecord(SCHEMA, 11, "xxx", "DDD"),
          RecordGenerator.newRecord(SCHEMA, 12, "xxx", "DDD"),
          RecordGenerator.newRecord(SCHEMA, 13, "xxx", "EEE"),
          RecordGenerator.newRecord(SCHEMA, 14, "xxx", "EEE"));

  private MixedTable table;
  private List<Record> target;
  private List<DataFile> initFiles;

  private void initTargetTable(PrimaryKeySpec keySpec, PartitionSpec ptSpec) {
    table =
        createTarget(
            SCHEMA, builder -> builder.withPartitionSpec(ptSpec).withPrimaryKeySpec(keySpec));
    initFiles = TestTableUtil.writeToBase(table, base);
    target = Lists.newArrayList(base);

    if (keySpec.primaryKeyExisted()) {
      List<DataFile> changeFiles =
          TestTableUtil.writeToChange(table.asKeyedTable(), change, ChangeAction.INSERT);
      initFiles.addAll(changeFiles);
      target.addAll(change);
    }

    createViewSource(SCHEMA, source);
  }

  private void assertFileLayout(TableFormat format) {
    TableFiles files = TestTableUtil.files(table);
    Set<String> initFileSet =
        initFiles.stream().map(f -> f.path().toString()).collect(Collectors.toSet());
    files = files.removeFiles(initFileSet);

    Asserts.assertAllFilesInBaseStore(files);
    if (MIXED_HIVE == format) {
      String hiveLocation = ((SupportHive) table).hiveLocation();
      Asserts.assertAllFilesInHiveLocation(files, hiveLocation);
    }
  }

  @BeforeEach
  void cleanVars() {
    this.table = null;
    this.target = Lists.newArrayList();
    this.initFiles = Lists.newArrayList();
  }

  public static Stream<Arguments> testDynamic() {
    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY));
  }

  @DisplayName("TestSQL: INSERT OVERWRITE dynamic mode")
  @ParameterizedTest()
  @MethodSource
  public void testDynamic(TableFormat format, PrimaryKeySpec keySpec) {
    spark().conf().set(OVERWRITE_MODE_KEY, DYNAMIC);

    initTargetTable(keySpec, PT_SPEC);

    sql("INSERT OVERWRITE " + target() + " SELECT * FROM " + source());

    table.refresh();
    List<Record> expects =
        ExpectResultUtil.dynamicOverwriteResult(target, source, r -> r.getField("pt"));
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("id").assertRecordsEqual();

    assertFileLayout(format);
  }

  private static Record setPtValue(Record r, String value) {
    Record record = r.copy();
    record.setField("pt", value);
    return record;
  }

  public static Stream<Arguments> testStatic() {
    Function<Record, Boolean> alwaysTrue = r -> true;
    Function<Record, Boolean> deleteAAA = r -> "AAA".equals(r.getField("pt"));
    Function<Record, Boolean> deleteDDD = r -> "DDD".equals(r.getField("pt"));

    Function<Record, Record> noTrans = Function.identity();
    Function<Record, Record> ptAAA = r -> setPtValue(r, "AAA");
    Function<Record, Record> ptDDD = r -> setPtValue(r, "DDD");

    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(
            MIXED_ICEBERG,
            ID_PRIMARY_KEY_SPEC,
            "PARTITION(pt = 'AAA')",
            "id, data",
            deleteAAA,
            ptAAA),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(
            MIXED_ICEBERG, NO_PRIMARY_KEY, "PARTITION(pt = 'DDD')", "id, data", deleteDDD, ptDDD),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(
            MIXED_HIVE, ID_PRIMARY_KEY_SPEC, "PARTITION(pt = 'AAA')", "id, data", deleteAAA, ptAAA),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(
            MIXED_HIVE, NO_PRIMARY_KEY, "PARTITION(pt = 'DDD')", "id, data", deleteDDD, ptDDD));
  }

  @DisplayName("TestSQL: INSERT OVERWRITE static mode")
  @ParameterizedTest(name = "{index} {0} {1} {2} SELECT {3}")
  @MethodSource
  public void testStatic(
      TableFormat format,
      PrimaryKeySpec keySpec,
      String ptFilter,
      String sourceProject,
      Function<Record, Boolean> deleteFilter,
      Function<Record, Record> sourceTrans) {
    spark().conf().set(OVERWRITE_MODE_KEY, STATIC);
    initTargetTable(keySpec, PT_SPEC);

    sql(
        "INSERT OVERWRITE "
            + target()
            + " "
            + ptFilter
            + " SELECT "
            + sourceProject
            + " FROM "
            + source());
    table.refresh();

    List<Record> expects =
        target.stream().filter(r -> !deleteFilter.apply(r)).collect(Collectors.toList());

    source.stream().map(sourceTrans).forEach(expects::add);

    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("pt", "id").assertRecordsEqual();

    assertFileLayout(format);
  }

  public static Stream<Arguments> testUnPartitioned() {

    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, DYNAMIC),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, STATIC),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, DYNAMIC),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, DYNAMIC),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, DYNAMIC),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, STATIC),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, DYNAMIC),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, DYNAMIC));
  }

  @DisplayName("TestSQL: INSERT OVERWRITE un-partitioned")
  @ParameterizedTest(name = "{index} {0} {1} partitionOverwriteMode={2}")
  @MethodSource
  public void testUnPartitioned(TableFormat format, PrimaryKeySpec keySpec, String mode) {
    spark().conf().set(OVERWRITE_MODE_KEY, mode);
    initTargetTable(keySpec, PartitionSpec.unpartitioned());

    sql("INSERT OVERWRITE " + target() + " SELECT * FROM " + source());

    table.refresh();
    List<Record> expects = Lists.newArrayList(source);
    List<Record> actual = TestTableUtil.tableRecords(table);
    DataComparator.build(expects, actual).ignoreOrder("pt", "id").assertRecordsEqual();

    assertFileLayout(format);
  }

  private static final Schema hiddenPartitionSchema =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "ts", Types.TimestampType.withZone()));
  private static final OffsetDateTime EPOCH =
      LocalDateTime.of(2000, 1, 1, 0, 0, 0).atOffset(ZoneOffset.UTC);
  private static final List<Record> hiddenPartitionSource =
      IntStream.range(0, 10)
          .boxed()
          .map(i -> RecordGenerator.newRecord(hiddenPartitionSchema, i, EPOCH.plusDays(i)))
          .collect(Collectors.toList());

  private static PartitionSpec.Builder ptBuilder() {
    return PartitionSpec.builderFor(hiddenPartitionSchema);
  }

  public static Stream<Arguments> testHiddenPartitions() {
    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, ptBuilder().year("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, ptBuilder().month("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, ptBuilder().day("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, ptBuilder().hour("ts").build()),
        Arguments.arguments(
            MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, ptBuilder().bucket("id", 8).build()),
        Arguments.arguments(
            MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, ptBuilder().truncate("id", 10).build()),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, ptBuilder().year("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, ptBuilder().month("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, ptBuilder().day("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, ptBuilder().hour("ts").build()),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, ptBuilder().bucket("id", 8).build()),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, ptBuilder().truncate("id", 10).build()));
  }

  @DisplayName("TestSQL: INSERT OVERWRITE hidden partition optimize write")
  @ParameterizedTest()
  @MethodSource
  public void testHiddenPartitions(
      TableFormat format, PrimaryKeySpec keySpec, PartitionSpec ptSpec) {
    spark().conf().set(OVERWRITE_MODE_KEY, DYNAMIC);
    spark().conf().set(SparkSQLProperties.OPTIMIZE_WRITE_ENABLED, "true");

    this.table =
        createTarget(
            hiddenPartitionSchema,
            builder -> builder.withPrimaryKeySpec(keySpec).withPartitionSpec(ptSpec));
    createViewSource(hiddenPartitionSchema, hiddenPartitionSource);
    this.initFiles = Lists.newArrayList();

    sql("INSERT OVERWRITE " + target() + " SELECT * FROM " + source());

    table.refresh();
    assertFileLayout(format);
  }

  public static Stream<Arguments> testOptimizeWrite() {
    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, UNPARTITIONED, STATIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, PT_SPEC, STATIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, UNPARTITIONED, STATIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, 1, true),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, 4, false),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, UNPARTITIONED, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, PT_SPEC, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, UNPARTITIONED, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, 1, true),
        Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, 4, false),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, 4, true),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, UNPARTITIONED, STATIC, 4, true),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, PT_SPEC, STATIC, 4, true),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, UNPARTITIONED, STATIC, 4, true),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, 1, true),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, 4, false),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, UNPARTITIONED, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, PT_SPEC, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, UNPARTITIONED, DYNAMIC, 4, true),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, 1, true),
        Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, 4, false));
  }

  @DisplayName("TestSQL: INSERT OVERWRITE optimize write works")
  @ParameterizedTest()
  @MethodSource
  public void testOptimizeWrite(
      TableFormat format,
      PrimaryKeySpec keySpec,
      PartitionSpec ptSpec,
      String mode,
      int bucket,
      boolean optimizeWriteEnable) {
    spark().conf().set(SparkSQLProperties.OPTIMIZE_WRITE_ENABLED, optimizeWriteEnable);
    spark().conf().set(OVERWRITE_MODE_KEY, mode);

    this.table =
        createTarget(
            SCHEMA,
            builder ->
                builder
                    .withPrimaryKeySpec(keySpec)
                    .withProperty(
                        TableProperties.BASE_FILE_INDEX_HASH_BUCKET, String.valueOf(bucket))
                    .withPartitionSpec(ptSpec));

    String[] ptValues = {"AAA", "BBB", "CCC", "DDD"};
    List<Record> source =
        IntStream.range(1, 100)
            .boxed()
            .map(
                i ->
                    RecordGenerator.newRecord(
                        SCHEMA, i, "index" + i, ptValues[i % ptValues.length]))
            .collect(Collectors.toList());
    createViewSource(SCHEMA, source);

    sql("INSERT OVERWRITE " + target() + " SELECT * FROM " + source());

    boolean shouldOptimized =
        optimizeWriteEnable && (keySpec.primaryKeyExisted() || ptSpec.isPartitioned());

    if (shouldOptimized) {
      table.refresh();
      TableFiles files = TestTableUtil.files(table);
      int expectFiles = ExpectResultUtil.expectOptimizeWriteFileCount(source, table, bucket);

      Assertions.assertEquals(expectFiles, files.baseDataFiles.size());
    }
  }

  public static Arguments[] testSourceDuplicateCheck() {
    return new Arguments[] {
      Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, true, true),
      Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, PT_SPEC, STATIC, true, false),
      Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, true, true),
      Arguments.arguments(MIXED_ICEBERG, NO_PRIMARY_KEY, PT_SPEC, DYNAMIC, true, false),
      Arguments.arguments(MIXED_ICEBERG, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, false, false),
      Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, STATIC, true, true),
      Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, PT_SPEC, STATIC, true, false),
      Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, true, true),
      Arguments.arguments(MIXED_HIVE, NO_PRIMARY_KEY, PT_SPEC, DYNAMIC, true, false),
      Arguments.arguments(MIXED_HIVE, ID_PRIMARY_KEY_SPEC, PT_SPEC, DYNAMIC, false, false),
    };
  }

  @DisplayName("TestSQL: INSERT OVERWRITE duplicate check source")
  @ParameterizedTest()
  @MethodSource
  public void testSourceDuplicateCheck(
      TableFormat format,
      PrimaryKeySpec keySpec,
      PartitionSpec ptSpec,
      String mode,
      boolean duplicateSource,
      boolean expectChecked) {
    spark().conf().set(OVERWRITE_MODE_KEY, mode);
    spark().conf().set(SparkSQLProperties.CHECK_SOURCE_DUPLICATES_ENABLE, true);

    table =
        createTarget(
            SCHEMA, builder -> builder.withPartitionSpec(ptSpec).withPrimaryKeySpec(keySpec));
    List<Record> sourceData = Lists.newArrayList(this.base);
    if (duplicateSource) {
      sourceData.addAll(this.source);
    }
    createViewSource(SCHEMA, sourceData);

    boolean failed = false;
    try {
      sql("INSERT OVERWRITE " + target() + " SELECT * FROM " + source());
    } catch (Exception e) {
      failed = true;
    }
    Assertions.assertEquals(expectChecked, failed);
  }
}
