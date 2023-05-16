package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class KeyedPartitionPlan extends AbstractMixedTablePartitionPlan {

  public KeyedPartitionPlan(boolean hasPartition) {
    super(new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, hasPartition));
  }

  @Parameterized.Parameters(name = "hasPartition={0}")
  public static Object[][] parameters() {
    return new Object[][] {
        {true},
        {false}};
  }

  @Test
  public void testMixedIceberg() {
    testSimple();
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    return new KeyedTablePartitionPlan(tableRuntime, getArcticTable(),
        isPartitionedTable() ? "op_time_day=2022-01-01" : "", System.currentTimeMillis());
  }
}
