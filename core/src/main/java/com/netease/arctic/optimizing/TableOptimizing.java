package com.netease.arctic.optimizing;

import java.io.Serializable;
import java.util.Map;

public interface TableOptimizing<I extends TableOptimizing.OptimizingInput,
    O extends TableOptimizing.OptimizingOutput> {

  I[] planInputs();

  OptimizingExecutorFactory<I> createExecutorFactory();

  OptimizingCommitterFactory<O> createCommitterFactory();

  interface OptimizingInput extends Serializable {

    void option(String name, String value);

    void options(Map<String, String> options);

    Map<String, String> getOptions();
  }

  interface OptimizingOutput extends Serializable {
    Map<String, String> summary();
  }

}
