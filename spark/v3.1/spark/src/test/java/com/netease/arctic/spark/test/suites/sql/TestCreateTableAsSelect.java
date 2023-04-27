package com.netease.arctic.spark.test.suites.sql;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.spark.test.Asserts;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;
import com.netease.arctic.spark.test.helper.DataComparator;
import com.netease.arctic.spark.test.helper.TableFiles;
import com.netease.arctic.spark.test.helper.TestTableHelper;
import com.netease.arctic.spark.test.helper.TestTables;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


// 1. ts handle
// 2. primary key spec/table schema
// 3. data expect
// 4. duplicate check
@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestCreateTableAsSelect extends SparkTableTestBase {


  public static final Schema simpleSourceSchema = TestTables.MixedIceberg.NoPK_PT.schema;
  public static final List<Record> simpleSourceData = TestTables.MixedIceberg.PK_PT.generator.records(1);


  public static Stream<Arguments> testTimestampZoneHandle() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id, pt)", true, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", false, Types.TimestampType.withZone()),
        Arguments.of(TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", true, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_HIVE, "", false, Types.TimestampType.withoutZone())
    );
  }

  @ParameterizedTest
  @MethodSource
  public void testTimestampZoneHandle(
      TableFormat format, String primaryKeyDDL, boolean timestampWithoutZone, Types.TimestampType expectType) {
    createViewSource(simpleSourceSchema, simpleSourceData);

    sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES
        + "`=" + timestampWithoutZone);

    String sqlText = "CREATE TABLE " + target() + " " + primaryKeyDDL
        + " USING " + provider(format) + " AS SELECT * FROM " + source();
    sql(sqlText);

    ArcticTable table = loadTable();
    Types.NestedField f = table.schema().findField("ts");
    Asserts.assertType(expectType, f.type());
  }


  private static PartitionSpec.Builder ptBuilder() {
    return PartitionSpec.builderFor(simpleSourceSchema);
  }

  public static Stream<Arguments> testSchemaAndData() {
    PrimaryKeySpec keyIdPtSpec = PrimaryKeySpec.builderFor(simpleSourceSchema)
        .addColumn("id")
        .addColumn("pt")
        .build();
    PrimaryKeySpec keyIdSpec = PrimaryKeySpec.builderFor(simpleSourceSchema)
        .addColumn("id")
        .build();


    return Stream.of(
//        Arguments.of(TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", "PARTITIONED BY(pt)",
//            keyIdPtSpec, ptBuilder().identity("pt").build()),
//        Arguments.of(TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", "",
//            keyIdPtSpec, PartitionSpec.unpartitioned()),
//        Arguments.of(TableFormat.MIXED_HIVE, "", "PARTITIONED BY(pt)",
//            PrimaryKeySpec.noPrimaryKey(),
//            ptBuilder().identity("pt").build()),
//        Arguments.of(TableFormat.MIXED_HIVE, "", "",
//            PrimaryKeySpec.noPrimaryKey(), PartitionSpec.unpartitioned()),

        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id, pt)", "",
            keyIdPtSpec, PartitionSpec.unpartitioned()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", "PARTITIONED BY(pt,id)",
            PrimaryKeySpec.noPrimaryKey(),
            ptBuilder().identity("pt").identity("id").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(years(ts))",
            keyIdSpec, ptBuilder().year("ts").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(months(ts))",
            keyIdSpec, ptBuilder().month("ts").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(days(ts))",
            keyIdSpec, ptBuilder().day("ts").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(date(ts))",
            keyIdSpec, ptBuilder().day("ts").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(hours(ts))",
            keyIdSpec, ptBuilder().hour("ts").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(date_hour(ts))",
            keyIdSpec, ptBuilder().hour("ts").build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(bucket(10, id))",
            keyIdSpec, ptBuilder().bucket("id", 10).build()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id)", "PARTITIONED BY(truncate(10, data))",
            keyIdSpec, ptBuilder().truncate("data", 10).build())
    );
  }


  @ParameterizedTest
  @MethodSource
  public void testSchemaAndData(
      TableFormat format, String primaryKeyDDL, String partitionDDL,
      PrimaryKeySpec keySpec, PartitionSpec ptSpec
  ) {
    spark.conf().set("spark.sql.session.timeZone", "UTC");
    createViewSource(simpleSourceSchema, simpleSourceData);

    sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES + "`=true" );

    String sqlText = "CREATE TABLE " + target() + " " + primaryKeyDDL
        + " USING " + provider(format) + " " + partitionDDL
        + " AS SELECT * FROM " + source();
    sql(sqlText);

    Schema expectSchema = TestTableHelper.toSchemaWithPrimaryKey(simpleSourceSchema, keySpec);
    expectSchema = TestTableHelper.timestampToWithoutZone(expectSchema);

    ArcticTable table = loadTable();
    Asserts.assertPartition(ptSpec, table.spec());
    Assertions.assertEquals(keySpec.primaryKeyExisted(), table.isKeyedTable());
    if (table.isKeyedTable()) {
      Asserts.assertPrimaryKey(keySpec, table.asKeyedTable().primaryKeySpec());
    }
    Asserts.assertType(expectSchema.asStruct(), table.schema().asStruct());
    TableFiles files = TestTableHelper.files(table);
    Asserts.assertAllFilesInBaseStore(files);

    if (TableFormat.MIXED_HIVE == format) {
      Table hiveTable = loadHiveTable();
      Asserts.assertHiveColumns(expectSchema, ptSpec, hiveTable.getSd().getCols());
      Asserts.assertHivePartition(ptSpec, hiveTable.getPartitionKeys());
      // TODO: CreateTableAsSelect should write to hive location but currently it has a bug. ARCTIC-1403
      // TODO: Add this assert if bug is fixed.
//      Asserts.assertAllFilesInHiveLocation(files, hiveTable.getSd().getLocation());
    }

    Dataset<Row> ds = sql("select * from " + source());
    List<Row> rows = ds.collectAsList();

    Dataset<Row> tDs = sql("select * from " + target());
    List<Row> tRows = tDs.collectAsList();



    List<Record> records = TestTableHelper.tableRecords(table);
    DataComparator.build(simpleSourceData, records)
        .ignoreOrder(Comparator.comparing(r -> (Integer)r.get(0)))
        .assertRecordsEqual();;
  }


}
