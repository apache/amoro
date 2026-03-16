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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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
  public static final ConfigOption<Duration> PROCESS_STATUS_TTL =
      ConfigOptions.key("process.status.ttl").durationType().defaultValue(Duration.ofHours(4));

  private final Map<String, ThreadPoolExecutor> pools = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ProcessHolder> processes = new ConcurrentHashMap<>();

  private long statusTtl = PROCESS_STATUS_TTL.defaultValue().toMillis();

  @Override
  public EngineType engineType() {
    return EngineType.of(ENGINE_NAME);
  }

  @Override
  public ProcessStatus getStatus(String processIdentifier) {
    if (processIdentifier == null || processIdentifier.isEmpty()) {
      return ProcessStatus.UNKNOWN;
    }
    expire();

    ProcessHolder process = processes.get(processIdentifier);
    if (process == null) {
      return ProcessStatus.UNKNOWN;
    }
    return process.getStatus();
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
    CompletableFuture<?> future = CompletableFuture.runAsync(localProcess::run, executor);
    processes.put(identifier, new ProcessHolder(future));
    expire();
    return identifier;
  }

  @Override
  public ProcessStatus tryCancelTableProcess(TableProcess tableProcess, String processIdentifier) {
    ProcessHolder p = this.processes.get(processIdentifier);
    if (p == null) {
      return ProcessStatus.UNKNOWN;
    }
    if (p.finishTime() > 0) {
      return p.getStatus();
    }
    p.cancel();
    return p.getStatus();
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
            .map(key -> key.substring(0, key.indexOf(".")))
            .map(String::toLowerCase)
            .filter(name -> !DEFAULT_POOL.equalsIgnoreCase(name))
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
    this.statusTtl = configs.getDurationInMillis(PROCESS_STATUS_TTL);
  }

  @Override
  public void close() {
    pools.values().forEach(ThreadPoolExecutor::shutdown);
    pools.clear();
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

  private void expire() {
    long threshold = System.currentTimeMillis() - statusTtl;
    Set<String> expireIdentifiers =
        processes.entrySet().stream()
            .filter(e -> e.getValue().finishTime() > 0 && e.getValue().finishTime() < threshold)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    expireIdentifiers.forEach(processes::remove);
  }

  private static class ProcessHolder {
    private final CompletableFuture<?> future;
    private final AtomicLong finishTime = new AtomicLong(-1);
    private final AtomicReference<ProcessStatus> status =
        new AtomicReference<>(ProcessStatus.RUNNING);
    private final AtomicReference<String> failedInfo = new AtomicReference<>("");

    public ProcessHolder(CompletableFuture<?> future) {
      this.future = future;
      this.future.whenComplete((v, t) -> onComplete(t));
    }

    private void onComplete(Throwable e) {
      finishTime.compareAndSet(-1, System.currentTimeMillis());
      if (e != null) {
        status.compareAndSet(ProcessStatus.RUNNING, ProcessStatus.FAILED);
        failedInfo.compareAndSet("", exceptionToString(e));
      } else {
        status.compareAndSet(ProcessStatus.RUNNING, ProcessStatus.SUCCESS);
      }
    }

    private static String exceptionToString(Throwable throwable) {
      StringWriter sw = new StringWriter();
      throwable.printStackTrace(new PrintWriter(sw));
      return sw.toString();
    }

    public ProcessStatus getStatus() {
      return status.get();
    }

    public void cancel() {
      if (finishTime() > 0) {
        return;
      }
      status.compareAndSet(ProcessStatus.RUNNING, ProcessStatus.CANCELED);
      future.cancel(true);
    }

    public long finishTime() {
      return this.finishTime.get();
    }
  }
}
