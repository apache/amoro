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

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.CloseablePredicate;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Multimap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Multimaps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.hash.BloomFilter;
import org.apache.amoro.utils.ContentFiles;
import org.apache.amoro.utils.DynMethods;
import org.apache.amoro.utils.map.StructLikeBaseMap;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.Accessor;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.data.BaseDeleteLoader;
import org.apache.iceberg.data.DeleteLoader;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.avro.DataReader;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.DeleteSchemaUtil;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.util.Filter;
import org.apache.iceberg.util.StructProjection;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Special point:
 *
 * <ul>
 *   <li>Apply all delete file to all data file
 *   <li>EQUALITY_DELETES only be written by flink in current, so the schemas of all
 *       EQUALITY_DELETES is primary key
 * </ul>
 */
public abstract class CombinedDeleteFilter<T extends StructLike> {

  private static final Logger LOG = LoggerFactory.getLogger(CombinedDeleteFilter.class);

  private static final Schema POS_DELETE_SCHEMA = DeleteSchemaUtil.pathPosSchema();

  private static final Accessor<StructLike> FILENAME_ACCESSOR =
      POS_DELETE_SCHEMA.accessorForField(MetadataColumns.DELETE_FILE_PATH.fieldId());
  private static final Accessor<StructLike> POSITION_ACCESSOR =
      POS_DELETE_SCHEMA.accessorForField(MetadataColumns.DELETE_FILE_POS.fieldId());

  // BaseDeleteLoader#getOrReadEqDeletes is a private method, accessed it via reflection
  private static final DynMethods.UnboundMethod READ_EQ_DELETES_METHOD =
      DynMethods.builder("getOrReadEqDeletes")
          .hiddenImpl(BaseDeleteLoader.class, DeleteFile.class, Schema.class)
          .build();

  @VisibleForTesting public static long FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 1000000L;

  private final RewriteFilesInput input;
  private final List<DeleteFile> posDeletes;
  // There may have multiple equality delete fields within a rewrite input
  private final Multimap<Set<Integer>, DeleteFile> eqDeleteFilesByDeleteIds =
      Multimaps.newMultimap(Maps.newHashMap(), Lists::newArrayList);

  private Map<String, Roaring64Bitmap> positionMap;

  private final Set<String> positionPathSets;

  // The delete ids are union of all equality delete fields
  private final Set<Integer> deleteIds = new HashSet<>();

  private CloseablePredicate<StructForDelete<T>> eqPredicate;

  // Include all identifier fields of equality delete files
  private final Schema requiredSchema;

  private final Map<Set<Integer>, Schema> deleteSchemaByDeleteIds = new HashMap<>();

  private StructLikeCollections structLikeCollections = StructLikeCollections.DEFAULT;

  private final long dataRecordCnt;
  private final boolean filterEqDelete;
  private final DeleteLoader deleteLoader;
  private final String deleteGroup;

  protected CombinedDeleteFilter(
      RewriteFilesInput rewriteFilesInput,
      Schema tableSchema,
      StructLikeCollections structLikeCollections,
      String deleteGroup) {
    this.input = rewriteFilesInput;
    this.dataRecordCnt =
        Arrays.stream(rewriteFilesInput.dataFiles()).mapToLong(ContentFile::recordCount).sum();
    ImmutableList.Builder<DeleteFile> posDeleteBuilder = ImmutableList.builder();
    if (rewriteFilesInput.deleteFiles() != null) {
      for (ContentFile<?> delete : rewriteFilesInput.deleteFiles()) {
        switch (delete.content()) {
          case POSITION_DELETES:
            posDeleteBuilder.add(ContentFiles.asDeleteFile(delete));
            break;
          case EQUALITY_DELETES:
            DeleteFile deleteFile = ContentFiles.asDeleteFile(delete);
            Set<Integer> eqFieldIds = Sets.newHashSet(delete.equalityFieldIds());
            deleteIds.addAll(eqFieldIds);
            eqDeleteFilesByDeleteIds.put(eqFieldIds, deleteFile);
            deleteSchemaByDeleteIds.computeIfAbsent(
                eqFieldIds, ids -> TypeUtil.select(tableSchema, ids));
            break;
          default:
            throw new UnsupportedOperationException(
                "Unknown delete file content: " + delete.content());
        }
      }
    }
    this.positionPathSets =
        Arrays.stream(rewriteFilesInput.dataFiles())
            .map(s -> s.path().toString())
            .collect(Collectors.toSet());
    this.posDeletes = posDeleteBuilder.build();
    this.requiredSchema = TypeUtil.select(tableSchema, deleteIds);

    if (structLikeCollections != null) {
      this.structLikeCollections = structLikeCollections;
    }
    this.filterEqDelete = filterEqDelete();
    this.deleteGroup = deleteGroup;
    this.deleteLoader = new CachingDeleteLoader(this::getInputFile);
  }

