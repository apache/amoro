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

package org.apache.amoro.listener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AmoroRunListener implements TestExecutionListener {
  private static final Logger LOG = LoggerFactory.getLogger(AmoroRunListener.class);
  private long startTime;

  private final Map<String, Long> singleTestStartTimes = new ConcurrentHashMap<>();
  private final PriorityQueue<TestCase> testCaseQueue = new PriorityQueue<>();

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    startTime = System.currentTimeMillis();
    LOG.info(
        "Tests started! Number of containers/tests in plan: {}",
        testPlan.countTestIdentifiers(id -> true));
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    long endTime = System.currentTimeMillis();
    long elapsedSeconds = (endTime - startTime) / 1000;
    LOG.info("Tests finished! Number of test cases recorded: {}", testCaseQueue.size());
    LOG.info("Elapsed time of tests execution: {} seconds", elapsedSeconds);
    int printNum = Math.min(testCaseQueue.size(), 50);
    LOG.info("Print the top cost test case method name:");
    for (int i = 0; i < printNum; i++) {
      TestCase testCase = testCaseQueue.poll();
      Objects.requireNonNull(testCase);
      LOG.info("NO-{}, cost: {}ms, methodName:{}", i + 1, testCase.cost, testCase.methodName);
    }
  }

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    if (testIdentifier.isTest()) {
      singleTestStartTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
      LOG.info("{} test is starting...", testIdentifier.getDisplayName());
    }
  }

  @Override
  public void executionFinished(
      TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    if (testIdentifier.isTest()) {
      Long start = singleTestStartTimes.remove(testIdentifier.getUniqueId());
      long cost = start == null ? 0 : System.currentTimeMillis() - start;
      String methodName = testIdentifier.getDisplayName();
      testCaseQueue.add(TestCase.of(cost, methodName));
      if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
        LOG.info("{} test FAILED!!!", methodName);
      }
      LOG.info("{} test is finished, cost {}ms...\n", methodName, cost);
    }
  }

  @Override
  public void executionSkipped(TestIdentifier testIdentifier, String reason) {
    if (testIdentifier.isTest()) {
      LOG.info(
          "@Disabled test method '{}', ignored reason '{}'.",
          testIdentifier.getDisplayName(),
          reason);
    }
  }

  private static class TestCase implements Comparable<TestCase> {
    private final Long cost;
    private final String methodName;

    private TestCase(long cost, String methodName) {
      this.cost = cost;
      this.methodName = methodName;
    }

    public static TestCase of(long cost, String methodName) {
      return new TestCase(cost, methodName);
    }

    @Override
    public int compareTo(AmoroRunListener.TestCase that) {
      Objects.requireNonNull(that);
      return that.cost.compareTo(cost);
    }
  }
}
