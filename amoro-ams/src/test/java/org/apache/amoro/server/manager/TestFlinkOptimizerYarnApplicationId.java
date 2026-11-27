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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class TestFlinkOptimizerYarnApplicationId {
  private static final String APPLICATION_ID = "application_1234567890123_42";
  private static final String PRE_SUBMISSION_APPLICATION_ID = "application_1234567890123_41";

  @Test
  void readsCommittedAndPostDeploymentMessages() {
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "INFO org.apache.hadoop.yarn.client.api.impl.YarnClientImpl - Submitted application "
                + APPLICATION_ID,
            YarnApplicationIdReader.Engine.FLINK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "2026-11-26 10:00:00,000 org.apache.flink.yarn.YarnClusterDescriptor"
                + "                  [] - Found Web Interface http://localhost:8081 of application '"
                + APPLICATION_ID
                + "'.",
            YarnApplicationIdReader.Engine.FLINK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "Found Web Interface localhost:8081 of application '" + APPLICATION_ID + "'.",
            YarnApplicationIdReader.Engine.FLINK));
  }

  @Test
  void rejectsSubmittingFailureAndOtherEngineMessages() {
    assertNull(
        YarnApplicationIdReader.parse(
            "INFO org.apache.flink.yarn.YarnClusterDescriptor: Submitting application master "
                + PRE_SUBMISSION_APPLICATION_ID,
            YarnApplicationIdReader.Engine.FLINK));
    assertNull(
        YarnApplicationIdReader.parse(
            "ERROR org.apache.flink.yarn.YarnClusterDescriptor: Failed to submit application "
                + APPLICATION_ID,
            YarnApplicationIdReader.Engine.FLINK));
    assertNull(
        YarnApplicationIdReader.parse(
            "OtherLogger: Found Web Interface localhost:8081 of application '"
                + APPLICATION_ID
                + "'.",
            YarnApplicationIdReader.Engine.FLINK));
    assertNull(
        YarnApplicationIdReader.parse(
            "INFO org.apache.spark.deploy.yarn.Client: Application report for "
                + APPLICATION_ID
                + " (state: RUNNING)",
            YarnApplicationIdReader.Engine.FLINK));
    assertNull(
        YarnApplicationIdReader.parse(
            "Found Web Interface localhost:8081 of application '" + APPLICATION_ID + "' extra",
            YarnApplicationIdReader.Engine.FLINK));
  }

  @Test
  void readsFlinkConfirmationAfterPreSubmissionMessageFromMergedStreams() throws Exception {
    Process process =
        startProcess(
            "printf '%s\\n' \"INFO YarnClusterDescriptor: Submitting application master "
                + PRE_SUBMISSION_APPLICATION_ID
                + "\" >&2; "
                + "printf '%s\\n' \"2026-11-26 10:00:00,000 org.apache.flink.yarn.YarnClusterDescriptor"
                + "                  [] - Found Web Interface http://localhost:8081 of application '"
                + APPLICATION_ID
                + "'.\"; "
                + "sleep 0.2");
    List<String> output = new ArrayList<>();
    try {
      assertEquals(
          APPLICATION_ID,
          YarnApplicationIdReader.read(
              process, YarnApplicationIdReader.Engine.FLINK, 1000, output::add));
      assertTrue(process.waitFor(2, TimeUnit.SECONDS));
      assertTrue(output.stream().anyMatch(line -> line.contains("Submitting application master")));
    } finally {
      process.destroyForcibly();
      process.waitFor(2, TimeUnit.SECONDS);
    }
  }

  @Test
  void doesNotTreatFailedSubmissionPreludeAsApplicationConfirmation() throws Exception {
    Process process =
        startProcess(
            "printf '%s\\n' \"Submitting application master "
                + PRE_SUBMISSION_APPLICATION_ID
                + "\"; exit 1");
    try {
      assertNull(
          YarnApplicationIdReader.read(
              process, YarnApplicationIdReader.Engine.FLINK, 1000, ignored -> {}));
      assertEquals(1, process.waitFor());
    } finally {
      process.destroyForcibly();
      process.waitFor(2, TimeUnit.SECONDS);
    }
  }

  private static Process startProcess(String script) throws Exception {
    return new ProcessBuilder("/bin/sh", "-c", script).redirectErrorStream(true).start();
  }
}
