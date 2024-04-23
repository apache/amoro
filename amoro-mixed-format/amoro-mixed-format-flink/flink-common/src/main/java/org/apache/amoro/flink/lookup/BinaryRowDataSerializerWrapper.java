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

package org.apache.amoro.flink.lookup;

import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.binary.BinaryRowData;
import org.apache.flink.table.runtime.typeutils.BinaryRowDataSerializer;
import org.apache.flink.table.runtime.typeutils.RowDataSerializer;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;

import java.io.IOException;
import java.io.Serializable;

/**
 * This is a wrapper for {@link BinaryRowDataSerializer}. It is used to serialize and deserialize
 * RowData. And serialize and deserialize operations are not thread-safe.
 */
public class BinaryRowDataSerializerWrapper implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  protected BinaryRowDataSerializer serializer;
  private RowDataSerializer rowDataSerializer;
  private DataOutputSerializer outputView;
  private DataInputDeserializer inputView;
  private final Schema schema;

  public BinaryRowDataSerializerWrapper(Schema schema) {
    this.serializer = new BinaryRowDataSerializer(schema.asStruct().fields().size());
    this.schema = schema;
  }

  public byte[] serialize(RowData rowData) throws IOException {
    if (rowDataSerializer == null) {
      RowType rowType = FlinkSchemaUtil.convert(schema);
      rowDataSerializer = new RowDataSerializer(rowType);
    }
    BinaryRowData binaryRowData = rowDataSerializer.toBinaryRow(rowData);
    if (outputView == null) {
      outputView = new DataOutputSerializer(32);
    }
    outputView.clear();
    serializer.serialize(binaryRowData, outputView);
    return outputView.getCopyOfBuffer();
  }

  public RowData deserialize(byte[] recordBytes) throws IOException {
    if (recordBytes == null) {
      return null;
    }
    if (inputView == null) {
      inputView = new DataInputDeserializer();
    }
    inputView.setBuffer(recordBytes);
    return serializer.deserialize(inputView);
  }

  @Override
  public BinaryRowDataSerializerWrapper clone() {
    return new BinaryRowDataSerializerWrapper(schema);
  }
}
