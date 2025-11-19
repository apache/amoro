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
import org.apache.amoro.api.AmoroException;
import org.apache.amoro.api.OptimizingService;
import org.apache.amoro.client.OptimizingClientPools;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.thrift.org.apache.thrift.TApplicationException;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractOptimizerOperator implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOptimizerOperator.class);

  // Call ams every 5 seconds by default
  private static long callAmsInterval = TimeUnit.SECONDS.toMillis(5);

  private final OptimizerConfig config;
  private final AtomicReference<String> token = new AtomicReference<>();
  private volatile boolean stopped = false;
  private transient volatile AmsNodeManager amsNodeManager;

  public AbstractOptimizerOperator(OptimizerConfig config) {
    Preconditions.checkNotNull(config);
    this.config = config;
    // Initialize AmsNodeManager if in master-slave mode
    if (config.isMasterSlaveMode() && config.getAmsUrl().startsWith("zookeeper://")) {
      try {
        this.amsNodeManager = new AmsNodeManager(config.getAmsUrl());
        LOG.info("Initialized AmsNodeManager for master-slave mode");
      } catch (Exception e) {
        LOG.warn("Failed to initialize AmsNodeManager, will use single AMS URL", e);
      }
    }
  }

  /** Get the AmsNodeManager instance if available. */
  protected AmsNodeManager getAmsNodeManager() {
    return amsNodeManager;
  }

  protected <T> T callAms(AmsCallOperation<T> operation) throws TException {
    return callAms(getConfig().getAmsUrl(), operation);
  }

  /**
   * Call AMS with a specific AMS URL.
   *
   * @param amsUrl The AMS node URL to call
   * @param operation The operation to execute
   * @return The result of the operation
   * @throws TException If the operation fails
   */
  protected <T> T callAms(String amsUrl, AmsCallOperation<T> operation) throws TException {
    while (isStarted()) {
      try {
        return operation.call(OptimizingClientPools.getClient(amsUrl));
      } catch (Throwable t) {
        if (shouldReturnNull(t)) {
          return null;
        } else if (shouldRetryLater(t)) {
          LOG.error("Call ams {} got an error and will try again later", amsUrl, t);
          waitAShortTime();
        } else {
          throw t;
        }
      }
    }
    throw new IllegalStateException("Operator is stopped");
  }

  private boolean shouldRetryLater(Throwable t) {
    if (t instanceof AmoroException) {
      AmoroException amoroException = (AmoroException) t;
      // Call ams again when got a persistence/undefined error
      return ErrorCodes.PERSISTENCE_ERROR_CODE == amoroException.getErrorCode()
          || ErrorCodes.UNDEFINED_ERROR_CODE == amoroException.getErrorCode();
    } else {
      // Call ams again when got an unexpected error
      return true;
    }
  }

  // Return null if got MISSING_RESULT error
  private boolean shouldReturnNull(Throwable t) {
    if (t instanceof TApplicationException) {
      TApplicationException applicationException = (TApplicationException) t;
      return applicationException.getType() == TApplicationException.MISSING_RESULT;
    }
    return false;
  }

  /**
   * Call authenticated AMS with a specific AMS URL.
   *
   * @param amsUrl The AMS node URL to call
   * @param operation The operation to execute
   * @return The result of the operation
   * @throws TException If the operation fails
   */
  protected <T> T callAuthenticatedAms(String amsUrl, AmsAuthenticatedCallOperation<T> operation)
      throws TException {
    // Maximum retry time window for auth errors in master-slave mode (30 seconds)
    long maxAuthRetryTimeWindow = TimeUnit.SECONDS.toMillis(90);
    Long firstAuthErrorTime = null;

    while (isStarted()) {
      if (tokenIsReady()) {
        String token = getToken();
        try {
          // Reset retry time on successful call
          firstAuthErrorTime = null;
          return operation.call(OptimizingClientPools.getClient(amsUrl), token);
        } catch (Throwable t) {
          if (t instanceof AmoroException
              && ErrorCodes.PLUGIN_RETRY_AUTH_ERROR_CODE == ((AmoroException) (t)).getErrorCode()) {
            // In master-slave mode, the slave node may not have completed optimizer
            // auto-registration
            // yet, so we should retry within a time window before resetting the token
            boolean isMasterSlaveMode = config.isMasterSlaveMode() && amsNodeManager != null;
            long currentTime = System.currentTimeMillis();

            if (isMasterSlaveMode) {
              if (firstAuthErrorTime == null) {
                // First auth error, record the time
                firstAuthErrorTime = currentTime;
              }

              long elapsedTime = currentTime - firstAuthErrorTime;
              if (elapsedTime < maxAuthRetryTimeWindow) {
                // Still within retry time window, retry after waiting
                LOG.warn(
                    "Got an authorization error while calling ams {} in master-slave mode (elapsed: {}ms/{}ms). "
                        + "This may be because the slave node hasn't completed optimizer auto-registration yet. "
                        + "Will retry after waiting.",
                    amsUrl,
                    elapsedTime,
                    maxAuthRetryTimeWindow,
                    t);
                waitAShortTime();
              } else {
                // Exceeded retry time window, reset token
                LOG.error(
                    "Got authorization errors from ams {} in master-slave mode for {}ms, exceeded retry time window ({}ms). "
                        + "Reset token and wait for a new one",
                    amsUrl,
                    elapsedTime,
                    maxAuthRetryTimeWindow,
                    t);
                resetToken(token);
                firstAuthErrorTime = null;
              }
            } else {
              // Non-master-slave mode, reset token immediately
              LOG.error(
                  "Got a authorization error while calling ams {}, reset token and wait for a new one",
                  amsUrl,
                  t);
              resetToken(token);
              firstAuthErrorTime = null;
            }
          } else if (shouldReturnNull(t)) {
            return null;
          } else if (shouldRetryLater(t)) {
            LOG.error("Call ams {} got an error and will try again later", amsUrl, t);
            waitAShortTime();
          } else {
            throw t;
          }
        }
      } else {
        LOG.debug("Optimizer wait for token is ready");
        waitAShortTime();
      }
    }
    throw new IllegalStateException("Operator is stopped");
  }

  /** Legacy method for backward compatibility. Uses the configured AMS URL. */
  protected <T> T callAuthenticatedAms(AmsAuthenticatedCallOperation<T> operation)
      throws TException {
    return callAuthenticatedAms(getConfig().getAmsUrl(), operation);
  }

  public static void setCallAmsInterval(long callAmsInterval) {
    AbstractOptimizerOperator.callAmsInterval = callAmsInterval;
  }

  protected OptimizerConfig getConfig() {
    return config;
  }

  protected String getToken() {
    return token.get();
  }

  protected boolean tokenIsReady() {
    return token.get() != null;
  }

  protected void resetToken(String oldToken) {
    token.compareAndSet(oldToken, null);
  }

  public void setToken(String newToken) {
    token.set(newToken);
  }

  public boolean isStarted() {
    return !stopped;
  }

  public void stop() {
    this.stopped = true;
  }

  protected void waitAShortTime() {
    waitAShortTime(callAmsInterval);
  }

  protected void waitAShortTime(long waitTime) {
    try {
      TimeUnit.MILLISECONDS.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  protected interface AmsCallOperation<T> {
    T call(OptimizingService.Iface client) throws TException;
  }

  protected interface AmsAuthenticatedCallOperation<T> {
    T call(OptimizingService.Iface client, String token) throws TException;
  }
}
