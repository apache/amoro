package com.netease.arctic.spark.test.suites.sql;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.spark.test.Asserts;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import com.netease.arctic.spark.test.helper.DataComparator;
import com.netease.arctic.spark.test.helper.ExpectResultHelper;
import com.netease.arctic.spark.test.helper.RecordGenerator;
import com.netease.arctic.spark.test.helper.TableFiles;
import com.netease.arctic.spark.test.helper.TestTableHelper;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.StructLikeSet;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 1. dynamic insert overwrite
 * 2. static insert overwrite
 * 3. for un-partitioned table
 * 3. duplicate check for insert overwrite
 * 4. optimize write is work for insert overwrite
 */
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestInsertOverwriteSQL extends SparkTableTestBase {


  static final Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "data", Types.StringType.get()),
      Types.NestedField.required(3, "pt", Types.StringType.get())
  );


  static final PrimaryKeySpec idPrimaryKeySpec = PrimaryKeySpec.builderFor(schema)
      .addColumn("id").build();

  static final PartitionSpec ptSpec = PartitionSpec.builderFor(schema)
      .identity("pt").build();

  List<Record> base = Lists.newArrayList(
      RecordGenerator.newRecord(schema, 1, "aaa", "AAA"),
      RecordGenerator.newRecord(schema, 2, "bbb", "AAA"),
      RecordGenerator.newRecord(schema, 3, "ccc", "BBB"),
      RecordGenerator.newRecord(schema, 4, "ddd", "BBB"),
      RecordGenerator.newRecord(schema, 5, "eee", "CCC"),
      RecordGenerator.newRecord(schema, 6, "fff", "CCC")
  );

  List<Record> change = Lists.newArrayList(
      RecordGenerator.newRecord(schema, 7, "ggg", "DDD"),
      RecordGenerator.newRecord(schema, 8, "hhh", "DDD"),
      RecordGenerator.newRecord(schema, 9, "jjj", "AAA"),
      RecordGenerator.newRecord(schema, 10, "kkk", "AAA")
  );

  List<Record> source = Lists.newArrayList(
      RecordGenerator.newRecord(schema, 1, "xxx", "AAA"),
      RecordGenerator.newRecord(schema, 2, "xxx", "AAA"),
      RecordGenerator.newRecord(schema, 11, "xxx", "DDD"),
      RecordGenerator.newRecord(schema, 12, "xxx", "DDD"),
      RecordGenerator.newRecord(schema, 13, "xxx", "EEE"),
      RecordGenerator.newRecord(schema, 14, "xxx", "EEE")
  );


  public static Stream<Arguments> testDynamic() {
    return Stream.of(
        Arguments.arguments(MIXED_ICEBERG, idPrimaryKeySpec),
        Arguments.arguments(MIXED_ICEBERG, noPrimaryKey),
        Arguments.arguments(MIXED_HIVE, idPrimaryKeySpec),
        Arguments.arguments(MIXED_HIVE, noPrimaryKey)
    );
  }

  @DisplayName("TestSQL: INSERT OVERWRITE dynamic mode")
  @ParameterizedTest()
  @MethodSource
  public void testDynamic(
      TableFormat format, PrimaryKeySpec keySpec
  ) {
    spark.conf().set("spark.sql.sources.partitionOverwriteMode", "DYNAMIC");

    ArcticTable table = createTarget(schema, builder ->
        builder.withPartitionSpec(ptSpec)
            .withPrimaryKeySpec(keySpec));

    TestTableHelper.writeToBase(table, base);
    List<Record> target = Lists.newArrayList(base);
    if (keySpec.primaryKeyExisted()) {
      TestTableHelper.writeToChange(
          table.asKeyedTable(), change, ChangeAction.INSERT);
      target.addAll(change);
    }

    createViewSource(schema, source);

    sql("INSERT OVERWRITE " + target() + " SELECT * FROM " + source());


    table.refresh();
    List<Record> expects = ExpectResultHelper.dynamicOverwriteResult(target, source, r -> r.getField("pt"));
    List<Record> actual = TestTableHelper.tableRecords(table);
    DataComparator.build(expects, actual)
        .ignoreOrder("id")
        .assertRecordsEqual();

    TableFiles files = TestTableHelper.files(table);

    PartitionKey partitionKey = new PartitionKey(ptSpec, schema);
    StructLikeSet partitions = StructLikeSet.createMemorySet(ptSpec.partitionType());
    source.stream().map(r -> {
      partitionKey.partition(r);
      return partitionKey.copy();
    }).forEach(partitions::add);

    files = files.filterByPartitions(partitions);

    Asserts.assertAllFilesInBaseStore(files);
    if (MIXED_HIVE == format) {
      String hiveLocation = ((SupportHive) table).hiveLocation();
      Asserts.assertAllFilesInHiveLocation(files, hiveLocation);
    }

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
        Arguments.arguments(MIXED_ICEBERG, idPrimaryKeySpec, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(MIXED_ICEBERG, idPrimaryKeySpec, "PARTITION(pt = 'AAA')", "id, data",
            deleteAAA, ptAAA),
        Arguments.arguments(MIXED_ICEBERG, noPrimaryKey, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(MIXED_ICEBERG, noPrimaryKey, "PARTITION(pt = 'DDD')", "id, data",
            deleteDDD, ptDDD),

        Arguments.arguments(MIXED_HIVE, idPrimaryKeySpec, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(MIXED_HIVE, idPrimaryKeySpec, "PARTITION(pt = 'AAA')", "id, data",
            deleteAAA, ptAAA),
        Arguments.arguments(MIXED_HIVE, noPrimaryKey, "", "*", alwaysTrue, noTrans),
        Arguments.arguments(MIXED_HIVE, noPrimaryKey, "PARTITION(pt = 'DDD')", "id, data",
            deleteDDD, ptDDD)
    );
  }


  @DisplayName("TestSQL: INSERT OVERWRITE static mode")
  @ParameterizedTest(name = "{index} {0} {1} {2} {3}")
  @MethodSource
  public void testStatic(
      TableFormat format, PrimaryKeySpec keySpec, String ptFilter, String sourceProject,
      Function<Record, Boolean> deleteFilter, Function<Record, Record> sourceTrans
  ) {
    spark.conf().set("spark.sql.sources.partitionOverwriteMode", "STATIC");
    ArcticTable table = createTarget(schema, builder ->
        builder.withPartitionSpec(ptSpec)
            .withPrimaryKeySpec(keySpec));
    List<DataFile> initFiles = TestTableHelper.writeToBase(table, base);
    List<Record> target = Lists.newArrayList(base);

    if (keySpec.primaryKeyExisted()) {
      List<DataFile> changeFiles = TestTableHelper.writeToChange(
          table.asKeyedTable(), change, ChangeAction.INSERT);
      initFiles.addAll(changeFiles);
      target.addAll(change);
    }

    createViewSource(schema, source);

    sql("INSERT OVERWRITE " + target() + " " + ptFilter +
        " SELECT " + sourceProject + " FROM " + source());
    table.refresh();


    List<Record> expects = target.stream()
        .filter(r -> !deleteFilter.apply(r))
        .collect(Collectors.toList());

    source.stream().map(sourceTrans).forEach(expects::add);


    List<Record> actual = TestTableHelper.tableRecords(table);
    sql("SELECT * FROM " + target());
    DataComparator.build(expects, actual)
        .ignoreOrder("pt", "id")
        .assertRecordsEqual();

    TableFiles files = TestTableHelper.files(table);
    Set<String> initFileSet = initFiles.stream().map(f -> f.path().toString()).collect(Collectors.toSet());
    files = files.removeFiles(initFileSet);

    Asserts.assertAllFilesInBaseStore(files);
    if (MIXED_HIVE == format) {
      String hiveLocation = ((SupportHive) table).hiveLocation();
      Asserts.assertAllFilesInHiveLocation(files, hiveLocation);
    }
  }

}
