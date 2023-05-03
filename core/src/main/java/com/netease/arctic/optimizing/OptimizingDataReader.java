package com.netease.arctic.optimizing;

import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;

public interface OptimizingDataReader {
  CloseableIterable<Record> readData();

  CloseableIterable<Record> readDeletedData();

  void close();
}
