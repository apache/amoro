package com.netease.arctic.optimizing;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseOptimizingInput implements TableOptimizing.OptimizingInput {

  private final Map<String, String> options = new HashMap<>();

  @Override
  public void option(String name, String value) {
    options.put(name, value);
  }

  @Override
  public void options(Map<String, String> options) {
    options.putAll(options);
  }

  @Override
  public Map<String, String> getOptions() {
    return options;
  }
}
