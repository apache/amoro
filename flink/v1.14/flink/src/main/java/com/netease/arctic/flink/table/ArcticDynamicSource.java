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

package com.netease.arctic.flink.table;

import com.netease.arctic.flink.lookup.ArcticRowDataLookupFunction;
import com.netease.arctic.flink.lookup.KVTableFactory;
import com.netease.arctic.flink.lookup.filter.RowDataPredicate;
import com.netease.arctic.flink.lookup.filter.RowDataPredicateExpressionVisitor;
import com.netease.arctic.flink.read.hybrid.reader.DataIteratorReaderFunction;
import com.netease.arctic.flink.read.hybrid.reader.RowDataReaderFunction;
import com.netease.arctic.flink.read.source.FlinkArcticMORDataReader;
import com.netease.arctic.flink.util.FilterUtil;
import com.netease.arctic.flink.util.IcebergAndFlinkFilters;
import com.netease.arctic.hive.io.reader.AbstractAdaptHiveArcticDataReader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.SchemaUtil;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.DataStreamScanProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.LookupTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.TableFunctionProvider;
import org.apache.flink.table.connector.source.abilities.SupportsFilterPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsLimitPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsProjectionPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsWatermarkPushDown;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.expressions.CallExpression;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.functions.BuiltInFunctionDefinitions;
import org.apache.flink.table.functions.FunctionIdentifier;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.utils.TypeConversions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.data.RowDataUtil;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
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

/**
 * Flink table api that generates source operators.
 */
