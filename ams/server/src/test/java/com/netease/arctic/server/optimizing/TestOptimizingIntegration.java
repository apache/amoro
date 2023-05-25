package com.netease.arctic.server.optimizing;

import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.AmsEnvironment;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.Tables;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Disabled
public class TestOptimizingIntegration {

  private static AmsEnvironment amsEnv;

  @TempDir(cleanup = CleanupMode.ALWAYS)
  public static File TEMP_DIR;

  private static final String DATABASE = "test_db";
  private static final TableIdentifier ICEBERG_TB_1 = TableIdentifier.of(AmsEnvironment.ICEBERG_CATALOG, DATABASE,
      "iceberg_table1");
  private static final TableIdentifier ICEBERG_TB_2 =
      TableIdentifier.of(AmsEnvironment.ICEBERG_CATALOG, DATABASE, "iceberg_table2");
  private static final TableIdentifier ICEBERG_TB_3 =
      TableIdentifier.of(AmsEnvironment.ICEBERG_CATALOG, DATABASE, "iceberg_table3");
  private static final TableIdentifier ICEBERG_TB_4 =
      TableIdentifier.of(AmsEnvironment.ICEBERG_CATALOG, DATABASE, "iceberg_table4");
  private static final TableIdentifier ICEBERG_TB_5 =
      TableIdentifier.of(AmsEnvironment.ICEBERG_CATALOG, DATABASE, "iceberg_table5");
  private static final TableIdentifier MIXED_ICEBERG_TB_1 =
      TableIdentifier.of(AmsEnvironment.MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table1");
  private static final TableIdentifier MIXED_ICEBERG_TB_2 =
      TableIdentifier.of(AmsEnvironment.MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table2");
  private static final TableIdentifier MIXED_ICEBERG_TB_3 =
      TableIdentifier.of(AmsEnvironment.MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table3");
  private static final TableIdentifier MIXED_ICEBERG_TB_4 =
      TableIdentifier.of(AmsEnvironment.MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table4");
  private static final TableIdentifier MIXED_ICEBERG_TB_5 =
      TableIdentifier.of(AmsEnvironment.MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table5");
  private static final TableIdentifier MIXED_ICEBERG_TB_6 =
      TableIdentifier.of(AmsEnvironment.MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table6");
  private static final TableIdentifier MIXED_HIVE_TB_1 =
      TableIdentifier.of(AmsEnvironment.MIXED_HIVE_CATALOG, DATABASE, "mix_hive_table1");
  private static final TableIdentifier MIXED_HIVE_TB_2 = TableIdentifier.of(AmsEnvironment.MIXED_HIVE_CATALOG,
      DATABASE, "mix_hive_table2");
  private static final Schema SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "name", Types.StringType.get()),
      Types.NestedField.required(3, "op_time", Types.TimestampType.withZone())
  );
  private static final PartitionSpec SPEC = PartitionSpec.builderFor(SCHEMA)
      .day("op_time").build();
  private static final PrimaryKeySpec PRIMARY_KEY = PrimaryKeySpec.builderFor(SCHEMA)
      .addColumn("id").build();

  @BeforeAll
  public static void before() throws Exception {
    String rootPath = TEMP_DIR.getAbsolutePath();
    amsEnv = new AmsEnvironment(rootPath);
    amsEnv.start();
    amsEnv.startOptimizer();
  }

  @AfterAll
  public static void after() throws IOException {
    amsEnv.stop();
  }

  @Test
  public void testIcebergTableFullOptimize() throws IOException {
    Table table = createIcebergTable(ICEBERG_TB_1, PartitionSpec.unpartitioned());
    assertTableExist(ICEBERG_TB_1);
    TestIcebergHadoopOptimizing testCase = new TestIcebergHadoopOptimizing(ICEBERG_TB_1, table);
    testCase.testIcebergTableFullOptimize();
  }

  @Test
  public void testIcebergTableOptimizing() throws IOException {
    Table table = createIcebergTable(ICEBERG_TB_2, PartitionSpec.unpartitioned());
    assertTableExist(ICEBERG_TB_2);
    TestIcebergHadoopOptimizing testCase = new TestIcebergHadoopOptimizing(ICEBERG_TB_2, table);
    testCase.testIcebergTableOptimizing();
  }

  @Test
  public void testPartitionIcebergTableOptimizing() throws IOException {
    Table table = createIcebergTable(ICEBERG_TB_3, SPEC);
    assertTableExist(ICEBERG_TB_3);
    TestIcebergHadoopOptimizing testCase = new TestIcebergHadoopOptimizing(ICEBERG_TB_3, table);
    testCase.testPartitionIcebergTableOptimizing();
  }

  @Test
  public void testV1IcebergTableOptimizing() throws IOException {
    Table table = createIcebergV1Table(ICEBERG_TB_4, PartitionSpec.unpartitioned());
    assertTableExist(ICEBERG_TB_4);
    TestIcebergHadoopOptimizing testCase = new TestIcebergHadoopOptimizing(ICEBERG_TB_4, table);
    testCase.testV1IcebergTableOptimizing();
  }

  @Test
  public void testPartitionIcebergTablePartialOptimizing() throws IOException {
    Table table = createIcebergTable(ICEBERG_TB_5, SPEC);
    assertTableExist(ICEBERG_TB_5);
    TestIcebergHadoopOptimizing testCase = new TestIcebergHadoopOptimizing(ICEBERG_TB_5, table);
    testCase.testPartitionIcebergTablePartialOptimizing();
  }

  @Test
  public void testPkTableOptimizing() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_1, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_1);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testKeyedTableContinueOptimizing();
  }

  @Test
  public void testPkPartitionTableOptimizing() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_2, PRIMARY_KEY, SPEC);
    assertTableExist(MIXED_ICEBERG_TB_2);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testKeyedTableContinueOptimizing();
  }

