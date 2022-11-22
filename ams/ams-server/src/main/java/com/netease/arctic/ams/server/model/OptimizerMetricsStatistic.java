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

package com.netease.arctic.ams.server.model;

import com.netease.arctic.ams.api.TableIdentifier;
import java.math.BigDecimal;

public class OptimizerMetricsStatistic {
  private long optimizerId;
  private String subtaskId;
  private String metricName;
  private BigDecimal metricValue;
  private Long commitTime;

  public long getOptimizerId() {
    return optimizerId;
  }

  public void setOptimizerId(long optimizerId) {
    this.optimizerId = optimizerId;
  }

  public String getSubtaskId() {
    return subtaskId;
  }

  public void setSubtaskId(String subtaskId) {
    this.subtaskId = subtaskId;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public BigDecimal getMetricValue() {
    return metricValue;
  }

  public void setMetricValue(BigDecimal metricValue) {
    this.metricValue = metricValue;
  }

  public Long getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(Long commitTime) {
    this.commitTime = commitTime;
  }
}
