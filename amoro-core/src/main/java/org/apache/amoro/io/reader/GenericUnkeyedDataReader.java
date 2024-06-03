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

package org.apache.amoro.io.reader;

import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.orc.OrcRowReader;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.types.Type;
import org.apache.orc.TypeDescription;
import org.apache.parquet.schema.MessageType;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GenericUnkeyedDataReader extends AbstractUnkeyedDataReader<Record> {
  public GenericUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      boolean reuseContainer) {
    super(
        fileIO,
        tableSchema,
        projectedSchema,
        nameMapping,
        caseSensitive,
        convertConstant,
        reuseContainer);
  }

  public GenericUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      boolean reuseContainer,
      StructLikeCollections structLikeCollections) {
    super(
        fileIO,
        tableSchema,
        projectedSchema,
        nameMapping,
        caseSensitive,
        convertConstant,
        reuseContainer,
        structLikeCollections);
  }

  public GenericUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      Set<DataTreeNode> sourceNodes,
      boolean reuseContainer) {
    super(
        fileIO,
        tableSchema,
        projectedSchema,
        primaryKeySpec,
        nameMapping,
        caseSensitive,
        convertConstant,
        sourceNodes,
        reuseContainer);
  }

  @Override
  protected Function<MessageType, ParquetValueReader<?>> getParquetReaderFunction(
      Schema projectedSchema, Map<Integer, ?> idToConstant) {
    return fileSchema ->
        GenericParquetReaders.buildReader(projectedSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<TypeDescription, OrcRowReader<?>> getOrcReaderFunction(
      Schema projectSchema, Map<Integer, ?> idToConstant) {
    return fileSchema -> new GenericOrcReader(projectSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<Schema, Function<Record, StructLike>> toStructLikeFunction() {
    return schema -> {
      final InternalRecordWrapper wrapper = new InternalRecordWrapper(schema.asStruct());
      return wrapper::copyFor;
    };
  }
}
