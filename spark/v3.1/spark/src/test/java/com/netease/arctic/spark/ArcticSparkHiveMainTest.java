package com.netease.arctic.spark;

import com.netease.arctic.spark.hive.SparkHiveTestContext;
import com.netease.arctic.spark.hive.TestMigrateHiveTable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestMigrateHiveTable.class})
public class ArcticSparkHiveMainTest {

  @BeforeClass
  public static void suiteSetup() throws IOException {
    SparkTestContext.setUpTestDirAndArctic();
    SparkHiveTestContext.setUpHMS();
    SparkTestContext.setUpSparkSession();
  }
  @AfterClass
  public static void suiteTeardown() {
    SparkHiveTestContext.cleanUpHive();
    SparkTestContext.cleanUpAms();
    SparkTestContext.cleanUpSparkSession();
  }
}
