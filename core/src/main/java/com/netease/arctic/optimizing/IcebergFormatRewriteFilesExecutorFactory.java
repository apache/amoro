package com.netease.arctic.optimizing;

import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;

public class IcebergFormatRewriteFilesExecutorFactory implements OptimizingExecutorFactory<RewriteFilesInput> {

  private Map<String, String> properties;

  @Override
  public void initialize(Map<String, String> properties) {
    this.properties = Maps.newHashMap(properties);
  }

  @Override
  public OptimizingExecutor createExecutor(RewriteFilesInput input) {
    OptimizingConfig optimizingConfig = new OptimizingConfig(properties);
    return new IcebergFormatRewriteFilesExecutor(input, input.getTable(), optimizingConfig.getStructLikeCollections());
  }
}