  @Test
  public void testPkTableMajorOptimizeLeftPosDelete() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_3, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_3);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testPkTableMajorOptimizeLeftPosDelete();
  }

  @Test
  public void testNoPkTableOptimizing() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_4, PrimaryKeySpec.noPrimaryKey(),
        PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_4);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testNoPkTableOptimizing();
  }

  @Test
  public void testNoPkPartitionTableOptimizing() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_5, PrimaryKeySpec.noPrimaryKey(), SPEC);
    assertTableExist(MIXED_ICEBERG_TB_5);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testNoPkPartitionTableOptimizing();
  }

  @Test
  public void testKeyedTableTxIdNotInOrder() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_6, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_6);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testKeyedTableTxIdNotInOrder();
  }

  @Test
  public void testPartitionArcticTablePartialOptimizing() {
    ArcticTable arcticTable = createArcticTable(MIXED_ICEBERG_TB_6, PRIMARY_KEY, SPEC);
    assertTableExist(MIXED_ICEBERG_TB_6);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(arcticTable);
    testCase.testPartitionArcticTablePartialOptimizing();
  }

  @Test
  public void testHiveKeyedTableMajorOptimizeNotMove() throws TException, IOException {
    createHiveArcticTable(MIXED_HIVE_TB_1, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_HIVE_TB_1);
    KeyedTable table = amsEnv.catalog(AmsEnvironment.MIXED_HIVE_CATALOG).loadTable(MIXED_HIVE_TB_1).asKeyedTable();
    TestMixedHiveOptimizing testCase =
        new TestMixedHiveOptimizing(table, amsEnv.getTestHMS().getHiveClient());
    testCase.testHiveKeyedTableMajorOptimizeNotMove();
  }

  @Test
  public void testHiveKeyedTableMajorOptimizeAndMove() throws TException, IOException {
    createHiveArcticTable(MIXED_HIVE_TB_2, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_HIVE_TB_2);
    KeyedTable table = amsEnv.catalog(AmsEnvironment.MIXED_HIVE_CATALOG).loadTable(MIXED_HIVE_TB_2).asKeyedTable();
    TestMixedHiveOptimizing testCase =
        new TestMixedHiveOptimizing(table, amsEnv.getTestHMS().getHiveClient());
    testCase.testHiveKeyedTableMajorOptimizeAndMove();
  }

  private Table createIcebergTable(TableIdentifier tableIdentifier, PartitionSpec partitionSpec) {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2");

    return hadoopTables.create(SCHEMA, partitionSpec, tableProperties,
        String.join("/",
            amsEnv.catalog(AmsEnvironment.ICEBERG_CATALOG).properties().get(CatalogMetaProperties.KEY_WAREHOUSE),
            tableIdentifier.getDatabase(), tableIdentifier.getTableName()));
  }

  private Table createIcebergV1Table(TableIdentifier tableIdentifier, PartitionSpec partitionSpec) {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "1");
    tableProperties.put(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2");

    return hadoopTables.create(SCHEMA, partitionSpec, tableProperties,
        String.join("/",
            amsEnv.catalog(AmsEnvironment.ICEBERG_CATALOG).properties().get(CatalogMetaProperties.KEY_WAREHOUSE),
            tableIdentifier.getDatabase(), tableIdentifier.getTableName()));
  }

  private ArcticTable createArcticTable(
      TableIdentifier tableIdentifier, PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {

    TableBuilder tableBuilder =
        amsEnv.catalog(AmsEnvironment.MIXED_ICEBERG_CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
            .withPrimaryKeySpec(primaryKeySpec)
            .withPartitionSpec(partitionSpec)
            .withProperty(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "1000");

    return tableBuilder.create();
  }

  private void createHiveArcticTable(
      TableIdentifier tableIdentifier, PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {
    TableBuilder tableBuilder =
        amsEnv.catalog(AmsEnvironment.MIXED_HIVE_CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
            .withPrimaryKeySpec(primaryKeySpec)
            .withPartitionSpec(partitionSpec)
            .withProperty(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "1000");

    tableBuilder.create();
  }

  private void assertTableExist(TableIdentifier tableIdentifier) {
    Assertions.assertTrue(amsEnv.tableExist(tableIdentifier));
  }
}
