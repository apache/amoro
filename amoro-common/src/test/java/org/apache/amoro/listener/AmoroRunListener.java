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

import java.util.Optional;
import java.util.PriorityQueue;

import org.junit.jupiter.api.Assertions;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmoroRunListener implements TestExecutionListener {
  private static final Logger LOG = LoggerFactory.getLogger(AmoroRunListener.class);
  private long startTime;
  private long singleTestStartTime;

  private final PriorityQueue<TestCase> testCaseQueue = new PriorityQueue<>();

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    startTime = System.currentTimeMillis();
    int testCount = (int) testPlan.countTestIdentifiers(TestIdentifier::isTest);
    LOG.info("Tests started! Number of Test case: {}", testCount);
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    long endTime = System.currentTimeMillis();
    int testCount = (int) testPlan.countTestIdentifiers(TestIdentifier::isTest);
    LOG.info("Tests finished! Number of test case: {}", testCount);
    long elapsedSeconds = (endTime - startTime) / 1000;
    LOG.info("Elapsed time of tests execution: {} seconds", elapsedSeconds);
    int printNum = Math.min(testCaseQueue.size(), 50);
    LOG.info("Print the top cost test case method name:");
    for (int i = 0; i < printNum; i++) {
      TestCase testCase = testCaseQueue.poll();
      Assertions.assertNotNull(testCase);
      LOG.info("NO-{}, cost: {}ms, methodName:{}", i + 1, testCase.cost, testCase.methodName);
    }
  }

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    if (testIdentifier.isTest()) {
      singleTestStartTime = System.currentTimeMillis();
      LOG.info("{} test is starting...", testIdentifier.getDisplayName());
    }
  }

  @Override
  public void executionFinished(
      TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    if (testIdentifier.isTest()) {
      long cost = System.currentTimeMillis() - singleTestStartTime;
      testCaseQueue.add(TestCase.of(cost, testIdentifier.getDisplayName()));
      LOG.info("{} test is finished, cost {}ms...\n", testIdentifier.getDisplayName(), cost);

      if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
        LOG.info("{} test FAILED!!!", testIdentifier.getDisplayName());
        Optional<Throwable> throwable = testExecutionResult.getThrowable();
        if (throwable.isPresent()) {
          LOG.info("Failure reason: {}", throwable.get().getMessage());
        }
      }
    }
  }

  @Override
  public void executionSkipped(TestIdentifier testIdentifier, String reason) {
    if (testIdentifier.isTest()) {
      String ignoredReason = reason != null && !reason.isEmpty()
          ? reason
          : "No reason provided";
      LOG.info(
          "@Disabled method '{}', ignored reason '{}'.",
          testIdentifier.getDisplayName(),
          ignoredReason);
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
      Assertions.assertNotNull(that);
      return that.cost.compareTo(cost);
    }
  }
}