public class ArcticDynamicSource implements ScanTableSource, SupportsFilterPushDown,
    SupportsProjectionPushDown, SupportsLimitPushDown, SupportsWatermarkPushDown, LookupTableSource {

  private static final Logger LOG = LoggerFactory.getLogger(ArcticDynamicSource.class);

  protected final String tableName;

  protected final ScanTableSource arcticDynamicSource;
  protected final ArcticTable arcticTable;
  protected final Map<String, String> properties;

  protected int[] projectFields;
  protected List<Expression> filters;
  protected ResolvedExpression flinkExpression;
  protected final ArcticTableLoader tableLoader;

  @Nullable
  protected WatermarkStrategy<RowData> watermarkStrategy;

  /**
   * @param tableName           tableName
   * @param arcticDynamicSource underlying source
   * @param arcticTable         arcticTable
   * @param properties          With all ArcticTable properties and sql options
   * @param tableLoader
   */
  public ArcticDynamicSource(String tableName,
                             ScanTableSource arcticDynamicSource,
                             ArcticTable arcticTable,
                             Map<String, String> properties,
                             ArcticTableLoader tableLoader) {
    this.tableName = tableName;
    this.arcticDynamicSource = arcticDynamicSource;
    this.arcticTable = arcticTable;
    this.properties = properties;
    this.tableLoader = tableLoader;
  }

  public ArcticDynamicSource(String tableName,
                             ScanTableSource arcticDynamicSource,
                             ArcticTable arcticTable,
                             Map<String, String> properties,
                             ArcticTableLoader tableLoader,
                             int[] projectFields,
                             List<Expression> filters,
                             ResolvedExpression flinkExpression) {
    this.tableName = tableName;
    this.arcticDynamicSource = arcticDynamicSource;
    this.arcticTable = arcticTable;
    this.properties = properties;
    this.tableLoader = tableLoader;
    this.projectFields = projectFields;
    this.filters = filters;
    this.flinkExpression = flinkExpression;
  }

  @Override
  public ChangelogMode getChangelogMode() {
    return arcticDynamicSource.getChangelogMode();
  }

  @Override
  public ScanRuntimeProvider getScanRuntimeProvider(ScanContext scanContext) {
    ScanRuntimeProvider origin = arcticDynamicSource.getScanRuntimeProvider(scanContext);
    Preconditions.checkArgument(origin instanceof DataStreamScanProvider,
        "file or log ScanRuntimeProvider should be DataStreamScanProvider, but provided is " +
            origin.getClass());
    return origin;
  }

  @Override
  public DynamicTableSource copy() {
    return
        new ArcticDynamicSource(
            tableName, arcticDynamicSource, arcticTable, properties, tableLoader, projectFields, filters,
            flinkExpression);
  }

  @Override
  public String asSummaryString() {
    return "Arctic Dynamic Source";
  }

  @Override
  public Result applyFilters(List<ResolvedExpression> filters) {
    IcebergAndFlinkFilters icebergAndFlinkFilters = FilterUtil.convertFlinkExpressToIceberg(filters);
    this.filters = icebergAndFlinkFilters.expressions();

    if (filters.size() == 1) {
      flinkExpression = filters.get(0);
    } else if (filters.size() >= 2) {
      flinkExpression = and(filters.get(0), filters.get(1));
      for (int i = 2; i < filters.size(); i++) {
        flinkExpression = and(flinkExpression, filters.subList(i, i + 1).get(0));
      }
    }

    if (arcticDynamicSource instanceof SupportsFilterPushDown) {
      return ((SupportsFilterPushDown) arcticDynamicSource).applyFilters(filters);
    } else {
      return Result.of(Collections.emptyList(), filters);
    }
  }

  @Override
  public boolean supportsNestedProjection() {
    if (arcticDynamicSource instanceof SupportsProjectionPushDown) {
      return ((SupportsProjectionPushDown) arcticDynamicSource).supportsNestedProjection();
    } else {
      return false;
    }
  }

  protected CallExpression and(ResolvedExpression left, ResolvedExpression right) {
    return new CallExpression(
        FunctionIdentifier.of(BuiltInFunctionDefinitions.AND.getName()),
        BuiltInFunctionDefinitions.AND,
        Arrays.asList(left, right),
        DataTypes.BOOLEAN()
    );
  }

  @Override
  public void applyProjection(int[][] projectedFields) {
    projectFields = new int[projectedFields.length];
    for (int i = 0; i < projectedFields.length; i++) {
      Preconditions.checkArgument(
          projectedFields[i].length == 1,
          "Don't support nested projection now.");
      projectFields[i] = projectedFields[i][0];
    }

    if (arcticDynamicSource instanceof SupportsProjectionPushDown) {
      ((SupportsProjectionPushDown) arcticDynamicSource).applyProjection(projectedFields);
    }
  }

  @Override
  public void applyLimit(long newLimit) {
    if (arcticDynamicSource instanceof SupportsLimitPushDown) {
      ((SupportsLimitPushDown) arcticDynamicSource).applyLimit(newLimit);
    }
  }

  @Override
  public void applyWatermark(WatermarkStrategy<RowData> watermarkStrategy) {
    if (arcticDynamicSource instanceof SupportsWatermarkPushDown) {
      ((SupportsWatermarkPushDown) arcticDynamicSource).applyWatermark(watermarkStrategy);
    }
  }

  @Override
  public LookupRuntimeProvider getLookupRuntimeProvider(LookupContext context) {
    int[] joinKeys = new int[context.getKeys().length];
    for (int i = 0; i < context.getKeys().length; i++) {
      Preconditions.checkArgument(
          context.getKeys()[i].length == 1,
          "Arctic lookup join doesn't support the row field as a joining key.");
      joinKeys[i] = context.getKeys()[i][0];
    }

    return TableFunctionProvider.of(getLookupFunction(joinKeys));
  }

  protected TableFunction<RowData> getLookupFunction(int[] joinKeys) {
    Schema projectedSchema = getProjectedSchema();

    List<String> joinKeyNames = getJoinKeyNames(joinKeys, projectedSchema);

    Configuration config = new Configuration();
    properties.forEach(config::setString);

    Optional<RowDataPredicate> rowDataPredicate = generatePredicate(projectedSchema, flinkExpression);

    AbstractAdaptHiveArcticDataReader<RowData> flinkArcticMORDataReader =
        generateMORReader(arcticTable, projectedSchema);
    DataIteratorReaderFunction<RowData> readerFunction = generateReaderFunction(arcticTable, projectedSchema);

    return
        new ArcticRowDataLookupFunction(
            KVTableFactory.INSTANCE,
            arcticTable,
            joinKeyNames,
            projectedSchema,
            filters,
            tableLoader,
            config,
            rowDataPredicate.orElse(null),
            flinkArcticMORDataReader,
            readerFunction);
  }

  protected DataIteratorReaderFunction<RowData> generateReaderFunction(
      ArcticTable arcticTable, Schema projectedSchema) {
    return new RowDataReaderFunction(
        new Configuration(),
        arcticTable.schema(),
        projectedSchema,
        arcticTable.asKeyedTable().primaryKeySpec(),
        null,
        true,
        arcticTable.io(),
        true
    );
  }

  protected AbstractAdaptHiveArcticDataReader<RowData> generateMORReader(
      ArcticTable arcticTable, Schema projectedSchema) {
    BiFunction<Type, Object, Object> convertConstant = new ConvertTask();

    return new FlinkArcticMORDataReader(
        arcticTable.io(),
        arcticTable.schema(),
        projectedSchema,
        arcticTable.asKeyedTable().primaryKeySpec(),
        null,
        true,
        convertConstant,
        true
    );
  }

  static class ConvertTask implements BiFunction<Type, Object, Object>, Serializable {
    private static final long serialVersionUID = 4607513893568225789L;

    @Override
    public Object apply(Type t, Object u) {
      return RowDataUtil.convertConstant(t, u);
    }
  }


  protected List<String> getJoinKeyNames(int[] joinKeys, Schema projectedSchema) {
    return Arrays
        .stream(joinKeys)
        .mapToObj(
            index -> projectedSchema.columns().get(index).name())
        .collect(Collectors.toList());
  }

  protected Schema getProjectedSchema() {
    Schema arcticTableSchema = arcticTable.schema();
    Schema projectedSchema;
    if (projectFields == null) {
      LOG.info("projectFields is null.");
      projectedSchema = arcticTable.schema();
    } else {
      List<String> projectedFieldNames =
          Arrays
              .stream(projectFields)
              .mapToObj(
                  index ->
                      arcticTable.schema().columns().get(index).name())
              .collect(Collectors.toList());
      projectedSchema = SchemaUtil.convertFieldsToSchema(arcticTableSchema, projectedFieldNames);
      LOG.info(
          "projected schema {}.\n table schema {}.",
          projectedSchema,
          arcticTable.schema());
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
      fieldDataTypeMap.put(field.name(), TypeConversions.fromLogicalToDataType(FlinkSchemaUtil.convert(field.type())));
    }

    RowDataPredicateExpressionVisitor visitor = generateExpressionVisitor(fieldIndexMap, fieldDataTypeMap);
    return flinkExpression.accept(visitor);
  }

  protected RowDataPredicateExpressionVisitor generateExpressionVisitor(
      Map<String, Integer> fieldIndexMap, Map<String, DataType> fieldDataTypeMap) {
    return new RowDataPredicateExpressionVisitor(
        fieldIndexMap,
        fieldDataTypeMap);
  }
}