  /**
   * Whether to use {@link BloomFilter} to filter eq delete and reduce the amount of data written to
   * {@link StructLikeBaseMap} by eq delete
   */
  private boolean filterEqDelete() {
    long eqDeleteRecordCnt =
        Arrays.stream(input.deleteFiles())
            .filter(file -> file.content() == FileContent.EQUALITY_DELETES)
            .mapToLong(ContentFile::recordCount)
            .sum();
    return eqDeleteRecordCnt > FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT;
  }

  @VisibleForTesting
  public boolean isFilterEqDelete() {
    return filterEqDelete;
  }

  protected abstract InputFile getInputFile(ContentFile<?> contentFile);

  protected abstract AuthenticatedFileIO getFileIO();

  /**
   * Get all delete ids of equality delete files
   *
   * <p>For example, if there are two equality delete fields, one is [1, 2] and another is [1], the
   * delete ids will be [1, 2].
   *
   * @return delete ids
   */
  public Set<Integer> deleteIds() {
    return deleteIds;
  }

  public boolean hasPosition() {
    return posDeletes != null && !posDeletes.isEmpty();
  }

  public void close() {
    positionMap = null;
    try {
      if (eqPredicate != null) {
        eqPredicate.close();
      }
    } catch (IOException e) {
      LOG.error("", e);
    }
    eqPredicate = null;
  }

  public CloseableIterable<StructForDelete<T>> filter(
      CloseableIterable<StructForDelete<T>> records) {
    return applyEqDeletes(applyPosDeletes(records));
  }

  public CloseableIterable<StructForDelete<T>> filterNegate(
      CloseableIterable<StructForDelete<T>> records) {
    Predicate<StructForDelete<T>> inEq = applyEqDeletes();
    Predicate<StructForDelete<T>> inPos = applyPosDeletes();
    Predicate<StructForDelete<T>> or = inEq.or(inPos);
    Filter<StructForDelete<T>> remainingRowsFilter =
        new Filter<StructForDelete<T>>() {
          @Override
          protected boolean shouldKeep(StructForDelete<T> item) {
            return or.test(item);
          }
        };

    return remainingRowsFilter.filter(records);
  }

  private Predicate<StructForDelete<T>> applyEqDeletes() {
    if (eqPredicate != null) {
      return eqPredicate;
    }

    if (eqDeleteFilesByDeleteIds.isEmpty()) {
      return record -> false;
    }

    List<Predicate<StructForDelete<T>>> isInDeleteSets = Lists.newArrayList();
    List<Closeable> structMapCloseable = Lists.newArrayList();
    BloomFilter<StructLike> bloomFilter = initializeBloomFilter();
    for (Map.Entry<Set<Integer>, Schema> deleteSchemaEntry : deleteSchemaByDeleteIds.entrySet()) {
      Predicate<StructForDelete<T>> predicate =
          applyEqDeletesForSchema(deleteSchemaEntry, bloomFilter, structMapCloseable);
      isInDeleteSets.add(predicate);
    }

    Predicate<StructForDelete<T>> isInDelete =
        isInDeleteSets.stream().reduce(Predicate::or).orElse(record -> false);
    this.eqPredicate =
        new CloseablePredicate<>(isInDelete, structMapCloseable.toArray(new Closeable[0]));
    return isInDelete;
  }

