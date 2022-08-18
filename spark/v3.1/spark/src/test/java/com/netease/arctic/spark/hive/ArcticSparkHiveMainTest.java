package com.netease.arctic.spark.hive;

import com.netease.arctic.spark.SparkTestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestCreateTableDDL.class,
    TestMigrateHiveTable.class})
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
