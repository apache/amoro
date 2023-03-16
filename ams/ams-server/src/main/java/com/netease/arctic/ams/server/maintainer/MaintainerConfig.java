package com.netease.arctic.ams.server.maintainer;

public class MaintainerConfig {

  private String thriftUrl;
  private String catalogName;
  private Integer maxFindSnapshotNum;
  private Integer maxRollbackSnapNum;

  public MaintainerConfig(String thriftUrl, String catalogName,
      Integer maxFindSnapshotNum, Integer maxRollbackSnapNum) {
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
