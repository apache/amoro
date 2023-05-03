package com.netease.arctic.ams.server.optimizing.plan;

import com.netease.arctic.optimizing.RewriteFilesInput;
import java.util.Map;

public class TaskDescriptor {
  private String partition;
  private RewriteFilesInput input;
  private Map<String, String> properties;

  TaskDescriptor(String partition, RewriteFilesInput input, Map<String, String> properties) {
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
}
