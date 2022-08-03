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

package com.netease.arctic.spark.reader;

import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.reader.BaseArcticDataReader;
import com.netease.arctic.spark.SparkRowWrapper;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.SparkParquetReaders;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.util.ByteBuffers;
import org.apache.parquet.schema.MessageType;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.Decimal;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.unsafe.types.UTF8String;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Function;

public class ArcticSparkDataReader extends BaseArcticDataReader<Row> {

  public ArcticSparkDataReader(
      ArcticFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive) {
    super(fileIO, tableSchema, projectedSchema, primaryKeySpec, nameMapping, caseSensitive,
        ArcticSparkDataReader::convertConstant, false);
  }

  protected static Object convertConstant(Type type, Object value) {
    if (value == null) {
      return null;
    }

    switch (type.typeId()) {
      case DECIMAL:
        return Decimal.apply((BigDecimal) value);
      case STRING:
        if (value instanceof Utf8) {
          Utf8 utf8 = (Utf8) value;
          return UTF8String.fromBytes(utf8.getBytes(), 0, utf8.getByteLength());
        }
        return UTF8String.fromString(value.toString());
      case FIXED:
        if (value instanceof byte[]) {
          return value;
        } else if (value instanceof GenericData.Fixed) {
          return ((GenericData.Fixed) value).bytes();
        }
        return ByteBuffers.toByteArray((ByteBuffer) value);
      case BINARY:
        return ByteBuffers.toByteArray((ByteBuffer) value);
      default:
    }
    return value;
  }

  @Override
  protected Function<MessageType, ParquetValueReader<?>> getNewReaderFunction(
      Schema projectSchema,
      Map<Integer, ?> idToConstant) {
    return fileSchema -> SparkParquetReaders.buildReader(projectSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<Schema, Function<Row, StructLike>> toStructLikeFunction() {
    return schema -> {
      final StructType structType = SparkSchemaUtil.convert(schema);
      return row -> new SparkRowWrapper(structType).wrap(row);
    };
  }
}
