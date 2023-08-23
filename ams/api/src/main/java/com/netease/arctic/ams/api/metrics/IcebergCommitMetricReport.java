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

package com.netease.arctic.ams.api.metrics;

public interface IcebergCommitMetricReport extends MetricReport{

  public final static String TABLE_NAME = "table_name";
  public final static String SNAPSHOT_ID = "snapshot_id";
  public final static String SEQUENCE_NUMBER = "sequence_number";
  public final static String OPERATION = "operation";

  @MetricWithTags.TagName(TABLE_NAME)
  String tableName();

  @MetricWithTags.TagName(SNAPSHOT_ID)
  long snapshotId();

  @MetricWithTags.TagName(SEQUENCE_NUMBER)
  long sequenceNumber();

  @MetricWithTags.TagName(OPERATION)
  String operation();

  @MetricWithTags.Metric
  IcebergCommitMetrics commitMetrics();
}
