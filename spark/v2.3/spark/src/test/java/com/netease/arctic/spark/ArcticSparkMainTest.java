package com.netease.arctic.spark;


import com.netease.arctic.spark.hive.SparkHiveTestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
@RunWith(Suite.class)
@Suite.SuiteClasses({})
public class ArcticSparkMainTest {
    @BeforeClass
    public static void suiteSetup() throws IOException {
        SparkTestContext.setUpTestDirAndArctic();
        SparkHiveTestContext.setUpHMS();
        SparkTestContext.cleanUpAdditionSparkConfigs();
        SparkTestContext.setUpSparkSession();
    }
    @AfterClass
    public static void suiteTeardown() {
        SparkHiveTestContext.cleanUpHive();
        SparkTestContext.cleanUpAms();
        SparkTestContext.cleanUpSparkSession();
    }
}
