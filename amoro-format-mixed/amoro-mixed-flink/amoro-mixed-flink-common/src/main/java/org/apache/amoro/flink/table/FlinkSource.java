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

import org.apache.amoro.flink.interceptor.ProxyFactory;
import org.apache.amoro.flink.read.MixedFormatSource;
import org.apache.amoro.flink.read.hybrid.reader.RowDataReaderFunction;
import org.apache.amoro.flink.read.source.MixedFormatScanContext;
import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.amoro.flink.util.CompatibleFlinkPropertyUtil;
import org.apache.amoro.flink.util.IcebergClassUtil;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.flink.util.ProxyUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.MixedTable;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.dag.Transformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.InputFormatSourceFunction;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.operators.OneInputStreamOperatorFactory;
import org.apache.flink.streaming.api.operators.StreamSource;
import org.apache.flink.streaming.api.transformations.LegacySourceTransformation;
import org.apache.flink.streaming.api.transformations.OneInputTransformation;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.ProviderContext;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.source.FlinkInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** An util class create mixed-format source data stream. */
public class FlinkSource {
  private FlinkSource() {}

  public static Builder forRowData() {
    return new Builder();
  }

  public static final class Builder {

    private static final Logger LOG = LoggerFactory.getLogger(Builder.class);
    private static final String MIXED_FORMAT_FILE_TRANSFORMATION = "mixed-format-file";
    private ProviderContext context;
    private StreamExecutionEnvironment env;
    private MixedTable mixedTable;
    private MixedFormatTableLoader tableLoader;
    private TableSchema projectedSchema;
    private List<Expression> filters;
    private ReadableConfig flinkConf = new Configuration();
    private final Map<String, String> properties = new HashMap<>();
    private long limit = -1L;
    private WatermarkStrategy<RowData> watermarkStrategy = WatermarkStrategy.noWatermarks();
    private final MixedFormatScanContext.Builder contextBuilder =
        MixedFormatScanContext.contextBuilder();
    private boolean batchMode = false;

    private Builder() {}

    public Builder context(ProviderContext context) {
      this.context = context;
      return this;
    }

    public Builder env(StreamExecutionEnvironment env) {
      this.env = env;
      return this;
    }

    public Builder mixedFormatTable(MixedTable mixedTable) {
      this.mixedTable = mixedTable;
      properties.putAll(mixedTable.properties());
      return this;
    }

    public Builder tableLoader(MixedFormatTableLoader tableLoader) {
      this.tableLoader = tableLoader;
      return this;
    }

    public Builder project(TableSchema tableSchema) {
      this.projectedSchema = tableSchema;
      return this;
    }

    public Builder limit(long limit) {
      this.limit = limit;
      contextBuilder.limit(limit);
      return this;
    }

    public Builder filters(List<Expression> filters) {
      this.filters = filters;
      contextBuilder.filters(filters);
      return this;
    }

    public Builder flinkConf(ReadableConfig flinkConf) {
      this.flinkConf = flinkConf;
      return this;
    }

    public Builder properties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    public Builder watermarkStrategy(WatermarkStrategy<RowData> watermarkStrategy) {
      if (watermarkStrategy != null) {
        this.watermarkStrategy = watermarkStrategy;
      }
      return this;
    }

    public Builder batchMode(boolean batchMode) {
      this.batchMode = batchMode;
      return this;
    }

    public DataStream<RowData> build() {
      Preconditions.checkNotNull(env, "StreamExecutionEnvironment should not be null");
      loadTableIfNeeded();

      if (mixedTable.isUnkeyedTable()) {
        String scanStartupMode = properties.get(MixedFormatValidator.SCAN_STARTUP_MODE.key());
        return buildUnkeyedTableSource(scanStartupMode);
      }

      boolean dimTable =
          CompatibleFlinkPropertyUtil.propertyAsBoolean(
              properties,
              MixedFormatValidator.DIM_TABLE_ENABLE.key(),
              MixedFormatValidator.DIM_TABLE_ENABLE.defaultValue());
      RowType rowType;

      if (projectedSchema == null) {
        contextBuilder.project(mixedTable.schema());
        rowType = FlinkSchemaUtil.convert(mixedTable.schema());
      } else {
        contextBuilder.project(
            FlinkSchemaUtil.convert(
                mixedTable.schema(),
                org.apache.amoro.flink.FlinkSchemaUtil.filterWatermark(projectedSchema)));
        // If dim table is enabled, we reserve a RowTime field in Emitter.
        if (dimTable) {
          rowType = org.apache.amoro.flink.FlinkSchemaUtil.toRowType(projectedSchema);
        } else {
          rowType =
              org.apache.amoro.flink.FlinkSchemaUtil.toRowType(
                  org.apache.amoro.flink.FlinkSchemaUtil.filterWatermark(projectedSchema));
        }
      }
      MixedFormatScanContext scanContext =
          contextBuilder.fromProperties(properties).batchMode(batchMode).build();

      RowDataReaderFunction rowDataReaderFunction =
          new RowDataReaderFunction(
              flinkConf,
              mixedTable.schema(),
              scanContext.project(),
              mixedTable.asKeyedTable().primaryKeySpec(),
              scanContext.nameMapping(),
              scanContext.caseSensitive(),
              mixedTable.io());

      int scanParallelism =
          flinkConf.getOptional(MixedFormatValidator.SCAN_PARALLELISM).orElse(env.getParallelism());
      DataStreamSource<RowData> sourceStream =
          env.fromSource(
                  new MixedFormatSource<>(
                      tableLoader,
                      scanContext,
                      rowDataReaderFunction,
                      InternalTypeInfo.of(rowType),
                      mixedTable.name(),
                      dimTable),
                  watermarkStrategy,
                  MixedFormatSource.class.getName())
              .setParallelism(scanParallelism);
      context.generateUid(MIXED_FORMAT_FILE_TRANSFORMATION).ifPresent(sourceStream::uid);
      return sourceStream;
    }

