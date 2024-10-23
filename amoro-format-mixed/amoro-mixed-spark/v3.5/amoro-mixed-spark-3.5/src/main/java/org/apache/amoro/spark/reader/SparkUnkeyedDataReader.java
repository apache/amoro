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

package org.apache.amoro.spark.reader;

import org.apache.amoro.hive.io.reader.AbstractAdaptHiveUnkeyedDataReader;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.spark.SparkInternalRowWrapper;
import org.apache.amoro.spark.util.MixedFormatSparkUtils;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.orc.OrcRowReader;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.SparkOrcReader;
import org.apache.orc.TypeDescription;
import org.apache.parquet.schema.MessageType;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructType;

import java.util.Map;
import java.util.function.Function;

public class SparkUnkeyedDataReader extends AbstractAdaptHiveUnkeyedDataReader<InternalRow> {

  public SparkUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      String nameMapping,
      boolean caseSensitive) {
    super(
        fileIO,
        tableSchema,
        projectedSchema,
        nameMapping,
        caseSensitive,
        MixedFormatSparkUtils::convertConstant,
        true);
  }

  @Override
  protected Function<MessageType, ParquetValueReader<?>> getParquetReaderFunction(
      Schema projectedSchema, Map<Integer, ?> idToConstant) {
    return fileSchema -> SparkParquetReaders.buildReader(projectedSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<TypeDescription, OrcRowReader<?>> getOrcReaderFunction(
      Schema projectSchema, Map<Integer, ?> idToConstant) {
    return fileSchema -> new SparkOrcReader(projectSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<Schema, Function<InternalRow, StructLike>> toStructLikeFunction() {
    return schema -> {
      final StructType structType = SparkSchemaUtil.convert(schema);
      SparkInternalRowWrapper wrapper = new SparkInternalRowWrapper(structType);
      return row -> wrapper.wrap(row);
    };
  }
}
