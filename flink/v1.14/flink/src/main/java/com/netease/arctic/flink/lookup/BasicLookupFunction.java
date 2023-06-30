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

package com.netease.arctic.flink.lookup;

import com.netease.arctic.flink.read.MixedIncrementalLoader;
import com.netease.arctic.flink.read.hybrid.enumerator.MergeOnReadIncrementalPlanner;
import com.netease.arctic.flink.read.hybrid.reader.DataIteratorReaderFunction;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.hive.io.reader.AbstractAdaptHiveArcticDataReader;
import com.netease.arctic.table.ArcticTable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.util.FlinkRuntimeException;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import static com.netease.arctic.flink.lookup.LookupMetrics.GROUP_NAME_LOOKUP;
import static com.netease.arctic.flink.lookup.LookupMetrics.LOADING_TIME_MS;
import static com.netease.arctic.flink.table.descriptors.ArcticValidator.LOOKUP_RELOADING_INTERVAL;
import static com.netease.arctic.flink.util.ArcticUtils.loadArcticTable;
import static org.apache.flink.util.Preconditions.checkArgument;

/**
 * This is a basic lookup function for an arctic table.
 */
public class BasicLookupFunction<T> implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(BasicLookupFunction.class);
  private static final long serialVersionUID = 1671720424494168710L;
  private ArcticTable arcticTable;
  private KVTable<T> kvTable;
  private final List<String> joinKeys;
  private final Schema projectSchema;
  private final List<Expression> filters;
  private final ArcticTableLoader loader;
  private boolean loading = false;
  private long nextLoadTime = Long.MIN_VALUE;
  private final long reloadIntervalSeconds;
  private MixedIncrementalLoader<T> incrementalLoader;
  private final Configuration config;
  private transient AtomicLong lookupLoadingTimeMs;
  private final Predicate<T> predicate;
  private final TableFactory<T> kvTableFactory;
  private final AbstractAdaptHiveArcticDataReader<T> flinkArcticMORDataReader;
  private final DataIteratorReaderFunction<T> readerFunction;

  public BasicLookupFunction(
      TableFactory<T> tableFactory,
      ArcticTable arcticTable,
      List<String> joinKeys,
      Schema projectSchema,
      List<Expression> filters,
      ArcticTableLoader tableLoader,
      Configuration config,
      Predicate<T> predicate,
      AbstractAdaptHiveArcticDataReader<T> flinkArcticMORDataReader,
      DataIteratorReaderFunction<T> readerFunction) {
    checkArgument(
        arcticTable.isKeyedTable(),
        String.format(
            "Only keyed arctic table support lookup join, this table [%s] is an unkeyed table.", arcticTable.name()));
    Preconditions.checkNotNull(tableFactory, "kvTableFactory cannot be null");
    this.kvTableFactory = tableFactory;
    this.joinKeys = joinKeys;
    this.projectSchema = projectSchema;
    this.filters = filters;
    this.loader = tableLoader;
    this.config = config;
    this.reloadIntervalSeconds = config.get(LOOKUP_RELOADING_INTERVAL).getSeconds();
    this.predicate = predicate;
    this.flinkArcticMORDataReader = flinkArcticMORDataReader;
    this.readerFunction = readerFunction;
  }

  /**
   * Open the lookup function, e.g.: create {@link KVTable} kvTable, and load data.
   *
   * @throws IOException If serialize or deserialize failed
   */
  public void open(FunctionContext context) throws IOException {
    LOG.info("lookup function rowDtaPredicate: {}.", predicate);
    MetricGroup metricGroup = context.getMetricGroup().addGroup(GROUP_NAME_LOOKUP);
    if (arcticTable == null) {
      arcticTable = loadArcticTable(loader).asKeyedTable();
    }
    arcticTable.refresh();

    lookupLoadingTimeMs = new AtomicLong();
    metricGroup.gauge(LOADING_TIME_MS, () -> lookupLoadingTimeMs.get());

    LOG.info(
        "projected schema {}.\n table schema {}.",
        projectSchema,
        arcticTable.schema());
    kvTable = kvTableFactory.create(
        new RowDataStateFactory(generateRocksDBPath(context, arcticTable.name()), metricGroup),
        arcticTable.asKeyedTable().primaryKeySpec().fieldNames(),
        joinKeys,
        projectSchema,
        config,
        predicate);
    kvTable.open();

    this.incrementalLoader =
        new MixedIncrementalLoader<>(
            new MergeOnReadIncrementalPlanner(loader),
            flinkArcticMORDataReader,
            readerFunction,
            filters
        );
    checkAndLoad();
  }

  public List<T> lookup(Object... values) {
    try {
      checkAndLoad();
      RowData lookupKey = GenericRowData.of(values);
      return kvTable.get(lookupKey);
    } catch (Exception e) {
      throw new FlinkRuntimeException(e);
    }
  }

  /**
   * Check whether it is time to periodically load data to kvTable.
   * Support to use {@link Expression} filters to filter the data.
   */
  private synchronized void checkAndLoad() {
    if (nextLoadTime > System.currentTimeMillis()) {
      return;
    }
    if (loading) {
      LOG.info("Mixed table incremental loader is running.");
      return;
    }
    nextLoadTime = System.currentTimeMillis() + 1000 * reloadIntervalSeconds;

    loading = true;
    long batchStart = System.currentTimeMillis();
    while (incrementalLoader.hasNext()) {
      long start = System.currentTimeMillis();
      arcticTable.io().doAs(() -> {
        try (CloseableIterator<T> iterator = incrementalLoader.next()) {
          if (kvTable.initialized()) {
            kvTable.upsert(iterator);
          } else {
            LOG.info("This table {} is still under initialization progress.", arcticTable.name());
            kvTable.initialize(iterator);
          }
        }
        return null;
      });
      LOG.info("Split task fetched, cost {}ms.", System.currentTimeMillis() - start);
    }
    if (!kvTable.initialized()) {
      kvTable.waitInitializationCompleted();
    }
    lookupLoadingTimeMs.set(System.currentTimeMillis() - batchStart);

    LOG.info("{} table lookup loading, these batch tasks completed, cost {}ms.",
        arcticTable.name(), lookupLoadingTimeMs.get());

    loading = false;
  }

  public void close() throws Exception {
    if (kvTable != null) {
      kvTable.close();
    }
  }

  private String generateRocksDBPath(FunctionContext context, String tableName) {
    String tmpPath = getTmpDirectoryFromTMContainer(context);
    File db = new File(tmpPath, tableName + "-lookup-" + UUID.randomUUID());
    return db.toString();
  }

  private static String getTmpDirectoryFromTMContainer(FunctionContext context) {
    try {
      Field field = context.getClass().getDeclaredField("context");
      field.setAccessible(true);
      StreamingRuntimeContext runtimeContext = (StreamingRuntimeContext) field.get(context);
      String[] tmpDirectories =
          runtimeContext.getTaskManagerRuntimeInfo().getTmpDirectories();
      return tmpDirectories[ThreadLocalRandom.current().nextInt(tmpDirectories.length)];
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
