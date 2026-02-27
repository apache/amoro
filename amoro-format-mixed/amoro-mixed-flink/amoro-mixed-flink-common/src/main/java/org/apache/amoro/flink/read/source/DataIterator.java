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

package org.apache.amoro.flink.read.source;

import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.flink.annotation.Internal;
import org.apache.iceberg.io.CloseableIterator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Flink data iterator that reads {@link MixedFileScanTask} into a {@link CloseableIterator}
 *
 * @param <T> T is the output data type returned by this iterator.
 */
@Internal
public class DataIterator<T> implements CloseableIterator<T> {

  private final FileScanTaskReader<T> fileScanTaskReader;
  private final int taskSize;

  private Iterator<MixedFileScanTask> tasks;
  private CloseableIterator<T> currentIterator;
  private int fileOffset;
  private long recordOffset;
  private long currentFileOffset;
  private final Function<T, Long> fileOffsetGetter;
  private final Function<T, T> metaColumnRemover;

  public DataIterator() {
    this(null, Collections.emptyList(), t -> Long.MIN_VALUE, t -> t);
  }

  public DataIterator(
      FileScanTaskReader<T> fileScanTaskReader,
      Collection<MixedFileScanTask> tasks,
      Function<T, Long> fileOffsetGetter,
      Function<T, T> metaColumnRemover) {
    this.fileScanTaskReader = fileScanTaskReader;
    this.tasks = tasks.iterator();
    this.taskSize = tasks.size();
    this.fileOffsetGetter = fileOffsetGetter;
    this.metaColumnRemover = metaColumnRemover;

    this.currentIterator = CloseableIterator.empty();

    // fileOffset starts at -1 because we started
    // from an empty iterator that is not from the split files.
    this.fileOffset = -1;
    // record offset points to the record that next() should return when called
    this.recordOffset = 0L;
    // actual record offset in data file.
    // it's incremental within inserting and deleting files in the same tree node group.
    this.currentFileOffset = 0L;
  }

  /**
   * (startingFileOffset, startingRecordOffset) points to the next row that the reader should resume
   * from. E.g., if the seek position is (file=0, record=1), seek moves the iterator position to the
   * second row in file 0. When next() is called after seek; the second row from file 0 should be
   * returned.
   */
  public void seek(int startingFileOffset, long startingRecordOffset) {
    // It means file is empty.
    if (taskSize == 0) {
      return;
    }
    Preconditions.checkState(
        fileOffset == -1, "Seek should be called before any other iterator actions");
    // skip files
    Preconditions.checkState(
        startingFileOffset < taskSize,
        "Invalid starting file offset %s for combined scan task with %s files.",
        startingFileOffset,
        taskSize);
    for (long i = 0L; i < startingFileOffset; ++i) {
      tasks.next();
    }

    updateCurrentIterator();
    // skip records within the file
    for (long i = 0; i < startingRecordOffset; ++i) {
      if (currentFileHasNext() && hasNext()) {
        next();
      } else {
        throw new IllegalStateException(
            String.format(
                "Invalid starting record offset %d for file %d from FileScanTask List.",
                startingRecordOffset, startingFileOffset));
      }
    }

    fileOffset = startingFileOffset;
    recordOffset = startingRecordOffset;
  }

  @Override
  public boolean hasNext() {
    updateCurrentIterator();
    return currentIterator.hasNext();
  }

  @Override
  public T next() {
    updateCurrentIterator();
    recordOffset += 1;
    T row = currentIterator.next();
    currentFileOffset = fileOffsetGetter.apply(row);
    return metaColumnRemover.apply(row);
  }

  public boolean currentFileHasNext() {
    return currentIterator.hasNext();
  }

  /** Updates the current iterator field to ensure that the current Iterator is not exhausted. */
  private void updateCurrentIterator() {
    try {
      while (!currentIterator.hasNext() && tasks.hasNext()) {
        currentIterator.close();
        currentIterator = openTaskIterator(tasks.next());
        fileOffset += 1;
        recordOffset = 0L;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private CloseableIterator<T> openTaskIterator(MixedFileScanTask scanTask) {
    return fileScanTaskReader.open(scanTask);
  }

  @Override
  public void close() throws IOException {
    // close the current iterator
    currentIterator.close();
    tasks = null;
  }

  public int fileOffset() {
    return fileOffset;
  }

  public long recordOffset() {
    return recordOffset;
  }

  public long currentMixedFormatFileOffset() {
    return currentFileOffset;
  }

  static <T> DataIterator<T> empty() {
    return new EmptyIterator<>();
  }

  private static class EmptyIterator<T> extends DataIterator<T> {

    public EmptyIterator() {
      super(null, Collections.emptyList(), t -> Long.MIN_VALUE, t -> t);
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public T next() {
      throw new NoSuchElementException();
    }

    @Override
    public void seek(int startingFileOffset, long startingRecordOffset) {}
  }
}
