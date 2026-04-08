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

package org.apache.amoro.server.resource;

import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Manages optimizer registration, authentication, heartbeat, and timeout detection. Extracted from
 * DefaultOptimizingService.
 */
public class OptimizerRegistryService extends StatedPersistentBase {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerRegistryService.class);

  private final long optimizerTouchTimeout;
  private final long taskAckTimeout;
  private final long taskExecuteTimeout;
  private final QuotaManager quotaManager;
  private final Map<String, OptimizerInstance> authOptimizers = new ConcurrentHashMap<>();
  private final OptimizerKeeper optimizerKeeper;

  public OptimizerRegistryService(
      long optimizerTouchTimeout,
      long taskAckTimeout,
      long taskExecuteTimeout,
      QuotaManager quotaManager) {
    this.optimizerTouchTimeout = optimizerTouchTimeout;
    this.taskAckTimeout = taskAckTimeout;
    this.taskExecuteTimeout = taskExecuteTimeout;
    this.quotaManager = quotaManager;
    this.optimizerKeeper = new OptimizerKeeper();
  }

  public String authenticate(OptimizerRegisterInfo registerInfo) {
    String token = UUID.randomUUID().toString();
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, token);
    registerOptimizer(optimizer, true);
    authOptimizers.put(token, optimizer);
    LOG.info(
        "Authenticated optimizer {} from group {} with token {}",
        optimizer.getResourceId(),
        optimizer.getGroupName(),
        token);
    return token;
  }

  public void touch(String authToken) {
    OptimizerInstance optimizer = authOptimizers.get(authToken);
    if (optimizer == null) {
      LOG.warn("Unknown auth token for touch: {}", authToken);
      return;
    }
    optimizer.touch();
  }

  public void registerOptimizer(OptimizerInstance optimizer, boolean needPersistent) {
    if (needPersistent) {
      doAs(OptimizerMapper.class, mapper -> mapper.insertOptimizer(optimizer));
    }
    quotaManager.addOptimizer(optimizer);
  }

  public void unregisterOptimizer(String token) {
    OptimizerInstance optimizer = authOptimizers.remove(token);
    if (optimizer != null) {
      quotaManager.removeOptimizer(optimizer);
      LOG.info(
          "Unregistered optimizer {} from group {}",
          optimizer.getResourceId(),
          optimizer.getGroupName());
    }
  }

  public OptimizerInstance getAuthenticatedOptimizer(String authToken) {
    return authOptimizers.get(authToken);
  }

  public OptimizerThread getOptimizerThread(String authToken, int threadId) {
    OptimizerInstance optimizer = authOptimizers.get(authToken);
    if (optimizer == null) {
      return null;
    }
    return optimizer.getThread(threadId);
  }

  public String getGroupNameByToken(String authToken) {
    OptimizerInstance optimizer = authOptimizers.get(authToken);
    return optimizer != null ? optimizer.getGroupName() : null;
  }

  public Set<String> getActiveTokens() {
    return Collections.unmodifiableSet(authOptimizers.keySet());
  }

  public void startKeeper(
      Predicate<TaskRuntime<?>> taskRetryPredicate,
      java.util.function.Function<String, List<TaskRuntime<?>>> taskCollector,
      java.util.function.Consumer<TaskRuntime<?>> taskRetryHandler) {
    optimizerKeeper.start(taskRetryPredicate, taskCollector, taskRetryHandler);
  }

  public void stopKeeper() {
    optimizerKeeper.stop();
  }

  public void deleteOptimizer(String group, String resourceId) {
    doAs(OptimizerMapper.class, mapper -> mapper.deleteOptimizer(resourceId));
  }

  /** Inner class for detecting expired optimizers and suspended tasks. */
  private class OptimizerKeeper {
    private final ScheduledExecutorService keeperExecutor =
        new ScheduledThreadPoolExecutor(
            1,
            r -> {
              Thread t = new Thread(r, "optimizer-keeper");
              t.setDaemon(true);
              return t;
            });

    private Predicate<TaskRuntime<?>> taskRetryPredicate;
    private java.util.function.Function<String, List<TaskRuntime<?>>> taskCollector;
    private java.util.function.Consumer<TaskRuntime<?>> taskRetryHandler;

    void start(
        Predicate<TaskRuntime<?>> taskRetryPredicate,
        java.util.function.Function<String, List<TaskRuntime<?>>> taskCollector,
        java.util.function.Consumer<TaskRuntime<?>> taskRetryHandler) {
      this.taskRetryPredicate = taskRetryPredicate;
      this.taskCollector = taskCollector;
      this.taskRetryHandler = taskRetryHandler;
      keeperExecutor.scheduleAtFixedRate(this::run, 0, 10, TimeUnit.SECONDS);
    }

    void stop() {
      keeperExecutor.shutdown();
    }

    private void run() {
      try {
        detectExpiredOptimizers();
        detectSuspendedTasks();
      } catch (Throwable t) {
        LOG.error("OptimizerKeeper error", t);
      }
    }

    private void detectExpiredOptimizers() {
      long now = System.currentTimeMillis();
      for (Map.Entry<String, OptimizerInstance> entry : authOptimizers.entrySet()) {
        OptimizerInstance optimizer = entry.getValue();
        if (now - optimizer.getTouchTime() > optimizerTouchTimeout) {
          LOG.warn(
              "Optimizer {} from group {} has expired, removing",
              optimizer.getResourceId(),
              optimizer.getGroupName());
          unregisterOptimizer(entry.getKey());
        }
      }
    }

    private void detectSuspendedTasks() {
      if (taskCollector == null || taskRetryPredicate == null || taskRetryHandler == null) {
        return;
      }
      for (String group : quotaManager.getGroupNames()) {
        try {
          List<TaskRuntime<?>> tasks = taskCollector.apply(group);
          if (tasks != null) {
            tasks.stream()
                .filter(taskRetryPredicate)
                .forEach(
                    task -> {
                      LOG.warn("Resetting suspended task {} in group {}", task.getTaskId(), group);
                      taskRetryHandler.accept(task);
                    });
          }
        } catch (Exception e) {
          LOG.error("Error detecting suspended tasks for group {}", group, e);
        }
      }
    }
  }
}
