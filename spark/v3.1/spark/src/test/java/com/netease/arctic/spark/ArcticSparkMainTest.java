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
    TestMigrateHiveTable.class})
public class ArcticSparkMainTest {
  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @BeforeClass
  public static void suiteSetup() throws IOException {
    sparkTestContext.setUpTestDirAndArctic();
    sparkTestContext.cleanUpAdditionSparkConfigs();
    sparkTestContext.setUpSparkSession();
  }
  @AfterClass
  public static void suiteTeardown() {
    sparkTestContext.cleanUpAms();
    sparkTestContext.cleanUpSparkSession();
  }

}
