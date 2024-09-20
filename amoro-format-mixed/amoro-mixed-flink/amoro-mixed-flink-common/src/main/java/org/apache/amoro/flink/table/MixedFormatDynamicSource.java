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

package org.apache.amoro.flink.table;

import org.apache.amoro.flink.lookup.KVTableFactory;
import org.apache.amoro.flink.lookup.MixedFormatRowDataLookupFunction;
import org.apache.amoro.flink.lookup.filter.RowDataPredicate;
import org.apache.amoro.flink.lookup.filter.RowDataPredicateExpressionVisitor;
import org.apache.amoro.flink.read.hybrid.reader.DataIteratorReaderFunction;
import org.apache.amoro.flink.read.hybrid.reader.RowDataReaderFunction;
import org.apache.amoro.flink.read.source.FlinkKeyedMORDataReader;
import org.apache.amoro.flink.util.FilterUtil;
import org.apache.amoro.flink.util.IcebergAndFlinkFilters;
import org.apache.amoro.hive.io.reader.AbstractAdaptHiveKeyedDataReader;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.SchemaUtil;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.DataStreamScanProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.LookupTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.abilities.SupportsFilterPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsLimitPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsProjectionPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsWatermarkPushDown;
import org.apache.flink.table.connector.source.lookup.LookupFunctionProvider;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.expressions.CallExpression;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.functions.BuiltInFunctionDefinitions;
import org.apache.flink.table.functions.FunctionIdentifier;
import org.apache.flink.table.functions.LookupFunction;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.data.RowDataUtil;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Flink table api that generates source operators. */
public class MixedFormatDynamicSource
    implements ScanTableSource,
        SupportsFilterPushDown,
        SupportsProjectionPushDown,
        SupportsLimitPushDown,
        SupportsWatermarkPushDown,
        LookupTableSource {

  private static final Logger LOG = LoggerFactory.getLogger(MixedFormatDynamicSource.class);

  protected final String tableName;

  protected final ScanTableSource mixedFormatDynamicSource;
  protected final MixedTable mixedTable;
  protected final Map<String, String> properties;

  protected int[] projectFields;
  protected List<Expression> filters;
  protected ResolvedExpression flinkExpression;
  protected final MixedFormatTableLoader tableLoader;

  @Nullable protected WatermarkStrategy<RowData> watermarkStrategy;

  /**
   * @param tableName tableName
   * @param mixedFormatDynamicSource underlying source
   * @param mixedTable mixedTable
   * @param properties With all mixed-format table properties and sql options
   * @param tableLoader
   */
  public MixedFormatDynamicSource(
      String tableName,
      ScanTableSource mixedFormatDynamicSource,
      MixedTable mixedTable,
      Map<String, String> properties,
      MixedFormatTableLoader tableLoader) {
    this.tableName = tableName;
    this.mixedFormatDynamicSource = mixedFormatDynamicSource;
    this.mixedTable = mixedTable;
    this.properties = properties;
    this.tableLoader = tableLoader;
  }

  public MixedFormatDynamicSource(
      String tableName,
      ScanTableSource mixedFormatDynamicSource,
      MixedTable mixedTable,
      Map<String, String> properties,
      MixedFormatTableLoader tableLoader,
      int[] projectFields,
      List<Expression> filters,
      ResolvedExpression flinkExpression) {
    this.tableName = tableName;
    this.mixedFormatDynamicSource = mixedFormatDynamicSource;
    this.mixedTable = mixedTable;
    this.properties = properties;
    this.tableLoader = tableLoader;
    this.projectFields = projectFields;
    this.filters = filters;
    this.flinkExpression = flinkExpression;
  }

  @Override
  public ChangelogMode getChangelogMode() {
    return mixedFormatDynamicSource.getChangelogMode();
  }

  @Override
  public ScanRuntimeProvider getScanRuntimeProvider(ScanContext scanContext) {
    ScanRuntimeProvider origin = mixedFormatDynamicSource.getScanRuntimeProvider(scanContext);
    Preconditions.checkArgument(
        origin instanceof DataStreamScanProvider,
        "file or log ScanRuntimeProvider should be DataStreamScanProvider, but provided is "
            + origin.getClass());
    return origin;
  }

  @Override
  public DynamicTableSource copy() {
    return new MixedFormatDynamicSource(
        tableName,
        mixedFormatDynamicSource,
        mixedTable,
        properties,
        tableLoader,
        projectFields,
        filters,
        flinkExpression);
  }

  @Override
  public String asSummaryString() {
    return "Mixed-format Dynamic Source";
  }

  @Override
  public Result applyFilters(List<ResolvedExpression> filters) {
    IcebergAndFlinkFilters icebergAndFlinkFilters =
        FilterUtil.convertFlinkExpressToIceberg(filters);
    this.filters = icebergAndFlinkFilters.expressions();

    if (filters.size() == 1) {
      flinkExpression = filters.get(0);
    } else if (filters.size() >= 2) {
      flinkExpression = and(filters.get(0), filters.get(1));
      for (int i = 2; i < filters.size(); i++) {
        flinkExpression = and(flinkExpression, filters.subList(i, i + 1).get(0));
      }
    }

    if (mixedFormatDynamicSource instanceof SupportsFilterPushDown) {
      return ((SupportsFilterPushDown) mixedFormatDynamicSource).applyFilters(filters);
    } else {
      return Result.of(Collections.emptyList(), filters);
    }
  }

  @Override
  public boolean supportsNestedProjection() {
    if (mixedFormatDynamicSource instanceof SupportsProjectionPushDown) {
      return ((SupportsProjectionPushDown) mixedFormatDynamicSource).supportsNestedProjection();
    } else {
      return false;
    }
  }

  protected CallExpression and(ResolvedExpression left, ResolvedExpression right) {
    return CallExpression.permanent(
        FunctionIdentifier.of(BuiltInFunctionDefinitions.AND.getName()),
        BuiltInFunctionDefinitions.AND,
        Arrays.asList(left, right),
        DataTypes.BOOLEAN());
  }

  @Override
  public void applyProjection(int[][] projectedFields, DataType producedDataType) {
    projectFields = new int[projectedFields.length];
    for (int i = 0; i < projectedFields.length; i++) {
      Preconditions.checkArgument(
          projectedFields[i].length == 1, "Don't support nested projection now.");
      projectFields[i] = projectedFields[i][0];
    }

    if (mixedFormatDynamicSource instanceof SupportsProjectionPushDown) {
      ((SupportsProjectionPushDown) mixedFormatDynamicSource)
          .applyProjection(projectedFields, producedDataType);
    }
  }

  @Override
  public void applyLimit(long newLimit) {
    if (mixedFormatDynamicSource instanceof SupportsLimitPushDown) {
      ((SupportsLimitPushDown) mixedFormatDynamicSource).applyLimit(newLimit);
    }
  }

  @Override
  public void applyWatermark(WatermarkStrategy<RowData> watermarkStrategy) {
    if (mixedFormatDynamicSource instanceof SupportsWatermarkPushDown) {
      ((SupportsWatermarkPushDown) mixedFormatDynamicSource).applyWatermark(watermarkStrategy);
    }
  }

  @Override
  public LookupRuntimeProvider getLookupRuntimeProvider(LookupContext context) {
    int[] joinKeys = new int[context.getKeys().length];
    for (int i = 0; i < context.getKeys().length; i++) {
      Preconditions.checkArgument(
          context.getKeys()[i].length == 1,
          "Mixed-format lookup join doesn't support the row field as a joining key.");
      joinKeys[i] = context.getKeys()[i][0];
    }

    return LookupFunctionProvider.of(getLookupFunction(joinKeys));
  }

  protected LookupFunction getLookupFunction(int[] joinKeys) {
    Schema projectedSchema = getProjectedSchema();

    List<String> joinKeyNames = getJoinKeyNames(joinKeys, projectedSchema);

    Configuration config = new Configuration();
    properties.forEach(config::setString);

    Optional<RowDataPredicate> rowDataPredicate =
        generatePredicate(projectedSchema, flinkExpression);

    AbstractAdaptHiveKeyedDataReader<RowData> flinkMORDataReader =
        generateMORReader(mixedTable, projectedSchema);
    DataIteratorReaderFunction<RowData> readerFunction =
        generateReaderFunction(mixedTable, projectedSchema);

    return new MixedFormatRowDataLookupFunction(
        KVTableFactory.INSTANCE,
        mixedTable,
        joinKeyNames,
        projectedSchema,
        filters,
        tableLoader,
        config,
        rowDataPredicate.orElse(null),
        flinkMORDataReader,
        readerFunction);
  }

  protected DataIteratorReaderFunction<RowData> generateReaderFunction(
      MixedTable mixedTable, Schema projectedSchema) {
    return new RowDataReaderFunction(
        new Configuration(),
        mixedTable.schema(),
        projectedSchema,
        mixedTable.asKeyedTable().primaryKeySpec(),
        null,
        true,
        mixedTable.io(),
        true);
  }

  protected AbstractAdaptHiveKeyedDataReader<RowData> generateMORReader(
      MixedTable mixedTable, Schema projectedSchema) {
    BiFunction<Type, Object, Object> convertConstant = new ConvertTask();

    return new FlinkKeyedMORDataReader(
        mixedTable.io(),
        mixedTable.schema(),
        projectedSchema,
        mixedTable.asKeyedTable().primaryKeySpec(),
        null,
        true,
        convertConstant,
        true);
  }

  static class ConvertTask implements BiFunction<Type, Object, Object>, Serializable {
    private static final long serialVersionUID = 4607513893568225789L;

    @Override
    public Object apply(Type t, Object u) {
      return RowDataUtil.convertConstant(t, u);
    }
  }

  protected List<String> getJoinKeyNames(int[] joinKeys, Schema projectedSchema) {
    return Arrays.stream(joinKeys)
        .mapToObj(index -> projectedSchema.columns().get(index).name())
        .collect(Collectors.toList());
  }

  protected Schema getProjectedSchema() {
    Schema mixedFormatTableSchema = mixedTable.schema();
    Schema projectedSchema;
    if (projectFields == null) {
      LOG.info("The projected fields is null.");
      projectedSchema = mixedTable.schema();
    } else {
      if (mixedTable.isUnkeyedTable()) {
        throw new UnsupportedOperationException("Unkeyed table doesn't support lookup join.");
      }
      List<String> primaryKeys = mixedTable.asKeyedTable().primaryKeySpec().fieldNames();
      List<Integer> projectFieldList =
          Arrays.stream(projectFields).boxed().collect(Collectors.toList());
      List<Types.NestedField> columns = mixedFormatTableSchema.columns();
      for (int i = 0; i < mixedFormatTableSchema.columns().size(); i++) {
        if (primaryKeys.contains(columns.get(i).name()) && !projectFieldList.contains(i)) {
          projectFieldList.add(i);
          LOG.info(
              "Add identifier field {} to projected schema, due to this field is mismatched.",
              columns.get(i).name());
        }
      }

      List<String> projectedFieldNames =
          projectFieldList.stream()
              .map(index -> columns.get(index).name())
              .collect(Collectors.toList());
      projectedSchema = SchemaUtil.selectInOrder(mixedFormatTableSchema, projectedFieldNames);
      LOG.info("The projected schema {}.\n table schema {}.", projectedSchema, mixedTable.schema());
    }
    return projectedSchema;
  }

  protected Optional<RowDataPredicate> generatePredicate(
      final Schema projectedSchema, final ResolvedExpression flinkExpression) {
    if (flinkExpression == null) {
      return Optional.empty();
    }

    final Map<String, Integer> fieldIndexMap = new HashMap<>();
    final Map<String, DataType> fieldDataTypeMap = new HashMap<>();
    List<Types.NestedField> fields = projectedSchema.asStruct().fields();
    for (int i = 0; i < fields.size(); i++) {
      Types.NestedField field = fields.get(i);
      fieldIndexMap.put(field.name(), i);
      fieldDataTypeMap.put(
          field.name(),
          TypeConversions.fromLogicalToDataType(FlinkSchemaUtil.convert(field.type())));
    }

    RowDataPredicateExpressionVisitor visitor =
        generateExpressionVisitor(fieldIndexMap, fieldDataTypeMap);
    return flinkExpression.accept(visitor);
  }

  protected RowDataPredicateExpressionVisitor generateExpressionVisitor(
      Map<String, Integer> fieldIndexMap, Map<String, DataType> fieldDataTypeMap) {
    return new RowDataPredicateExpressionVisitor(fieldIndexMap, fieldDataTypeMap);
  }
}
