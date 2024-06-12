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

import org.apache.amoro.data.ChangedLsn;
import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.CloseableIterableWrapper;
import org.apache.amoro.io.CloseablePredicate;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MetadataColumns;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.utils.NodeFilter;
import org.apache.amoro.utils.map.StructLikeBaseMap;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.Accessor;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.avro.DataReader;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.Filter;
import org.apache.iceberg.util.StructProjection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract implementation of filtering equality and position delete files with insert files and
 * base files.
 *
 * @param <T> to indicate the record data type.
 */
public abstract class MixedDeleteFilter<T> {

  private static final Schema POS_DELETE_SCHEMA =
      new Schema(
          org.apache.iceberg.MetadataColumns.DELETE_FILE_PATH,
          org.apache.iceberg.MetadataColumns.DELETE_FILE_POS);

  private static final Accessor<StructLike> FILENAME_ACCESSOR =
      POS_DELETE_SCHEMA.accessorForField(
          org.apache.iceberg.MetadataColumns.DELETE_FILE_PATH.fieldId());
  private static final Accessor<StructLike> POSITION_ACCESSOR =
      POS_DELETE_SCHEMA.accessorForField(
          org.apache.iceberg.MetadataColumns.DELETE_FILE_POS.fieldId());

  private final Set<PrimaryKeyedFile> eqDeletes;
  private final List<DeleteFile> posDeletes;
  private final Schema requiredSchema;
  private final Accessor<StructLike> dataTransactionIdAccessor;
  private final Accessor<StructLike> dataOffsetAccessor;
  private final Accessor<StructLike> deleteTransactionIdAccessor;
  private final Accessor<StructLike> deleteOffsetAccessor;
  private final Set<Integer> primaryKeyId;
  private final Schema deleteSchema;
  private final Filter<Record> deleteNodeFilter;
  private CloseablePredicate<T> eqPredicate;
  private Map<String, Set<Long>> positionMap;
  private final Accessor<StructLike> posAccessor;
  private final Accessor<StructLike> filePathAccessor;
  private final Set<String> pathSets;

  private String currentDataPath;
  private Set<Long> currentPosSet;

  private StructLikeCollections structLikeCollections = StructLikeCollections.DEFAULT;

  protected MixedDeleteFilter(
      KeyedTableScanTask keyedTableScanTask,
      Schema tableSchema,
      Schema requestedSchema,
      PrimaryKeySpec primaryKeySpec) {
    this(keyedTableScanTask, tableSchema, requestedSchema, primaryKeySpec, null);
  }

  protected MixedDeleteFilter(
      KeyedTableScanTask keyedTableScanTask,
      Schema tableSchema,
      Schema requestedSchema,
      PrimaryKeySpec primaryKeySpec,
      Set<DataTreeNode> sourceNodes,
      StructLikeCollections structLikeCollections) {
    this(keyedTableScanTask, tableSchema, requestedSchema, primaryKeySpec, sourceNodes);
    this.structLikeCollections = structLikeCollections;
  }

  protected MixedDeleteFilter(
      KeyedTableScanTask keyedTableScanTask,
      Schema tableSchema,
      Schema requestedSchema,
      PrimaryKeySpec primaryKeySpec,
      Set<DataTreeNode> sourceNodes) {
    this.eqDeletes =
        keyedTableScanTask.mixedEquityDeletes().stream()
            .map(MixedFileScanTask::file)
            .collect(Collectors.toSet());

    Map<String, DeleteFile> map = new HashMap<>();
    for (MixedFileScanTask mixedFileScanTask : keyedTableScanTask.dataTasks()) {
      for (DeleteFile deleteFile : mixedFileScanTask.deletes()) {
        map.putIfAbsent(deleteFile.path().toString(), deleteFile);
      }
    }
    this.posDeletes = new ArrayList<>(map.values());

    this.pathSets =
        keyedTableScanTask.dataTasks().stream()
            .map(s -> s.file().path().toString())
            .collect(Collectors.toSet());

    this.primaryKeyId =
        primaryKeySpec.primaryKeyStruct().fields().stream()
            .map(Types.NestedField::fieldId)
            .collect(Collectors.toSet());
    this.requiredSchema = fileProjection(tableSchema, requestedSchema, eqDeletes, posDeletes);
    this.deleteSchema =
        TypeUtil.join(
            TypeUtil.select(requiredSchema, Sets.newHashSet(primaryKeyId)),
            new Schema(MetadataColumns.FILE_OFFSET_FILED, MetadataColumns.TRANSACTION_ID_FILED));
    if (CollectionUtils.isNotEmpty(sourceNodes)) {
      this.deleteNodeFilter =
          new NodeFilter<>(
              sourceNodes,
              deleteSchema,
              primaryKeySpec,
              record -> new InternalRecordWrapper(deleteSchema.asStruct()).wrap(record));
    } else {
      this.deleteNodeFilter = null;
    }
    this.dataTransactionIdAccessor =
        requiredSchema.accessorForField(MetadataColumns.TRANSACTION_ID_FILED_ID);
    this.dataOffsetAccessor = requiredSchema.accessorForField(MetadataColumns.FILE_OFFSET_FILED_ID);
    this.deleteTransactionIdAccessor =
        deleteSchema.accessorForField(MetadataColumns.TRANSACTION_ID_FILED_ID);
    this.deleteOffsetAccessor = deleteSchema.accessorForField(MetadataColumns.FILE_OFFSET_FILED_ID);

    this.posAccessor =
        requiredSchema.accessorForField(org.apache.iceberg.MetadataColumns.ROW_POSITION.fieldId());
    this.filePathAccessor =
        requiredSchema.accessorForField(org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId());
  }

