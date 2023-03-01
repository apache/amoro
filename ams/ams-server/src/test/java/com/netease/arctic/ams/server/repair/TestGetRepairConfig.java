package com.netease.arctic.ams.server.repair;

import org.junit.Assert;
import org.junit.Test;

public class TestGetRepairConfig {

  @Test
  public void testGetConfig() {
    RepairConfig repairConfig = RepairMain.getRepairConfig(new String[]{
        "thrift://localhost:1260/my_catalog",
        "src/test/resources/test.yaml"
    });
    Assert.assertEquals(repairConfig.getCatalogName(), "my_catalog");
    Assert.assertEquals(repairConfig.getThriftUrl(), "thrift://localhost:1260");
    Assert.assertEquals(repairConfig.getMaxFindSnapshotNum().intValue(), 666);
    Assert.assertEquals(repairConfig.getMaxRollbackSnapNum().intValue(), 888);

    RepairConfig repairConfig2 = RepairMain.getRepairConfig(new String[]{
        "thrift://localhost:1260",
        "src/test/resources/test.yaml"
    });
    Assert.assertEquals(repairConfig2.getCatalogName(), "");
    Assert.assertEquals(repairConfig2.getThriftUrl(), "thrift://localhost:1260");
    Assert.assertEquals(repairConfig2.getMaxFindSnapshotNum().intValue(), 666);
    Assert.assertEquals(repairConfig2.getMaxRollbackSnapNum().intValue(), 888);
  }

}
