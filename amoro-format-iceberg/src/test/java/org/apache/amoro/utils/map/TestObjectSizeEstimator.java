/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.utils.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.util.RamUsageEstimator;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TestObjectSizeEstimator {
  @Test
  void preservesEstimatesForSupportedValues() {
    List<Object> values =
        new ArrayList<>(
            Arrays.asList(
                null,
                true,
                (byte) 1,
                (short) 2,
                3,
                4.5f,
                6.7d,
                Long.valueOf(0),
                Long.valueOf(1000),
                "",
                "ASCII",
                "中文\uD83D\uDE00",
                new boolean[0],
                new boolean[13],
                new byte[13],
                new char[13],
                new short[13],
                new int[13],
                new long[13],
                new float[13],
                new double[13],
                new Object[0],
                new Object[] {new Object(), "text"},
                new String[] {"text", null, "中文"},
                new Child(),
                Collections.emptyList(),
                Collections.emptyMap(),
                Arrays.asList("one", Long.valueOf(1000), new Child())));
    Map<Object, Object> map = new LinkedHashMap<>();
    map.put("key", Arrays.asList("nested", new Child()));
    map.put(7, Collections.singletonMap("next", Collections.singletonList("value")));
    values.add(map);
    Map<Object, Object> cycle = new HashMap<>();
    cycle.put("self", cycle);
    values.add(cycle);
    for (Object value : values) {
      assertEquals(
          RamUsageEstimator.sizeOfObject(value, 0),
          ObjectSizeEstimator.sizeOf(value),
          value == null ? "null" : value.getClass().getName());
    }
  }

  @Test
  void handlesEveryArrayLengthAndLongCacheBoundary() {
    for (int size = 0; size < 129; size++) {
      assertEquals(
          RamUsageEstimator.sizeOfObject(new byte[size], 0),
          ObjectSizeEstimator.sizeOf(new byte[size]));
      assertEquals(
          RamUsageEstimator.sizeOfObject(new Object[size], 0),
          ObjectSizeEstimator.sizeOf(new Object[size]));
    }
    for (long value = -130; value <= 130; value++) {
      assertEquals(
          RamUsageEstimator.sizeOfObject(Long.valueOf(value), 0),
          ObjectSizeEstimator.sizeOf(Long.valueOf(value)));
    }
  }

  @Test
  void defaultEstimatorRunsWithoutLuceneOnTheRuntimeClasspath() throws Exception {
    URL classes = DefaultSizeEstimator.class.getProtectionDomain().getCodeSource().getLocation();
    try (URLClassLoader isolated =
        new URLClassLoader(new URL[] {classes}, ClassLoader.getPlatformClassLoader())) {
      Class<?> type = isolated.loadClass(DefaultSizeEstimator.class.getName());
      Object estimator = type.getConstructor().newInstance();
      assertEquals(
          RamUsageEstimator.sizeOfObject("payload", 0),
          type.getMethod("sizeEstimate", Object.class).invoke(estimator, "payload"));
    }
  }

  private static class Parent {
    private static final int IGNORED = 3;
    private int number;
    private Object reference;
  }

  private static class Child extends Parent {
    private byte small;
    private double decimal;
  }
}
