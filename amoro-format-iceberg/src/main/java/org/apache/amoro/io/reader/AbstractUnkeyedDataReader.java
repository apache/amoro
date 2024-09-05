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
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.utils.NodeFilter;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.mapping.NameMappingParser;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.orc.OrcRowReader;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.Filter;
import org.apache.orc.TypeDescription;
import org.apache.parquet.schema.MessageType;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract implementation of iceberg data reader consuming {@link MixedFileScanTask}.
 *
 * @param <T> to indicate the record data type.
 */
public abstract class AbstractUnkeyedDataReader<T> {

  protected final Schema tableSchema;
  protected final Schema projectedSchema;
  protected final String nameMapping;
  protected final boolean caseSensitive;
  protected final AuthenticatedFileIO fileIO;
  protected final BiFunction<Type, Object, Object> convertConstant;
  protected final Filter<T> dataNodeFilter;
  protected final boolean reuseContainer;
  private StructLikeCollections structLikeCollections = StructLikeCollections.DEFAULT;

  public AbstractUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      boolean reuseContainer,
      StructLikeCollections structLikeCollections) {
    this(
        fileIO,
        tableSchema,
        projectedSchema,
        null,
        nameMapping,
        caseSensitive,
        convertConstant,
        null,
        reuseContainer);
    this.structLikeCollections = structLikeCollections;
  }

  public AbstractUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      boolean reuseContainer) {
    this(
        fileIO,
        tableSchema,
        projectedSchema,
        null,
        nameMapping,
        caseSensitive,
        convertConstant,
        null,
        reuseContainer);
  }

  public AbstractUnkeyedDataReader(
      AuthenticatedFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      Set<DataTreeNode> sourceNodes,
      boolean reuseContainer) {
    this.tableSchema = tableSchema;
    this.projectedSchema = projectedSchema;
    this.nameMapping = nameMapping;
    this.caseSensitive = caseSensitive;
    this.fileIO = fileIO;
    this.convertConstant = convertConstant;
    this.reuseContainer = reuseContainer;
    if (sourceNodes != null) {
      this.dataNodeFilter =
          new NodeFilter<>(
              sourceNodes,
              projectedSchema,
              primaryKeySpec,
              toStructLikeFunction().apply(projectedSchema));
    } else {
      this.dataNodeFilter = null;
    }
  }

  public CloseableIterable<T> readData(FileScanTask task) {

    Map<Integer, ?> idToConstant =
        DataReaderCommon.getIdToConstant(task, projectedSchema, convertConstant);

    DeleteFilter<T> deleteFilter =
        new GenericDeleteFilter(task, tableSchema, projectedSchema, structLikeCollections);

    CloseableIterable<T> iterable =
        deleteFilter.filter(newIterable(task, deleteFilter.requiredSchema(), idToConstant));

    if (dataNodeFilter != null) {
      return dataNodeFilter.filter(iterable);
    }

    return iterable;
  }

  private CloseableIterable<T> newIterable(
      FileScanTask task, Schema schema, Map<Integer, ?> idToConstant) {
    CloseableIterable<T> iter;
    if (task.isDataTask()) {
      throw new UnsupportedOperationException("Cannot read data task.");
    } else {
      switch (task.file().format()) {
        case PARQUET:
          iter = newParquetIterable(task, schema, idToConstant);
          break;
        case ORC:
          iter = newOrcIterable(task, schema, idToConstant);
          break;
        default:
          throw new UnsupportedOperationException(
              "Cannot read unknown format: " + task.file().format());
      }
    }

    return iter;
  }

  protected CloseableIterable<T> newParquetIterable(
      FileScanTask task, Schema schema, Map<Integer, ?> idToConstant) {
    Parquet.ReadBuilder builder =
        Parquet.read(fileIO.newInputFile(task.file().path().toString()))
            .split(task.start(), task.length())
            .project(schema)
            .createReaderFunc(getParquetReaderFunction(schema, idToConstant))
            .filter(task.residual())
            .caseSensitive(caseSensitive);

    if (reuseContainer) {
      builder.reuseContainers();
    }
    if (nameMapping != null) {
      builder.withNameMapping(NameMappingParser.fromJson(nameMapping));
    }

    return fileIO.doAs(builder::build);
  }

  protected CloseableIterable<T> newOrcIterable(
      FileScanTask task, Schema schema, Map<Integer, ?> idToConstant) {
    Schema readSchemaWithoutConstantAndMetadataFields =
        TypeUtil.selectNot(
            schema, Sets.union(idToConstant.keySet(), MetadataColumns.metadataFieldIds()));

    ORC.ReadBuilder builder =
        ORC.read(fileIO.newInputFile(task.file().path().toString()))
            .project(readSchemaWithoutConstantAndMetadataFields)
            .split(task.start(), task.length())
            .createReaderFunc(getOrcReaderFunction(schema, idToConstant))
            .filter(task.residual())
            .caseSensitive(caseSensitive);

    if (nameMapping != null) {
      builder.withNameMapping(NameMappingParser.fromJson(nameMapping));
    }

    return builder.build();
  }

  protected abstract Function<MessageType, ParquetValueReader<?>> getParquetReaderFunction(
      Schema projectedSchema, Map<Integer, ?> idToConstant);

  protected abstract Function<TypeDescription, OrcRowReader<?>> getOrcReaderFunction(
      Schema projectSchema, Map<Integer, ?> idToConstant);

  protected abstract Function<Schema, Function<T, StructLike>> toStructLikeFunction();

  protected class GenericDeleteFilter extends DeleteFilter<T> {

    protected Function<T, StructLike> asStructLike;

    GenericDeleteFilter(
        FileScanTask task,
        Schema tableSchema,
        Schema requestedSchema,
        StructLikeCollections structLikeCollections) {
      super(task, tableSchema, requestedSchema, structLikeCollections);
      this.asStructLike = toStructLikeFunction().apply(requiredSchema());
    }

    GenericDeleteFilter(FileScanTask task, Schema tableSchema, Schema requestedSchema) {
      super(task, tableSchema, requestedSchema);
      this.asStructLike = toStructLikeFunction().apply(requiredSchema());
    }

    @Override
    protected StructLike asStructLike(T row) {
      return asStructLike.apply(row);
    }

    @Override
    protected InputFile getInputFile(String location) {
      return fileIO.newInputFile(location);
    }
  }
}
