package com.netease.arctic.optimizing;

import com.netease.arctic.utils.map.StructLikeCollections;

import java.util.Map;

import static org.apache.iceberg.relocated.com.google.common.base.Preconditions.checkNotNull;

public class OptimizingConfig {

  public static final String ENABLE_SPILL_MAP = "";

  public static final String MAX_IN_MEMORY_SIZE_IN_BYTES = "";

  public static final String OUTPUT_DIR = "";


  private Map<String, String> properties;

  public OptimizingConfig(Map<String, String> properties) {
    this.properties = checkNotNull(properties);
  }

  public StructLikeCollections getStructLikeCollections() {
    String enableSpillMapStr = properties.get(ENABLE_SPILL_MAP);
    boolean enableSpillMap = enableSpillMapStr == null ? false : Boolean.parseBoolean(enableSpillMapStr);

    String maxInMemoryStr = properties.get(MAX_IN_MEMORY_SIZE_IN_BYTES);
    Long maxInMemory = maxInMemoryStr == null ? null : Long.parseLong(maxInMemoryStr);

    return new StructLikeCollections(enableSpillMap, maxInMemory);
  }

  public String getOutputDir() {
    return properties.get(OUTPUT_DIR);
  }

}
