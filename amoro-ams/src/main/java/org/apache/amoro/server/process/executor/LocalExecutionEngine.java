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

package org.apache.amoro.server.process.executor;

import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Local execution engine that runs {@link LocalProcess} instances in AMS thread pools.
 *
 * <p>The engine maintains multiple thread pools keyed by {@link LocalProcess#tag()}.
 */
public class LocalExecutionEngine implements ExecuteEngine {

  public static final String ENGINE_NAME = "local";
  public static final String DEFAULT_POOL = "default";
  public static final String SNAPSHOTS_EXPIRING_POOL = "snapshots-expiring";

  private final Map<String, ThreadPoolExecutor> pools = new ConcurrentHashMap<>();
  private final Map<String, Future<?>> activeInstances = new ConcurrentHashMap<>();
  private final Map<String, Future<?>> cancelingInstances = new ConcurrentHashMap<>();

  @Override
  public EngineType engineType() {
    return EngineType.of(ENGINE_NAME);
  }

  @Override
  public ProcessStatus getStatus(String processIdentifier) {
    if (processIdentifier == null || processIdentifier.isEmpty()) {
      return ProcessStatus.UNKNOWN;
    }

    Map<String, Future<?>> instances =
        cancelingInstances.containsKey(processIdentifier) ? cancelingInstances : activeInstances;

    Future<?> future = instances.get(processIdentifier);
    if (future == null) {
      return ProcessStatus.KILLED;
    }

    if (future.isCancelled()) {
      instances.remove(processIdentifier);
      return ProcessStatus.CANCELED;
    }

    if (future.isDone()) {
      instances.remove(processIdentifier);
      try {
        future.get();
        return ProcessStatus.SUCCESS;
      } catch (Exception e) {
        return ProcessStatus.FAILED;
      }
    }

    return cancelingInstances.containsKey(processIdentifier)
        ? ProcessStatus.CANCELING
        : ProcessStatus.RUNNING;
  }

  @Override
  public String submitTableProcess(TableProcess tableProcess) {
    if (!(tableProcess instanceof LocalProcess)) {
      throw new IllegalArgumentException(
          "LocalExecutionEngine only supports LocalProcess, but got: " + tableProcess.getClass());
    }

    LocalProcess localProcess = (LocalProcess) tableProcess;
    String identifier = UUID.randomUUID().toString();

    ThreadPoolExecutor executor = getOrCreatePool(localProcess.tag());
    Future<?> future =
        executor.submit(
            () -> {
              localProcess.run();
              return null;
            });

    activeInstances.put(identifier, future);
    return identifier;
  }

  @Override
  public ProcessStatus tryCancelTableProcess(TableProcess tableProcess, String processIdentifier) {
    Future<?> future = activeInstances.get(processIdentifier);
    if (future == null) {
      return ProcessStatus.CANCELED;
    }

    activeInstances.remove(processIdentifier);
    cancelingInstances.put(processIdentifier, future);

    if (future.isDone()) {
      try {
        future.get();
        return ProcessStatus.SUCCESS;
      } catch (Exception e) {
        return ProcessStatus.FAILED;
      }
    }

    if (future.isCancelled()) {
      return ProcessStatus.CANCELED;
    }

    future.cancel(true);
    return ProcessStatus.CANCELING;
  }

  @Override
  public void open(Map<String, String> properties) {
    String defaultSizeValue = properties == null ? null : properties.get("default.thread-count");
    int defaultSize = parseInt(defaultSizeValue, 10);
    pools.put(DEFAULT_POOL, newFixedPool(DEFAULT_POOL, defaultSize));

    String snapshotsExpiringSizeValue =
        properties == null ? null : properties.get("snapshots-expiring.thread-count");
    int snapshotsExpiringSize = parseInt(snapshotsExpiringSizeValue, defaultSize);
    pools.put(
        SNAPSHOTS_EXPIRING_POOL,
        newFixedPool(SNAPSHOTS_EXPIRING_POOL, Math.max(snapshotsExpiringSize, 1)));
  }

  @Override
  public void close() {
    pools.values().forEach(ThreadPoolExecutor::shutdown);
    pools.clear();
    activeInstances.clear();
    cancelingInstances.clear();
  }

  @Override
  public String name() {
    return ENGINE_NAME;
  }

  private ThreadPoolExecutor getOrCreatePool(String tag) {
    if (tag == null || tag.isEmpty()) {
      tag = DEFAULT_POOL;
    }

    return pools.computeIfAbsent(tag, t -> newFixedPool(t, 10));
  }

  private ThreadPoolExecutor newFixedPool(String tag, int size) {
    return new ThreadPoolExecutor(
        size,
        size,
        60,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new ThreadFactoryBuilder().setDaemon(false).setNameFormat("local-" + tag + "-%d").build());
  }

  private int parseInt(String value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
