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

import com.netease.arctic.utils.AmoroTypeUtil;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.avro.DataReader;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.orc.OrcRowReader;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.parquet.ParquetValueReader;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.math.LongMath;
import org.apache.iceberg.util.Tasks;
import org.apache.iceberg.util.ThreadPools;
import org.apache.orc.TypeDescription;
import org.apache.parquet.schema.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

/** Copy from {@link org.apache.iceberg.data.BaseDeleteLoader}. */
public class CombinedBaseDeleteLoader implements CombinedDeleteLoader {

  private static final Logger LOG = LoggerFactory.getLogger(CombinedBaseDeleteLoader.class);

  private final Function<DeleteFile, InputFile> loadInputFile;
  private final ExecutorService workerPool;

  public CombinedBaseDeleteLoader(Function<DeleteFile, InputFile> loadInputFile) {
    // when iceberg version upgrade 1.5.0, modify to `ThreadPools.getDeleteWorkerPool()`
    this(loadInputFile, ThreadPools.getWorkerPool());
  }

  /**
   * Checks if the given number of bytes can be cached.
   *
   * <p>Implementations should override this method if they support caching. It is also recommended
   * to use the provided size as a guideline to decide whether the value is eligible for caching.
   * For instance, it may be beneficial to discard values that are too large to optimize the cache
   * performance and utilization.
   */
  protected boolean canCache(long size) {
    return false;
  }

  /**
   * Gets the cached value for the key or populates the cache with a new mapping.
   *
   * <p>If the value for the specified key is in the cache, it should be returned. If the value is
   * not in the cache, implementations should compute the value using the provided supplier, cache
   * it, and then return it.
   *
   * <p>This method will be called only if {@link #canCache(long)} returned true.
   */
  protected <V> V getOrLoad(String key, Supplier<V> valueSupplier, long valueSize) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support caching");
  }

  public CombinedBaseDeleteLoader(
      Function<DeleteFile, InputFile> loadInputFile, ExecutorService workerPool) {
    this.loadInputFile = loadInputFile;
    this.workerPool = workerPool;
  }

  @Override
  public Iterable<RecordWithLsn> loadEqualityDeletes(
      Iterable<DeleteFile> deleteFiles, Schema projection) {
    Iterable<Iterable<RecordWithLsn>> deletes =
        execute(deleteFiles, deleteFile -> getOrReadEqDeletes(deleteFile, projection));
    return Iterables.concat(deletes);
  }

  private Iterable<RecordWithLsn> getOrReadEqDeletes(DeleteFile deleteFile, Schema projection) {
    long estimatedSize = estimateEqDeletesSize(deleteFile, projection);
    if (canCache(estimatedSize)) {
      String cacheKey = deleteFile.path().toString();
      return getOrLoad(cacheKey, () -> readEqDeletes(deleteFile, projection, true), estimatedSize);
    } else {
      return readEqDeletes(deleteFile, projection, false);
    }
  }

  private Iterable<RecordWithLsn> readEqDeletes(
      DeleteFile deleteFile, Schema projection, boolean materialize) {
    CloseableIterable<RecordWithLsn> recordWithLsns =
        CloseableIterable.transform(
            openDeletes(deleteFile, projection),
            r -> new RecordWithLsn(deleteFile.dataSequenceNumber(), r));
    CloseableIterable<RecordWithLsn> copiedDeletes =
        CloseableIterable.transform(recordWithLsns, RecordWithLsn::recordCopy);
    return materialize ? materialize(copiedDeletes) : copiedDeletes;
  }

  // materializes the iterable and releases resources so that the result can be cached
  private <T> Iterable<T> materialize(CloseableIterable<T> iterable) {
    try (CloseableIterable<T> closeableIterable = iterable) {
      return ImmutableList.copyOf(closeableIterable);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close iterable", e);
    }
  }

  private CloseableIterable<Record> openDeletes(DeleteFile deleteFile, Schema projection) {
    return openDeletes(deleteFile, projection, null /* no filter */);
  }

  private CloseableIterable<Record> openDeletes(
      DeleteFile deleteFile, Schema projection, Expression filter) {

    FileFormat format = deleteFile.format();
    LOG.trace("Opening delete file {}", deleteFile.path());
    InputFile inputFile = loadInputFile.apply(deleteFile);

    switch (format) {
      case AVRO:
        return Avro.read(inputFile)
            .project(projection)
            .reuseContainers()
            .createReaderFunc(DataReader::create)
            .build();

      case PARQUET:
        return Parquet.read(inputFile)
            .project(projection)
            .filter(filter)
            .reuseContainers()
            .createReaderFunc(newParquetReaderFunc(projection))
            .build();

      case ORC:
        // reusing containers is automatic for ORC, no need to call 'reuseContainers'
        return ORC.read(inputFile)
            .project(projection)
            .filter(filter)
            .createReaderFunc(newOrcReaderFunc(projection))
            .build();

      default:
        throw new UnsupportedOperationException(
            String.format(
                "Cannot read deletes, %s is not a supported file format: %s",
                format.name(), inputFile.location()));
    }
  }

  private Function<MessageType, ParquetValueReader<?>> newParquetReaderFunc(Schema projection) {
    return fileSchema -> GenericParquetReaders.buildReader(projection, fileSchema);
  }

  private Function<TypeDescription, OrcRowReader<?>> newOrcReaderFunc(Schema projection) {
    return fileSchema -> GenericOrcReader.buildReader(projection, fileSchema);
  }

  private <I, O> Iterable<O> execute(Iterable<I> objects, Function<I, O> func) {
    Queue<O> output = new ConcurrentLinkedQueue<>();

    Tasks.foreach(objects)
        .executeWith(workerPool)
        .stopOnFailure()
        .onFailure((object, exc) -> LOG.error("Failed to process {}", object, exc))
        .run(object -> output.add(func.apply(object)));

    return output;
  }

  // estimates the memory required to cache equality deletes (in bytes)
  private long estimateEqDeletesSize(DeleteFile deleteFile, Schema projection) {
    try {
      long recordCount = deleteFile.recordCount();
      int recordSize = estimateRecordSize(projection);
      return LongMath.checkedMultiply(recordCount, recordSize);
    } catch (ArithmeticException e) {
      return Long.MAX_VALUE;
    }
  }

  private int estimateRecordSize(Schema schema) {
    return schema.columns().stream().mapToInt(AmoroTypeUtil::estimateSize).sum();
  }
}
