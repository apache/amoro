package com.netease.arctic.spark;

import com.netease.arctic.spark.hive.TestCreateTableDDL;
import com.netease.arctic.spark.hive.TestMigrateHiveTable;
import com.netease.arctic.spark.source.TestKeyedTableDataFrameAPI;
import com.netease.arctic.spark.source.TestUnKeyedTableDataFrameAPI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestCreateTableDDL.class,
    TestMigrateHiveTable.class,
    TestHiveTableMergeOnRead.class,
    TestAlterKeyedTable.class,
    TestKeyedTableDDL.class,
    TestKeyedTableDML.class,
    TestKeyedTableDMLInsertOverwriteDynamic.class,
    TestKeyedTableDMLInsertOverwriteStatic.class,
    TestUnKeyedTableDDL.class,
    TestMigrateNonHiveTable.class,
    TestOptimizeWrite.class,
    TestUnKeyedTableDML.class,
    TestKeyedTableDataFrameAPI.class,
    TestUnKeyedTableDataFrameAPI.class,
    TestCreateKeyedTableAsSelect.class})
public class ArcticSparkHiveMainTest {

  @BeforeClass
  public static void suiteSetup() throws IOException {
    SparkTestContext.startAll();
  }

  @AfterClass
  public static void suiteTeardown() {
    SparkTestContext.stopAll();
  }
}
