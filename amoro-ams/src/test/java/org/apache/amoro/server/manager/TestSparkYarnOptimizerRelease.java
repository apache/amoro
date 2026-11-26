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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class TestSparkYarnOptimizerRelease {

  private static final String APPLICATION_ID = "application_1700000000000_0001";
  private static final String OTHER_APPLICATION_ID = "application_1700000000000_0002";

  @TempDir Path root;

  @Test
  void waitsForYarnKillAndUsesTheRegisteredApplicationId() throws Exception {
    writeExecutable("yarn", "printf '%s\n' \"$*\" > \"$TEST_ROOT/yarn-args\"");
    Resource resource =
        resource(APPLICATION_ID)
            .addProperties(
                SparkOptimizerContainer.YARN_APPLICATION_ID_PROPERTY, OTHER_APPLICATION_ID)
            .build();

    container("cluster").releaseResource(resource);

    assertEquals(
        "application -kill " + APPLICATION_ID, Files.readString(root.resolve("yarn-args")).trim());
  }

  @Test
  void doesNotReportSuccessWhenTheKillCommandFails() throws Exception {
    writeExecutable("spark-submit", "exit 0");
    writeExecutable("yarn", "exit 37");

    IllegalStateException failure =
        assertThrows(
            IllegalStateException.class,
            () -> container("cluster").releaseResource(resource(APPLICATION_ID).build()));

    assertTrue(failure.getMessage().contains(APPLICATION_ID));
    assertTrue(failure.getMessage().contains("37"));
  }

  @Test
  void rejectsAnInvalidApplicationIdBeforeStartingYarn() throws Exception {
    writeExecutable("yarn", "touch \"$TEST_ROOT/yarn-called\"");

    assertThrows(
        IllegalArgumentException.class,
        () -> container("cluster").releaseResource(resource("not-an-application-id").build()));

    assertFalse(Files.exists(root.resolve("yarn-called")));
  }

  @Test
  void refusesToKillAnIdKnownOnlyFromTheLegacyLogReader() throws Exception {
    writeExecutable("yarn", "touch \"$TEST_ROOT/yarn-called\"");
    Resource resource =
        new Resource.Builder("spark", "test-group", ResourceType.OPTIMIZER)
            .addProperties(SparkOptimizerContainer.YARN_APPLICATION_ID_PROPERTY, APPLICATION_ID)
            .build();

    assertThrows(
        IllegalArgumentException.class, () -> container("cluster").releaseResource(resource));

    assertFalse(Files.exists(root.resolve("yarn-called")));
  }

  @Test
  void doesNotReportSuccessForAnUnverifiedLegacyClientDriver() throws Exception {
    writeExecutable("yarn", "touch \"$TEST_ROOT/yarn-killed\"");

    IllegalStateException failure =
        assertThrows(
            IllegalStateException.class,
            () -> container("client").releaseResource(resource(APPLICATION_ID).build()));

    assertTrue(Files.exists(root.resolve("yarn-killed")));
    assertTrue(failure.getMessage().contains("client driver"));
  }

  @Test
  void stopsTheClientDriverBeforeReleasingYarn() throws Exception {
    writeExecutable(
        "spark-submit",
        "trap 'touch \"$TEST_ROOT/driver-stopped\"; exit 0' TERM\n"
            + "echo $$ > \"$TEST_ROOT/driver-pid\"\n"
            + "echo 'Submitted application "
            + APPLICATION_ID
            + "'\n"
            + "while :; do sleep 1; done");
    writeExecutable(
        "yarn",
        "if [ \"$3\" = \""
            + APPLICATION_ID
            + "\" ]\n"
            + "then\n"
            + "  test -f \"$TEST_ROOT/driver-stopped\" && touch \"$TEST_ROOT/yarn-killed\"\n"
            + "elif [ \"$3\" = \""
            + OTHER_APPLICATION_ID
            + "\" ]\n"
            + "then\n"
            + "  touch \"$TEST_ROOT/other-yarn-killed\"\n"
            + "else\n"
            + "  exit 2\n"
            + "fi");
    Resource resource =
        resource(APPLICATION_ID)
            .addProperties(OptimizerProperties.OPTIMIZER_SHUTDOWN_TIMEOUT_MS, "1000")
            .addProperties("spark-conf.spark.optimizer.test.argument", "x".repeat(6000))
            .build();
    SparkOptimizerContainer submitter = container("client");
    submitter.requestResource(resource);
    ProcessHandle driver =
        ProcessHandle.of(Long.parseLong(Files.readString(root.resolve("driver-pid")).trim()))
            .orElseThrow();
    try {
      // A new container has no in-memory launch state.
      SparkOptimizerContainer releaser = container("client");
      Resource otherResource = resource(OTHER_APPLICATION_ID).build();
      otherResource.getProperties().putAll(resource.getProperties());
      otherResource.getProperties().put(Resource.PROPERTY_JOB_ID, OTHER_APPLICATION_ID);
      assertThrows(IllegalStateException.class, () -> releaser.releaseResource(otherResource));
      assertTrue(driver.isAlive());
      assertTrue(Files.exists(root.resolve("other-yarn-killed")));
      assertFalse(Files.exists(root.resolve("yarn-killed")));

      releaser.releaseResource(resource);
      assertFalse(driver.isAlive());
      assertTrue(Files.exists(root.resolve("yarn-killed")));
    } finally {
      if (driver.isAlive()) {
        driver.destroyForcibly();
        driver.onExit().get(5, TimeUnit.SECONDS);
      }
    }
  }

  private Resource.Builder resource(String applicationId) {
    return new Resource.Builder("spark", "test-group", ResourceType.OPTIMIZER)
        .setThreadCount(1)
        .addProperties(Resource.PROPERTY_JOB_ID, applicationId);
  }

  private SparkOptimizerContainer container(String deployMode) {
    Map<String, String> properties = new HashMap<>();
    properties.put(OptimizerProperties.AMS_HOME, root.toString());
    properties.put(OptimizerProperties.AMS_OPTIMIZER_URI, "thrift://127.0.0.1:1261");
    properties.put(SparkOptimizerContainer.SPARK_HOME_PROPERTY, root.toString());
    properties.put(SparkOptimizerContainer.SPARK_DEPLOY_MODE, deployMode);
    properties.put("export.HADOOP_HOME", root.toString());
    properties.put("export.HADOOP_YARN_HOME", root.toString());
    properties.put("export.TEST_ROOT", root.toString());
    SparkOptimizerContainer container = new SparkOptimizerContainer();
    container.init("spark", properties);
    return container;
  }

  private void writeExecutable(String name, String script) throws IOException {
    Path bin = Files.createDirectories(root.resolve("bin"));
    Path executable = Files.writeString(bin.resolve(name), "#!/bin/sh\n" + script + "\n");
    assertTrue(executable.toFile().setExecutable(true));
  }
}
