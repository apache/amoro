package com.netease.arctic.spark;


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
        SparkTestContext.cleanUpAdditionSparkConfigs();
        SparkTestContext.setUpSparkSession();
    }
    @AfterClass
    public static void suiteTeardown() {
        SparkTestContext.cleanUpAms();
        SparkTestContext.cleanUpSparkSession();
    }
}
