package com.netease.arctic.optimizing;

import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;

import java.util.Map;

public class RewriteFilesOutput implements TableOptimizing.OptimizingOutput {
  private final DataFile[] dataFiles;
  private final DeleteFile[] deleteFiles;
  private final Map<String, String> summary;

  public RewriteFilesOutput(DataFile[] dataFiles, DeleteFile[] deleteFiles, Map<String, String> summary) {
    this.dataFiles = dataFiles;
    this.deleteFiles = deleteFiles;
    this.summary = summary;
  }

  public DataFile[] getDataFiles() {
    return dataFiles;
  }

  public DeleteFile[] getDeleteFiles() {
    return deleteFiles;
  }

  @Override
  public Map<String, String> summary() {
    return summary;
  }
}
