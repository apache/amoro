package com.netease.arctic.optimizing;

import java.io.Serializable;
import java.util.Map;

public interface OptimizingCommitterFactory<O extends TableOptimizing.OptimizingOutput> extends Serializable {

  OptimizingCommitter createCommitter(O[] outputs, Map<String, String> properties);

  interface OptimizingCommitter extends Serializable {

    void commit();

    Map<String, String> summary();
  }
}
