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

package com.netease.arctic.spark.actions;

import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScanTaskSetManager {

  private static final ScanTaskSetManager INSTANCE = new ScanTaskSetManager();

  private final Map<Pair<String, String>, List<?>> tasksMap =
      Maps.newConcurrentMap();

  private ScanTaskSetManager() {

  }

  public static ScanTaskSetManager get() {
    return INSTANCE;
  }

  public <T> void stageTasks(ArcticTable table, String setId, List<T> tasks) {
    Preconditions.checkArgument(
        tasks != null && tasks.size() > 0, "Cannot stage null or empty tasks");
    Pair<String, String> id = toId(table, setId);
    tasksMap.put(id, tasks);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> fetchTasks(ArcticTable table, String setId) {
    Pair<String, String> id = toId(table, setId);
    return (List<T>) tasksMap.get(id);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> removeTasks(ArcticTable table, String setId) {
    Pair<String, String> id = toId(table, setId);
    return (List<T>) tasksMap.remove(id);
  }

  public Set<String> fetchSetIds(ArcticTable table) {
    return tasksMap.keySet().stream()
        .filter(e -> e.first().equals(tableUUID(table)))
        .map(Pair::second)
        .collect(Collectors.toSet());
  }

  private String tableUUID(ArcticTable arcticTable) {
    Table table = arcticTable.isKeyedTable() ? arcticTable.asKeyedTable().baseTable() : arcticTable.asUnkeyedTable();
    TableOperations ops = ((HasTableOperations) table).operations();
    return ops.current().uuid();
  }

  private Pair<String, String> toId(ArcticTable table, String setId) {
    return Pair.of(tableUUID(table), setId);
  }
}
