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

import com.netease.arctic.flink.shuffle.ReadShuffleRulePolicy;
import com.netease.arctic.flink.shuffle.ShuffleHelper;
import com.netease.arctic.flink.util.CompatibleFlinkPropertyUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.DistributionHashMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.DataStreamScanProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.abilities.SupportsFilterPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsLimitPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsProjectionPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsWatermarkPushDown;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.flink.table.types.logical.RowType;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.iceberg.DistributionMode;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netease.arctic.flink.FlinkSchemaUtil.addPrimaryKey;
import static com.netease.arctic.flink.FlinkSchemaUtil.filterWatermark;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.DIM_TABLE_ENABLE;
import static com.netease.arctic.table.TableProperties.READ_DISTRIBUTION_HASH_MODE;
import static com.netease.arctic.table.TableProperties.READ_DISTRIBUTION_HASH_MODE_DEFAULT;
import static com.netease.arctic.table.TableProperties.READ_DISTRIBUTION_MODE;
import static com.netease.arctic.table.TableProperties.READ_DISTRIBUTION_MODE_DEFAULT;

/**
 * Flink table api that generates source operators.
 */
public class ArcticDynamicSource implements ScanTableSource, SupportsFilterPushDown,
    SupportsProjectionPushDown, SupportsLimitPushDown, SupportsWatermarkPushDown {

  public static final Logger LOG = LoggerFactory.getLogger(ArcticDynamicSource.class);

  protected final String tableName;

  private final ScanTableSource arcticDynamicSource;
  private final ArcticTable arcticTable;
  private final Map<String, String> properties;
  private RowType flinkSchemaRowType;
  private Schema readSchema;

  @Nullable
  protected WatermarkStrategy<RowData> watermarkStrategy;

  public ArcticDynamicSource(String tableName,
                             ScanTableSource arcticDynamicSource,
                             ArcticTable arcticTable,
                             Schema readSchema,
                             RowType flinkSchemaRowType,
                             Map<String, String> properties) {
    this.tableName = tableName;
    this.arcticDynamicSource = arcticDynamicSource;
    this.arcticTable = arcticTable;
    this.properties = properties;
    this.readSchema = readSchema;
    this.flinkSchemaRowType = flinkSchemaRowType;
  }

  /**
   * @param tableName           tableName
   * @param arcticDynamicSource underlying source
   * @param arcticTable         arcticTable
   * @param projectedSchema     read schema
   * @param properties          With all ArcticTable properties and sql options
   */
  public ArcticDynamicSource(String tableName,
                             ScanTableSource arcticDynamicSource,
                             ArcticTable arcticTable,
                             TableSchema projectedSchema,
                             Map<String, String> properties) {
    this.tableName = tableName;
    this.arcticDynamicSource = arcticDynamicSource;
    this.arcticTable = arcticTable;
    this.properties = properties;

    if (projectedSchema == null) {
      readSchema = arcticTable.schema();
      flinkSchemaRowType = FlinkSchemaUtil.convert(readSchema);
    } else {
      boolean dimTable = CompatibleFlinkPropertyUtil.propertyAsBoolean(properties, DIM_TABLE_ENABLE.key(),
          DIM_TABLE_ENABLE.defaultValue());
      if (dimTable) {
        readSchema = TypeUtil.reassignIds(
          FlinkSchemaUtil.convert(filterWatermark(projectedSchema)), arcticTable.schema());
      } else {
        readSchema = TypeUtil.reassignIds(FlinkSchemaUtil.convert(projectedSchema), arcticTable.schema());
      }
      flinkSchemaRowType = (RowType) projectedSchema.toRowDataType().getLogicalType();
    }
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
    DataStreamScanProvider dataStreamScanProvider = (DataStreamScanProvider) origin;

    DistributionHashMode distributionHashMode = getDistributionHashMode();
    return new DataStreamScanProvider() {
      @Override
      public DataStream<RowData> produceDataStream(StreamExecutionEnvironment execEnv) {
        DataStream<RowData> ds = distribute(dataStreamScanProvider.produceDataStream(execEnv), distributionHashMode);
        UserGroupInformation.reset();
        LOG.info("ugi reset");
        return ds;
      }

      @Override
      public boolean isBounded() {
        return false;
      }
    };
  }

  private DistributionHashMode getDistributionHashMode() {
    String modeName = PropertyUtil.propertyAsString(properties,
        READ_DISTRIBUTION_MODE,
        READ_DISTRIBUTION_MODE_DEFAULT);

    DistributionMode mode = DistributionMode.fromName(modeName);
    switch (mode) {
      case NONE:
        return DistributionHashMode.NONE;
      case HASH:
        String hashMode = PropertyUtil.propertyAsString(properties, READ_DISTRIBUTION_HASH_MODE, null);
        if (hashMode == null) {
          // default none shuffle for unkeyed table
          if (arcticTable.isUnkeyedTable()) {
            return DistributionHashMode.NONE;
          }
          hashMode = READ_DISTRIBUTION_HASH_MODE_DEFAULT;
        }
        return DistributionHashMode.valueOfDesc(hashMode);
      default:
        return DistributionHashMode.AUTO;
    }
  }

  private DataStream<RowData> distribute(DataStream<RowData> source, DistributionHashMode mode) {
    ShuffleHelper helper = ShuffleHelper.build(arcticTable, readSchema, flinkSchemaRowType);
    if (mode == DistributionHashMode.AUTO) {
      mode = DistributionHashMode.autoSelect(arcticTable.isKeyedTable(), helper.isPartitionKeyExist());
    }
    LOG.info("source distribute mode in effect. {}", mode);
    switch (mode) {
      case NONE:
        return source;
      case PRIMARY_KEY:
        Preconditions.checkArgument(arcticTable.isKeyedTable(),
            "illegal shuffle policy " + mode.getDesc() + " for table without primary key");
        return hash(source, helper, DistributionHashMode.PRIMARY_KEY);
      case PARTITION_KEY:
        Preconditions.checkArgument(!arcticTable.spec().isUnpartitioned(),
            "illegal shuffle policy " + mode.getDesc() + " for table without partition key");
        return hash(source, helper, DistributionHashMode.PARTITION_KEY);
      case PRIMARY_PARTITION_KEY:
        Preconditions.checkArgument(arcticTable.isKeyedTable() && !arcticTable.spec().isUnpartitioned(),
            "illegal shuffle policy " + mode.getDesc() +
                " for table without primary key or partition key");
        return hash(source, helper, DistributionHashMode.PRIMARY_PARTITION_KEY);
      default:
        throw new RuntimeException("Unrecognized " + READ_DISTRIBUTION_HASH_MODE + ": " + mode);
    }
  }

  public DataStream<RowData> hash(DataStream<RowData> source, ShuffleHelper helper, DistributionHashMode hashMode) {
    ReadShuffleRulePolicy shuffleRulePolicy = new ReadShuffleRulePolicy(helper, hashMode);
    return source.partitionCustom(shuffleRulePolicy.generatePartitioner(), shuffleRulePolicy.generateKeySelector());
  }

  @Override
  public DynamicTableSource copy() {
    return new ArcticDynamicSource(tableName, arcticDynamicSource, arcticTable, readSchema, flinkSchemaRowType,
        properties);
  }

  @Override
  public String asSummaryString() {
    return "Arctic Dynamic Source";
  }

  @Override
  public Result applyFilters(List<ResolvedExpression> filters) {
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

  @Override
  public void applyProjection(int[][] projectedFields) {
    int[] projectionFields = new int[projectedFields.length];
    for (int i = 0; i < projectedFields.length; i++) {
      org.apache.flink.util.Preconditions.checkArgument(
          projectedFields[i].length == 1,
          "Don't support nested projection now.");
      projectionFields[i] = projectedFields[i][0];
    }
    final List<Types.NestedField> columns = readSchema.columns();
    List<Types.NestedField> projectedColumns = Arrays.stream(projectionFields)
        .mapToObj(columns::get)
        .collect(Collectors.toList());

    readSchema = new Schema(addPrimaryKey(projectedColumns, arcticTable));
    flinkSchemaRowType = FlinkSchemaUtil.convert(readSchema);
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
}