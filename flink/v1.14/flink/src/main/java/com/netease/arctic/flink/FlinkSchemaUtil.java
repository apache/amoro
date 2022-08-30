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

package com.netease.arctic.flink;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.WatermarkSpec;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.utils.TypeConversions;

import java.util.List;
import java.util.function.Function;

/**
 * An util that converts flink table schema.
 */
public class FlinkSchemaUtil {
  /**
   * Convert a {@link RowType} to a {@link TableSchema}.
   *
   * @param rowType     a RowType
   * @param primaryKeys
   * @return Flink TableSchema
   */
  public static TableSchema toSchema(RowType rowType, List<String> primaryKeys) {
    TableSchema.Builder builder = TableSchema.builder();
    for (RowType.RowField field : rowType.getFields()) {
      builder.field(field.getName(), TypeConversions.fromLogicalToDataType(field.getType()));
    }
    if (CollectionUtils.isNotEmpty(primaryKeys)) {
      builder.primaryKey(primaryKeys.toArray(new String[0]));
    }
    return builder.build();
  }

  /**
   * Add watermark info to help {@link com.netease.arctic.flink.table.FlinkSource}
   * and {@link com.netease.arctic.flink.table.ArcticDynamicSource} distinguish the watermark field.
   * For now, it only be used in the case of Arctic as dim-table.
   */
  public static TableSchema getPhysicalSchema(TableSchema tableSchema, boolean addWatermark) {
    if (!addWatermark) {
      return tableSchema;
    }
    TableSchema.Builder builder = filter(tableSchema, TableColumn::isPhysical);
    tableSchema.getWatermarkSpecs().forEach(builder::watermark);
    return builder.build();
  }

  /**
   * filter watermark due to watermark is a virtual field for now, not in arctic physical table.
   */
  public static TableSchema filterWatermark(TableSchema tableSchema) {
    List<WatermarkSpec> watermarkSpecs = tableSchema.getWatermarkSpecs();
    if (watermarkSpecs.isEmpty()) {
      return tableSchema;
    }

    Function<TableColumn, Boolean> filter = (tableColumn) -> {
      boolean isWatermark = false;
      for (WatermarkSpec spec : watermarkSpecs) {
        if (spec.getRowtimeAttribute().equals(tableColumn.getName())) {
          isWatermark = true;
          break;
        }
      }
      return !isWatermark;
    };
    return filter(tableSchema, filter).build();
  }

  /**
   * If filter result is true, keep the column; otherwise, remove the column.
   */
  public static TableSchema.Builder filter(TableSchema tableSchema, Function<TableColumn, Boolean> filter) {
    TableSchema.Builder builder = TableSchema.builder();

    tableSchema
        .getTableColumns()
        .forEach(
            tableColumn -> {
              if (!filter.apply(tableColumn)) {
                return;
              }
              builder.field(tableColumn.getName(), tableColumn.getType());
            });
    tableSchema
        .getPrimaryKey()
        .ifPresent(
            uniqueConstraint ->
                builder.primaryKey(
                    uniqueConstraint.getName(),
                    uniqueConstraint.getColumns().toArray(new String[0])));
    return builder;
  }

  public static RowType toRowType(TableSchema tableSchema) {
    LogicalType[] fields = new LogicalType[tableSchema.getFieldCount()];

    for (int i = 0; i < fields.length; i++) {
      TableColumn tableColumn = tableSchema.getTableColumn(i).get();
      fields[i] = tableColumn.getType().getLogicalType();
    }
    return RowType.of(fields);
  }

}
