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

package org.apache.amoro.hive.io.writer;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.hive.table.HiveLocationKind;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.io.writer.CommonOutputFileFactory;
import org.apache.amoro.io.writer.GenericBaseTaskWriter;
import org.apache.amoro.io.writer.GenericChangeTaskWriter;
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
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.MetricsModes;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.AdaptHiveGenericAppenderFactory;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Locale;

/** Builder to create writers for {@link KeyedTable} writing {@link Record}. */
public class AdaptHiveGenericTaskWriterBuilder implements TaskWriterBuilder<Record> {

  private final MixedTable table;

  private Long transactionId;
  private int partitionId = 0;
  private int taskId = 0;
  private ChangeAction changeAction = ChangeAction.INSERT;
  private String customHiveSubdirectory;
  private Long targetFileSize;
  private boolean orderedWriter = false;
  private Boolean hiveConsistentWrite;

  private AdaptHiveGenericTaskWriterBuilder(MixedTable table) {
    this.table = table;
    this.hiveConsistentWrite =
        PropertyUtil.propertyAsBoolean(
            table.properties(),
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED,
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED_DEFAULT);
  }

  public AdaptHiveGenericTaskWriterBuilder withTransactionId(Long transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder withPartitionId(int partitionId) {
    this.partitionId = partitionId;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder withTaskId(int taskId) {
    this.taskId = taskId;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder withChangeAction(ChangeAction changeAction) {
    this.changeAction = changeAction;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder withCustomHiveSubdirectory(
      String customHiveSubdirectory) {
    this.customHiveSubdirectory = customHiveSubdirectory;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder withTargetFileSize(long targetFileSize) {
    this.targetFileSize = targetFileSize;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder withOrdered() {
    this.orderedWriter = true;
    return this;
  }

  public AdaptHiveGenericTaskWriterBuilder hiveConsistentWrite(boolean enabled) {
    this.hiveConsistentWrite = enabled;
    return this;
  }

  @Override
  public TaskWriter<Record> buildWriter(WriteOperationKind writeOperationKind) {
    LocationKind locationKind =
        AdaptHiveOperateToTableRelation.INSTANT.getLocationKindsFromOperateKind(
            table, writeOperationKind);
    return buildWriter(locationKind);
  }

  @Override
  public TaskWriter<Record> buildWriter(LocationKind locationKind) {
    if (locationKind == ChangeLocationKind.INSTANT) {
      return buildChangeWriter();
    } else if (locationKind == BaseLocationKind.INSTANT
        || locationKind == HiveLocationKind.INSTANT) {
      return buildBaseWriter(locationKind);
    } else {
      throw new IllegalArgumentException("Not support Location Kind:" + locationKind);
    }
  }

  public SortedPosDeleteWriter<Record> buildBasePosDeleteWriter(
      long mask, long index, StructLike partitionKey) {
    writeBasePreconditions();
    UnkeyedTable baseTable =
        this.table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    FileFormat fileFormat =
        FileFormat.valueOf(
            (baseTable
                .properties()
                .getOrDefault(
                    TableProperties.BASE_FILE_FORMAT, TableProperties.BASE_FILE_FORMAT_DEFAULT)
                .toUpperCase(Locale.ENGLISH)));
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(baseTable.schema(), baseTable.spec());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX
            + MetadataColumns.DELETE_FILE_PATH.name(),
        MetricsModes.Full.get().toString());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX
            + MetadataColumns.DELETE_FILE_POS.name(),
        MetricsModes.Full.get().toString());
    return new SortedPosDeleteWriter<>(
        appenderFactory,
        new CommonOutputFileFactory(
            baseTable.location(),
            baseTable.spec(),
            fileFormat,
            baseTable.io(),
            baseTable.encryption(),
            partitionId,
            taskId,
            transactionId),
        table.io(),
        fileFormat,
        mask,
        index,
        partitionKey);
  }

  private GenericBaseTaskWriter buildBaseWriter(LocationKind locationKind) {
    writeBasePreconditions();
    FileFormat fileFormat =
        FileFormat.valueOf(
            (table
                .properties()
                .getOrDefault(
                    TableProperties.BASE_FILE_FORMAT, TableProperties.BASE_FILE_FORMAT_DEFAULT)
                .toUpperCase(Locale.ENGLISH)));
    long fileSizeBytes;
    if (this.targetFileSize == null) {
      fileSizeBytes =
          PropertyUtil.propertyAsLong(
              table.properties(),
              TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
              TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    } else {
      fileSizeBytes = this.targetFileSize;
    }
    long mask =
        PropertyUtil.propertyAsLong(
                table.properties(),
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT)
            - 1;

    String baseLocation;
    EncryptionManager encryptionManager;
    Schema schema;
    PrimaryKeySpec primaryKeySpec = null;
    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      baseLocation = keyedTable.baseLocation();
      encryptionManager = keyedTable.baseTable().encryption();
      schema = keyedTable.baseTable().schema();
      primaryKeySpec = keyedTable.primaryKeySpec();
    } else {
      UnkeyedTable table = this.table.asUnkeyedTable();
      baseLocation = table.location();
      encryptionManager = table.encryption();
      schema = table.schema();
    }

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
                customHiveSubdirectory,
                hiveConsistentWrite)
            : new CommonOutputFileFactory(
                baseLocation,
                table.spec(),
                fileFormat,
                table.io(),
                encryptionManager,
                partitionId,
                taskId,
                transactionId);
    FileAppenderFactory<Record> appenderFactory =
        TableTypeUtil.isHive(table)
            ? new AdaptHiveGenericAppenderFactory(schema, table.spec())
            : new GenericAppenderFactory(schema, table.spec());
    return new GenericBaseTaskWriter(
        fileFormat,
        appenderFactory,
        outputFileFactory,
        table.io(),
        fileSizeBytes,
        mask,
        schema,
        table.spec(),
        primaryKeySpec,
        orderedWriter);
  }

  private GenericChangeTaskWriter buildChangeWriter() {
    if (table.isUnkeyedTable()) {
      throw new IllegalArgumentException("UnKeyed table UnSupport change writer");
    }
    KeyedTable table = (KeyedTable) this.table;

    FileFormat fileFormat =
        FileFormat.valueOf(
            (table
                .properties()
                .getOrDefault(
                    TableProperties.CHANGE_FILE_FORMAT, TableProperties.CHANGE_FILE_FORMAT_DEFAULT)
                .toUpperCase(Locale.ENGLISH)));
    long fileSizeBytes;
    if (this.targetFileSize == null) {
      fileSizeBytes =
          PropertyUtil.propertyAsLong(
              table.properties(),
              TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
              TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    } else {
      fileSizeBytes = this.targetFileSize;
    }
    long mask =
        PropertyUtil.propertyAsLong(
                table.properties(),
                TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET,
                TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET_DEFAULT)
            - 1;
    Schema changeWriteSchema = SchemaUtil.changeWriteSchema(table.changeTable().schema());
    FileAppenderFactory<Record> appenderFactory =
        TableTypeUtil.isHive(table)
            ? new AdaptHiveGenericAppenderFactory(changeWriteSchema, table.spec())
            : new GenericAppenderFactory(changeWriteSchema, table.spec());
    return new GenericChangeTaskWriter(
        fileFormat,
        appenderFactory,
        new CommonOutputFileFactory(
            table.changeLocation(),
            table.spec(),
            fileFormat,
            table.io(),
            table.changeTable().encryption(),
            partitionId,
            taskId,
            transactionId),
        table.io(),
        fileSizeBytes,
        mask,
        table.changeTable().schema(),
        table.spec(),
        table.primaryKeySpec(),
        changeAction,
        orderedWriter);
  }

  private void writeBasePreconditions() {
    if (table.isKeyedTable()) {
      Preconditions.checkNotNull(transactionId);
    } else {
      Preconditions.checkArgument(transactionId == null);
    }
  }

  public static AdaptHiveGenericTaskWriterBuilder builderFor(MixedTable table) {
    return new AdaptHiveGenericTaskWriterBuilder(table);
  }
}
