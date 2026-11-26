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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.utils.FlinkClientUtil;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.messages.Acknowledge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Timeout(15)
class TestFlinkYarnOptimizerRelease {
  private static final String APPLICATION_ID = "application_123456789_0001";
  private static final String JOB_ID = "0123456789abcdef0123456789abcdef";
  private static final String YARN_PER_JOB = "yarn-per-job";
  private static final String STATUS_CALL = "yarn application -status " + APPLICATION_ID;
  private static final String CANCEL_OPTIONS =
      " -Dyarn.application.id=" + APPLICATION_ID + " " + JOB_ID;

  @TempDir Path temp;

  @ParameterizedTest
  @ValueSource(strings = {YARN_PER_JOB, "yarn-application"})
  void waitsForDedicatedApplicationAfterCancel(String target) throws IOException {
    Resource resource = resource();
    String status =
        "if [ -f \"$TRACE.cancelled\" ]; then\n"
            + "  if [ -f \"$TRACE.seen-running\" ]; then\n"
            + report(resource, "FINISHED")
            + "    exit 0\n"
            + "  fi\n"
            + "  : > \"$TRACE.seen-running\"\n"
            + "fi\n"
            + report(resource, "RUNNING");
    FlinkOptimizerContainer container = container(target, "6", status, ": > \"$TRACE.cancelled\"");
    container.releaseResource(resource);
    assertEquals(List.of(STATUS_CALL, cancelCall(target), STATUS_CALL, STATUS_CALL), trace());
  }

  @ParameterizedTest
  @ValueSource(strings = {"FINISHED", "FAILED", "KILLED"})
  void alreadyTerminalDoesNotRequireAJobIdOrRepeatCancel(String state) throws IOException {
    Resource resource = resource();
    resource.getProperties().remove(Resource.PROPERTY_JOB_ID);
    FlinkOptimizerContainer container =
        container(YARN_PER_JOB, "5", report(resource, state), "exit 99");
    container.releaseResource(resource);
    assertEquals(List.of(STATUS_CALL), trace());
  }

