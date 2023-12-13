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

import com.netease.arctic.ams.api.TableIdentifier;
import org.apache.iceberg.metrics.MetricsReport;

/** Event content for {@link EventType#IcebergReport} */
public class IcebergReportEventContent {

  private final MetricsReport report;
  private final TableIdentifier identifier;

  public IcebergReportEventContent(TableIdentifier identifier, MetricsReport data) {
    this.identifier = identifier;
    this.report = data;
  }

  /**
   * Table identifier of event
   *
   * @return Table identifier
   */
  public TableIdentifier getIdentifier() {
    return identifier;
  }

  /**
   * Iceberg metric report
   *
   * @return iceberg metric report
   */
  public MetricsReport getReport() {
    return report;
  }
}
