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
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableSet;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.hash.BloomFilter;
import org.apache.amoro.utils.ContentFiles;
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
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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

  @VisibleForTesting public static long FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 1000000L;

  private final RewriteFilesInput input;
  private final List<DeleteFile> posDeletes;
  private final List<DeleteFile> eqDeletes;

  private Map<String, Roaring64Bitmap> positionMap;

  private final Set<String> positionPathSets;

  private Set<Integer> deleteIds = new HashSet<>();

  private CloseablePredicate<StructForDelete<T>> eqPredicate;

  private final Schema deleteSchema;

  private StructLikeCollections structLikeCollections = StructLikeCollections.DEFAULT;

  private final long dataRecordCnt;
  private final boolean filterEqDelete;

  protected CombinedDeleteFilter(
      RewriteFilesInput rewriteFilesInput,
      Schema tableSchema,
      StructLikeCollections structLikeCollections) {
    this.input = rewriteFilesInput;
    this.dataRecordCnt =
        Arrays.stream(rewriteFilesInput.dataFiles()).mapToLong(ContentFile::recordCount).sum();
    ImmutableList.Builder<DeleteFile> posDeleteBuilder = ImmutableList.builder();
    ImmutableList.Builder<DeleteFile> eqDeleteBuilder = ImmutableList.builder();
    if (rewriteFilesInput.deleteFiles() != null) {
      String firstDeleteFilePath = null;
      for (ContentFile<?> delete : rewriteFilesInput.deleteFiles()) {
        switch (delete.content()) {
          case POSITION_DELETES:
            posDeleteBuilder.add(ContentFiles.asDeleteFile(delete));
            break;
          case EQUALITY_DELETES:
            if (deleteIds.isEmpty()) {
              deleteIds = ImmutableSet.copyOf(ContentFiles.asDeleteFile(delete).equalityFieldIds());
              firstDeleteFilePath = delete.path().toString();
            } else {
              Set<Integer> currentDeleteIds =
                  ImmutableSet.copyOf(ContentFiles.asDeleteFile(delete).equalityFieldIds());
              if (!deleteIds.equals(currentDeleteIds)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Equality delete files have different delete fields, first equality field ids:[%s],"
                            + " current equality field ids:[%s], first delete file path:[%s], "
                            + " current delete file path: [%s].",
                        deleteIds,
                        currentDeleteIds,
                        firstDeleteFilePath,
                        delete.path().toString()));
              }
            }
            eqDeleteBuilder.add(ContentFiles.asDeleteFile(delete));
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
    this.eqDeletes = eqDeleteBuilder.build();
    this.deleteSchema = TypeUtil.select(tableSchema, deleteIds);

    if (structLikeCollections != null) {
      this.structLikeCollections = structLikeCollections;
    }
    this.filterEqDelete = filterEqDelete();
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

  public Set<Integer> deleteIds() {
    return deleteIds;
  }

  public boolean hasPosition() {
    return posDeletes != null && posDeletes.size() > 0;
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

    if (eqDeletes.isEmpty()) {
      return record -> false;
    }

    InternalRecordWrapper internalRecordWrapper =
        new InternalRecordWrapper(deleteSchema.asStruct());

    BloomFilter<StructLike> bloomFilter = null;
    if (filterEqDelete) {
      LOG.debug(
          "Enable bloom-filter to filter eq-delete, (rewrite + rewrite pos) data count is {}",
          dataRecordCnt);
      // one million data is about 1.71M memory usage
      bloomFilter = BloomFilter.create(StructLikeFunnel.INSTANCE, dataRecordCnt, 0.001);
      try (CloseableIterable<Record> deletes =
          CloseableIterable.concat(
              CloseableIterable.transform(
                  CloseableIterable.withNoopClose(
                      Arrays.stream(input.dataFiles()).collect(Collectors.toList())),
                  s -> openFile(s, deleteSchema)))) {
        for (Record record : deletes) {
          StructLike identifier = internalRecordWrapper.copyFor(record);
          bloomFilter.put(identifier);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    CloseableIterable<RecordWithLsn> deleteRecords =
        CloseableIterable.transform(
            CloseableIterable.concat(
                Iterables.transform(
                    eqDeletes,
                    s ->
                        CloseableIterable.transform(
                            openFile(s, deleteSchema),
                            r -> new RecordWithLsn(s.dataSequenceNumber(), r)))),
            RecordWithLsn::recordCopy);

    StructLikeBaseMap<Long> structLikeMap =
        structLikeCollections.createStructLikeMap(deleteSchema.asStruct());

    // init map
    try (CloseableIterable<RecordWithLsn> deletes = deleteRecords) {
      Iterator<RecordWithLsn> it =
          getFileIO() == null ? deletes.iterator() : getFileIO().doAs(deletes::iterator);
      while (it.hasNext()) {
        RecordWithLsn recordWithLsn = it.next();
        StructLike deletePK = internalRecordWrapper.copyFor(recordWithLsn.getRecord());
        if (filterEqDelete && !bloomFilter.mightContain(deletePK)) {
          continue;
        }
        Long lsn = recordWithLsn.getLsn();
        Long old = structLikeMap.get(deletePK);
        if (old == null || old.compareTo(lsn) <= 0) {
          structLikeMap.put(deletePK, lsn);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Predicate<StructForDelete<T>> isInDeleteSet =
        structForDelete -> {
          StructLike dataPk = internalRecordWrapper.copyFor(structForDelete.getPk());
          Long dataLSN = structForDelete.getLsn();
          Long deleteLsn = structLikeMap.get(dataPk);
          if (deleteLsn == null) {
            return false;
          }

          return deleteLsn.compareTo(dataLSN) > 0;
        };

    CloseablePredicate<StructForDelete<T>> closeablePredicate =
        new CloseablePredicate<>(isInDeleteSet, structLikeMap);
    this.eqPredicate = closeablePredicate;
    return isInDeleteSet;
  }

  private CloseableIterable<StructForDelete<T>> applyEqDeletes(
      CloseableIterable<StructForDelete<T>> records) {
    Predicate<StructForDelete<T>> remainingRows = applyEqDeletes().negate();
    return eqDeletesBase(records, remainingRows);
  }

  private CloseableIterable<StructForDelete<T>> eqDeletesBase(
      CloseableIterable<StructForDelete<T>> records, Predicate<StructForDelete<T>> predicate) {
    // Predicate to test whether a row should be visible to user after applying equality deletions.
    if (eqDeletes.isEmpty()) {
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

  static class RecordWithLsn {
    private final Long lsn;
    private Record record;

    public RecordWithLsn(Long lsn, Record record) {
      this.lsn = lsn;
      this.record = record;
    }

    public Long getLsn() {
      return lsn;
    }

    public Record getRecord() {
      return record;
    }

    public RecordWithLsn recordCopy() {
      record = record.copy();
      return this;
    }
  }
}
