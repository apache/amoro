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

package com.netease.arctic.flink.write;

import com.netease.arctic.flink.util.ArcticUtils;
import com.netease.arctic.io.writer.CommonOutputFileFactory;
import com.netease.arctic.io.writer.OutputFileFactory;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.SchemaUtil;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.sink.FlinkAppenderFactory;
import org.apache.iceberg.flink.sink.TaskWriterFactory;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Locale;

/**
 * This is a keyed table writer factory.
 */
public class KeyedRowDataTaskWriterFactory implements TaskWriterFactory<RowData> {

  private final KeyedTable table;
  private final FileFormat format;
  private final RowType flinkSchema;
  private FileAppenderFactory<RowData> appenderFactory;

  private final boolean overwrite;

  private transient Long mask = null;
  private transient Long transactionId = null;
  /**
   * FlinkBaseTaskWriter must using base table info to create OutputFileFactory,
   * and FlinkChangeTaskWriter must using change table info to create OutputFileFactory.
   */
  private transient OutputFileFactory outputFileFactory;

  public KeyedRowDataTaskWriterFactory(KeyedTable table,
                                       RowType flinkSchema,
                                       boolean overwrite) {
    this.table = table;
    this.flinkSchema = flinkSchema;
    this.overwrite = overwrite;
    if (ArcticUtils.isToBase(overwrite)) {
      this.format = FileFormat.valueOf((table.properties().getOrDefault(TableProperties.BASE_FILE_FORMAT,
          TableProperties.BASE_FILE_FORMAT_DEFAULT).toUpperCase(Locale.ENGLISH)));
    } else {
      this.format = FileFormat.valueOf((table.properties().getOrDefault(TableProperties.CHANGE_FILE_FORMAT,
          TableProperties.CHANGE_FILE_FORMAT_DEFAULT).toUpperCase(Locale.ENGLISH)));
    }
  }

  public void setMask(long mask) {
    this.mask = mask;
  }

  public void setTransactionId(long transactionId) {
    this.transactionId = transactionId;
  }

  @Override
  public void initialize(int taskId, int attemptId) {
    Preconditions.checkNotNull(transactionId, "TransactionId should be set first. " +
        "Invoke setTransactionId() before this method");
    this.outputFileFactory = createOutputFileFactory(taskId, attemptId);
  }

  private OutputFileFactory createOutputFileFactory(int subtaskId, int attemptId) {
    if (ArcticUtils.isToBase(overwrite)) {
      return new CommonOutputFileFactory(
          table.baseLocation(), table.spec(), format, table.io(),
          table.baseTable().encryption(), subtaskId, attemptId, transactionId);
    }
    return new CommonOutputFileFactory(
        table.changeLocation(), table.spec(), format, table.io(),
        table.changeTable().encryption(), subtaskId, attemptId, transactionId);
  }

  @Override
  public TaskWriter<RowData> create() {
    Preconditions.checkNotNull(outputFileFactory,
        "The outputFileFactory shouldn't be null if we have invoked the initialize().");
    Preconditions.checkNotNull(mask, "Mask should be set first. Invoke setMask() before this method");

    long fileSizeBytes = PropertyUtil.propertyAsLong(table.properties(), TableProperties.WRITE_TARGET_FILE_SIZE_BYTES,
        TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    Schema schema = TypeUtil.reassignIds(
        FlinkSchemaUtil.convert(FlinkSchemaUtil.toSchema(flinkSchema)), table.schema());

    if (ArcticUtils.isToBase(overwrite)) {
      appenderFactory = new FlinkAppenderFactory(
          table.baseTable().schema(), flinkSchema, table.properties(), table.spec());
      return new FlinkBaseTaskWriter(
          format,
          appenderFactory,
          outputFileFactory,
          table.io(), fileSizeBytes, mask,
          schema, flinkSchema, table.spec(), table.primaryKeySpec());
    } else {
      Schema changeSchemaWithMeta = SchemaUtil.changeWriteSchema(table.baseTable().schema());
      RowType flinkSchemaWithMeta = FlinkSchemaUtil.convert(changeSchemaWithMeta);
      appenderFactory = new FlinkAppenderFactory(
          changeSchemaWithMeta, flinkSchemaWithMeta, table.properties(), table.spec());
      return new FlinkChangeTaskWriter(
          format,
          appenderFactory,
          outputFileFactory,
          table.io(), fileSizeBytes, mask,
          schema, flinkSchema, table.spec(), table.primaryKeySpec());
    }
  }

}
