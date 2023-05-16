package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BasicMixedIcebergPartitionPlan extends AbstractMixedTablePartitionPlan {

  public BasicMixedIcebergPartitionPlan(boolean hasPrimaryKey, boolean hasPartition) {
    super(new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(hasPrimaryKey, hasPartition));
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
  public void testMixedIceberg() {
    testSimple();
  }

  protected AbstractPartitionPlan getPartitionPlan() {
    if (isKeyedTable()) {
      return new KeyedTablePartitionPlan(tableRuntime, getArcticTable(),
          isPartitionedTable() ? "op_time_day=2022-01-01" : "", System.currentTimeMillis());
    } else {
      return new UnkeyedTablePartitionPlan(tableRuntime, getArcticTable(),
          isPartitionedTable() ? "op_time_day=2022-01-01" : "", System.currentTimeMillis());
    }
  }
}