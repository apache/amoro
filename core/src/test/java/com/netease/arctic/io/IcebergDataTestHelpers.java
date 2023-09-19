/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.io;

import com.google.common.collect.Lists;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.RecordWithAction;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.BaseTaskWriter;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.ArrayUtil;
import org.apache.iceberg.util.PropertyUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.netease.arctic.table.TableProperties.WRITE_TARGET_FILE_SIZE_BYTES;
import static com.netease.arctic.table.TableProperties.WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT;

public class IcebergDataTestHelpers {

  private IcebergDataTestHelpers() {
  }

  public static WriteResult insert(Table table, List<Record> records) throws IOException {
    try (TaskWriter<Record> writer = IcebergTaskWriters.buildFor(table)) {
      return writeRecords(writer, records);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static WriteResult delta(Table table, List<RecordWithAction> records) throws IOException {
    long targetFileSize = PropertyUtil.propertyAsLong(
        table.properties(),
        WRITE_TARGET_FILE_SIZE_BYTES,
        WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT);
    return delta(table, records, targetFileSize);
  }

  public static WriteResult delta(Table table, List<RecordWithAction> records, long targetFileSize) throws IOException {
    Schema eqDeleteSchema = table.schema().select(table.schema().identifierFieldNames());
    GenericTaskDeltaWriter deltaWriter = createTaskWriter(
        eqDeleteSchema
            .columns()
            .stream()
            .map(Types.NestedField::fieldId).collect(Collectors.toList()),
        table.schema(),
        table,
        FileFormat.PARQUET,
        OutputFileFactory.builderFor(
            table,
            1,
            1).format(FileFormat.PARQUET).build(),
        targetFileSize
    );
    for (RecordWithAction record : records) {
      if (record.getAction() == ChangeAction.DELETE || record.getAction() == ChangeAction.UPDATE_BEFORE) {
        deltaWriter.delete(record);
      } else {
        deltaWriter.write(record);
      }
    }
    return deltaWriter.complete();
  }

  public static WriteResult writeRecords(
      TaskWriter<Record> taskWriter, List<Record> records) {
    try {
      records.forEach(d -> {
        try {
          taskWriter.write(d);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      return taskWriter.complete();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static GenericTaskDeltaWriter createTaskWriter(
      List<Integer> equalityFieldIds,
      Schema eqDeleteRowSchema,
      Table table,
      FileFormat format,
      OutputFileFactory fileFactory,
      long targetFileSize) {
    FileAppenderFactory<Record> appenderFactory =
        new GenericAppenderFactory(
            table.schema(),
            table.spec(),
            ArrayUtil.toIntArray(equalityFieldIds),
            eqDeleteRowSchema,
            null);

    List<String> columns = Lists.newArrayList();
    for (Integer fieldId : equalityFieldIds) {
      columns.add(table.schema().findField(fieldId).name());
    }
    Schema deleteSchema = table.schema().select(columns);

    PartitionKey partitionKey = new PartitionKey(table.spec(), table.schema());

    return new GenericTaskDeltaWriter(
        table.schema(),
        deleteSchema,
        table.spec(),
        format,
        appenderFactory,
        fileFactory,
        table.io(),
        partitionKey,
        targetFileSize);
  }

  private static class GenericTaskDeltaWriter extends BaseTaskWriter<Record> {
    private final GenericEqualityDeltaWriter deltaWriter;

    private GenericTaskDeltaWriter(
        Schema schema,
        Schema deleteSchema,
        PartitionSpec spec,
        FileFormat format,
        FileAppenderFactory<Record> appenderFactory,
        OutputFileFactory fileFactory,
        FileIO io,
        PartitionKey partitionKey,
        long targetFileSize) {
      super(spec, format, appenderFactory, fileFactory, io, targetFileSize);
      this.deltaWriter = new GenericEqualityDeltaWriter(partitionKey, schema, deleteSchema);
    }

    @Override
    public void write(Record row) throws IOException {
      deltaWriter.write(row);
    }

    public void delete(Record row) throws IOException {
      deltaWriter.delete(row);
    }

    // The caller of this function is responsible for passing in a record with only the key fields
    public void deleteKey(Record key) throws IOException {
      deltaWriter.deleteKey(key);
    }

    @Override
    public void close() throws IOException {
      deltaWriter.close();
    }

    private class GenericEqualityDeltaWriter extends BaseEqualityDeltaWriter {

      private final InternalRecordWrapper dataWrapper;
      private final InternalRecordWrapper keyWrapper;

      private GenericEqualityDeltaWriter(
          PartitionKey partition, Schema schema, Schema eqDeleteSchema) {
        super(partition, schema, eqDeleteSchema);
        this.dataWrapper = new InternalRecordWrapper(schema.asStruct());
        this.keyWrapper = new InternalRecordWrapper(eqDeleteSchema.asStruct());
      }

      @Override
      protected StructLike asStructLike(Record row) {
        return dataWrapper.copyFor(row);
      }

      @Override
      protected StructLike asStructLikeKey(Record data) {
        return keyWrapper.copyFor(data);
      }
    }
  }
}
