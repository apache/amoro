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

package org.apache.amoro;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;

import java.util.Map;

/**
 * Table formats Amoro supported
 *
 * @since 0.4.0
 */
public final class TableFormat {
  private static final Map<String, TableFormat> registeredFormats = Maps.newConcurrentMap();

  /** Open-source table formats */
  public static final TableFormat ICEBERG = register("ICEBERG");

  public static final TableFormat MIXED_ICEBERG = register("MIXED_ICEBERG");
  public static final TableFormat MIXED_HIVE = register("MIXED_ICEBERG");
  public static final TableFormat PAIMON = register("PAIMON");
  public static final TableFormat HUDI = register("HUDI");

  /**
   * Get all registered formats
   *
   * @return registered formats
   */
  public static TableFormat[] values() {
    return registeredFormats.values().toArray(new TableFormat[0]);
  }

  /**
   * Register a new TableFormat
   *
   * @param name table format name
   * @return TableFormat.
   */
  public static TableFormat register(String name) {
    return registeredFormats.computeIfAbsent(name, s -> new TableFormat(name));
  }

  /**
   * Get TableFormat by name
   *
   * @param name name
   * @return TableFormat
   */
  public static TableFormat valueOf(String name) {
    return registeredFormats.get(name);
  }

  private final String name;

  private TableFormat(String name) {
    Preconditions.checkNotNull(name, "TableFormat name should not be null");
    this.name = name;
  }

  public String name() {
    return name;
  }

  public boolean in(TableFormat... tableFormats) {
    for (TableFormat tableFormat : tableFormats) {
      if (this.equals(tableFormat)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return this.name.equals(((TableFormat) other).name);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}