  @Test
  void acceptsFailedCancelOnlyWhenTheOwnedApplicationTerminates() throws IOException {
    Resource resource = resource();
    String status =
        "if [ -f \"$TRACE.cancelled\" ]; then\n"
            + report(resource, "KILLED")
            + "  exit 0\n"
            + "fi\n"
            + report(resource, "RUNNING");
    FlinkOptimizerContainer container =
        container(
            YARN_PER_JOB,
            "5",
            status,
            ": > \"$TRACE.cancelled\"\necho 'Job not found' >&2\nexit 37");
    container.releaseResource(resource);
    assertEquals(List.of(STATUS_CALL, cancelCall(YARN_PER_JOB), STATUS_CALL), trace());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 37})
  void reportsFailureWhenApplicationStaysRunning(int cancelExit) throws IOException {
    Resource resource = resource();
    String status =
        report(resource, "RUNNING")
            + "printf '%s\\n' 'Diagnostics : simulated message' 'State : FINISHED'";
    FlinkOptimizerContainer container = container(YARN_PER_JOB, "2", status, "exit " + cancelExit);
    IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> container.releaseResource(resource));

    List<String> calls = trace();
    assertEquals(List.of(STATUS_CALL, cancelCall(YARN_PER_JOB)), calls.subList(0, 2));
    assertTrue(calls.size() >= 3);
    assertTrue(calls.subList(2, calls.size()).stream().allMatch(STATUS_CALL::equals));
    Throwable timeout = failure.getCause();
    if (cancelExit != 0) {
      assertTrue(timeout.getMessage().contains("exit code " + cancelExit));
      assertEquals(1, timeout.getSuppressed().length);
      timeout = timeout.getSuppressed()[0];
    }
    assertTrue(
        timeout.getMessage().toLowerCase(Locale.ROOT).contains("timed out"), timeout.getMessage());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"ApplicationNotFoundException", "Permission denied", "Connection refused"})
  void doesNotCancelWhenReportIsUnavailable(String error) throws IOException {
    FlinkOptimizerContainer container =
        container(YARN_PER_JOB, "5", "echo '" + error + "' >&2\nexit 1", "exit 99");
    assertThrows(IllegalStateException.class, () -> container.releaseResource(resource()));
    assertEquals(List.of(STATUS_CALL), trace());
  }

  @Test
  void doesNotTrustTerminalStateForAnotherOptimizer() throws IOException {
    Resource resource = resource();
    FlinkOptimizerContainer container =
        container(YARN_PER_JOB, "5", report(resource(), "FINISHED"), "exit 99");
    assertThrows(IllegalStateException.class, () -> container.releaseResource(resource));
    assertEquals(List.of(STATUS_CALL), trace());
  }

  @Test
  void rejectsFlinkJobIdAsYarnApplicationIdBeforeStartingAnyCommand() throws IOException {
    Resource resource = resource();
    resource.getProperties().put(FlinkOptimizerContainer.YARN_APPLICATION_ID_PROPERTY, JOB_ID);
    FlinkOptimizerContainer container = container(YARN_PER_JOB, "5", "exit 99", "exit 99");
    assertThrows(IllegalArgumentException.class, () -> container.releaseResource(resource));
    assertFalse(Files.exists(temp.resolve("trace")));
  }

  @Test
  void rejectsYarnApplicationIdAsFlinkJobIdBeforeCancel() throws IOException {
    Resource resource = resource();
    resource.getProperties().put(Resource.PROPERTY_JOB_ID, APPLICATION_ID);
    FlinkOptimizerContainer container =
        container(YARN_PER_JOB, "5", report(resource, "RUNNING"), "exit 99");
    assertThrows(IllegalArgumentException.class, () -> container.releaseResource(resource));
    assertEquals(List.of(STATUS_CALL), trace());
  }

  @Test
  void preservesInterruptDuringYarnRelease() throws IOException {
    Resource resource = resource();
    FlinkOptimizerContainer container =
        container(YARN_PER_JOB, "5", "while :\ndo\n  :\ndone", "exit 99");
    Thread.currentThread().interrupt();
    try {
      IllegalStateException failure =
          assertThrows(IllegalStateException.class, () -> container.releaseResource(resource));
      assertInstanceOf(InterruptedException.class, failure.getCause());
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void sessionCancelsOnlyTheTargetJobThroughRest() throws Exception {
    JobID jobId = JobID.fromHexString(JOB_ID);
    Resource resource = resource();
    resource.getProperties().put(FlinkOptimizerContainer.SESSION_CLUSTER_JOB_ID, JOB_ID);
    FlinkOptimizerContainer container = container("session", "5", "exit 99", "exit 99");
    RestClusterClient<String> restClient = mock(RestClusterClient.class);
    when(restClient.cancel(jobId)).thenReturn(CompletableFuture.completedFuture(Acknowledge.get()));
    try (MockedStatic<FlinkClientUtil> clientUtil = mockStatic(FlinkClientUtil.class)) {
      clientUtil
          .when(() -> FlinkClientUtil.getRestClusterClient(any(Configuration.class)))
          .thenReturn(restClient);
      container.releaseResource(resource);
      verify(restClient).cancel(jobId);
      verify(restClient).close();
      verifyNoMoreInteractions(restClient);
    }
    assertFalse(Files.exists(temp.resolve("trace")));
  }

  private FlinkOptimizerContainer container(
      String target, String timeout, String status, String cancel) throws IOException {
    Path bin = Files.createDirectories(temp.resolve("bin"));
    Files.writeString(Files.createDirectories(temp.resolve("conf")).resolve("config.yaml"), "{}\n");
    writeExecutable(bin.resolve("yarn"), "yarn", "application -status " + APPLICATION_ID, status);
    writeExecutable(bin.resolve("flink"), "flink", "cancel -t " + target + CANCEL_OPTIONS, cancel);
    FlinkOptimizerContainer container = new FlinkOptimizerContainer();
    container.init(
        "flink",
        Map.of(
            OptimizerProperties.AMS_HOME,
            temp.toString(),
            OptimizerProperties.AMS_OPTIMIZER_URI,
            "thrift://localhost:1261",
            OptimizerProperties.EXPORT_PROPERTY_PREFIX + "HADOOP_YARN_HOME",
            "'" + temp + "'",
            OptimizerProperties.EXPORT_PROPERTY_PREFIX + "TRACE",
            "'" + temp.resolve("trace") + "'",
            FlinkOptimizerContainer.FLINK_HOME_PROPERTY,
            temp.toString(),
            FlinkOptimizerContainer.FLINK_JOB_URI,
            "local:///unused.jar",
            FlinkOptimizerContainer.FLINK_RUN_TARGET,
            target,
            FlinkOptimizerContainer.FLINK_CLIENT_TIMEOUT_SECOND,
            timeout));
    return container;
  }

  private static void writeExecutable(Path path, String command, String expectedArgs, String body)
      throws IOException {
    Files.writeString(
        path,
        "#!/bin/sh\nprintf '%s %s\\n' '"
            + command
            + "' \"$*\" >> \"$TRACE\"\n"
            + "[ \"$*\" = '"
            + expectedArgs
            + "' ] || exit 97\n"
            + body
            + "\n");
    assertTrue(path.toFile().setExecutable(true));
  }

  private static Resource resource() {
    return new Resource.Builder("flink", "group", ResourceType.OPTIMIZER)
        .addProperties(FlinkOptimizerContainer.YARN_APPLICATION_ID_PROPERTY, APPLICATION_ID)
        .addProperties(Resource.PROPERTY_JOB_ID, JOB_ID)
        .build();
  }

  private static String report(Resource resource, String state) {
    return "printf '%s\\n' 'Application-Id : "
        + APPLICATION_ID
        + "' 'Application-Name : "
        + "Amoro-flink-optimizer-"
        + resource.getGroupName()
        + "-"
        + resource.getResourceId()
        + "' 'State : "
        + state
        + "'\n";
  }

  private static String cancelCall(String target) {
    return "flink cancel -t " + target + CANCEL_OPTIONS;
  }

  private List<String> trace() throws IOException {
    return Files.readAllLines(temp.resolve("trace"));
  }
}
