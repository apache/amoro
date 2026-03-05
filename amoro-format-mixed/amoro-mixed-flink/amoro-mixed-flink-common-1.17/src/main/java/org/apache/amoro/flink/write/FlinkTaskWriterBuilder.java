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

package org.apache.amoro.flink.write;

import org.apache.amoro.hive.io.writer.AdaptHiveOperateToTableRelation;
import org.apache.amoro.hive.io.writer.AdaptHiveOutputFileFactory;
import org.apache.amoro.hive.table.HiveLocationKind;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.io.writer.CommonOutputFileFactory;
import org.apache.amoro.io.writer.OutputFileFactory;
import org.apache.amoro.io.writer.SortedPosDeleteWriter;
import org.apache.amoro.io.writer.TaskWriterBuilder;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.BaseLocationKind;
import org.apache.amoro.table.ChangeLocationKind;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.LocationKind;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.table.WriteOperationKind;
import org.apache.amoro.utils.SchemaUtil;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.sink.FlinkAppenderFactory;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Locale;

public class FlinkTaskWriterBuilder implements TaskWriterBuilder<RowData> {

  private final MixedTable table;
  private Long transactionId;
  private int partitionId = 0;
  private long taskId = 0;
  private RowType flinkSchema;
  private long mask;

  private FlinkTaskWriterBuilder(MixedTable table) {
    this.table = table;
  }

  public FlinkTaskWriterBuilder withTransactionId(Long transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public FlinkTaskWriterBuilder withPartitionId(int partitionId) {
    this.partitionId = partitionId;
    return this;
  }

  public FlinkTaskWriterBuilder withTaskId(long taskId) {
    this.taskId = taskId;
    return this;
  }

  public FlinkTaskWriterBuilder withFlinkSchema(RowType flinkSchema) {
    this.flinkSchema = flinkSchema;
    return this;
  }

  public FlinkTaskWriterBuilder withMask(long mask) {
    this.mask = mask;
    return this;
  }

  @Override
  public TaskWriter<RowData> buildWriter(WriteOperationKind writeOperationKind) {
    LocationKind locationKind =
        AdaptHiveOperateToTableRelation.INSTANT.getLocationKindsFromOperateKind(
            table, writeOperationKind);
    return buildWriter(locationKind);
  }

  @Override
  public TaskWriter<RowData> buildWriter(LocationKind locationKind) {
    if (locationKind == ChangeLocationKind.INSTANT) {
      return buildChangeWriter();
    } else if (locationKind == BaseLocationKind.INSTANT
        || locationKind == HiveLocationKind.INSTANT) {
      return buildBaseWriter(locationKind);
    } else {
      throw new IllegalArgumentException("Not support Location Kind:" + locationKind);
    }
  }

  private FlinkBaseTaskWriter buildBaseWriter(LocationKind locationKind) {
    Preconditions.checkArgument(transactionId == null);
    FileFormat fileFormat =
        FileFormat.valueOf(
            (table
                .properties()
                .getOrDefault(
                    TableProperties.BASE_FILE_FORMAT, TableProperties.BASE_FILE_FORMAT_DEFAULT)
                .toUpperCase(Locale.ENGLISH)));
    long fileSizeBytes =
        PropertyUtil.propertyAsLong(
            table.properties(),
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);

    String baseLocation;
    EncryptionManager encryptionManager;
    Schema schema;
    Table icebergTable;
    PrimaryKeySpec primaryKeySpec = null;
    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      baseLocation = keyedTable.baseLocation();
      encryptionManager = keyedTable.baseTable().encryption();
      schema = keyedTable.baseTable().schema();
      primaryKeySpec = keyedTable.primaryKeySpec();
      icebergTable = keyedTable.baseTable();
    } else {
      UnkeyedTable table = this.table.asUnkeyedTable();
      baseLocation = table.location();
      encryptionManager = table.encryption();
      schema = table.schema();
      icebergTable = table;
    }

    Schema selectSchema =
        TypeUtil.reassignIds(
            FlinkSchemaUtil.convert(FlinkSchemaUtil.toSchema(flinkSchema)), schema);
    boolean hiveConsistentWriteEnabled =
        PropertyUtil.propertyAsBoolean(
            table.properties(),
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED,
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED_DEFAULT);

    OutputFileFactory outputFileFactory =
        locationKind == HiveLocationKind.INSTANT
            ? new AdaptHiveOutputFileFactory(
                ((SupportHive) table).hiveLocation(),
                table.spec(),
                fileFormat,
                table.io(),
                encryptionManager,
                partitionId,
                taskId,
                transactionId,
                hiveConsistentWriteEnabled)
            : new CommonOutputFileFactory(
                baseLocation,
                table.spec(),
                fileFormat,
                table.io(),
                encryptionManager,
                partitionId,
                taskId,
                transactionId);
    FileAppenderFactory<RowData> appenderFactory =
        TableTypeUtil.isHive(table)
            ? new AdaptHiveFlinkAppenderFactory(
                schema, flinkSchema, table.properties(), table.spec())
            : new FlinkAppenderFactory(
                icebergTable,
                schema,
                flinkSchema,
                table.properties(),
                table.spec(),
                null,
                null,
                null);
    return new FlinkBaseTaskWriter(
        fileFormat,
        appenderFactory,
        outputFileFactory,
        table.io(),
        fileSizeBytes,
        mask,
        selectSchema,
        flinkSchema,
        table.spec(),
        primaryKeySpec);
  }

