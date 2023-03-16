package com.netease.arctic.ams.server.maintainer;

import org.junit.Assert;
import org.junit.Test;

public class TestGetMaintainerConfig {

  @Test
  public void testGetConfig() {
    MaintainerConfig maintainerConfig = MaintainerMain.getMaintainerConfig(new String[]{
        "thrift://localhost:1260/my_catalog",
        "src/test/resources/test.yaml"
    });
    Assert.assertEquals("my_catalog", maintainerConfig.getCatalogName());
    Assert.assertEquals("thrift://localhost:1260", maintainerConfig.getThriftUrl());
    Assert.assertEquals(666, maintainerConfig.getMaxFindSnapshotNum().intValue());
    Assert.assertEquals(888, maintainerConfig.getMaxRollbackSnapNum().intValue());

    MaintainerConfig maintainerConfig2 = MaintainerMain.getMaintainerConfig(new String[]{
        "thrift://localhost:1260",
        "src/test/resources/test.yaml"
    });
    Assert.assertEquals(null, maintainerConfig2.getCatalogName());
    Assert.assertEquals("thrift://localhost:1260", maintainerConfig2.getThriftUrl());
    Assert.assertEquals(666, maintainerConfig2.getMaxFindSnapshotNum().intValue());
    Assert.assertEquals(888, maintainerConfig2.getMaxRollbackSnapNum().intValue());
  }

}
