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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractOptimizerOperator implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOptimizerOperator.class);

  // Call ams every 5 seconds by default
  private static long callAmsInterval = TimeUnit.SECONDS.toMillis(5);

  /**
   * In master-slave mode, the maximum number of consecutive shouldRetryLater errors allowed per
   * node before failing fast and letting the caller try the next node. Prevents the multi-node
   * polling loop in OptimizerExecutor from getting stuck indefinitely on a single unreachable node.
   */
  static final int MAX_RETRIES_PER_NODE_IN_MASTER_SLAVE = 5;

  private final OptimizerConfig config;
  private final AtomicReference<String> token = new AtomicReference<>();
  private volatile boolean stopped = false;
  private transient volatile AmsNodeManager amsNodeManager;
  private transient volatile ThriftAmsNodeManager thriftAmsNodeManager;
  // Throttle the "no AMS nodes discovered" warning to at most once per minute.
  private volatile long lastEmptyNodesWarnTime = 0;

  public AbstractOptimizerOperator(OptimizerConfig config) {
    Preconditions.checkNotNull(config);
    this.config = config;
    // Node managers are initialized lazily in ensureNodeManagerInitialized() rather than here,
    // because Flink serializes operators on the client side and deserializes them on TaskManagers.
    // Since amsNodeManager and thriftAmsNodeManager are transient (they hold non-serializable
    // ZK/Thrift clients), they would be lost after deserialization. Lazy initialization in
    // getAmsNodeUrls() ensures they are properly created on the TaskManager where they are needed.
  }

  /**
   * Lazily initialize the node manager after Flink deserialization.
   *
   * <p>Flink serializes operators (including OptimizerExecutor/OptimizerToucher) on the client side
   * and deserializes them on TaskManagers. Since {@code amsNodeManager} and {@code
   * thriftAmsNodeManager} are {@code transient} (they hold non-serializable ZK/Thrift clients),
   * they are {@code null} after deserialization. This method re-creates them on demand.
   */
  private void ensureNodeManagerInitialized() {
    if (!config.isMasterSlaveMode()) {
      return;
    }
    String amsUrl = config.getAmsUrl();
    if (amsUrl.startsWith("zookeeper://")) {
      if (amsNodeManager == null) {
        synchronized (this) {
          if (amsNodeManager == null) {
            try {
              this.amsNodeManager = new AmsNodeManager(amsUrl);
              LOG.info("Initialized ZK AmsNodeManager for master-slave mode (url: {})", amsUrl);
            } catch (Exception e) {
              LOG.warn("Failed to initialize AmsNodeManager, will use single AMS URL", e);
            }
          }
        }
      }
    } else {
      if (thriftAmsNodeManager == null) {
        synchronized (this) {
          if (thriftAmsNodeManager == null) {
            try {
              this.thriftAmsNodeManager = new ThriftAmsNodeManager(amsUrl);
              LOG.info(
                  "Initialized ThriftAmsNodeManager for master-slave mode (DB HA, url: {})",
                  amsUrl);
            } catch (Exception e) {
              LOG.warn("Failed to initialize ThriftAmsNodeManager, will use single AMS URL", e);
            }
          }
        }
      }
    }
  }

  /** Get the AmsNodeManager instance if available (ZK mode). */
  protected AmsNodeManager getAmsNodeManager() {
    ensureNodeManagerInitialized();
    return amsNodeManager;
  }

  /** Get the ThriftAmsNodeManager instance if available (DB mode). */
  protected ThriftAmsNodeManager getThriftAmsNodeManager() {
    ensureNodeManagerInitialized();
    return thriftAmsNodeManager;
  }

  /**
   * Returns true if any node manager (ZK or Thrift) is active. Used by OptimizerExecutor to decide
   * whether to use master-slave polling logic.
   */
  protected boolean hasAmsNodeManager() {
    ensureNodeManagerInitialized();
    return amsNodeManager != null || thriftAmsNodeManager != null;
  }

  /**
   * Get all AMS URLs from the active node manager. Falls back to the single configured URL when no
   * node manager is available or the node list is empty.
   */
  protected List<String> getAmsNodeUrls() {
    ensureNodeManagerInitialized();
    if (amsNodeManager != null) {
      List<String> urls = amsNodeManager.getAllAmsUrls();
      if (!urls.isEmpty()) {
        return urls;
      }
      long now = System.currentTimeMillis();
      if (now - lastEmptyNodesWarnTime > TimeUnit.MINUTES.toMillis(1)) {
        lastEmptyNodesWarnTime = now;
        LOG.warn(
            "Optimizer is configured for master-slave mode but no AMS nodes were discovered "
                + "from ZooKeeper nodes path. Falling back to the configured URL [{}] (master only). "
                + "Ensure that all AMS nodes have 'ha.use-master-slave-mode: true' configured so "
                + "they register themselves to ZooKeeper and can be discovered by optimizers.",
            getConfig().getAmsUrl());
      }
    }
    if (thriftAmsNodeManager != null) {
      return thriftAmsNodeManager.getAllAmsUrls();
    }
    return Collections.singletonList(getConfig().getAmsUrl());
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
    // Per-node retry budget: in master-slave mode, limit consecutive shouldRetryLater retries so
    // that a permanently unreachable node does not block the multi-node polling loop indefinitely.
    int consecutiveRetries = 0;

    while (isStarted()) {
      if (tokenIsReady()) {
        String token = getToken();
        try {
          return operation.call(OptimizingClientPools.getClient(amsUrl), token);
        } catch (Throwable t) {
          if (t instanceof AmoroException
              && ErrorCodes.PLUGIN_RETRY_AUTH_ERROR_CODE == ((AmoroException) (t)).getErrorCode()) {
            boolean isMasterSlaveMode = config.isMasterSlaveMode() && hasAmsNodeManager();

            if (isMasterSlaveMode) {
              // In master-slave mode, fail fast and let the multi-node polling loop in
              // OptimizerExecutor move on to the next node immediately.
              //
              // The root cause of auth failures on a slave is usually that the slave has not yet
              // synced the optimizer registration from the database. Blocking here for 30 seconds
              // and then resetting the GLOBAL token (which is also used by the master and other
              // healthy nodes) would:
              //   1. Block the for-loop from polling other nodes for 30+ seconds.
              //   2. Re-register with master → get new token → slave still fails → infinite loop.
              //
              // Instead, throw immediately so pollTask() returns, the for-loop advances to the
              // next node, and the slave is retried on the NEXT poll round (basePollInterval
              // later).
              // The global token is left intact so master/other nodes continue to work normally.
              LOG.warn(
                  "Optimizer executor got authorization error from AMS {} in master-slave mode. "
                      + "The slave node may not have synced the optimizer registration yet. "
                      + "Skipping this node for this round; will retry on the next poll cycle.",
                  amsUrl,
                  t);
              if (t instanceof TException) {
                throw (TException) t;
              }
              throw new TException("Auth error for slave " + amsUrl, t);
            } else {
              // Single-node mode: reset token and wait for OptimizerToucher to re-register.
              LOG.error(
                  "Got authorization error while calling AMS {} (token: {} is now invalid). "
                      + "Token reset; will re-register on the next heartbeat cycle.",
                  amsUrl,
                  token,
                  t);
              resetToken(token);
            }
          } else if (shouldReturnNull(t)) {
            return null;
          } else if (shouldRetryLater(t)) {
            LOG.error("Call ams {} got an error and will try again later", amsUrl, t);
            if (hasAmsNodeManager()) {
              consecutiveRetries++;
              if (consecutiveRetries > MAX_RETRIES_PER_NODE_IN_MASTER_SLAVE) {
                LOG.warn(
                    "Per-node retry budget ({}) exhausted for AMS {}, failing fast to try next node",
                    MAX_RETRIES_PER_NODE_IN_MASTER_SLAVE,
                    amsUrl);
                if (t instanceof TException) {
                  throw (TException) t;
                }
                throw new TException(
                    "Per-node retry budget exhausted for " + amsUrl + ": " + t.getMessage(), t);
              }
            }
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
