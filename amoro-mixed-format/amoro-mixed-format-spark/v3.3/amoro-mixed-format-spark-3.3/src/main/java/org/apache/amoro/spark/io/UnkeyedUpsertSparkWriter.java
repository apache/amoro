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

import static org.apache.spark.sql.types.DataTypes.IntegerType;
import static org.apache.spark.sql.types.DataTypes.StringType;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.writer.OutputFileFactory;
import org.apache.amoro.io.writer.SortedPosDeleteWriter;
import org.apache.amoro.spark.SparkInternalRowCastWrapper;
import org.apache.amoro.spark.SparkInternalRowWrapper;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.catalyst.InternalRow;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnkeyedUpsertSparkWriter<T> implements TaskWriter<T> {

  private final List<DeleteFile> completedDeleteFiles = Lists.newArrayList();
  private final List<DataFile> completedDataFiles = Lists.newArrayList();

  private final FileAppenderFactory<InternalRow> appenderFactory;
  private final OutputFileFactory fileFactory;
  private final FileFormat format;
  private final Schema schema;
  private final MixedTable table;
  private final SparkBaseTaskWriter writer;
  private final Map<PartitionKey, SortedPosDeleteWriter<InternalRow>> writerMap = new HashMap<>();
  private boolean closed = false;

  public UnkeyedUpsertSparkWriter(
      MixedTable table,
      FileAppenderFactory<InternalRow> appenderFactory,
      OutputFileFactory fileFactory,
      FileFormat format,
      Schema schema,
      SparkBaseTaskWriter writer) {
    this.table = table;
    this.appenderFactory = appenderFactory;
    this.fileFactory = fileFactory;
    this.format = format;
    this.schema = schema;
    this.writer = writer;
  }

  @Override
  public void write(T row) throws IOException {
    if (closed) {
      throw new IllegalStateException(
          "Pos-delete writer for table " + table.id().toString() + " already closed");
    }

    SparkInternalRowCastWrapper internalRow = (SparkInternalRowCastWrapper) row;
    StructLike structLike =
        new SparkInternalRowWrapper(SparkSchemaUtil.convert(schema)).wrap(internalRow.getRow());
    PartitionKey partitionKey = new PartitionKey(table.spec(), schema);
    partitionKey.partition(structLike);
    if (writerMap.get(partitionKey) == null) {
      SortedPosDeleteWriter<InternalRow> writer =
          new SortedPosDeleteWriter<>(
              appenderFactory, fileFactory, table.io(), format, partitionKey);
      writerMap.putIfAbsent(partitionKey, writer);
    }
    if (internalRow.getChangeAction() == ChangeAction.DELETE) {
      SortedPosDeleteWriter<InternalRow> deleteWriter = writerMap.get(partitionKey);
      int numFields = internalRow.getRow().numFields();
      Object file = internalRow.getRow().get(numFields - 2, StringType);
      Object pos = internalRow.getRow().get(numFields - 1, IntegerType);
      deleteWriter.delete(file.toString(), Long.parseLong(pos.toString()), null);
    } else {
      this.writer.write(internalRow.getRow());
    }
  }

  @Override
  public void abort() throws IOException {
    close();
  }

  @Override
  public WriteResult complete() throws IOException {
    for (Map.Entry<PartitionKey, SortedPosDeleteWriter<InternalRow>> entry : writerMap.entrySet()) {
      completedDeleteFiles.addAll(entry.getValue().complete());
    }
    close();
    completedDataFiles.addAll(Arrays.asList(writer.complete().dataFiles()));
    return WriteResult.builder()
        .addDeleteFiles(completedDeleteFiles)
        .addDataFiles(completedDataFiles)
        .build();
  }

  @Override
  public void close() throws IOException {
    this.closed = true;
  }
}
