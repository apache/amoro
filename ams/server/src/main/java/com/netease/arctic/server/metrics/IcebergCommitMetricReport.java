/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.AbstractMetricReport;
import com.netease.arctic.ams.api.metrics.FormatType;
import com.netease.arctic.ams.api.metrics.MetricType;
import java.util.Map;
import org.apache.iceberg.metrics.CommitMetricsResult;
import org.apache.iceberg.metrics.ImmutableCommitReport;

public class IcebergCommitMetricReport extends AbstractMetricReport<IcebergCommitMetrics> {
  public final static String TABLE_NAME = "table_name";
  public final static String SNAPSHOT_ID = "snapshot_id";
  public final static String SEQUENCE_NUMBER = "sequence_number";
  public final static String OPERATION = "operation";
  private final ImmutableCommitReport originalReport;

  @TagName(TABLE_NAME)
  private final String tableName;
  @TagName(SNAPSHOT_ID)
  private final long snapshotId;
  @TagName(SEQUENCE_NUMBER)
  private final long sequenceNumber;
  @TagName(OPERATION)
  private final String operation;
  private final IcebergCommitMetrics commitMetrics;

  public IcebergCommitMetricReport(ImmutableCommitReport originalReport) {
    this.originalReport = originalReport;
    this.tableName = originalReport.tableName();
    this.snapshotId = originalReport.snapshotId();
    this.sequenceNumber = originalReport.sequenceNumber();
    this.operation = originalReport.operation();
    this.commitMetrics = new IcebergCommitMetrics(originalReport.commitMetrics());
  }

  @Override
  public FormatType format() {
    return FormatType.ICEBERG;
  }

  @Override
  public IcebergCommitMetrics metrics() {
    return commitMetrics;
  }

  @Override
  public MetricType getType() {
    return MetricType.ICEBERG_COMMIT_METRIC;
  }
}
