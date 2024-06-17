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

package org.apache.amoro.spark.io;

import org.apache.amoro.hive.io.writer.AdaptHiveOutputFileFactory;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.io.writer.ChangeTaskWriter;
import org.apache.amoro.io.writer.CommonOutputFileFactory;
import org.apache.amoro.io.writer.OutputFileFactory;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SchemaUtil;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;

import java.util.Locale;

public class TaskWriters {
  private final MixedTable table;
  private Long transactionId;
  private int partitionId = 0;
  private long taskId = 0;
  private StructType dsSchema;
  private String hiveSubdirectory;
  private boolean orderedWriter = false;

  private final boolean isHiveTable;
  private final FileFormat fileFormat;
  private final long fileSize;
  private final long mask;

  protected TaskWriters(MixedTable table) {
    this.table = table;
    this.isHiveTable = table instanceof SupportHive;

    this.fileFormat =
        FileFormat.valueOf(
            (table
                .properties()
                .getOrDefault(
                    TableProperties.BASE_FILE_FORMAT, TableProperties.BASE_FILE_FORMAT_DEFAULT)
                .toUpperCase(Locale.ENGLISH)));
    this.fileSize =
        PropertyUtil.propertyAsLong(
            table.properties(),
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    this.mask =
        PropertyUtil.propertyAsLong(
                table.properties(),
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT)
            - 1;
  }

  public static TaskWriters of(MixedTable table) {
    return new TaskWriters(table);
  }

  public TaskWriters withTransactionId(Long transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public TaskWriters withPartitionId(int partitionId) {
    this.partitionId = partitionId;
    return this;
  }

  public TaskWriters withTaskId(long taskId) {
    this.taskId = taskId;
    return this;
  }

  public TaskWriters withDataSourceSchema(StructType dsSchema) {
    this.dsSchema = dsSchema;
    return this;
  }

  public TaskWriters withHiveSubdirectory(String hiveSubdirectory) {
    this.hiveSubdirectory = hiveSubdirectory;
    return this;
  }

  public TaskWriters withOrderedWriter(boolean orderedWriter) {
    this.orderedWriter = orderedWriter;
    return this;
  }

  public TaskWriter<InternalRow> newBaseWriter(boolean isOverwrite) {
    preconditions();

    String baseLocation;
    EncryptionManager encryptionManager;
    Schema schema;
    PrimaryKeySpec primaryKeySpec = null;
    Table icebergTable;

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

    FileAppenderFactory<InternalRow> appenderFactory =
        InternalRowFileAppenderFactory.builderFor(icebergTable, schema, dsSchema)
            .writeHive(isHiveTable)
            .build();
    boolean hiveConsistentWrite =
        PropertyUtil.propertyAsBoolean(
            table.properties(),
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED,
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED_DEFAULT);
    OutputFileFactory outputFileFactory;
    if (isHiveTable && isOverwrite) {
      outputFileFactory =
          new AdaptHiveOutputFileFactory(
              ((SupportHive) table).hiveLocation(),
              table.spec(),
              fileFormat,
              table.io(),
              encryptionManager,
              partitionId,
              taskId,
              transactionId,
              hiveSubdirectory,
              hiveConsistentWrite);
    } else {
      outputFileFactory =
          new CommonOutputFileFactory(
              baseLocation,
              table.spec(),
              fileFormat,
              table.io(),
              encryptionManager,
              partitionId,
              taskId,
              transactionId);
    }

    return new SparkBaseTaskWriter(
        fileFormat,
        appenderFactory,
        outputFileFactory,
        table.io(),
        fileSize,
        mask,
        schema,
        table.spec(),
        primaryKeySpec,
        orderedWriter);
  }

  public ChangeTaskWriter<InternalRow> newChangeWriter() {
    preconditions();
    String changeLocation;
    EncryptionManager encryptionManager;
    Schema schema;
    PrimaryKeySpec primaryKeySpec = null;
    Table icebergTable;

    if (table.isKeyedTable()) {
      KeyedTable keyedTable = table.asKeyedTable();
      changeLocation = keyedTable.changeLocation();
      encryptionManager = keyedTable.changeTable().encryption();
      schema = SchemaUtil.changeWriteSchema(keyedTable.changeTable().schema());
      primaryKeySpec = keyedTable.primaryKeySpec();
      icebergTable = keyedTable.baseTable();
    } else {
      throw new UnsupportedOperationException("Unkeyed table does not support change writer");
    }
    FileAppenderFactory<InternalRow> appenderFactory =
        InternalRowFileAppenderFactory.builderFor(
                icebergTable, schema, SparkSchemaUtil.convert(schema))
            .writeHive(isHiveTable)
            .build();

    OutputFileFactory outputFileFactory;
    outputFileFactory =
        new CommonOutputFileFactory(
            changeLocation,
            table.spec(),
            fileFormat,
            table.io(),
            encryptionManager,
            partitionId,
            taskId,
            transactionId);

    return new SparkChangeTaskWriter(
        fileFormat,
        appenderFactory,
        outputFileFactory,
        table.io(),
        fileSize,
        mask,
        schema,
        table.spec(),
        primaryKeySpec,
        orderedWriter);
  }

  public TaskWriter<InternalRow> newUnkeyedUpsertWriter() {
    preconditions();
    Schema schema = table.schema();
    InternalRowFileAppenderFactory build =
        new InternalRowFileAppenderFactory.Builder(table.asUnkeyedTable(), schema, dsSchema)
            .build();
    long fileSizeBytes =
        PropertyUtil.propertyAsLong(
            table.properties(),
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
            TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    long mask =
        PropertyUtil.propertyAsLong(
                table.properties(),
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT)
            - 1;
    CommonOutputFileFactory commonOutputFileFactory =
        new CommonOutputFileFactory(
            table.location(),
            table.spec(),
            fileFormat,
            table.io(),
            table.asUnkeyedTable().encryption(),
            partitionId,
            taskId,
            transactionId);
    SparkBaseTaskWriter sparkBaseTaskWriter =
        new SparkBaseTaskWriter(
            fileFormat,
            build,
            commonOutputFileFactory,
            table.io(),
            fileSizeBytes,
            mask,
            schema,
            table.spec(),
            null,
            orderedWriter);
    return new UnkeyedUpsertSparkWriter<>(
        table, build, commonOutputFileFactory, fileFormat, schema, sparkBaseTaskWriter);
  }

  private void preconditions() {
    if (table.isKeyedTable()) {
      Preconditions.checkState(transactionId != null, "Transaction id is not set for KeyedTable");
    } else {
      Preconditions.checkState(
          transactionId == null, "Transaction id should be null for UnkeyedTable");
    }
    Preconditions.checkState(partitionId >= 0, "Partition id is not set");
    Preconditions.checkState(taskId >= 0, "Task id is not set");
    Preconditions.checkState(dsSchema != null, "Data source schema is not set");
  }
}
