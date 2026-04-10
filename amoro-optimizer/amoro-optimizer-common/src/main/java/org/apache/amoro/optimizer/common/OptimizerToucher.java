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

import org.apache.amoro.ErrorCodes;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.api.AmoroException;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OptimizerToucher extends AbstractOptimizerOperator {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizerToucher.class);

  private transient TokenChangeListener tokenChangeListener;
  private final Map<String, String> registerProperties = Maps.newHashMap();
  private final long startTime;

  public OptimizerToucher(OptimizerConfig config) {
    super(config);
    this.startTime = System.currentTimeMillis();
  }

  public OptimizerToucher withTokenChangeListener(TokenChangeListener tokenChangeListener) {
    this.tokenChangeListener = tokenChangeListener;
    return this;
  }

  public OptimizerToucher withRegisterProperty(String name, String value) {
    registerProperties.put(name, value);
    LOG.info("Adding register property {}:{} into optimizer", name, value);
    return this;
  }

  public void start() {
    LOG.info("Starting optimizer toucher with configuration:{}", getConfig());
    while (isStarted()) {
      try {
        if (checkToken()) {
          touch();
        }
        waitAShortTime(getConfig().getHeartBeat());
      } catch (Throwable t) {
        LOG.error("Optimizer toucher got an unexpected error", t);
      }
    }
    LOG.info("Optimizer toucher stopped");
  }

  private boolean checkToken() {
    if (!tokenIsReady()) {
      LOG.info(
          "Registering optimizer to AMS {} (group: {}, mode: {}, threads: {}, memory: {}MB) ...",
          getConfig().getAmsUrl(),
          getConfig().getGroupName(),
          getConfig().isMasterSlaveMode() ? "master-slave" : "single-node",
          getConfig().getExecutionParallel(),
          getConfig().getMemorySize());
      try {
        String token =
            callAms(
                client -> {
                  withRegisterProperty(
                      OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL,
                      String.valueOf(getConfig().getHeartBeat()));
                  OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
                  registerInfo.setThreadCount(getConfig().getExecutionParallel());
                  registerInfo.setMemoryMb(getConfig().getMemorySize());
                  registerInfo.setGroupName(getConfig().getGroupName());
                  registerInfo.setProperties(registerProperties);
                  registerInfo.setResourceId(getConfig().getResourceId());
                  registerInfo.setStartTime(startTime);
                  return client.authenticate(registerInfo);
                });
        setToken(token);
        if (tokenChangeListener != null) {
          tokenChangeListener.tokenChange(token);
        }
        LOG.info(
            "Successfully registered optimizer to AMS {} (group: {}) with token: {}",
            getConfig().getAmsUrl(),
            getConfig().getGroupName(),
            token);
        return true;
      } catch (TException e) {
        LOG.error(
            "Failed to register optimizer to AMS {} (group: {})",
            getConfig().getAmsUrl(),
            getConfig().getGroupName(),
            e);
        if (e instanceof AmoroException
            && ErrorCodes.FORBIDDEN_ERROR_CODE == ((AmoroException) e).getErrorCode()) {
          System.exit(1); // Don't need to try again
        }
        return false;
      }
    }
    return true;
  }

  private void touch() {
    String currentToken = getToken();
    try {
      callAms(
          client -> {
            client.touch(currentToken);
            return null;
          });
      LOG.debug(
          "Optimizer touch AMS {} succeeded (token: {})", getConfig().getAmsUrl(), currentToken);
    } catch (TException e) {
      if (e instanceof AmoroException
          && ErrorCodes.PLUGIN_RETRY_AUTH_ERROR_CODE == ((AmoroException) e).getErrorCode()) {
        LOG.error(
            "Got authorization error from AMS {} during touch (token: {} is now invalid). "
                + "Will re-register on the next heartbeat cycle.",
            getConfig().getAmsUrl(),
            currentToken,
            e);
        setToken(null);
      } else {
        LOG.error("Touch AMS {} failed (token: {})", getConfig().getAmsUrl(), currentToken, e);
      }
    }
  }

  public interface TokenChangeListener {
    void tokenChange(String newToken);
  }
}
