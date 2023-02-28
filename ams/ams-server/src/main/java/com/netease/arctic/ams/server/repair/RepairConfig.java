package com.netease.arctic.ams.server.repair;

public class RepairConfig {

  public String thriftUrl;
  public String catalogName;
  public Integer maxFindSnapshotNum;
  public Integer maxRollbackSnapNum;

  public RepairConfig(String thriftUrl, String catalogName, Integer maxFindSnapshotNum, Integer maxRollbackSnapNum) {
    this.thriftUrl = thriftUrl;
    this.catalogName = catalogName;
    this.maxFindSnapshotNum = maxFindSnapshotNum;
    this.maxRollbackSnapNum = maxRollbackSnapNum;
  }

  public String getThriftUrl() {
    return thriftUrl;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public Integer getMaxFindSnapshotNum() {
    return maxFindSnapshotNum;
  }

  public Integer getMaxRollbackSnapNum() {
    return maxRollbackSnapNum;
  }

}
