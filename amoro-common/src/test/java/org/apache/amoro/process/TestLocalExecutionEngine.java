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

package org.apache.amoro.process;

import static org.mockito.Mockito.mock;

import org.apache.amoro.Action;
import org.apache.amoro.TableRuntime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestLocalExecutionEngine {

  private LocalExecutionEngine engine;

  @After
  public void tearDown() {
    if (engine != null) {
      engine.close();
    }
  }

  @Test
  public void testSubmitUsesCustomPoolByTag() throws Exception {
    engine = createEngineWithTtl("1h");

    CountDownLatch started = new CountDownLatch(1);
    AtomicReference<String> threadName = new AtomicReference<>();

    LocalProcessTableProcess process =
        new LocalProcessTableProcess(
            mock(TableRuntime.class),
            engine,
            "snapshots-expiring",
            () -> {
              threadName.set(Thread.currentThread().getName());
              started.countDown();
            });

    String identifier = engine.submitTableProcess(process);

    Assert.assertTrue("process should start", started.await(5, TimeUnit.SECONDS));
    Assert.assertTrue(
        "should run in custom pool",
        threadName.get() != null && threadName.get().startsWith("local-snapshots-expiring-"));

    waitForStatus(identifier, ProcessStatus.SUCCESS, 5000);
  }

  @Test
  public void testCancelRunningProcess() throws Exception {
    engine = createEngineWithTtl("1h");

    CountDownLatch blockLatch = new CountDownLatch(1);
    LocalProcessTableProcess process =
        new LocalProcessTableProcess(
            mock(TableRuntime.class),
            engine,
            "default",
            () -> {
              try {
                blockLatch.await();
              } catch (InterruptedException ignored) {
                // ignore
              }
            });

    String identifier = engine.submitTableProcess(process);

    // Process may not be scheduled yet, but holder is already created.
    Assert.assertEquals(ProcessStatus.RUNNING, engine.getStatus(identifier));

    engine.tryCancelTableProcess(process, identifier);

    // Eventually the process should be marked as canceled.
    waitForStatus(identifier, ProcessStatus.CANCELED, 5000);
  }

  @Test
  public void testFinishedStatusExpired() throws Exception {
    engine = createEngineWithTtl("100ms");

    LocalProcessTableProcess process =
        new LocalProcessTableProcess(mock(TableRuntime.class), engine, "default", () -> {});

    String identifier = engine.submitTableProcess(process);

    waitForStatus(identifier, ProcessStatus.SUCCESS, 5000);

    // Wait until process info should be expired.
    Thread.sleep(200);

    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.getStatus(identifier));
  }

  @Test
  public void testFailedProcessStatus() throws Exception {
    engine = createEngineWithTtl("1h");

    LocalProcessTableProcess process =
        new LocalProcessTableProcess(
            mock(TableRuntime.class),
            engine,
            "default",
            () -> {
              throw new RuntimeException("boom");
            });

    String identifier = engine.submitTableProcess(process);

    waitForStatus(identifier, ProcessStatus.FAILED, 5000);
  }

  @Test
  public void testGetStatusForInvalidIdentifier() {
    engine = createEngineWithTtl("1h");

    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.getStatus(null));
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.getStatus(""));
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.getStatus("not-exist"));
  }

  private LocalExecutionEngine createEngineWithTtl(String ttl) {
    LocalExecutionEngine localEngine = new LocalExecutionEngine();
    Map<String, String> properties = new HashMap<>();
    properties.put("pool.default.thread-count", "1");
    properties.put("pool.snapshots-expiring.thread-count", "1");
    properties.put("process.status.ttl", ttl);
    localEngine.open(properties);
    return localEngine;
  }

  private void waitForStatus(String identifier, ProcessStatus expected, long timeoutMillis)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeoutMillis;
    while (System.currentTimeMillis() < deadline) {
      ProcessStatus status = engine.getStatus(identifier);
      if (status == expected) {
        return;
      }
      Thread.sleep(10);
    }
    Assert.fail(
        "Timeout waiting for status " + expected + ", current=" + engine.getStatus(identifier));
  }

  private static class LocalProcessTableProcess extends TableProcess implements LocalProcess {

    private final String tag;
    private final Runnable runnable;

    LocalProcessTableProcess(
        TableRuntime tableRuntime, ExecuteEngine engine, String tag, Runnable runnable) {
      super(tableRuntime, engine);
      this.tag = tag;
      this.runnable = runnable;
    }

    @Override
    public void run() {
      runnable.run();
    }

    @Override
    public String tag() {
      return tag;
    }

    @Override
    public Action getAction() {
      return Action.register("TEST");
    }

    @Override
    public Map<String, String> getProcessParameters() {
      return new HashMap<>();
    }

    @Override
    public Map<String, String> getSummary() {
      return new HashMap<>();
    }
  }
}
