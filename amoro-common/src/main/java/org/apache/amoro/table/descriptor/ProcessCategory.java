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

package org.apache.amoro.table.descriptor;

import java.util.Arrays;

/**
 * Categories for table processes displayed in the dashboard.
 * <li>OPTIMIZING: Performance optimization processes (e.g., compaction, clustering).
 * <li>CLEANUP: Space reclamation and lifecycle management processes (e.g., expire snapshots, clean
 *     orphan files).
 * <li>PROFILING: Information enrichment and metadata augmentation processes (e.g., auto create
 *     tags).
 */
public enum ProcessCategory {
  OPTIMIZING("OPTIMIZING"),
  CLEANUP("CLEANUP"),
  PROFILING("PROFILING");

  private final String name;

  ProcessCategory(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static ProcessCategory fromString(String value) {
    if (value == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(c -> c.name.equalsIgnoreCase(value))
        .findFirst()
        .orElse(null);
  }
}
