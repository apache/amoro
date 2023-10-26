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

import static org.apache.flink.table.descriptors.DescriptorProperties.DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.EXPR;
import static org.apache.flink.table.descriptors.DescriptorProperties.NAME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_ROWTIME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_EXPR;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableColumn.ComputedColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.api.WatermarkSpec;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.logical.utils.LogicalTypeParser;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** An util that converts flink table schema. */
public class FlinkSchemaUtil {

  private static final Logger LOG = LoggerFactory.getLogger(FlinkSchemaUtil.class);
  public static final String FLINK_PREFIX = "flink";

  public static final String COMPUTED_COLUMNS = "computed-column";

  public static final String WATERMARK = "watermark";

  /**
   * Convert iceberg Schema to flink TableSchema.
   *
   * @param arcticSchema
   * @param arcticProperties
   * @return Flink TableSchema
   */
  public static TableSchema toSchema(
      Schema arcticSchema, List<String> primaryKeys, Map<String, String> arcticProperties) {
    TableSchema.Builder builder = TableSchema.builder();
    RowType rowType = org.apache.iceberg.flink.FlinkSchemaUtil.convert(arcticSchema);

    // add physical columns.
    for (RowType.RowField field : rowType.getFields()) {
      builder.field(field.getName(), TypeConversions.fromLogicalToDataType(field.getType()));
    }

    // add primary key
    if (CollectionUtils.isNotEmpty(primaryKeys)) {
      builder.primaryKey(primaryKeys.toArray(new String[0]));
    }

    // add watermark
    if (arcticProperties.keySet().stream()
        .anyMatch(key -> key.startsWith(compoundKey(FLINK_PREFIX, WATERMARK)))) {
      builder.watermark(deserializeWatermarkSpec(arcticProperties));
    }

    // add computed columns
    HashSet<Integer> computedIndex = new HashSet<>();
    arcticProperties.forEach(
        (k, v) -> {
          if (k.startsWith(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS))
              && arcticProperties.get(k) != null) {
            int start =
                k.indexOf(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS))
                    + compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS).length()
                    + 1;
            int end = k.lastIndexOf('.');
            computedIndex.add(NumberUtils.toInt(k.substring(start, end)));
          }
        });
    computedIndex.stream()
        .map(index -> deserializeComputeColumn(arcticProperties, index))
        .forEach(builder::add);
    return builder.build();
  }

  /**
   * Add watermark info to help {@link com.netease.arctic.flink.table.FlinkSource} and {@link
   * com.netease.arctic.flink.table.ArcticDynamicSource} distinguish the watermark field. For now,
   * it only be used in the case of Arctic as dim-table.
   */
  public static TableSchema getPhysicalSchema(TableSchema tableSchema, boolean addWatermark) {
    if (!addWatermark) {
      return tableSchema;
    }
    TableSchema.Builder builder = filter(tableSchema, TableColumn::isPhysical);
    tableSchema.getWatermarkSpecs().forEach(builder::watermark);
    return builder.build();
  }

  /** filter watermark due to watermark is a virtual field for now, not in arctic physical table. */
  public static TableSchema filterWatermark(TableSchema tableSchema) {
    List<WatermarkSpec> watermarkSpecs = tableSchema.getWatermarkSpecs();
    if (watermarkSpecs.isEmpty()) {
      return tableSchema;
    }

    Function<TableColumn, Boolean> filter =
        (tableColumn) -> {
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

  /** If filter result is true, keep the column; otherwise, remove the column. */
  public static TableSchema.Builder filter(
      TableSchema tableSchema, Function<TableColumn, Boolean> filter) {
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

  /**
   * Primary keys are the required fields to guarantee that readers can read keyed table in right
   * order, due to the automatic scaling in/out of nodes. The required fields should be added even
   * though projection push down
   */
  @Deprecated
  public static List<Types.NestedField> addPrimaryKey(
      List<Types.NestedField> projectedColumns, ArcticTable table) {
    List<String> primaryKeys =
        table.isUnkeyedTable()
            ? Collections.EMPTY_LIST
            : table.asKeyedTable().primaryKeySpec().fields().stream()
                .map(PrimaryKeySpec.PrimaryKeyField::fieldName)
                .collect(Collectors.toList());

    List<Types.NestedField> columns = new ArrayList<>(projectedColumns);
    Set<String> projectedNames = new HashSet<>();

    projectedColumns.forEach(c -> projectedNames.add(c.name()));

    primaryKeys.forEach(
        pk -> {
          if (!projectedNames.contains(pk)) {
            columns.add(table.schema().findField(pk));
          }
        });

    LOG.info("Projected Columns after addPrimaryKey, columns:{}", columns);
    return columns;
  }

  /**
   * Primary keys are the required fields to guarantee that readers can read keyed table in right
   * order, due to the automatic scaling in/out of nodes. The required fields should be added even
   * though projection push down
   */
  @Deprecated
  public static void addPrimaryKey(
      TableSchema.Builder builder,
      ArcticTable table,
      TableSchema tableSchema,
      String[] projectedColumns) {
    Set<String> projectedNames = Arrays.stream(projectedColumns).collect(Collectors.toSet());

    if (!table.isKeyedTable()) {
      return;
    }

    List<String> pks = table.asKeyedTable().primaryKeySpec().fieldNames();
    pks.forEach(
        pk -> {
          if (projectedNames.contains(pk)) {
            return;
          }
          builder.field(
              pk,
              tableSchema
                  .getFieldDataType(pk)
                  .orElseThrow(
                      () ->
                          new ValidationException(
                              "Arctic primary key should be declared in table")));
        });
  }
  /**
   * Generate table properties for watermark and computed columns from flink TableSchema.
   *
   * @param schema flink TableSchema.
   * @return Table properties map.
   */
  public static Map<String, String> addSchemaProperties(TableSchema schema) {
    Map<String, String> properties = Maps.newHashMap();

    // field name -> index
    final Map<String, Integer> indexMap = new HashMap<>();
    List<TableColumn> tableColumns = schema.getTableColumns();
    for (int i = 0; i < tableColumns.size(); i++) {
      indexMap.put(tableColumns.get(i).getName(), i);
    }

    List<TableColumn> computedColumns =
        schema.getTableColumns().stream()
            .filter(column -> column instanceof ComputedColumn)
            .collect(Collectors.toList());
    properties.putAll(serializeComputeColumn(indexMap, computedColumns));

    // watermark,only support one watermark now
    List<WatermarkSpec> watermarkSpecs = schema.getWatermarkSpecs();
    if (!watermarkSpecs.isEmpty()) {
      if (watermarkSpecs.size() > 1) {
        throw new IllegalStateException("Multiple watermark definition is not supported yet.");
      }
      properties.putAll(serializeWatermarkSpec(watermarkSpecs.get(0)));
    }

    return properties;
  }

  /** serialize computeColumns into properties */
  private static Map<String, String> serializeComputeColumn(
      Map<String, Integer> indexMap, List<TableColumn> computedColumns) {
    Map<String, String> serialized = new HashMap<>();
    computedColumns.stream()
        .forEach(
            column -> {
              int index = indexMap.get(column.getName());
              serialized.put(
                  compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, NAME), column.getName());
              serialized.put(
                  compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, DATA_TYPE),
                  column.getType().getLogicalType().asSerializableString());
              serialized.put(
                  compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, EXPR),
                  ((TableColumn.ComputedColumn) column).getExpression());
            });

    return serialized;
  }

  /** deserialize computeColumns from properties */
  private static TableColumn deserializeComputeColumn(
      Map<String, String> arcticProperties, int index) {
    String name = arcticProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, NAME));
    DataType dataType =
        TypeConversions.fromLogicalToDataType(
            LogicalTypeParser.parse(
                arcticProperties.get(
                    compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, DATA_TYPE))));

    TableColumn column =
        TableColumn.computed(
            name,
            dataType,
            arcticProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, EXPR)));
    return column;
  }

  /** serialize watermarkSpec into properties */
  private static Map<String, String> serializeWatermarkSpec(WatermarkSpec watermarkSpec) {
    Map<String, String> serializedWatermarkSpec = new HashMap<>();
    serializedWatermarkSpec.put(
        compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_ROWTIME),
        watermarkSpec.getRowtimeAttribute());
    serializedWatermarkSpec.put(
        compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_EXPR),
        watermarkSpec.getWatermarkExpr());
    serializedWatermarkSpec.put(
        compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_DATA_TYPE),
        watermarkSpec.getWatermarkExprOutputType().getLogicalType().asSerializableString());

    return serializedWatermarkSpec;
  }

  /** deserialize watermarkSpec from properties */
  private static WatermarkSpec deserializeWatermarkSpec(Map<String, String> arcticProperties) {
    String rowtimeAttribute =
        arcticProperties.get(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_ROWTIME));
    String watermarkExpressionString =
        arcticProperties.get(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_EXPR));
    DataType watermarkExprOutputType =
        TypeConversions.fromLogicalToDataType(
            LogicalTypeParser.parse(
                arcticProperties.get(
                    compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_DATA_TYPE))));

    return new WatermarkSpec(rowtimeAttribute, watermarkExpressionString, watermarkExprOutputType);
  }

  private static String compoundKey(Object... components) {
    return Stream.of(components).map(Object::toString).collect(Collectors.joining("."));
  }

  /**
   * get physical tableSchema
   *
   * @param tableSchema
   * @return flink tableSchema
   */
  public static TableSchema getPhysicalSchema(TableSchema tableSchema) {
    TableSchema.Builder builder = filter(tableSchema, TableColumn::isPhysical);
    return builder.build();
  }
}
