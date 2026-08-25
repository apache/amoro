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

package org.apache.amoro.optimizer.common;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestOptimizerToucher extends OptimizerTestBase {

  @Test
  public void testRegisterOptimizer() throws InterruptedException {
    OptimizerConfig optimizerConfig =
        OptimizerTestHelpers.buildOptimizerConfig(TEST_AMS.getServerUrl());
    OptimizerToucher optimizerToucher = new OptimizerToucher(optimizerConfig);

    TestTokenChangeListener tokenChangeListener = new TestTokenChangeListener();
    optimizerToucher.withTokenChangeListener(tokenChangeListener);
    optimizerToucher.withRegisterProperty("test_k", "test_v");
    new Thread(optimizerToucher::start).start();
    tokenChangeListener.waitForTokenChange();
    Assertions.assertEquals(1, tokenChangeListener.tokenList().size());
    Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().size());
    Map<String, String> optimizerProperties = Maps.newHashMap();
    optimizerProperties.put("test_k", "test_v");
    optimizerProperties.put(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL, "1000");
    validateRegisteredOptimizer(
        tokenChangeListener.tokenList().get(0), optimizerConfig, optimizerProperties);

    // clear all optimizer, toucher will register again
    TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().clear();
    tokenChangeListener.waitForTokenChange();
    Assertions.assertEquals(2, tokenChangeListener.tokenList().size());
    Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().size());
    validateRegisteredOptimizer(
        tokenChangeListener.tokenList().get(1), optimizerConfig, optimizerProperties);

    optimizerToucher.stop();
  }

  @Test
  public void testNoReRegistrationInDrainMode() throws InterruptedException {
    OptimizerConfig optimizerConfig =
        OptimizerTestHelpers.buildOptimizerConfig(TEST_AMS.getServerUrl());
    OptimizerToucher optimizerToucher = new OptimizerToucher(optimizerConfig);
    TestTokenChangeListener tokenChangeListener = new TestTokenChangeListener();
    optimizerToucher.withTokenChangeListener(tokenChangeListener);
    new Thread(optimizerToucher::start).start();
    try {
      tokenChangeListener.waitForTokenChange();
      Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().size());

      // Drain begins (graceful shutdown keeps the toucher alive for heartbeats), then AMS
      // unregisters this optimizer the way a scale-down does before deleting the pod.
      optimizerToucher.enterDrainMode();
      TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().clear();

      // Give the heartbeat loop several cycles to hit the auth error.
      TimeUnit.MILLISECONDS.sleep(3_000);

      Assertions.assertTrue(
          TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().isEmpty(),
          "Toucher must not re-register a ghost optimizer while draining");
      Assertions.assertEquals(
          1,
          tokenChangeListener.tokenList().size(),
          "Executor tokens must not be rotated during drain");
    } finally {
      optimizerToucher.stop();
    }
  }

  private void validateRegisteredOptimizer(
      String token, OptimizerConfig registerConfig, Map<String, String> optimizerProperties) {
    Map<String, OptimizerRegisterInfo> registeredOptimizerMap =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers();
    Assertions.assertTrue(registeredOptimizerMap.containsKey(token));

    OptimizerRegisterInfo registerInfo = registeredOptimizerMap.get(token);
    Assertions.assertEquals(registerConfig.getResourceId(), registerInfo.getResourceId());
    Assertions.assertEquals(registerConfig.getGroupName(), registerInfo.getGroupName());
    Assertions.assertEquals(registerConfig.getMemorySize(), registerInfo.getMemoryMb());
    Assertions.assertEquals(registerConfig.getExecutionParallel(), registerInfo.getThreadCount());
    Assertions.assertEquals(optimizerProperties, registerInfo.getProperties());
  }

  static class TestTokenChangeListener implements OptimizerToucher.TokenChangeListener {
    private final List<String> tokenList = Lists.newArrayList();
    private transient CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void tokenChange(String newToken) {
      tokenList.add(newToken);
      latch.countDown();
    }

    List<String> tokenList() {
      return tokenList;
    }

    void waitForTokenChange() throws InterruptedException {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Wait for token change timeout");
      }
      latch = new CountDownLatch(1);
    }
  }
}
