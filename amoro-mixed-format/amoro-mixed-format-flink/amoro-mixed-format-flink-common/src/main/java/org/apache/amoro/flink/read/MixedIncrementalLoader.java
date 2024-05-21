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

package org.apache.amoro.flink.read;

import org.apache.amoro.flink.read.hybrid.enumerator.ContinuousEnumerationResult;
import org.apache.amoro.flink.read.hybrid.enumerator.ContinuousSplitPlanner;
import org.apache.amoro.flink.read.hybrid.enumerator.MixedFormatEnumeratorOffset;
import org.apache.amoro.flink.read.hybrid.reader.DataIteratorReaderFunction;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.hive.io.reader.AbstractAdaptHiveKeyedDataReader;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a mixed-format table(mixed iceberg, mixed-hive) incremental loader.
 *
 * <p>This loader is used to load data by the merge on read approach first, then by the incremental
 * pull approach.
 *
 * <p>Merge on read approach only contain INSERT rows.
 *
 * <p>Incremental pull approach contains INSERT, DELETE, UPDATE_BEFORE, and UPDATE_AFTER.
 *
 * <p>Support projection and filter push-down to speed up the loading process.
 */
public class MixedIncrementalLoader<T> implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(MixedIncrementalLoader.class);
  private final ContinuousSplitPlanner continuousSplitPlanner;
  private final DataIteratorReaderFunction<T> readerFunction;
  private AbstractAdaptHiveKeyedDataReader<T> flinkMORDataReader;
  private final List<Expression> filters;
  private final AtomicReference<MixedFormatEnumeratorOffset> enumeratorPosition;
  private final Queue<MixedFormatSplit> splitQueue;

  public MixedIncrementalLoader(
      ContinuousSplitPlanner continuousSplitPlanner,
      AbstractAdaptHiveKeyedDataReader<T> flinkMORDataReader,
      DataIteratorReaderFunction<T> readerFunction,
      List<Expression> filters) {
    this.continuousSplitPlanner = continuousSplitPlanner;
    this.flinkMORDataReader = flinkMORDataReader;
    this.readerFunction = readerFunction;
    this.filters = filters;
    this.enumeratorPosition = new AtomicReference<>();
    this.splitQueue = new ArrayDeque<>();
  }

  public MixedIncrementalLoader(
      ContinuousSplitPlanner continuousSplitPlanner,
      DataIteratorReaderFunction<T> readerFunction,
      List<Expression> filters) {
    this.continuousSplitPlanner = continuousSplitPlanner;
    this.readerFunction = readerFunction;
    this.filters = filters;
    this.enumeratorPosition = new AtomicReference<>();
    this.splitQueue = new ArrayDeque<>();
  }

  public boolean hasNext() {
    if (splitQueue.isEmpty()) {
      ContinuousEnumerationResult planResult =
          continuousSplitPlanner.planSplits(enumeratorPosition.get(), filters);
      if (!planResult.isEmpty()) {
        planResult.splits().forEach(split -> LOG.info("Putting this split into queue: {}.", split));
        splitQueue.addAll(planResult.splits());
      }
      if (!planResult.toOffset().isEmpty()) {
        enumeratorPosition.set(planResult.toOffset());
      }
      LOG.info(
          "Currently, queue contain {} splits, scan position is {}.",
          splitQueue.size(),
          enumeratorPosition.get());
      return !splitQueue.isEmpty();
    }
    return true;
  }

  public CloseableIterator<T> next() {
    MixedFormatSplit split = splitQueue.poll();
    if (split == null) {
      throw new IllegalStateException("next() called, but no more valid splits");
    }

    LOG.info("Fetching data by this split:{}.", split);
    if (split.isMergeOnReadSplit()) {
      return flinkMORDataReader.readData(split.asMergeOnReadSplit().keyedTableScanTask());
    }
    return readerFunction.createDataIterator(split);
  }

  @Override
  public void close() throws Exception {
    continuousSplitPlanner.close();
  }
}
