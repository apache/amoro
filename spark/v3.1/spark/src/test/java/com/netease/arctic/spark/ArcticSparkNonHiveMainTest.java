package com.netease.arctic.spark;

import com.netease.arctic.spark.hive.TestMigrateHiveTable;
import com.netease.arctic.spark.source.TestKeyedTableDataFrameAPI;
import com.netease.arctic.spark.source.TestUnKeyedTableDataFrameAPI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
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
public class ArcticSparkNonHiveMainTest {

  @BeforeClass
  public static void suiteSetup() throws IOException {
    SparkTestContext.setUpTestDirAndArctic();
    SparkTestContext.cleanUpAdditionSparkConfigs();
    SparkTestContext.setUpSparkSession();
  }
  @AfterClass
  public static void suiteTeardown() {
    SparkTestContext.cleanUpAms();
    SparkTestContext.cleanUpSparkSession();
  }

}
