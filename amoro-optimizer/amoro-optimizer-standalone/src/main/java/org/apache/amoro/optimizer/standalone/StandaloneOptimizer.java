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

package org.apache.amoro.optimizer.standalone;

import org.apache.amoro.optimizer.common.Optimizer;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.resource.Resource;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.util.ShutdownHookManager;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

public class StandaloneOptimizer {

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneOptimizer.class);

  public static void main(String[] args) throws CmdLineException {
    OptimizerConfig optimizerConfig = new OptimizerConfig(args);
    Optimizer optimizer = new Optimizer(optimizerConfig);

    // calculate optimizer memory allocation
    long memorySize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    optimizerConfig.setMemorySize((int) memorySize);

    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    String processId = runtimeMXBean.getName().split("@")[0];
    optimizer.getToucher().withRegisterProperty(Resource.PROPERTY_JOB_ID, processId);

    // Register with Hadoop's ShutdownHookManager (priority > FS_CACHE) so that our hook
    // runs to completion before Hadoop closes cached FileSystems. JVM Runtime shutdown
    // hooks fire concurrently and lose this race, causing in-flight writers to hit
    // ClosedChannelException during row-group flush.
    // Pass an explicit timeout — the 2-arg overload uses hadoop.service.shutdown.timeout
    // (default 30s), which would cancel our hook well before stopOptimizing's
    // shutdownTimeoutMs deadline.
    ShutdownHookManager.get()
        .addShutdownHook(
            () -> {
              LOG.info("Received shutdown signal, initiating graceful shutdown...");
              optimizer.stopOptimizing();
            },
            FileSystem.SHUTDOWN_HOOK_PRIORITY + 10,
            optimizerConfig.getShutdownTimeoutMs() + 10_000L,
            TimeUnit.MILLISECONDS);
    optimizer.startOptimizing();
  }
}
