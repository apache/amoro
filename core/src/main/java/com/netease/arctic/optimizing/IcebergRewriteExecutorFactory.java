package com.netease.arctic.optimizing;

import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;

public class IcebergRewriteExecutorFactory implements OptimizingExecutorFactory<RewriteFilesInput> {

  private Map<String, String> properties;

  @Override
  public void initialize(Map<String, String> properties) {
    this.properties = Maps.newHashMap(properties);
  }

  @Override
  public OptimizingExecutor createExecutor(RewriteFilesInput input) {
    OptimizingInputProperties optimizingConfig = OptimizingInputProperties.parse(properties);
    return new IcebergRewriteExecutor(input, input.getTable(), optimizingConfig.getStructLikeCollections());
  }
}
