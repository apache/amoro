package com.netease.arctic.ams.server.model;

/**
 * @Auth: hzwangtao6
 * @Time: 2022/8/9 14:17
 * @Description:
 */
public class TableOperation {
  long ts;
  String operation;

  public TableOperation() {
  }

  public TableOperation(long ts, String operation) {
    this.ts = ts;
    this.operation = operation;
  }

  public long getTs() {
    return ts;
  }

  public void setTs(long ts) {
    this.ts = ts;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public static TableOperation buildFromDDLInfo(DDLInfo ddlInfo) {
    return ddlInfo != null ? new TableOperation(ddlInfo.getCommitTime(), ddlInfo.getDdl()) : null;
  }
}
