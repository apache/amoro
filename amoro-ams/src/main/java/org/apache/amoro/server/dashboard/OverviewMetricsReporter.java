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

package org.apache.amoro.server.dashboard;

import org.apache.amoro.metrics.MetricReporter;
import org.apache.amoro.metrics.MetricSet;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Overview exporter */
public class OverviewMetricsReporter implements MetricReporter {

  public static final String REFRESH_INTERVAL = "refresh-interval";
  public static final String MAX_HISTORY_RECORDS = "max-history-records";

  private final ScheduledExecutorService overviewUpdaterScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("overview-updater-scheduler-%d")
              .setDaemon(true)
              .build());

  private long overviewRefreshingInterval;
  private int maxRecordCount;

  @Override
  public void open(Map<String, String> properties) {
    overviewRefreshingInterval =
        Optional.ofNullable(properties.get(REFRESH_INTERVAL))
            .map(Long::valueOf)
            .orElseThrow(
                () -> new IllegalArgumentException("Lack required property: " + REFRESH_INTERVAL));

    maxRecordCount =
        Optional.ofNullable(properties.get(MAX_HISTORY_RECORDS))
            .map(Integer::valueOf)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Lack required property: " + MAX_HISTORY_RECORDS));
  }

  @Override
  public void close() {}

  @Override
  public String name() {
    return "overview-exporter";
  }

  @Override
  public void setGlobalMetricSet(MetricSet globalMetricSet) {
    OverviewCache overviewCache = OverviewCache.getInstance();
    overviewCache.initialize(maxRecordCount, globalMetricSet);
    overviewUpdaterScheduler.scheduleAtFixedRate(
        overviewCache::refresh, 1000L, overviewRefreshingInterval, TimeUnit.MILLISECONDS);
  }
}
