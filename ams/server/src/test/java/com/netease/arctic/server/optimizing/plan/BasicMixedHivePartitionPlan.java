package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.table.SupportHive;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BasicMixedHivePartitionPlan extends AbstractMixedTablePartitionPlan {
  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public BasicMixedHivePartitionPlan(boolean hasPrimaryKey, boolean hasPartition) {
    super(new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(hasPrimaryKey, hasPartition));
  }

  @Parameterized.Parameters(name = "hasPrimaryKey={0}，hasPartition={1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {true, true},
        {true, false},
        {false, true},
        {false, false}};
  }

  @Test
  public void testHive() {
    testSimple();
  }


  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    SupportHive hiveTable = (SupportHive) getArcticTable();
    String hiveLocation = hiveTable.hiveLocation();
    if (isKeyedTable()) {
      return new HiveKeyedTablePartitionPlan(tableRuntime, getArcticTable(),
          isPartitionedTable() ? "op_time_day=2022-01-01" : "", hiveLocation, System.currentTimeMillis());
    } else {
      return new HiveUnkeyedTablePartitionPlan(tableRuntime, getArcticTable(),
          isPartitionedTable() ? "op_time_day=2022-01-01" : "", hiveLocation, System.currentTimeMillis());
    }
  }
}
