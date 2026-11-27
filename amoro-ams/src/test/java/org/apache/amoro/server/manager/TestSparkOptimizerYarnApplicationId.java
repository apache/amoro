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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class TestSparkOptimizerYarnApplicationId {
  private static final String APPLICATION_ID = "application_1234567890123_42";
  private static final String OTHER_APPLICATION_ID = "application_1234567890123_41";

  @Test
  void readsHadoopSubmissionAndSparkReportMessages() {
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "26/11/25 18:49:45 INFO [main] YarnClientImpl: Submitted application " + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "INFO org.apache.hadoop.yarn.client.api.impl.YarnClientImpl - Submitted application "
                + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "INFO org.apache.hadoop.yarn.client.api.impl.YarnClientImpl"
                + "                                      [] - Submitted application "
                + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "26/11/25 18:49:46 INFO Client: Application report for "
                + APPLICATION_ID
                + " (state: RUNNING)",
            YarnApplicationIdReader.Engine.SPARK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "Application report for " + APPLICATION_ID + " (state: ACCEPTED)",
            YarnApplicationIdReader.Engine.SPARK));
    assertEquals(
        APPLICATION_ID,
        YarnApplicationIdReader.parse(
            "Submitted application " + APPLICATION_ID, YarnApplicationIdReader.Engine.SPARK));
  }

  @Test
  void rejectsDisplayNamesErrorsAndNestedApplicationIds() {
    assertNull(
        YarnApplicationIdReader.parse(
            "26/11/25 18:49:45 INFO SparkContext: Application name: " + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
    assertNull(
        YarnApplicationIdReader.parse(
            "26/11/25 18:49:45 ERROR Client: Failed to submit application " + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
    assertNull(
        YarnApplicationIdReader.parse(
            "OtherLogger: ignored Client: Application report for "
                + APPLICATION_ID
                + " (state: RUNNING)",
            YarnApplicationIdReader.Engine.SPARK));
    assertNull(
        YarnApplicationIdReader.parse(
            "26/11/25 18:49:45 INFO NotYarnClientImpl: Submitted application " + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
    assertNull(
        YarnApplicationIdReader.parse(
            "Submitted application " + APPLICATION_ID + " with extra text",
            YarnApplicationIdReader.Engine.SPARK));
    assertNull(
        YarnApplicationIdReader.parse(
            "Submitting application master " + APPLICATION_ID,
            YarnApplicationIdReader.Engine.SPARK));
  }

  @Test
  void readsFirstConfirmedIdFromMergedLauncherOutputAndKeepsDraining() throws Exception {
    Process process =
        startProcess(
            "printf '%s\\n' \"Application name: "
                + OTHER_APPLICATION_ID
                + "\"; "
                + "printf '%s\\n' \"launcher warning "
                + OTHER_APPLICATION_ID
                + "\" >&2; "
                + "printf '%s\\n' \"INFO YarnClientImpl: Submitted application "
                + APPLICATION_ID
                + "\"; "
                + "yes x | head -c 100000; printf '\\n'; sleep 0.2");
    List<String> output = new ArrayList<>();
    try {
      assertEquals(
          APPLICATION_ID,
          YarnApplicationIdReader.read(
              process, YarnApplicationIdReader.Engine.SPARK, 1000, output::add));
      assertTrue(process.waitFor(2, TimeUnit.SECONDS));
      assertTrue(output.stream().anyMatch(line -> line.contains("launcher warning")));
    } finally {
      process.destroyForcibly();
      process.waitFor(2, TimeUnit.SECONDS);
    }
  }

  @Test
  void readsPartialLineAtProcessEnd() throws Exception {
    Process process = startProcess("printf '%s' \"Submitted application " + APPLICATION_ID + "\"");
    try {
      assertEquals(
          APPLICATION_ID,
          YarnApplicationIdReader.read(
              process, YarnApplicationIdReader.Engine.SPARK, 1000, ignored -> {}));
    } finally {
      process.destroyForcibly();
      process.waitFor(2, TimeUnit.SECONDS);
    }
  }

  @Test
  void returnsUnknownAtSilentDeadlineWithoutKillingTheProcess() throws Exception {
    Process process = startProcess("sleep 2");
    long started = System.nanoTime();
    try {
      assertNull(
          YarnApplicationIdReader.read(
              process, YarnApplicationIdReader.Engine.SPARK, 100, ignored -> {}));
      assertFalse(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) >= 1500);
      assertTrue(process.isAlive());
    } finally {
      process.destroyForcibly();
      process.waitFor(2, TimeUnit.SECONDS);
    }
  }

  private static Process startProcess(String script) throws Exception {
    return new ProcessBuilder("/bin/sh", "-c", script).redirectErrorStream(true).start();
  }
}
