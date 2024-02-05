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

package com.netease.arctic.optimizer.common;

import com.netease.arctic.ams.api.OptimizerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Optimizer {
  private static final Logger LOG = LoggerFactory.getLogger(Optimizer.class);

  private final OptimizerConfig config;
  private final OptimizerToucher toucher;
  private final OptimizerExecutor[] executors;

  public Optimizer(OptimizerConfig config) {
    this.config = config;
    this.toucher = new OptimizerToucher(config);
    this.executors = new OptimizerExecutor[config.getExecutionParallel()];
    IntStream.range(0, config.getExecutionParallel())
        .forEach(i -> executors[i] = new OptimizerExecutor(config, i));
    if (config.getResourceId() != null) {
      toucher.withRegisterProperty(OptimizerProperties.RESOURCE_ID, config.getResourceId());
    }
  }

  public void startOptimizing() {
    LOG.info("Starting optimizer with configuration:{}", config);
    Arrays.stream(executors)
        .forEach(
            optimizerExecutor -> {
              new Thread(
                      optimizerExecutor::start,
                      String.format("Optimizer-executor-%d", optimizerExecutor.getThreadId()))
                  .start();
            });
    toucher.withTokenChangeListener(new SetTokenToExecutors()).start();
  }

  public void stopOptimizing() {
    toucher.stop();
    Arrays.stream(executors).forEach(OptimizerExecutor::stop);
  }

  public OptimizerToucher getToucher() {
    return toucher;
  }

  public OptimizerExecutor[] getExecutors() {
    return executors;
  }

  class SetTokenToExecutors implements OptimizerToucher.TokenChangeListener {

    @Override
    public void tokenChange(String newToken) {
      Arrays.stream(executors).forEach(optimizerExecutor -> optimizerExecutor.setToken(newToken));
    }
  }
}
