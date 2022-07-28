package com.netease.arctic.spark.optimize;

import com.netease.arctic.spark.SparkTestContext;
import com.netease.arctic.spark.TestAlterKeyedTable;
import com.netease.arctic.spark.TestKeyedTableDDL;
import com.netease.arctic.spark.TestKeyedTableDML;
import com.netease.arctic.spark.TestKeyedTableDMLInsertOverwriteDynamic;
import com.netease.arctic.spark.TestKeyedTableDMLInsertOverwriteStatic;
import com.netease.arctic.spark.TestMigrateNonHiveTable;
import com.netease.arctic.spark.TestOptimizeWrite;
import com.netease.arctic.spark.TestUnKeyedTableDDL;
import com.netease.arctic.spark.TestUnKeyedTableDML;
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
    TestUnKeyedTableDataFrameAPI.class})
public class TestMain1 {
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
