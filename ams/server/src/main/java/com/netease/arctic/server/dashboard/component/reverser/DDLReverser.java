/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard.component.reverser;

import com.google.common.collect.Maps;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.table.TableIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

public class DDLReverser<T> {

  private final TableMetaExtract<T> tableMetaExtract;

  private final MetadataChangeHandler metadataChangeHandler;

  public DDLReverser(TableMetaExtract<T> tableMetaExtract, MetadataChangeHandler metadataChangeHandler) {
    this.tableMetaExtract = tableMetaExtract;
    this.metadataChangeHandler = metadataChangeHandler;
  }

  public List<DDLInfo> reverse(T table, TableIdentifier tableIdentifier) throws Exception {
    List<TableMetaExtract.InternalTableMeta> internalTableMetas = tableMetaExtract.extractTable(table);
    if (internalTableMetas.isEmpty() || internalTableMetas.size() == 1) {
      return Collections.emptyList();
    }

    List<DDLInfo> result = new ArrayList<>();

    for (int i = 1; i < internalTableMetas.size(); i++) {
      TableMetaExtract.InternalTableMeta pre = internalTableMetas.get(i - 1);
      TableMetaExtract.InternalTableMeta current = internalTableMetas.get(i);

      compareProperties(pre.getProperties(), current.getProperties())
          .forEach(sql -> result.add(
              DDLInfo.of(tableIdentifier, sql, DDLInfo.DDLType.UPDATE_PROPERTIES, current.getTime()))
          );

      compareSchemas(pre.getInternalSchema(), current.getInternalSchema())
          .forEach(sql -> result.add(
              DDLInfo.of(tableIdentifier, sql, DDLInfo.DDLType.UPDATE_SCHEMA, current.getTime()))
          );
    }
    return result;
  }

  private List<String> compareProperties(Map<String, String> pre, Map<String, String> current) {
    // Although only one SQL statement can be executed at a time,
    // using the Java API to make modifications can result in the effect of multiple SQL statements.
    List<String> result = new ArrayList<>();

    // Remove properties
    Set<String> removeProperties = Sets.difference(pre.keySet(), current.keySet());
    if (!removeProperties.isEmpty()) {
      result.add(metadataChangeHandler.removeProperties(removeProperties));
    }

    // Change and add properties
    Map<String, String> changeAndAddProperties = Maps.newHashMap();
    for (Map.Entry<String, String> currentEntry : current.entrySet()) {
      String key = currentEntry.getKey();
      String value = currentEntry.getValue();
      if (pre.containsKey(key)) {
        if (!pre.get(key).equals(value)) {
          changeAndAddProperties.put(key, value);
        }
      } else {
        changeAndAddProperties.put(key, value);
      }
    }
    if (!changeAndAddProperties.isEmpty()) {
      result.add(metadataChangeHandler.changeAndAddProperties(changeAndAddProperties));
    }

    return result;
  }

  private List<String> compareSchemas(List<TableMetaExtract.InternalSchema> pre, List<TableMetaExtract.InternalSchema> current) {
    // Although only one SQL statement can be executed at a time,
    // using the Java API to make modifications can result in the effect of multiple SQL statements.
    List<String> result = new ArrayList<>();

    Map<Integer, TableMetaExtract.InternalSchema> preMap = pre.stream().collect(Collectors.toMap(TableMetaExtract.InternalSchema::getId, v -> v));
    Map<Integer, TableMetaExtract.InternalSchema> currentMap = current.stream().collect(Collectors.toMap(
        TableMetaExtract.InternalSchema::getId, v -> v));
    
    //Remove columns.
    Set<Integer> removeColumns = Sets.difference(preMap.keySet(), currentMap.keySet());
    Set<Integer> normalizedRemoveColumns = removeColumns.stream()
        .filter(s -> !removeColumns.contains(preMap.get(s).getParentId()))
        .collect(Collectors.toSet());
    if (!normalizedRemoveColumns.isEmpty()) {
      result.add(
          metadataChangeHandler
              .dropColumns(
                  normalizedRemoveColumns
                      .stream()
                      .map(preMap::get)
                      .map(TableMetaExtract.InternalSchema::getName)
                      .collect(Collectors.toSet())
              )
      );
    }

    //Add new columns.
    Set<Integer> newColumns = Sets.difference(currentMap.keySet(), preMap.keySet());
    Set<Integer> normalizedNewColumns = newColumns.stream()
        .filter(s -> !newColumns.contains(currentMap.get(s).getParentId()))
        .collect(Collectors.toSet());
    if (!normalizedNewColumns.isEmpty()) {
      result.add(
          metadataChangeHandler
              .addNewColumns(
                  normalizedNewColumns
                      .stream()
                      .map(currentMap::get)
                      .collect(Collectors.toList())
              )
      );
    }

    //Change columns.
    for (TableMetaExtract.InternalSchema currentSchema : current) {
      if (!preMap.containsKey(currentSchema.getId())) {
        continue;
      }

      TableMetaExtract.InternalSchema preSchema = preMap.get(currentSchema.getId());
      if (!Objects.equals(preSchema.getName(), currentSchema.getName())) {
        result.add(metadataChangeHandler.renameColumnName(preSchema.getName(), currentSchema.getName()));
      }
      if (!Objects.equals(preSchema.getType(), currentSchema.getType())) {
        result.add(metadataChangeHandler.changeColumnType(currentSchema.getName(), currentSchema.getType()));
      }
      if (!Objects.equals(preSchema.getComment(), currentSchema.getComment())) {
        result.add(metadataChangeHandler.changeColumnsComment(currentSchema.getName(), currentSchema.getComment()));
      }
      if (preSchema.isRequired() != currentSchema.isRequired()) {
        result.add(metadataChangeHandler.changeColumnsRequire(currentSchema.getName(), currentSchema.isRequired()));
      }
    }

    return result;
  }
}
