package com.netease.arctic.optimizing;

import com.netease.arctic.utils.map.StructLikeCollections;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.apache.iceberg.relocated.com.google.common.base.Preconditions.checkNotNull;

public class OptimizingInputProperties {

  public static final String ENABLE_SPILL_MAP = "enable_spill_map";

  public static final String MAX_IN_MEMORY_SIZE_IN_BYTES = "max_size_in_memory";

  public static final String OUTPUT_DIR = "output_location";

  public static final String MOVE_FILE_TO_HIVE_LOCATION = "move-files-to-hive-location";

  private Map<String, String> properties;

  private OptimizingInputProperties(Map<String, String> properties) {
    this.properties = checkNotNull(properties);
  }

  public OptimizingInputProperties() {
    properties = new HashMap<>();
  }

  public static OptimizingInputProperties parse(Map<String, String> properties) {
    return new OptimizingInputProperties(properties);
  }

  public OptimizingInputProperties enableSpillMap() {
    properties.put(ENABLE_SPILL_MAP, "true");
    return this;
  }

  public OptimizingInputProperties setMaxSizeInMemory(long maxSizeInMemory) {
    properties.put(MAX_IN_MEMORY_SIZE_IN_BYTES, String.valueOf(maxSizeInMemory));
    return this;
  }

  public OptimizingInputProperties setOutputDir(String outputDir) {
    properties.put(OUTPUT_DIR, outputDir);
    return this;
  }

  public OptimizingInputProperties needMoveFile2HiveLocation() {
    properties.put(MOVE_FILE_TO_HIVE_LOCATION, "true");
    return this;
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

  public boolean getMoveFile2HiveLocation() {
    String s = properties.get(MOVE_FILE_TO_HIVE_LOCATION);
    if (StringUtils.isBlank(s)) {
      return false;
    }
    return Boolean.parseBoolean(s);
  }
}
