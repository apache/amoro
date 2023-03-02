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
    Assert.assertEquals("my_catalog", repairConfig.getCatalogName());
    Assert.assertEquals("thrift://localhost:1260", repairConfig.getThriftUrl());
    Assert.assertEquals(666, repairConfig.getMaxFindSnapshotNum().intValue());
    Assert.assertEquals(888, repairConfig.getMaxRollbackSnapNum().intValue());

    RepairConfig repairConfig2 = RepairMain.getRepairConfig(new String[]{
        "thrift://localhost:1260",
        "src/test/resources/test.yaml"
    });
    Assert.assertEquals(null, repairConfig2.getCatalogName());
    Assert.assertEquals("thrift://localhost:1260", repairConfig2.getThriftUrl());
    Assert.assertEquals(666, repairConfig2.getMaxFindSnapshotNum().intValue());
    Assert.assertEquals(888, repairConfig2.getMaxRollbackSnapNum().intValue());
  }

}