  public Schema requiredSchema() {
    return requiredSchema;
  }

  /** Wrap the data as a {@link StructLike}. */
  protected abstract StructLike asStructLike(T record);

  protected abstract InputFile getInputFile(String location);

  protected long pos(T record) {
    return (Long) posAccessor.get(asStructLike(record));
  }

  protected String filePath(T record) {
    return filePathAccessor.get(asStructLike(record)).toString();
  }

  protected AuthenticatedFileIO getFileIO() {
    return null;
  }

  /** @return The data not in equity delete file */
  public CloseableIterable<T> filter(CloseableIterable<T> records) {
    return new CloseableIterableWrapper<>(
        apply(apply(records, applyPosDeletes().negate()), applyEqDeletes().negate()), eqPredicate);
  }

  /** @return The data in equity delete file */
  public CloseableIterable<T> filterNegate(CloseableIterable<T> records) {
    return new CloseableIterableWrapper<>(
        apply(records, applyEqDeletes().or(applyPosDeletes())), eqPredicate);
  }

  public void setCurrentDataPath(String currentDataPath) {
    this.currentDataPath = currentDataPath;
    this.currentPosSet = null;
  }

  private ChangedLsn deleteLSN(StructLike structLike) {
    Long transactionId = (Long) deleteTransactionIdAccessor.get(structLike);
    Long deleteOffset = (Long) deleteOffsetAccessor.get(structLike);
    return ChangedLsn.of(transactionId, deleteOffset);
  }

  private ChangedLsn dataLSN(StructLike structLike) {
    Long transactionId = (Long) dataTransactionIdAccessor.get(structLike);
    Long deleteOffset = (Long) dataOffsetAccessor.get(structLike);
    return ChangedLsn.of(transactionId, deleteOffset);
  }

