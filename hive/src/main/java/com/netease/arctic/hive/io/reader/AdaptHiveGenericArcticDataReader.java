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

package com.netease.arctic.hive.io.reader;

import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.iceberg.optimize.InternalRecordWrapper;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.reader.BaseArcticDataReader;
import com.netease.arctic.io.reader.DataReaderCommon;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.scan.KeyedTableScanTask;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.map.StructLikeCollections;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.types.Type;
import org.apache.parquet.schema.MessageType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BaseArcticDataReader} with record type {@link Record}.
 */
public class AdaptHiveGenericArcticDataReader extends AdaptHiveBaseArcticDataReader<Record> {

  public AdaptHiveGenericArcticDataReader(
      ArcticFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      Set<DataTreeNode> sourceNodes,
      boolean reuseContainer,
      StructLikeCollections structLikeCollections) {
    super(fileIO, tableSchema, projectedSchema, primaryKeySpec, nameMapping, caseSensitive, convertConstant,
        sourceNodes, reuseContainer, structLikeCollections);
  }

  public AdaptHiveGenericArcticDataReader(
      ArcticFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant) {
    super(fileIO, tableSchema, projectedSchema, primaryKeySpec, nameMapping, caseSensitive, convertConstant, false);
  }

  public AdaptHiveGenericArcticDataReader(
      ArcticFileIO fileIO,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      BiFunction<Type, Object, Object> convertConstant,
      Set<DataTreeNode> sourceNodes, boolean reuseContainer) {
    super(fileIO, tableSchema, projectedSchema, primaryKeySpec,
        nameMapping, caseSensitive, convertConstant, sourceNodes, reuseContainer);
  }

  @Override
  public CloseableIterator<Record> readData(KeyedTableScanTask keyedTableScanTask) {
    AdaptHiveGenericArcticDeleteFilter arcticDeleteFilter =
        new AdaptHiveGenericArcticDeleteFilter(keyedTableScanTask, tableSchema, projectedSchema, primaryKeySpec,
            sourceNodes, structLikeCollections);
    Schema newProjectedSchema = arcticDeleteFilter.requiredSchema();

    CloseableIterable<Record> dataIterable = CloseableIterable.concat(CloseableIterable.transform(
        CloseableIterable.withNoopClose(keyedTableScanTask.dataTasks()),
        fileScanTask -> arcticDeleteFilter.filter(newParquetIterable(fileScanTask, newProjectedSchema,
            DataReaderCommon.getIdToConstant(fileScanTask, newProjectedSchema, convertConstant)))));
    return dataIterable.iterator();
  }

  @Override
  public CloseableIterator<Record> readDeletedData(KeyedTableScanTask keyedTableScanTask) {
    List<PrimaryKeyedFile> equDeleteFiles = keyedTableScanTask.arcticEquityDeletes().stream()
        .map(ArcticFileScanTask::file).collect(Collectors.toList());

    if (!equDeleteFiles.isEmpty()) {
      AdaptHiveGenericArcticDeleteFilter arcticDeleteFilter = new AdaptHiveGenericArcticDeleteFilter(
          keyedTableScanTask, tableSchema, projectedSchema, primaryKeySpec, sourceNodes, structLikeCollections
      );

      Schema newProjectedSchema = arcticDeleteFilter.requiredSchema();

      CloseableIterable<Record> dataIterable = CloseableIterable.concat(CloseableIterable.transform(
          CloseableIterable.withNoopClose(keyedTableScanTask.dataTasks()),
          fileScanTask -> arcticDeleteFilter.filterNegate(
              newParquetIterable(fileScanTask, newProjectedSchema,
                  DataReaderCommon.getIdToConstant(fileScanTask, newProjectedSchema, convertConstant)))));
      return dataIterable.iterator();
    } else {
      return CloseableIterator.empty();
    }
  }

  @Override
  protected Function<MessageType, ParquetValueReader<?>> getNewReaderFunction(
      Schema projectSchema,
      Map<Integer, ?> idToConstant) {
    return fileSchema -> AdaptHiveGenericParquetReaders.buildReader(projectSchema, fileSchema, idToConstant);
  }

  @Override
  protected Function<Schema, Function<Record, StructLike>> toStructLikeFunction() {
    return schema -> record -> new InternalRecordWrapper(schema.asStruct()).wrap(record);
  }

  private class AdaptHiveGenericArcticDeleteFilter extends AdaptHiveArcticDeleteFilter<Record> {

    protected Function<Record, StructLike> asStructLike;

    protected AdaptHiveGenericArcticDeleteFilter(
        KeyedTableScanTask keyedTableScanTask,
        Schema tableSchema, Schema requestedSchema, PrimaryKeySpec primaryKeySpec) {
      super(keyedTableScanTask, tableSchema, requestedSchema, primaryKeySpec);
      this.asStructLike = AdaptHiveGenericArcticDataReader.this.toStructLikeFunction().apply(requiredSchema());
    }

    protected AdaptHiveGenericArcticDeleteFilter(
        KeyedTableScanTask keyedTableScanTask,
        Schema tableSchema,
        Schema requestedSchema,
        PrimaryKeySpec primaryKeySpec,
        Set<DataTreeNode> sourceNodes,
        StructLikeCollections structLikeCollections) {
      super(keyedTableScanTask, tableSchema, requestedSchema, primaryKeySpec,
          sourceNodes, structLikeCollections);
      this.asStructLike = AdaptHiveGenericArcticDataReader.this.toStructLikeFunction().apply(requiredSchema());
    }

    protected AdaptHiveGenericArcticDeleteFilter(
        KeyedTableScanTask keyedTableScanTask,
        Schema tableSchema,
        Schema requestedSchema,
        PrimaryKeySpec primaryKeySpec,
        Set<DataTreeNode> sourceNodes) {
      super(keyedTableScanTask, tableSchema, requestedSchema, primaryKeySpec, sourceNodes);
      this.asStructLike = AdaptHiveGenericArcticDataReader.this.toStructLikeFunction().apply(requiredSchema());
    }

    @Override
    protected StructLike asStructLike(Record record) {
      return asStructLike.apply(record);
    }

    @Override
    protected InputFile getInputFile(String location) {
      return fileIO.newInputFile(location);
    }

    @Override
    protected ArcticFileIO getArcticFileIo() {
      return fileIO;
    }
  }
}
