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

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class TestSparkOptimizerContainerProxyUser {
  private static final String HADOOP_USER_NAME = "export.HADOOP_USER_NAME";

  @Test
  void testEmptyProxyUserOmitsArgument() {
    String command = buildCommand(Map.of(HADOOP_USER_NAME, ""));

    Assertions.assertFalse(command.contains("--proxy-user"));
  }

  @Test
  void testOmittedProxyUserKeepsDefault() {
    Assertions.assertTrue(buildCommand(Map.of()).contains(" --proxy-user hadoop "));
  }

  @Test
  void testConfiguredProxyUserIsPreserved() {
    Assertions.assertTrue(
        buildCommand(Map.of(HADOOP_USER_NAME, "optimizer")).contains(" --proxy-user optimizer "));
  }

  private String buildCommand(Map<String, String> options) {
    Map<String, String> properties = new HashMap<>(options);
    properties.put(OptimizerProperties.AMS_HOME, "/home/ams");
    properties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");
    properties.put(SparkOptimizerContainer.SPARK_HOME_PROPERTY, "/home/ams");
    SparkOptimizerContainer container = new SparkOptimizerContainer();
    container.init("spark", properties);
    Resource resource =
        new Resource.Builder("spark", "test-group", ResourceType.OPTIMIZER)
            .setThreadCount(1)
            .build();
    return container.buildOptimizerStartupArgsString(resource);
  }
}
