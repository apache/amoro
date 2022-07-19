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

package com.netease.arctic.io.reader;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.iceberg.optimize.DeleteFilter;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.table.MetadataColumns;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.NodeFilter;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.mapping.NameMappingParser;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.Filter;
import org.apache.iceberg.util.PartitionUtil;
import org.apache.parquet.schema.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract implementation of iceberg data reader consuming {@link ArcticFileScanTask}.
 * @param <T> to indicate the record data type.
 */
public abstract class BaseIcebergDataReader<T> {

  protected final Schema tableSchema;
  protected final Schema projectedSchema;
  protected final String nameMapping;
  protected final boolean caseSensitive;
  protected final ArcticFileIO fileIO;
  protected final BiFunction<Type, Object, Object> convertConstant;
  protected final Filter<T> dataNodeFilter;
  protected final boolean reuseContainer;

  public BaseIcebergDataReader(
      ArcticFileIO fileIO, Schema tableSchema, Schema projectedSchema,
      String nameMapping, boolean caseSensitive, BiFunction<Type, Object, Object> convertConstant,
      boolean reuseContainer) {
    this(fileIO, tableSchema, projectedSchema, null, nameMapping,
        caseSensitive, convertConstant, null, reuseContainer);
  }

  public BaseIcebergDataReader(
      ArcticFileIO fileIO, Schema tableSchema, Schema projectedSchema, PrimaryKeySpec primaryKeySpec,
      String nameMapping, boolean caseSensitive, BiFunction<Type, Object, Object> convertConstant,
      Set<DataTreeNode> sourceNodes, boolean reuseContainer) {
    this.tableSchema = tableSchema;
    this.projectedSchema = projectedSchema;
    this.nameMapping = nameMapping;
    this.caseSensitive = caseSensitive;
    this.fileIO = fileIO;
    this.convertConstant = convertConstant;
    this.reuseContainer = reuseContainer;
    if (sourceNodes != null) {
      this.dataNodeFilter = new NodeFilter<>(sourceNodes, projectedSchema, primaryKeySpec,
          toStructLikeFunction().apply(projectedSchema));
    } else {
      this.dataNodeFilter = null;
    }
  }

  public CloseableIterable<T> readData(ArcticFileScanTask task) {

    Map<Integer, ?> idToConstant = getIdToConstant(task);

    DeleteFilter<T> deleteFilter = new GenericDeleteFilter(task, tableSchema, projectedSchema);

    CloseableIterable<T> iterable = deleteFilter.filter(
        newIterable(task, deleteFilter.requiredSchema(), idToConstant)
    );

    if (dataNodeFilter != null) {
      return dataNodeFilter.filter(iterable);
    }

    return iterable;
  }

  protected Map<Integer, ?> getIdToConstant(ArcticFileScanTask task) {
    Schema partitionSchema = TypeUtil.select(projectedSchema, task.spec().identitySourceIds());
    Map<Integer, Object> idToConstant = new HashMap<>();
    if (!partitionSchema.columns().isEmpty()) {
      idToConstant.putAll(PartitionUtil.constantsMap(task, convertConstant));
    }
    idToConstant.put(org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId(), task.file().path().toString());
    idToConstant.put(
        MetadataColumns.TRANSACTION_ID_FILED_ID,
        convertConstant.apply(Types.LongType.get(), task.file().transactionId()));

    if (task.fileType() == DataFileType.BASE_FILE) {
      idToConstant.put(
          MetadataColumns.FILE_OFFSET_FILED_ID,
          convertConstant.apply(Types.LongType.get(), Long.MAX_VALUE));
    }
    if (task.fileType() == DataFileType.EQ_DELETE_FILE) {
      idToConstant.put(MetadataColumns.CHANGE_ACTION_ID, convertConstant.apply(
          Types.StringType.get(),
          ChangeAction.DELETE.toString()));
    } else if (task.fileType() == DataFileType.INSERT_FILE) {
      idToConstant.put(MetadataColumns.CHANGE_ACTION_ID, convertConstant.apply(
          Types.StringType.get(),
          ChangeAction.INSERT.toString()));
    }
    return idToConstant;
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
        default:
          throw new UnsupportedOperationException(
              "Cannot read unknown format: " + task.file().format());
      }
    }

    return iter;
  }

  private CloseableIterable<T> newParquetIterable(
      FileScanTask task, Schema schema, Map<Integer, ?> idToConstant) {
    Parquet.ReadBuilder builder = Parquet.read(fileIO.newInputFile(task.file().path().toString()))
        .split(task.start(), task.length())
        .project(schema)
        .createReaderFunc(getNewReaderFunction(schema, idToConstant))
        .filter(task.residual())
        .caseSensitive(caseSensitive);

    if (reuseContainer) {
      builder.reuseContainers();
    }
    if (nameMapping != null) {
      builder.withNameMapping(NameMappingParser.fromJson(nameMapping));
    }

    return builder.build();
  }

  protected abstract Function<MessageType, ParquetValueReader<?>> getNewReaderFunction(
      Schema projectedSchema,
      Map<Integer, ?> idToConstant);

  protected abstract Function<Schema, Function<T, StructLike>> toStructLikeFunction();

  protected class GenericDeleteFilter extends DeleteFilter<T> {

    protected Function<T, StructLike> asStructLike;

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
