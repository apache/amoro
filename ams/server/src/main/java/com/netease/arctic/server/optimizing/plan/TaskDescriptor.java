package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.optimizing.RewriteFilesInput;

import java.util.Map;

public class TaskDescriptor {
  private final long tableId;
  private final String partition;
  private final RewriteFilesInput input;
  private final Map<String, String> properties;

  TaskDescriptor(long tableId, String partition, RewriteFilesInput input, Map<String, String> properties) {
    this.tableId = tableId;
    this.partition = partition;
    this.input = input;
    this.properties = properties;
  }

  public String getPartition() {
    return partition;
  }

  public RewriteFilesInput getInput() {
    return input;
  }

  public Map<String, String> properties() {
    return properties;
  }

  public long getTableId() {
    return tableId;
  }
}
