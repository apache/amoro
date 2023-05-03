package com.netease.arctic.optimizing;

import java.io.Serializable;
import java.util.Map;

public interface OptimizingExecutorFactory<I extends TableOptimizing.OptimizingInput> extends Serializable {

  void initialize(Map<String, String> properties);

  OptimizingExecutor createExecutor(I input);
}
