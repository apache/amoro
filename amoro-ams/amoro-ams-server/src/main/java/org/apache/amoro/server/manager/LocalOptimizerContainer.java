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

package org.apache.amoro.server.manager;

import org.apache.amoro.api.resource.Resource;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class LocalOptimizerContainer extends AbstractResourceContainer {

  private static final Logger LOG = LoggerFactory.getLogger(LocalOptimizerContainer.class);

  public static final String JOB_MEMORY_PROPERTY = "memory";

  @Override
  protected Map<String, String> doScaleOut(Resource resource) {
    String startUpArgs = this.buildOptimizerStartupArgsString(resource);
    try {
      String exportCmd =
          String.format(
              " export OPTIMIZER_LOG_DIR_NAME=\"optimizer-%s-%s\" ",
              resource.getGroupName(), resource.getResourceId());
      String startUpCommand = exportCmd + " && " + startUpArgs;
      String[] cmd = {"/bin/sh", "-c", startUpCommand};
      LOG.info("Starting local optimizer using command : {}", startUpCommand);
      ExecUtil.exec(cmd, new ArrayList<>());
      return Collections.emptyMap();
    } catch (Exception e) {
      throw new RuntimeException("Failed to scale out optimizer.", e);
    }
  }

  @Override
  protected String buildOptimizerStartupArgsString(Resource resource) {
    String jobMemoryStr = resource.getRequiredProperty(JOB_MEMORY_PROPERTY);
    Preconditions.checkArgument(
        jobMemoryStr.matches("\\d+"), "Invalid memory value: %s, must be a number.", jobMemoryStr);

    long memoryPerThread = Long.parseLong(jobMemoryStr);
    long memory = memoryPerThread * resource.getThreadCount();
    return String.format(
        "%s/bin/optimizer.sh start %s %s",
        amsHome, memory, super.buildOptimizerStartupArgsString(resource));
  }

  @Override
  public void releaseOptimizer(Resource resource) {
    long jobId = Long.parseLong(resource.getRequiredProperty(Resource.PROPERTY_JOB_ID));

    String os = System.getProperty("os.name").toLowerCase();
    String cmd;
    String[] finalCmd;
    if (os.contains("win")) {
      cmd = "taskkill /PID " + jobId + " /F ";
      finalCmd = new String[] {"cmd", "/c", cmd};
    } else {
      cmd = "kill -9 " + jobId;
      finalCmd = new String[] {"/bin/sh", "-c", cmd};
    }
    try {
      Runtime runtime = Runtime.getRuntime();
      LOG.info("Stopping optimizer using command:{}", cmd);
      runtime.exec(finalCmd);
    } catch (Exception e) {
      throw new RuntimeException("Failed to release optimizer.", e);
    }
  }
}
