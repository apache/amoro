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
    System.out.println("================== begin arctic hive test ==================");
    HiveTableTestBase.startMetastore();
  }

  @AfterClass
  public static void cleanDown(){
    System.out.println("================== end arctic hive test ===================");
    HiveTableTestBase.stopMetastore();
  }
}
