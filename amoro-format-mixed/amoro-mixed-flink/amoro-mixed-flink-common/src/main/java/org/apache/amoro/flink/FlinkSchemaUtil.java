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

package org.apache.amoro.flink;

import static org.apache.flink.table.descriptors.DescriptorProperties.DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.EXPR;
import static org.apache.flink.table.descriptors.DescriptorProperties.NAME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_ROWTIME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_EXPR;
import static org.apache.flink.table.descriptors.Schema.SCHEMA_PROCTIME;

import org.apache.amoro.flink.table.FlinkSource;
import org.apache.amoro.flink.table.MixedFormatDynamicSource;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** An util that converts flink table schema. */
public class FlinkSchemaUtil {

  private static final Logger LOG = LoggerFactory.getLogger(FlinkSchemaUtil.class);
  public static final String FLINK_PREFIX = "flink";

  public static final String COMPUTED_COLUMNS = "computed-column";

  public static final String WATERMARK = "watermark";
  public static final String PROCTIME_FUNCTION = SCHEMA_PROCTIME + "()";
  public static final Pattern COMPUTE_PATTERN =
      Pattern.compile("flink\\.computed-column\\.(\\d+)\\.name");

  /**
   * Convert iceberg Schema to flink TableSchema.
   *
   * @param icebergSchema
   * @param tableProperties
   * @return Flink TableSchema
   */
  public static TableSchema toSchema(
      Schema icebergSchema, List<String> primaryKeys, Map<String, String> tableProperties) {
    TableSchema.Builder builder = TableSchema.builder();
    RowType rowType = org.apache.iceberg.flink.FlinkSchemaUtil.convert(icebergSchema);

    // add physical columns.
    for (RowType.RowField field : rowType.getFields()) {
      builder.field(field.getName(), TypeConversions.fromLogicalToDataType(field.getType()));
    }

    // add primary key
    if (CollectionUtils.isNotEmpty(primaryKeys)) {
      builder.primaryKey(primaryKeys.toArray(new String[0]));
    }

    Set<Integer> computeIndex = getComputeIndex(tableProperties);
    List<String> fieldNames = rowType.getFieldNames();

    // add computed columns
    for (int index : computeIndex) {
      builder.add(deserializeComputeColumn(tableProperties, index, fieldNames));
      fieldNames.add(tableProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, NAME)));
    }

    // add watermark
    if (isWatermarkValid(tableProperties)) {
      builder.watermark(deserializeWatermarkSpec(tableProperties, fieldNames));
    }
    return builder.build();
  }

  /**
   * Add watermark info to help {@link FlinkSource} and {@link MixedFormatDynamicSource} distinguish
   * the watermark field. For now, it only be used in the case of mixed-format table as dim-table.
   */
  public static TableSchema getPhysicalSchemaForDimTable(TableSchema tableSchema) {
    TableSchema.Builder builder = filter(tableSchema, TableColumn::isPhysical);
    tableSchema.getWatermarkSpecs().forEach(builder::watermark);
    return builder.build();
  }

  /**
   * filter watermark due to watermark is a virtual field for now, not in mixed-format physical
   * table.
   */
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
      List<Types.NestedField> projectedColumns, MixedTable table) {
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
      MixedTable table,
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
                              "Mixed-format table primary key should be declared in table")));
        });
  }

  /**
   * Generate table properties for watermark and computed columns from flink TableSchema.
   *
   * @param schema Flink TableSchema.
   * @return tableProperties.
   */
  public static Map<String, String> generateExtraOptionsFrom(TableSchema schema) {
    Map<String, String> properties = Maps.newHashMap();

    // add properties for computeColumns
    Map<String, String> computeColumnProperties = serializeComputeColumn(schema);
    properties.putAll(computeColumnProperties);

    // add properties for watermark,only support one watermark now
    List<WatermarkSpec> watermarkSpecs = schema.getWatermarkSpecs();
    if (!watermarkSpecs.isEmpty()) {
      if (watermarkSpecs.size() > 1) {
        throw new IllegalStateException("Multiple watermark definition is not supported yet.");
      }
      properties.putAll(serializeWatermarkSpec(watermarkSpecs.get(0)));
    }

    return properties;
  }

  /** Serialize compute columns into properties. */
  private static Map<String, String> serializeComputeColumn(TableSchema schema) {
    Map<String, String> serialized = new HashMap<>();
    List<TableColumn> tableColumns = schema.getTableColumns();
    // index in compute Column, starting from 1
    int computeIndex = 1;
    for (TableColumn column : tableColumns) {
      if (column instanceof ComputedColumn) {
        serialized.put(
            compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, computeIndex, NAME), column.getName());
        serialized.put(
            compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, computeIndex, DATA_TYPE),
            column.getType().getLogicalType().asSerializableString());
        serialized.put(
            compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, computeIndex, EXPR),
            ((TableColumn.ComputedColumn) column).getExpression());
        computeIndex++;
      }
    }
    return serialized;
  }

  /** Deserialize compute columns from properties. */
  private static TableColumn deserializeComputeColumn(
      Map<String, String> tableProperties, int index, List<String> fieldNames) {
    String expr = tableProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, EXPR));
    if (!isExprContainField(expr, fieldNames)) {
      throw new IllegalStateException(
          "expression " + expr + " does not match any columns in the table. ");
    }
    DataType dataType =
        TypeConversions.fromLogicalToDataType(
            LogicalTypeParser.parse(
                tableProperties.get(
                    compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, DATA_TYPE))));
    TableColumn column =
        TableColumn.computed(
            tableProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, NAME)),
            dataType,
            expr);
    return column;
  }

  private static boolean isExprContainField(String expr, List<String> fieldNames) {
    if (expr.equalsIgnoreCase(PROCTIME_FUNCTION)) {
      return true;
    }
    for (String fieldName : fieldNames) {
      if (expr.contains("`" + fieldName + "`")) {
        return true;
      }
    }
    return false;
  }

  private static boolean isComputeValid(Map<String, String> tableProperties, int index) {
    // check if properties for computeColumn is valid and complete
    if (StringUtils.isNotBlank(
            tableProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, NAME)))
        && StringUtils.isNotBlank(
            tableProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, DATA_TYPE)))
        && StringUtils.isNotBlank(
            tableProperties.get(compoundKey(FLINK_PREFIX, COMPUTED_COLUMNS, index, EXPR)))) {
      return true;
    }
    LOG.warn(
        "properties for computeColumn {} is incomplete, It should contain {}, {}, {}. skip to convert it into computeColumn ",
        index,
        NAME,
        DATA_TYPE,
        EXPR);
    return false;
  }

  private static Set<Integer> getComputeIndex(Map<String, String> tableProperties) {
    Set<Integer> computedIndex = new TreeSet<>();
    tableProperties
        .keySet()
        .forEach(
            k -> {
              Matcher matcher = COMPUTE_PATTERN.matcher(k);
              if (matcher.find()) {
                int indexId = NumberUtils.toInt(matcher.group(1));
                if (indexId > 0 && isComputeValid(tableProperties, indexId)) {
                  computedIndex.add(indexId);
                }
              }
            });
    return computedIndex;
  }

  /** Serialize watermarkSpec into properties. */
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

  /** Deserialize watermarkSpec from properties. */
  private static WatermarkSpec deserializeWatermarkSpec(
      Map<String, String> tableProperties, List<String> fieldNames) {
    String rowtimeAttribute =
        tableProperties.get(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_ROWTIME));
    if (!fieldNames.contains(rowtimeAttribute)) {
      throw new IllegalStateException(
          "Watermark rowtime attribute '"
              + rowtimeAttribute
              + " does not match any columns in the table. ");
    }
    DataType watermarkExprOutputType =
        TypeConversions.fromLogicalToDataType(
            LogicalTypeParser.parse(
                tableProperties.get(
                    compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_DATA_TYPE))));
    return new WatermarkSpec(
        rowtimeAttribute,
        tableProperties.get(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_EXPR)),
        watermarkExprOutputType);
  }

  private static boolean isWatermarkValid(Map<String, String> tableProperties) {
    // check if properties for watermark is valid and complete
    if (StringUtils.isNotBlank(
            tableProperties.get(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_ROWTIME)))
        && StringUtils.isNotBlank(
            tableProperties.get(compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_EXPR)))
        && StringUtils.isNotBlank(
            tableProperties.get(
                compoundKey(FLINK_PREFIX, WATERMARK, WATERMARK_STRATEGY_DATA_TYPE)))) {
      return true;
    }
    LOG.warn(
        "properties for watermark is incomplete, It should contain {}, {}, {}. skip to convert it into watermark strategy ",
        WATERMARK_ROWTIME,
        WATERMARK_STRATEGY_EXPR,
        WATERMARK_STRATEGY_DATA_TYPE);
    return false;
  }

  private static String compoundKey(Object... components) {
    return Stream.of(components).map(Object::toString).collect(Collectors.joining("."));
  }

  /**
   * get physical tableSchema
   *
   * @param tableSchema Flink TableSchema
   * @return Flink tableSchema
   */
  public static TableSchema getPhysicalSchema(TableSchema tableSchema) {
    TableSchema.Builder builder = filter(tableSchema, TableColumn::isPhysical);
    return builder.build();
  }
}
