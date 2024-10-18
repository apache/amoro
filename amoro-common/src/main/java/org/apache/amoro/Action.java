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

import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.thrift.org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Action {

  private static final Map<Integer, Action> ACTIONS = Maps.newConcurrentMap();

  private static final TableFormat[] DEFAULT_FORMATS =
      new TableFormat[] {TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE};

  static {
    // default optimizing action
    register(1, 10, "rewrite");
    // expire all metadata and data files necessarily.
    register(4, 1, "expire-data");
    // delete orphan files
    register(5, 2, "delete-orphans");
    // sync optimizing commit to hive
    register(6, 3, "sync-hive");
  }

  /** supported table formats of this action */
  private final TableFormat[] formats;
  /**
   * storage code of this action, normally this code should be identical within supported formats
   */
  private final int code;
  /**
   * the weight number of this action, the bigger the weight number, the higher positions of
   * schedulers or front pages
   */
  private final int weight;
  /** description of this action, will be shown in front pages */
  private final String desc;

  private Action(TableFormat[] formats, int code, int weight, String desc) {
    this.formats = formats;
    this.code = code;
    this.desc = desc;
    this.weight = weight;
  }

  public static Action valueOf(int code) {
    return ACTIONS.get(code);
  }

  public static synchronized void register(int code, int weight, String desc) {
    register(DEFAULT_FORMATS, code, weight, desc);
  }

  public static synchronized void register(
      TableFormat[] formats, int code, int weight, String desc) {
    Map<TableFormat, Set<String>> format2Actions = buildMapFromActions();
    for (TableFormat format : formats) {
      if (format2Actions.get(format).contains(desc)) {
        throw new IllegalArgumentException("Duplicated action: " + desc + " in format: " + format);
      }
    }
    if (ACTIONS.put(code, new Action(formats, code, weight, desc)) != null) {
      throw new IllegalArgumentException("Duplicated action code: " + code);
    }
  }

  private static Map<TableFormat, Set<String>> buildMapFromActions() {
    return ACTIONS.values().stream()
        .flatMap(
            action -> Arrays.stream(action.formats).map(format -> Pair.of(format, action.desc)))
        .collect(
            Collectors.groupingBy(
                Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet())));
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public int getWeight() {
    return weight;
  }

  public TableFormat[] supportedFormats() {
    return formats;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Action action = (Action) o;
    return code == action.code;
  }

  @Override
  public int hashCode() {
    return code;
  }
}