  private BloomFilter<StructLike> initializeBloomFilter() {
    if (!filterEqDelete) {
      return null;
    }

    LOG.debug(
        "Enable bloom-filter to filter eq-delete, (rewrite + rewrite pos) data count is {}",
        dataRecordCnt);
    // one million data is about 1.71M memory usage
    BloomFilter<StructLike> bloomFilter =
        BloomFilter.create(StructLikeFunnel.INSTANCE, dataRecordCnt, 0.001);

    Map<Set<Integer>, InternalRecordWrapper> recordWrappers = Maps.newHashMap();
    for (Map.Entry<Set<Integer>, Schema> deleteSchemaEntry : deleteSchemaByDeleteIds.entrySet()) {
      Set<Integer> ids = deleteSchemaEntry.getKey();
      Schema deleteSchema = deleteSchemaEntry.getValue();

      InternalRecordWrapper internalRecordWrapper =
          new InternalRecordWrapper(deleteSchema.asStruct());
      recordWrappers.put(ids, internalRecordWrapper);
    }

    try (CloseableIterable<Record> deletes = readRecords()) {
      for (Record record : deletes) {
        recordWrappers.forEach(
            (ids, internalRecordWrapper) -> {
              Schema deleteSchema = deleteSchemaByDeleteIds.get(ids);
              StructProjection projection =
                  StructProjection.create(requiredSchema, deleteSchema).wrap(record);
              StructLike deletePK = internalRecordWrapper.copyFor(projection);
              bloomFilter.put(deletePK);
            });
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return bloomFilter;
  }

  private CloseableIterable<Record> readRecords() {
    return CloseableIterable.concat(
        CloseableIterable.transform(
            CloseableIterable.withNoopClose(
                Arrays.stream(input.dataFiles()).collect(Collectors.toList())),
            s -> openFile(s, requiredSchema)));
  }

  private Predicate<StructForDelete<T>> applyEqDeletesForSchema(
      Map.Entry<Set<Integer>, Schema> deleteSchemaEntry,
      BloomFilter<StructLike> bloomFilter,
      List<Closeable> structMapCloseable) {
    Set<Integer> ids = deleteSchemaEntry.getKey();
    Schema deleteSchema = deleteSchemaEntry.getValue();
    Iterable<DeleteFile> eqDeletes = eqDeleteFilesByDeleteIds.get(ids);

    InternalRecordWrapper internalRecordWrapper =
        new InternalRecordWrapper(deleteSchema.asStruct());
    StructLikeBaseMap<Long> structLikeMap =
        structLikeCollections.createStructLikeMap(deleteSchema.asStruct());
    structMapCloseable.add(structLikeMap);

    for (DeleteFile deleteFile : eqDeletes) {
      for (StructLike record : openEqualityDeletes(deleteFile, deleteSchema)) {
        StructLike deletePK = internalRecordWrapper.copyFor(record);
        if (filterEqDelete && !bloomFilter.mightContain(deletePK)) {
          continue;
        }
        Long lsn = deleteFile.dataSequenceNumber();
        structLikeMap.merge(deletePK, lsn, Math::max);
      }
    }

    return structForDelete -> {
      StructProjection deleteProjection =
          StructProjection.create(requiredSchema, deleteSchema).wrap(structForDelete.getPk());
      StructLike dataPk = internalRecordWrapper.copyFor(deleteProjection);
      Long dataLSN = structForDelete.getLsn();
      Long deleteLsn = structLikeMap.get(dataPk);
      if (deleteLsn == null) {
        return false;
      }

      return deleteLsn.compareTo(dataLSN) > 0;
    };
  }

  private CloseableIterable<StructForDelete<T>> applyEqDeletes(
      CloseableIterable<StructForDelete<T>> records) {
    Predicate<StructForDelete<T>> remainingRows = applyEqDeletes().negate();
    return eqDeletesBase(records, remainingRows);
  }

  private CloseableIterable<StructForDelete<T>> eqDeletesBase(
      CloseableIterable<StructForDelete<T>> records, Predicate<StructForDelete<T>> predicate) {
    // Predicate to test whether a row should be visible to user after applying equality deletions.
    if (eqDeleteFilesByDeleteIds.isEmpty()) {
      return records;
    }

    Filter<StructForDelete<T>> remainingRowsFilter =
        new Filter<StructForDelete<T>>() {
          @Override
          protected boolean shouldKeep(StructForDelete<T> item) {
            return predicate.test(item);
          }
        };

    return remainingRowsFilter.filter(records);
  }

  private CloseableIterable<StructForDelete<T>> applyPosDeletes(
      CloseableIterable<StructForDelete<T>> records) {
    return applyPosDeletesBase(records, applyPosDeletes().negate());
  }

  private Predicate<StructForDelete<T>> applyPosDeletes() {

    if (posDeletes.isEmpty()) {
      return record -> false;
    }

    if (positionMap == null) {
      positionMap = new HashMap<>();
      List<CloseableIterable<Record>> deletes = Lists.transform(posDeletes, this::openPosDeletes);
      CloseableIterator<Record> iterator = CloseableIterable.concat(deletes).iterator();
      while (iterator.hasNext()) {
        Record deleteRecord = iterator.next();
        String path = FILENAME_ACCESSOR.get(deleteRecord).toString();
        if (positionPathSets != null && !positionPathSets.contains(path)) {
          continue;
        }
        Roaring64Bitmap posBitMap = positionMap.computeIfAbsent(path, k -> new Roaring64Bitmap());
        posBitMap.add((Long) POSITION_ACCESSOR.get(deleteRecord));
      }
    }

    return structLikeForDelete -> {
      Roaring64Bitmap posSet = positionMap.get(structLikeForDelete.filePath());

      if (posSet == null || posSet.isEmpty()) {
        return false;
      }
      return posSet.contains(structLikeForDelete.getPosition());
    };
  }

  private CloseableIterable<StructForDelete<T>> applyPosDeletesBase(
      CloseableIterable<StructForDelete<T>> records, Predicate<StructForDelete<T>> predicate) {
    if (posDeletes.isEmpty()) {
      return records;
    }

    Filter<StructForDelete<T>> filter =
        new Filter<StructForDelete<T>>() {
          @Override
          protected boolean shouldKeep(StructForDelete<T> item) {
            return predicate.test(item);
          }
        };

    return filter.filter(records);
  }

  private CloseableIterable<Record> openPosDeletes(DeleteFile file) {
    return openFile(file, POS_DELETE_SCHEMA);
  }

  private Iterable<StructLike> openEqualityDeletes(DeleteFile file, Schema deleteSchema) {
    return READ_EQ_DELETES_METHOD.invoke(deleteLoader, file, deleteSchema);
  }

  private CloseableIterable<Record> openFile(ContentFile<?> contentFile, Schema deleteSchema) {
    InputFile input = getInputFile(contentFile);
    switch (contentFile.format()) {
      case AVRO:
        return Avro.read(input)
            .project(deleteSchema)
            .reuseContainers()
            .createReaderFunc(DataReader::create)
            .build();

      case PARQUET:
        return Parquet.read(input)
            .project(deleteSchema)
            .reuseContainers()
            .createReaderFunc(
                fileSchema -> GenericParquetReaders.buildReader(deleteSchema, fileSchema))
            .build();

      case ORC:
        // Reusing containers is automatic for ORC. No need to set 'reuseContainers' here.
        return ORC.read(input)
            .project(deleteSchema)
            .createReaderFunc(fileSchema -> GenericOrcReader.buildReader(deleteSchema, fileSchema))
            .build();

      default:
        throw new UnsupportedOperationException(
            String.format(
                "Cannot read deletes, %s is not a supported format: %s",
                contentFile.format().name(), contentFile.path()));
    }
  }

  class CachingDeleteLoader extends BaseDeleteLoader {
    private final DeleteCache cache;

    public CachingDeleteLoader(Function<DeleteFile, InputFile> loadInputFile) {
      super(loadInputFile);
      this.cache = DeleteCache.getInstance();
    }

    @Override
    protected boolean canCache(long size) {
      return cache != null && size < cache.maxEntrySize();
    }

    @Override
    protected <V> V getOrLoad(String key, Supplier<V> valueSupplier, long valueSize) {
      return cache.getOrLoad(CombinedDeleteFilter.this.deleteGroup, key, valueSupplier, valueSize);
    }
  }
}
