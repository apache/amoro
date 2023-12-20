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

package com.netease.arctic.ams.api.metrics;

import static com.netease.arctic.ams.api.metrics.MetricDefine.defineGauge;

/** All metric defines. */
public class MetricDefines {

  // table optimizing status duration metrics
  public static final MetricDefine TABLE_OPTIMIZING_STATE_IDLE_DURATION =
      defineGauge("table_optimizing_status_idle_duration_seconds")
          .withDescription("Duration in seconds after table be in idle state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_PENDING_DURATION =
      defineGauge("table_optimizing_status_pending_duration_seconds")
          .withDescription("Duration in seconds after table be in pending state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_PLANNING_DURATION =
      defineGauge("table_optimizing_status_idle_duration_seconds")
          .withDescription("Duration in seconds after table be in planning state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_EXECUTING_DURATION =
      defineGauge("table_optimizing_status_idle_duration_seconds")
          .withDescription("Duration in seconds after table be in executing state")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_OPTIMIZING_STATE_COMMITTING_DURATION =
      defineGauge("table_optimizing_status_idle_duration_seconds")
          .withDescription("Duration in seconds after table be in committing state")
          .withTags("catalog", "database", "table")
          .build();
}
