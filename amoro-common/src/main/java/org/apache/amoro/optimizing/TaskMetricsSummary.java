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

package org.apache.amoro.optimizing;

import java.util.Map;

/**
 * Format-agnostic task-level metrics summary abstraction that lives in {@code amoro-common} so that
 * {@link org.apache.amoro.process.StagedTaskDescriptor#toMetricsSummary()} can declare a return
 * type without dragging Iceberg/Paimon types across the module boundary.
 *
 * <p>The Iceberg {@code MetricsSummary} implements this interface directly; the Paimon side emits a
 * lightweight adapter from {@code PaimonMetricsSummary#toMetricsSummary()} that writes Iceberg
 * compatible keys ({@code input-data-files}, {@code input-data-size}, {@code output-data-files},
 * {@code output-data-size}) so downstream aggregation can treat both worlds uniformly.
 */
public interface TaskMetricsSummary {

  /**
   * Serialize the summary as a flat {@code key -> stringified value} map, the canonical shape
   * persisted in process tables and rendered by the dashboard.
   *
   * @param humanReadable when {@code true}, byte counts may be pretty-printed (e.g. {@code 1.2 GB})
   *     instead of raw byte numbers; when {@code false}, emit raw numeric strings
   */
  Map<String, String> summaryAsMap(boolean humanReadable);
}
