package com.netease.arctic.flink.read.source;

import org.apache.iceberg.io.CloseableIterator;

import java.io.IOException;
import java.util.Collections;

/**
 * Flink data iterator that reads {@link com.netease.arctic.scan.KeyedTableScanTask} into a {@link CloseableIterator}
 *
 * @param <T> is the output data type returned by this iterator.
 */
public class MORDataIterator<T> extends DataIterator<T> {

  private CloseableIterator<T> iterator;

  public MORDataIterator(CloseableIterator<T> iterator) {
    super(null, Collections.emptyList(), null, null);
    this.iterator = iterator;
  }

  @Override
  public void seek(int startingFileOffset, long startingRecordOffset) {
    // skip records within the file
    for (long i = 0; i < startingRecordOffset; ++i) {
      if (hasNext()) {
        next();
      } else {
        throw new IllegalStateException(String.format(
            "Invalid starting record offset %d for file %d from KeyedTableScanTask List.",
            startingRecordOffset, startingFileOffset));
      }
    }
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public T next() {
    return this.iterator.next();
  }

  public boolean currentFileHasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public void close() throws IOException {
    // close the current iterator
    this.iterator.close();
    this.iterator = null;
  }
}
