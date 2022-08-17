package com.netease.arctic.spark.hive;

import com.netease.arctic.spark.SparkTestContext;
import com.netease.arctic.spark.hive.SparkHiveTestContext;
import com.netease.arctic.spark.hive.TestCreateTableDDL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestCreateTableDDL.class})
public class ArcticSparkHiveMainTest {

  @BeforeClass
  public static void suiteSetup() throws IOException {
    SparkHiveTestContext.setUpTestDirAndArctic();
    SparkHiveTestContext.setUpHMS();
    SparkTestContext.cleanUpAdditionSparkConfigs();
    SparkHiveTestContext.setUpSparkSession();
  }
  @AfterClass
  public static void suiteTeardown() {
    SparkHiveTestContext.cleanUpHive();
    SparkTestContext.cleanUpAms();
    SparkTestContext.cleanUpSparkSession();
  }
}