    private void loadTableIfNeeded() {
      if (tableLoader == null || mixedTable != null) {
        return;
      }
      mixedTable = MixedFormatUtils.loadMixedTable(tableLoader);
      properties.putAll(mixedTable.properties());
    }

    public DataStream<RowData> buildUnkeyedTableSource(String scanStartupMode) {
      scanStartupMode = scanStartupMode == null ? null : scanStartupMode.toLowerCase();
      Preconditions.checkArgument(
          Objects.isNull(scanStartupMode)
              || Objects.equals(scanStartupMode, MixedFormatValidator.SCAN_STARTUP_MODE_EARLIEST)
              || Objects.equals(scanStartupMode, MixedFormatValidator.SCAN_STARTUP_MODE_LATEST),
          String.format(
              "only support %s, %s when %s is %s",
              MixedFormatValidator.SCAN_STARTUP_MODE_EARLIEST,
              MixedFormatValidator.SCAN_STARTUP_MODE_LATEST,
              MixedFormatValidator.MIXED_FORMAT_READ_MODE,
              MixedFormatValidator.MIXED_FORMAT_READ_FILE));
      org.apache.iceberg.flink.source.FlinkSource.Builder builder =
          org.apache.iceberg.flink.source.FlinkSource.forRowData()
              .env(env)
              .project(org.apache.amoro.flink.FlinkSchemaUtil.filterWatermark(projectedSchema))
              .tableLoader(tableLoader)
              .filters(filters)
              .setAll(properties)
              .flinkConf(flinkConf)
              .limit(limit);
      if (MixedFormatValidator.SCAN_STARTUP_MODE_LATEST.equalsIgnoreCase(scanStartupMode)) {
        Optional<Snapshot> startSnapshotOptional =
            Optional.ofNullable(tableLoader.loadTable().currentSnapshot());
        if (startSnapshotOptional.isPresent()) {
          Snapshot snapshot = startSnapshotOptional.get();
          LOG.info(
              "Get starting snapshot id {} based on scan startup mode {}",
              snapshot.snapshotId(),
              scanStartupMode);
          builder.startSnapshotId(snapshot.snapshotId());
        }
      }
      DataStream<RowData> origin = builder.build();
      return wrapKrb(origin).assignTimestampsAndWatermarks(watermarkStrategy);
    }

    /** extract op from dataStream, and wrap krb support */
    private DataStream<RowData> wrapKrb(DataStream<RowData> ds) {
      IcebergClassUtil.clean(env);
      Transformation origin = ds.getTransformation();
      int scanParallelism =
          flinkConf
              .getOptional(MixedFormatValidator.SCAN_PARALLELISM)
              .orElse(origin.getParallelism());

      if (origin instanceof OneInputTransformation) {
        OneInputTransformation<RowData, RowData> tf =
            (OneInputTransformation<RowData, RowData>) ds.getTransformation();
        OneInputStreamOperatorFactory op = (OneInputStreamOperatorFactory) tf.getOperatorFactory();
        ProxyFactory<FlinkInputFormat> inputFormatProxyFactory =
            IcebergClassUtil.getInputFormatProxyFactory(op, mixedTable.io(), mixedTable.schema());

        if (tf.getInputs().isEmpty()) {
          return env.addSource(
                  new UnkeyedInputFormatSourceFunction(inputFormatProxyFactory, tf.getOutputType()))
              .setParallelism(scanParallelism);
        }

        LegacySourceTransformation tfSource = (LegacySourceTransformation) tf.getInputs().get(0);
        StreamSource source = tfSource.getOperator();
        SourceFunction function = IcebergClassUtil.getSourceFunction(source);

        SourceFunction functionProxy =
            (SourceFunction) ProxyUtil.getProxy(function, mixedTable.io());
        DataStreamSource sourceStream =
            env.addSource(functionProxy, tfSource.getName(), tfSource.getOutputType());
        context.generateUid(MIXED_FORMAT_FILE_TRANSFORMATION).ifPresent(sourceStream::uid);
        if (sourceStream instanceof ParallelSourceFunction) {
          sourceStream.setParallelism(scanParallelism);
        }
        return sourceStream.transform(
            tf.getName(),
            tf.getOutputType(),
            new UnkeyedInputFormatOperatorFactory(inputFormatProxyFactory));
      }

      LegacySourceTransformation tfSource = (LegacySourceTransformation) origin;
      StreamSource source = tfSource.getOperator();
      InputFormatSourceFunction function =
          (InputFormatSourceFunction) IcebergClassUtil.getSourceFunction(source);

      InputFormat inputFormatProxy =
          (InputFormat) ProxyUtil.getProxy(function.getFormat(), mixedTable.io());
      DataStreamSource sourceStream =
          env.createInput(inputFormatProxy, tfSource.getOutputType())
              .setParallelism(scanParallelism);
      context.generateUid(MIXED_FORMAT_FILE_TRANSFORMATION).ifPresent(sourceStream::uid);
      return sourceStream;
    }
  }
}
