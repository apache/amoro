package com.netease.arctic.ams.server.utils;

import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import java.util.Arrays;

public class TableOptimizeStatusUtil {
  public static boolean in(TableOptimizeRuntime.OptimizeStatus status,
      TableOptimizeRuntime.OptimizeStatus... statuses) {
    return Arrays.stream(statuses).anyMatch(s -> s.equals(status));
  }

  public static boolean notIn(TableOptimizeRuntime.OptimizeStatus status,
      TableOptimizeRuntime.OptimizeStatus... statuses) {
    return Arrays.stream(statuses).noneMatch(s -> s.equals(status));
  }
}