  private Predicate<T> applyEqDeletes() {
    if (eqPredicate != null) {
      return eqPredicate;
    }

    if (eqDeletes.isEmpty()) {
      return record -> false;
    }

    Schema pkSchema = TypeUtil.select(requiredSchema, primaryKeyId);
    // a projection to select and reorder fields of the file schema to match the delete rows
    StructProjection deletePKProjectRow = StructProjection.create(deleteSchema, pkSchema);
    StructProjection dataPKProjectRow = StructProjection.create(requiredSchema, pkSchema);

    Iterable<CloseableIterable<Record>> deleteRecords =
        Iterables.transform(eqDeletes, this::openDeletes);

    // copy the delete records because they will be held in a map
    CloseableIterable<Record> records =
        CloseableIterable.transform(CloseableIterable.concat(deleteRecords), Record::copy);
    if (deleteNodeFilter != null) {
      records = deleteNodeFilter.filter(records);
    }

    InternalRecordWrapper internalRecordWrapper =
        new InternalRecordWrapper(deleteSchema.asStruct());
    CloseableIterable<StructLike> structLikeIterable =
        CloseableIterable.transform(records, internalRecordWrapper::copyFor);

    StructLikeBaseMap<ChangedLsn> structLikeMap =
        structLikeCollections.createStructLikeMap(pkSchema.asStruct());
    // init map
    try (CloseableIterable<StructLike> deletes = structLikeIterable) {
      Iterator<StructLike> it =
          getFileIO() == null ? deletes.iterator() : getFileIO().doAs(deletes::iterator);
      while (it.hasNext()) {
        StructLike structLike = it.next();
        StructLike deletePK = deletePKProjectRow.copyFor(structLike);
        ChangedLsn deleteLsn = deleteLSN(structLike);

        ChangedLsn old = structLikeMap.get(deletePK);
        if (old == null || old.compareTo(deleteLsn) <= 0) {
          structLikeMap.put(deletePK, deleteLsn);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Predicate<T> isInDeleteSet =
        record -> {
          StructLike data = asStructLike(record);
          StructLike dataPk = dataPKProjectRow.copyFor(data);
          ChangedLsn dataLSN = dataLSN(data);
          ChangedLsn deleteLsn = structLikeMap.get(dataPk);
          if (deleteLsn == null) {
            return false;
          }

          return deleteLsn.compareTo(dataLSN) > 0;
        };

    this.eqPredicate = new CloseablePredicate<>(isInDeleteSet, structLikeMap);
    return isInDeleteSet;
  }

  private CloseableIterable<T> applyEqDeletes(
      CloseableIterable<T> records, Predicate<T> predicate) {
    if (eqDeletes.isEmpty()) {
      return records;
    }

    Filter<T> remainingRowsFilter =
        new Filter<T>() {
          @Override
          protected boolean shouldKeep(T item) {
            return predicate.test(item);
          }
        };

    return remainingRowsFilter.filter(records);
  }

  private CloseableIterable<Record> openDeletes(PrimaryKeyedFile deleteFile) {
    InputFile input = getInputFile(deleteFile.path().toString());
    Map<Integer, Object> idToConstant = new HashMap<>();
    idToConstant.put(MetadataColumns.TRANSACTION_ID_FILED_ID, deleteFile.transactionId());
    switch (deleteFile.format()) {
      case AVRO:
        return Avro.read(input)
            .project(deleteSchema)
            .reuseContainers()
            .createReaderFunc(
                fileSchema -> DataReader.create(deleteSchema, fileSchema, idToConstant))
            .build();

      case PARQUET:
        return openParquet(input, deleteSchema, idToConstant);

      case ORC:
        return ORC.read(input)
            .project(deleteSchema)
            .createReaderFunc(
                fileSchema -> GenericOrcReader.buildReader(deleteSchema, fileSchema, idToConstant))
            .build();
      default:
        throw new UnsupportedOperationException(
            String.format(
                "Cannot read deletes, %s is not a supported format: %s",
                deleteFile.format().name(), deleteFile.path()));
    }
  }

  protected CloseableIterable<Record> openParquet(
      InputFile input, Schema deleteSchema, Map<Integer, Object> idToConstant) {
    Parquet.ReadBuilder builder =
        Parquet.read(input)
            .project(deleteSchema)
            .reuseContainers()
            .createReaderFunc(
                fileSchema ->
                    GenericParquetReaders.buildReader(deleteSchema, fileSchema, idToConstant));

    return builder.build();
  }

  private Predicate<T> applyPosDeletes() {
    if (posDeletes.isEmpty()) {
      return record -> false;
    }

    // if there are fewer deletes than a reasonable number to keep in memory, use a set
    if (positionMap == null) {
      positionMap = new HashMap<>();
      List<CloseableIterable<Record>> deletes = Lists.transform(posDeletes, this::openPosDeletes);
      CloseableIterator<Record> iterator = CloseableIterable.concat(deletes).iterator();
      while (iterator.hasNext()) {
        Record deleteRecord = iterator.next();
        String path = FILENAME_ACCESSOR.get(deleteRecord).toString();
        if (!pathSets.contains(path)) {
          continue;
        }
        Set<Long> posSet = positionMap.get(path);
        if (posSet == null) {
          posSet = new HashSet<>();
          positionMap.put(path, posSet);
        }
        posSet.add((Long) POSITION_ACCESSOR.get(deleteRecord));
      }
    }

    return item -> {
      Set<Long> posSet;
      if (currentDataPath != null) {
        if (currentPosSet == null) {
          currentPosSet = positionMap.get(currentDataPath);
        }
        posSet = currentPosSet;
      } else {
        posSet = positionMap.get(filePath(item));
      }

      if (posSet == null) {
        return false;
      }
      return posSet.contains(pos(item));
    };
  }

  private CloseableIterable<T> apply(CloseableIterable<T> records, Predicate<T> predicate) {
    Filter<T> filter =
        new Filter<T>() {
          @Override
          protected boolean shouldKeep(T item) {
            return predicate.test(item);
          }
        };

    return filter.filter(records);
  }

  private CloseableIterable<Record> openPosDeletes(DeleteFile file) {
    return openPositionDeletes(file, POS_DELETE_SCHEMA);
  }

  private CloseableIterable<Record> openPositionDeletes(
      DeleteFile deleteFile, Schema deleteSchema) {
    InputFile input = getInputFile(deleteFile.path().toString());
    switch (deleteFile.format()) {
      case AVRO:
        return Avro.read(input)
            .project(deleteSchema)
            .reuseContainers()
            .createReaderFunc(DataReader::create)
            .build();

      case PARQUET:
        Parquet.ReadBuilder builder =
            Parquet.read(input)
                .project(deleteSchema)
                .reuseContainers()
                .createReaderFunc(
                    fileSchema -> GenericParquetReaders.buildReader(deleteSchema, fileSchema));

        return builder.build();

      case ORC:
        return ORC.read(input)
            .project(deleteSchema)
            .createReaderFunc(fileSchema -> GenericOrcReader.buildReader(deleteSchema, fileSchema))
            .build();
      default:
        throw new UnsupportedOperationException(
            String.format(
                "Cannot read deletes, %s is not a supported format: %s",
                deleteFile.format().name(), deleteFile.path()));
    }
  }

  private Schema fileProjection(
      Schema tableSchema,
      Schema requestedSchema,
      Collection<PrimaryKeyedFile> eqDeletes,
      Collection<DeleteFile> posDeletes) {
    if (eqDeletes.isEmpty() && posDeletes.isEmpty()) {
      return requestedSchema;
    }

    Set<Integer> requiredIds = Sets.newLinkedHashSet();
    if (!posDeletes.isEmpty()) {
      requiredIds.add(org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId());
      requiredIds.add(org.apache.iceberg.MetadataColumns.ROW_POSITION.fieldId());
    }

    if (!eqDeletes.isEmpty()) {
      requiredIds.addAll(primaryKeyId);
      requiredIds.add(MetadataColumns.TRANSACTION_ID_FILED.fieldId());
      requiredIds.add(MetadataColumns.FILE_OFFSET_FILED.fieldId());
    }

    Set<Integer> missingIds =
        Sets.newLinkedHashSet(
            Sets.difference(requiredIds, TypeUtil.getProjectedIds(requestedSchema)));

    // TODO: support adding nested columns. this will currently fail when finding nested columns to
    // add
    List<Types.NestedField> columns = Lists.newArrayList(requestedSchema.columns());
    for (int fieldId : missingIds) {
      if (fieldId == org.apache.iceberg.MetadataColumns.ROW_POSITION.fieldId()
          || fieldId == org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId()
          || fieldId == MetadataColumns.TRANSACTION_ID_FILED.fieldId()
          || fieldId == MetadataColumns.FILE_OFFSET_FILED.fieldId()) {
        continue;
      }

      Types.NestedField field = tableSchema.asStruct().field(fieldId);
      Preconditions.checkArgument(field != null, "Cannot find required field for ID %s", fieldId);

      columns.add(field);
    }

    if (missingIds.contains(org.apache.iceberg.MetadataColumns.FILE_PATH.fieldId())) {
      columns.add(org.apache.iceberg.MetadataColumns.FILE_PATH);
    }

    if (missingIds.contains(org.apache.iceberg.MetadataColumns.ROW_POSITION.fieldId())) {
      columns.add(org.apache.iceberg.MetadataColumns.ROW_POSITION);
    }

    // add lsn
    if (missingIds.contains(MetadataColumns.TRANSACTION_ID_FILED.fieldId())) {
      columns.add(MetadataColumns.TRANSACTION_ID_FILED);
    }
    if (missingIds.contains(MetadataColumns.FILE_OFFSET_FILED.fieldId())) {
      columns.add(MetadataColumns.FILE_OFFSET_FILED);
    }

    return new Schema(columns);
  }
}
