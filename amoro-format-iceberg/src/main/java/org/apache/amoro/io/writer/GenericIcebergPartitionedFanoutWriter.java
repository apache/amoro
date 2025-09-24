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

package org.apache.amoro.io.writer;

import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.FileAppenderFactory;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.PartitionedFanoutWriter;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link PartitionedFanoutWriter} for generic records. This class can write multiple partition
 * files.
 */
public class GenericIcebergPartitionedFanoutWriter extends PartitionedFanoutWriter<Record> {

  private final PartitionKey partitionKey;
  private final InternalRecordWrapper wrapper;
  private final List<Integer> positionToUpdateKey;

  public GenericIcebergPartitionedFanoutWriter(
      Schema schema,
      PartitionSpec spec,
      FileFormat format,
      FileAppenderFactory<Record> appenderFactory,
      OutputFileFactory fileFactory,
      FileIO io,
      long targetFileSize) {
    super(spec, format, appenderFactory, fileFactory, io, targetFileSize);
    Schema schemaWithoutUUID = getSchemaWithoutUUID(schema);
    this.partitionKey = new PartitionKey(spec, schemaWithoutUUID);
    this.wrapper = new InternalRecordWrapper(schemaWithoutUUID.asStruct());
    this.positionToUpdateKey = getPositionToUpdateKey(schema);
  }

  @Override
  protected PartitionKey partition(Record row) {
    StructLike structLike = wrapper.wrap(getGenericRecordWithoutUUID(row));
    partitionKey.partition(structLike);
    return partitionKey;
  }

  /**
   * According to the <a href="https://github.com/apache/iceberg/pull/13087/files">GitHub Issue</a>
   * there is a fix for the UUID types to cast to another internal type
   *
   * @param row original row with data
   * @return GenericRecord as a single class that inherits Row interface
   */
  private GenericRecord getGenericRecordWithoutUUID(Record row) {
    GenericRecord record = (GenericRecord) row;
    positionToUpdateKey.forEach(i -> record.set(i, (byte[]) record.get(i)));
    return record;
  }

  /**
   * Get rid of the UUID type if any
   *
   * @param schema provided schema
   * @return the same schema that is provided but with all the UUID type transformed into the
   *     FixedType[16] type
   */
  private Schema getSchemaWithoutUUID(Schema schema) {
    return new Schema(
        schema.columns().stream()
            .map(
                column -> {
                  if (column.type().equals(Types.UUIDType.get())) {
                    return Types.NestedField.of(
                        column.fieldId(),
                        column.isOptional(),
                        column.name(),
                        Types.FixedType.ofLength(16));
                  }

                  return column;
                })
            .collect(Collectors.toList()));
  }

  /**
   * Receive list of positions where UUID values placed in the schema
   *
   * @param schema that is provided for the certain table
   * @return list of integers which are indexes of UUID columns in the table
   */
  private List<Integer> getPositionToUpdateKey(Schema schema) {
    List<Types.NestedField> columns = schema.columns();
    int size = columns.size();
    List<Integer> positionToUpdateKey = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      Type currentType = columns.get(i).type();
      if (currentType.equals(Types.UUIDType.get())) {
        positionToUpdateKey.add(i);
      }
    }
    return positionToUpdateKey;
  }
}
