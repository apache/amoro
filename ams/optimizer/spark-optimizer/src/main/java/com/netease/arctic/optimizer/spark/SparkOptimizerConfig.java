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

package com.netease.arctic.optimizer.spark;

import com.netease.arctic.optimizer.common.OptimizerConfig;
import org.apache.iceberg.relocated.com.google.common.base.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

public class SparkOptimizerConfig extends OptimizerConfig {
  private final String AMS_POLLING_TIMEOUT = "polling-timeout";

  @Option(
      name = "-pt",
      aliases = "--" + AMS_POLLING_TIMEOUT,
      usage = "The task polling timeout, default 10min",
      required = true)
  private long pollingTimeout = 600000; // 10min

  public SparkOptimizerConfig() {}

  public SparkOptimizerConfig(String[] args) throws CmdLineException {
    super(args);
  }

  public long getPollingTimeout() {
    return pollingTimeout;
  }

  public void setPollingTimeout(long pollingTimeout) {
    this.pollingTimeout = pollingTimeout;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("amsUrl", getAmsUrl())
        .add("executionParallel", getExecutionParallel())
        .add("memorySize", getMemorySize())
        .add("groupName", getGroupName())
        .add("heartBeat", getHeartBeat())
        .add("extendDiskStorage", isExtendDiskStorage())
        .add("rocksDBBasePath", getDiskStoragePath())
        .add("memoryStorageSize", getMemoryStorageSize())
        .add("resourceId", getResourceId())
        .add("pollingTimeout", getPollingTimeout())
        .toString();
  }
}
