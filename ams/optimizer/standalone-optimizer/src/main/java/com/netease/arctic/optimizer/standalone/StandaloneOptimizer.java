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

package com.netease.arctic.optimizer.standalone;

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.common.Optimizer;
import com.netease.arctic.optimizer.common.OptimizerConfig;
import org.kohsuke.args4j.CmdLineException;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class StandaloneOptimizer {
  public static void main(String[] args) throws CmdLineException {
    OptimizerConfig optimizerConfig = new OptimizerConfig(args);
    Optimizer optimizer = new Optimizer(optimizerConfig);

    // calculate optimizer memory allocation
    long memorySize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    optimizerConfig.setMemorySize((int) memorySize);

    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    String processId = runtimeMXBean.getName().split("@")[0];
    optimizer.getToucher().withRegisterProperty(Resource.PROPERTY_JOB_ID, processId);
    optimizer.startOptimizing();
  }
}
