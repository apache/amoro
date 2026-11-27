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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class TestSparkOptimizerContainerApplicationName {
  private static final String APP_NAME = "spark-conf.spark.app.name";
  private static final String KUBERNETES_MASTER = "k8s://https://127.0.0.1:6443";

  @Test
  void testGroupApplicationNameOverridesContainerAndIsOneArgument() throws Exception {
    String name = "optimizer's group $42";
    SparkOptimizerContainer container = container("yarn", Map.of(APP_NAME, "container-name"));
    List<String> args =
        shellArguments(container.buildOptimizerStartupArgsString(resource(Map.of(APP_NAME, name))));

    Assertions.assertEquals(
        List.of("spark.app.name=" + name),
        args.stream()
            .filter(arg -> arg.startsWith("spark.app.name="))
            .collect(Collectors.toList()));
    Assertions.assertFalse(args.contains("--name"));
  }

  @Test
  void testContainerApplicationNameWithoutGroupOverride() throws Exception {
    SparkOptimizerContainer container = container("yarn", Map.of(APP_NAME, "container-name"));

    Assertions.assertTrue(
        shellArguments(container.buildOptimizerStartupArgsString(resource(Map.of())))
            .contains("spark.app.name=container-name"));
  }

  @Test
  void testDefaultApplicationNameOnYarnAndKubernetes() throws Exception {
    for (String master : List.of("yarn", KUBERNETES_MASTER)) {
      Resource resource = resource(Map.of(APP_NAME, " "));
      List<String> args =
          shellArguments(container(master, Map.of()).buildOptimizerStartupArgsString(resource));

      Assertions.assertTrue(
          args.contains("spark.app.name=amoro-spark-optimizer-" + resource.getResourceId()));
      if (master.equals(KUBERNETES_MASTER)) {
        Assertions.assertTrue(
            args.contains(
                "spark.kubernetes.driver.pod.name=amoro-optimizer-" + resource.getResourceId()));
      }
    }
  }

  @Test
  void testMultilineApplicationNameIsRejected() {
    SparkOptimizerContainer container = container("yarn", Map.of());

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            container.buildOptimizerStartupArgsString(resource(Map.of(APP_NAME, "first\nsecond"))));
  }

  private SparkOptimizerContainer container(String master, Map<String, String> options) {
    Map<String, String> properties = new HashMap<>(options);
    properties.put(OptimizerProperties.AMS_HOME, "/home/ams");
    properties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");
    properties.put(SparkOptimizerContainer.SPARK_HOME_PROPERTY, "/home/ams");
    properties.put(SparkOptimizerContainer.SPARK_MASTER, master);
    if (master.equals(KUBERNETES_MASTER)) {
      properties.put(
          "spark-conf.spark.kubernetes.container.image", "apache/amoro-spark-optimizer:test");
    }
    SparkOptimizerContainer container = new SparkOptimizerContainer();
    container.init("spark", properties);
    return container;
  }

  private Resource resource(Map<String, String> properties) {
    return new Resource.Builder("spark", "test-group", ResourceType.OPTIMIZER)
        .setThreadCount(1)
        .setProperties(properties)
        .build();
  }

  private List<String> shellArguments(String command) throws IOException, InterruptedException {
    Process process =
        new ProcessBuilder("/bin/sh", "-c", "set -- " + command + "\nprintf '%s\\n' \"$@\"")
            .redirectErrorStream(true)
            .start();
    try {
      Assertions.assertTrue(process.waitFor(5, TimeUnit.SECONDS));
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      Assertions.assertEquals(0, process.exitValue(), output);
      return output.lines().collect(Collectors.toList());
    } finally {
      process.destroyForcibly();
    }
  }
}
