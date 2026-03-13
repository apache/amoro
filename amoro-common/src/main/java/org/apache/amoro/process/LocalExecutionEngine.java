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

package org.apache.amoro.process;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Local execution engine that runs {@link org.apache.amoro.process.LocalProcess} instances in AMS
 * thread pools.
 *
 * <p>The engine maintains multiple thread pools keyed by {@link
 * org.apache.amoro.process.LocalProcess#tag()}.
 */
public class LocalExecutionEngine implements ExecuteEngine {
  private static final Logger LOG = LoggerFactory.getLogger(LocalExecutionEngine.class);

  public static final String ENGINE_NAME = "local";
  public static final String DEFAULT_POOL = "default";
  public static final String POOL_CONFIG_PREFIX = "pool.";
  public static final String POOL_SIZE_SUFFIX = ".thread-count";
  public static final ConfigOption<Integer> DEFAULT_POOL_SIZE =
      ConfigOptions.key(POOL_CONFIG_PREFIX + DEFAULT_POOL + POOL_SIZE_SUFFIX)
          .intType()
          .defaultValue(10);

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

    ThreadPoolExecutor executor = getPool(localProcess.tag());
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
    Configurations configs = Configurations.fromMap(properties);
    int defaultSize = configs.getInteger(DEFAULT_POOL_SIZE);
    pools.put(DEFAULT_POOL, newFixedPool(DEFAULT_POOL, defaultSize));

    Set<String> customPools =
        properties.keySet().stream()
            .filter(key -> key.startsWith(POOL_CONFIG_PREFIX))
            .map(key -> key.substring(POOL_CONFIG_PREFIX.length()))
            .map(key -> key.substring(0, key.indexOf(".") + 1))
            .collect(Collectors.toSet());

    customPools.forEach(
        name -> {
          ConfigOption<Integer> poolSizeOpt =
              ConfigOptions.key(POOL_CONFIG_PREFIX + name + POOL_SIZE_SUFFIX)
                  .intType()
                  .defaultValue(-1);
          int size = configs.getInteger(poolSizeOpt);
          Preconditions.checkArgument(size > 0, "Pool thread-count is not configured for %s", name);
          pools.put(name, newFixedPool(name, size));
          LOG.info("Initialize local execute pool:{} with size:{}", name, size);
        });
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

  private ThreadPoolExecutor getPool(String tag) {
    if (pools.containsKey(tag)) {
      return pools.get(tag);
    }
    return pools.get(DEFAULT_POOL);
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
}
