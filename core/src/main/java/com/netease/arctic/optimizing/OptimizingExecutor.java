package com.netease.arctic.optimizing;

import java.io.Serializable;

public interface OptimizingExecutor<O extends TableOptimizing.OptimizingOutput> extends Serializable {
  O execute();
}
