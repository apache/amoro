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

package com.netease.arctic.ams.server.model;

import com.netease.arctic.ams.api.OptimizeType;

import javax.annotation.Nullable;

public class TaskConfig {
  private final OptimizeType optimizeType;
  private final String partition;
  @Nullable
  private final Long maxTransactionId;
  private final Long minTransactionId;
  private final String commitGroup;
  private final String planGroup;
  private final long createTime;
  private final String customHiveSubdirectory;

  public TaskConfig(String partition, @Nullable Long maxTransactionId,
                    @Nullable Long minTransactionId,
                    String commitGroup, String planGroup,
                    OptimizeType optimizeType, long createTime,
                    @Nullable String customHiveSubdirectory) {
    this.optimizeType = optimizeType;
    this.partition = partition;
    this.maxTransactionId = maxTransactionId;
    this.minTransactionId = minTransactionId;
    this.commitGroup = commitGroup;
    this.planGroup = planGroup;
    this.createTime = createTime;
    this.customHiveSubdirectory = customHiveSubdirectory;
  }

  public OptimizeType getOptimizeType() {
    return optimizeType;
  }

  public String getPartition() {
    return partition;
  }

  @javax.annotation.Nullable
  public Long getMaxTransactionId() {
    return maxTransactionId;
  }

  @javax.annotation.Nullable
  public Long getMinTransactionId() {
    return minTransactionId;
  }

  public String getCommitGroup() {
    return commitGroup;
  }

  public String getPlanGroup() {
    return planGroup;
  }

  public long getCreateTime() {
    return createTime;
  }

  public String getCustomHiveSubdirectory() {
    return customHiveSubdirectory;
  }
}