  private TaskWriter<RowData> buildChangeWriter() {
    if (table.isUnkeyedTable()) {
      throw new IllegalArgumentException("UnKeyed table UnSupport change writer");
    }
    Preconditions.checkArgument(transactionId == null);

    FileFormat fileFormat =
        FileFormat.valueOf(
            (table
                .properties()
                .getOrDefault(
                    TableProperties.BASE_FILE_FORMAT, TableProperties.BASE_FILE_FORMAT_DEFAULT)
                .toUpperCase(Locale.ENGLISH)));
    long fileSizeBytes =
        PropertyUtil.propertyAsLong(
            table.properties(),
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);

    KeyedTable keyedTable = table.asKeyedTable();
    Schema selectSchema =
        TypeUtil.reassignIds(
            FlinkSchemaUtil.convert(FlinkSchemaUtil.toSchema(flinkSchema)),
            keyedTable.baseTable().schema());
    Schema changeSchemaWithMeta = SchemaUtil.changeWriteSchema(keyedTable.baseTable().schema());
    RowType flinkSchemaWithMeta = FlinkSchemaUtil.convert(changeSchemaWithMeta);

    OutputFileFactory outputFileFactory =
        new CommonOutputFileFactory(
            keyedTable.changeLocation(),
            keyedTable.spec(),
            fileFormat,
            keyedTable.io(),
            keyedTable.baseTable().encryption(),
            partitionId,
            taskId,
            transactionId);
    FileAppenderFactory<RowData> appenderFactory =
        TableTypeUtil.isHive(table)
            ? new AdaptHiveFlinkAppenderFactory(
                changeSchemaWithMeta,
                flinkSchemaWithMeta,
                keyedTable.properties(),
                keyedTable.spec())
            : new FlinkAppenderFactory(
                keyedTable.changeTable(),
                changeSchemaWithMeta,
                flinkSchemaWithMeta,
                keyedTable.properties(),
                keyedTable.spec(),
                null,
                null,
                null);
    boolean upsert =
        table.isKeyedTable()
            && PropertyUtil.propertyAsBoolean(
                table.properties(),
                TableProperties.UPSERT_ENABLED,
                TableProperties.UPSERT_ENABLED_DEFAULT);
    return new FlinkChangeTaskWriter(
        fileFormat,
        appenderFactory,
        outputFileFactory,
        keyedTable.io(),
        fileSizeBytes,
        mask,
        selectSchema,
        flinkSchema,
        keyedTable.spec(),
        keyedTable.primaryKeySpec(),
        upsert);
  }

  @Override
  public SortedPosDeleteWriter<RowData> buildBasePosDeleteWriter(
      long mask, long index, StructLike partitionKey) {
    throw new UnsupportedOperationException("flink not support position delete");
  }

  public static FlinkTaskWriterBuilder buildFor(MixedTable table) {
    return new FlinkTaskWriterBuilder(table);
  }
}
