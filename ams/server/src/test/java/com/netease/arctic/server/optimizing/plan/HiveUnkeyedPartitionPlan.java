package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

@RunWith(Parameterized.class)
public class HiveUnkeyedPartitionPlan extends UnkeyedPartitionPlan {
  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public HiveUnkeyedPartitionPlan(CatalogTestHelper catalogTestHelper,
                                  TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(false, true)},
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(false, false)}};
  }

  @Override
  protected void assertTaskProperties(Map<String, String> expect, Map<String, String> actual) {
    actual = Maps.newHashMap(actual);
    String outputDir = actual.remove(OptimizingInputProperties.OUTPUT_DIR);
    Assert.assertTrue(Long.parseLong(outputDir.split("_")[1]) > 0);
    super.assertTaskProperties(expect, actual);
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    SupportHive hiveTable = (SupportHive) getArcticTable();
    String hiveLocation = hiveTable.hiveLocation();
    return new HiveUnkeyedTablePartitionPlan(tableRuntime, getArcticTable(), getPartition(), hiveLocation,
        System.currentTimeMillis());
  }
}
