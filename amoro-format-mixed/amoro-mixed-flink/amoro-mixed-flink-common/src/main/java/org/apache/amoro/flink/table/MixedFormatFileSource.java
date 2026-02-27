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

import static org.apache.flink.api.common.RuntimeExecutionMode.BATCH;
import static org.apache.flink.configuration.ExecutionOptions.RUNTIME_MODE;

import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.util.CompatibleFlinkPropertyUtil;
import org.apache.amoro.flink.util.FilterUtil;
import org.apache.amoro.flink.util.IcebergAndFlinkFilters;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.table.MixedTable;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.ProviderContext;
import org.apache.flink.table.connector.source.DataStreamScanProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.abilities.SupportsFilterPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsLimitPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsProjectionPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsWatermarkPushDown;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.expressions.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/** Flink table api that generates mixed-format base/change file source operators. */
public class MixedFormatFileSource
    implements ScanTableSource,
        SupportsFilterPushDown,
        SupportsProjectionPushDown,
        SupportsLimitPushDown,
        SupportsWatermarkPushDown {

  private static final Logger LOG = LoggerFactory.getLogger(MixedFormatFileSource.class);

  private int[] projectedFields;
  private long limit;
  private List<Expression> filters;
  private final MixedTable table;
  @Nullable protected WatermarkStrategy<RowData> watermarkStrategy;

  private final MixedFormatTableLoader loader;
  private final TableSchema tableSchema;
  private final ReadableConfig readableConfig;
  private final boolean batchMode;

  private MixedFormatFileSource(MixedFormatFileSource toCopy) {
    this.loader = toCopy.loader;
    this.tableSchema = toCopy.tableSchema;
    this.projectedFields = toCopy.projectedFields;
    this.limit = toCopy.limit;
    this.filters = toCopy.filters;
    this.readableConfig = toCopy.readableConfig;
    this.table = toCopy.table;
    this.watermarkStrategy = toCopy.watermarkStrategy;
    this.batchMode = toCopy.batchMode;
  }

  public MixedFormatFileSource(
      MixedFormatTableLoader loader,
      TableSchema tableSchema,
      int[] projectedFields,
      MixedTable table,
      long limit,
      List<Expression> filters,
      ReadableConfig readableConfig,
      boolean batchMode) {
    this.loader = loader;
    this.tableSchema = tableSchema;
    this.projectedFields = projectedFields;
    this.limit = limit;
    this.table = table;
    this.filters = filters;
    this.readableConfig = readableConfig;
    this.batchMode = batchMode;
  }

  public MixedFormatFileSource(
      MixedFormatTableLoader loader,
      TableSchema tableSchema,
      MixedTable table,
      ReadableConfig readableConfig,
      boolean batchMode) {
    this(loader, tableSchema, null, table, -1, ImmutableList.of(), readableConfig, batchMode);
  }

  @Override
  public void applyProjection(int[][] projectFields) {
    this.projectedFields = new int[projectFields.length];
    for (int i = 0; i < projectFields.length; i++) {
      Preconditions.checkArgument(
          projectFields[i].length == 1, "Don't support nested projection now.");
      this.projectedFields[i] = projectFields[i][0];
    }
  }

  private DataStream<RowData> createDataStream(
      ProviderContext providerContext, StreamExecutionEnvironment execEnv) {
    return FlinkSource.forRowData()
        .context(providerContext)
        .env(execEnv)
        .tableLoader(loader)
        .mixedFormatTable(table)
        .project(getProjectedSchema())
        .limit(limit)
        .filters(filters)
        .flinkConf(readableConfig)
        .batchMode(execEnv.getConfiguration().get(RUNTIME_MODE).equals(BATCH))
        .watermarkStrategy(watermarkStrategy)
        .build();
  }

  private TableSchema getProjectedSchema() {
    if (projectedFields == null) {
      return tableSchema;
    } else {
      String[] fullNames = tableSchema.getFieldNames();
      DataType[] fullTypes = tableSchema.getFieldDataTypes();

      String[] projectedColumns =
          Arrays.stream(projectedFields).mapToObj(i -> fullNames[i]).toArray(String[]::new);
      TableSchema.Builder builder =
          TableSchema.builder()
              .fields(
                  projectedColumns,
                  Arrays.stream(projectedFields)
                      .mapToObj(i -> fullTypes[i])
                      .toArray(DataType[]::new));
      boolean dimTable =
          CompatibleFlinkPropertyUtil.propertyAsBoolean(
              table.properties(),
              MixedFormatValidator.DIM_TABLE_ENABLE.key(),
              MixedFormatValidator.DIM_TABLE_ENABLE.defaultValue());
      if (dimTable) {
        builder.watermark(tableSchema.getWatermarkSpecs().get(0));
      }

      TableSchema ts = builder.build();
      LOG.info("TableSchema after projection:{}", ts);
      return ts;
    }
  }

  @Override
  public void applyLimit(long newLimit) {
    this.limit = newLimit;
  }

  @Override
  public Result applyFilters(List<ResolvedExpression> flinkFilters) {
    IcebergAndFlinkFilters icebergAndFlinkFilters =
        FilterUtil.convertFlinkExpressToIceberg(flinkFilters);
    this.filters = icebergAndFlinkFilters.expressions();
    return Result.of(icebergAndFlinkFilters.acceptedFilters(), flinkFilters);
  }

  @Override
  public boolean supportsNestedProjection() {
    // TODO: support nested projection
    return false;
  }

  @Override
  public ChangelogMode getChangelogMode() {
    if (table.isUnkeyedTable() || batchMode) {
      return ChangelogMode.insertOnly();
    }
    return ChangelogMode.newBuilder()
        .addContainedKind(RowKind.DELETE)
        .addContainedKind(RowKind.INSERT)
        .addContainedKind(RowKind.UPDATE_AFTER)
        .addContainedKind(RowKind.UPDATE_BEFORE)
        .build();
  }

  @Override
  public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
    return new DataStreamScanProvider() {
      @Override
      public DataStream<RowData> produceDataStream(
          ProviderContext providerContext, StreamExecutionEnvironment execEnv) {
        return createDataStream(providerContext, execEnv);
      }

      @Override
      public boolean isBounded() {
        return org.apache.iceberg.flink.source.FlinkSource.isBounded(table.properties());
      }
    };
  }

  @Override
  public DynamicTableSource copy() {
    return new MixedFormatFileSource(this);
  }

  @Override
  public String asSummaryString() {
    return "Mixed-Format File Source";
  }

  @Override
  public void applyWatermark(WatermarkStrategy<RowData> watermarkStrategy) {
    Configuration conf = Configuration.fromMap(table.properties());
    boolean dimTable =
        CompatibleFlinkPropertyUtil.propertyAsBoolean(conf, MixedFormatValidator.DIM_TABLE_ENABLE);
    if (!dimTable) {
      this.watermarkStrategy = watermarkStrategy;
    }
  }
}
