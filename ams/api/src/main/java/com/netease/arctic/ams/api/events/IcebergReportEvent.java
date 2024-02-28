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

package com.netease.arctic.ams.api.events;

import com.netease.arctic.ams.api.TableFormat;
import org.apache.iceberg.metrics.MetricsReport;

/** An event triggered when iceberg report metrics */
public class IcebergReportEvent implements TableEvent {
  private final MetricsReport report;
  private final long timestamp;
  private final String catalog;
  private final String database;
  private final String table;

  public IcebergReportEvent(String catalog, String database, String table, MetricsReport report) {
    this.report = report;
    this.timestamp = System.currentTimeMillis();
    this.catalog = catalog;
    this.database = database;
    this.table = table;
  }

  @Override
  public EventType type() {
    return EventType.ICEBERG_REPORT;
  }

  @Override
  public long timestampMillis() {
    return timestamp;
  }

  @Override
  public String catalog() {
    return catalog;
  }

  @Override
  public String database() {
    return database;
  }

  @Override
  public String table() {
    return table;
  }

  @Override
  public TableFormat format() {
    return TableFormat.ICEBERG;
  }

  /**
   * Get iceberg metric report content
   *
   * @return Iceberg metric report content
   */
  public MetricsReport getReport() {
    return report;
  }
}
