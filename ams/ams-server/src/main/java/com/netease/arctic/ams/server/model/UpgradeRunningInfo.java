package com.netease.arctic.ams.server.model;

import com.netease.arctic.table.TableIdentifier;

/**
 * Created by shendanfeng on 2022/8/16.
 */
public class UpgradeRunningInfo {
  private String status = UpgradeStatus.UPGRADING.getName();
  private String errorMessage = "";

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
