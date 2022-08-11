package com.netease.arctic.hive;

import com.netease.arctic.hive.catalog.TestArcticHiveCatalog;
import com.netease.arctic.hive.op.TestRewritePartitions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestArcticHiveCatalog.class,
    TestRewritePartitions.class
})
public class ArcticHiveTestMain {

  @BeforeClass
  public static void setup() throws Exception {
    HiveTableTestBase.startMetastore();
  }

  @AfterClass
  public static void cleanDown(){
    HiveTableTestBase.stopMetastore();
  }
}
