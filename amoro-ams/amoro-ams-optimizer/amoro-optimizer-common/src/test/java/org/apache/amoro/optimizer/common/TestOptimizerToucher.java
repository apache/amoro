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

import org.apache.amoro.api.OptimizerProperties;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

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
    Assert.assertEquals(1, tokenChangeListener.tokenList().size());
    Assert.assertEquals(1, TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().size());
    Map<String, String> optimizerProperties = Maps.newHashMap();
    optimizerProperties.put("test_k", "test_v");
    optimizerProperties.put(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL, "1000");
    validateRegisteredOptimizer(
        tokenChangeListener.tokenList().get(0), optimizerConfig, optimizerProperties);

    // clear all optimizer, toucher will register again
    TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().clear();
    tokenChangeListener.waitForTokenChange();
    Assert.assertEquals(2, tokenChangeListener.tokenList().size());
    Assert.assertEquals(1, TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().size());
    validateRegisteredOptimizer(
        tokenChangeListener.tokenList().get(1), optimizerConfig, optimizerProperties);

    optimizerToucher.stop();
  }

  private void validateRegisteredOptimizer(
      String token, OptimizerConfig registerConfig, Map<String, String> optimizerProperties) {
    Map<String, OptimizerRegisterInfo> registeredOptimizerMap =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers();
    Assert.assertTrue(registeredOptimizerMap.containsKey(token));

    OptimizerRegisterInfo registerInfo = registeredOptimizerMap.get(token);
    Assert.assertEquals(registerConfig.getResourceId(), registerInfo.getResourceId());
    Assert.assertEquals(registerConfig.getGroupName(), registerInfo.getGroupName());
    Assert.assertEquals(registerConfig.getMemorySize(), registerInfo.getMemoryMb());
    Assert.assertEquals(registerConfig.getExecutionParallel(), registerInfo.getThreadCount());
    Assert.assertEquals(optimizerProperties, registerInfo.getProperties());
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
