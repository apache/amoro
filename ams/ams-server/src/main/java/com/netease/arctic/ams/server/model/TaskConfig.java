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
  private final String group;
  private final String historyId;
  private final long createTime;
  private final String customHiveSubdirectory;

  public TaskConfig(String partition, @Nullable Long maxTransactionId,
                    String group, String historyId,
                    OptimizeType optimizeType, long createTime,
                    @Nullable String customHiveSubdirectory) {
    this.optimizeType = optimizeType;
    this.partition = partition;
    this.maxTransactionId = maxTransactionId;
    this.group = group;
    this.historyId = historyId;
    this.createTime = createTime;
    this.customHiveSubdirectory = customHiveSubdirectory;
  }

  public OptimizeType getOptimizeType() {
    return optimizeType;
  }

  public String getPartition() {
    return partition;
  }

  @org.jetbrains.annotations.Nullable
  public Long getMaxTransactionId() {
    return maxTransactionId;
  }

  public String getGroup() {
    return group;
  }

  public String getHistoryId() {
    return historyId;
  }

  public long getCreateTime() {
    return createTime;
  }

  public String getCustomHiveSubdirectory() {
    return customHiveSubdirectory;
  }
}
