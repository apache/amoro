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

package org.apache.amoro.server.config;

import org.apache.amoro.config.ConfigurationManager;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.DynamicConfigurations;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link ConfigurationManager} implementation backed by {@link DynamicConfigStore}.
 *
 * <p>This manager is responsible for:
 *
 * <ul>
 *   <li>periodically reloading AMS service level overrides,
 *   <li>periodically reloading plugin level overrides for registered plugins,
 *   <li>notifying all registered {@link DynamicConfigurations} instances to refresh themselves.
 * </ul>
 *
 * <p>Merge semantics are implemented in {@link DynamicConfigurations}: for service level
 * configuration, {@code currentConfig = merge(baseConfig, getServerConfigurations())}; for plugin
 * level configuration, {@code currentConfig = merge(baseConfig,
 * getPluginConfigurations(pluginCategory, pluginName))}.
 */
public class DbConfigurationManager implements ConfigurationManager {

  private static final Logger LOG = LoggerFactory.getLogger(DbConfigurationManager.class);

  private final DynamicConfigStore store;
  private final ScheduledExecutorService scheduler;
  private final long refreshIntervalMillis;

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private final AtomicReference<Configurations> serverOverrides =
      new AtomicReference<>(new Configurations());

  private final ConcurrentMap<PluginKey, AtomicReference<Configurations>> pluginOverrides =
      new ConcurrentHashMap<>();

  private final CopyOnWriteArrayList<DynamicConfigurations> dynamicConfigs =
      new CopyOnWriteArrayList<>();

  public DbConfigurationManager(DynamicConfigStore store, Duration refreshInterval) {
    Preconditions.checkNotNull(store, "store must not be null");
    Preconditions.checkNotNull(refreshInterval, "refreshInterval must not be null");
    this.store = store;
    this.refreshIntervalMillis = Math.max(1L, refreshInterval.toMillis());
    this.scheduler =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("db-configuration-manager-%d")
                .build());
  }

  @Override
  public Configurations getServerConfigurations() {
    Configurations overrides = serverOverrides.get();
    return overrides != null ? overrides : new Configurations();
  }

  @Override
  public Configurations getPluginConfigurations(String pluginCategory, String pluginName) {
    if (pluginCategory == null) {
      return new Configurations();
    }
    PluginKey key = new PluginKey(pluginCategory.trim(), pluginName);
    AtomicReference<Configurations> ref = pluginOverrides.get(key);
    return ref != null && ref.get() != null ? ref.get() : new Configurations();
  }

  @Override
  public void start() {
    if (!started.compareAndSet(false, true)) {
      return;
    }
    LOG.info("Starting DbConfigurationManager with refresh interval {} ms", refreshIntervalMillis);
    // initial load before scheduling periodic refresh
    safeRefresh();
    scheduler.scheduleWithFixedDelay(
        this::safeRefresh, refreshIntervalMillis, refreshIntervalMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    if (!stopped.compareAndSet(false, true)) {
      return;
    }
    LOG.info("Stopping DbConfigurationManager");
    scheduler.shutdownNow();
    dynamicConfigs.clear();
    pluginOverrides.clear();
  }

  @Override
  public void registerDynamicConfig(DynamicConfigurations dyn) {
    if (dyn == null) {
      return;
    }
    dynamicConfigs.add(dyn);
  }

  private void safeRefresh() {
    if (stopped.get()) {
      return;
    }
    try {
      refreshInternal();
    } catch (Throwable t) {
      LOG.warn("Failed to refresh dynamic configuration overrides", t);
    }
  }

  private void refreshInternal() {
    // 1) reload AMS service level overrides
    Map<String, String> serverMap = store.loadServerOverrides();
    serverOverrides.set(Configurations.fromMap(serverMap));

    // 2) reload plugin level overrides for all registered plugin dynamic configurations
    Map<PluginKey, Configurations> latestPluginOverrides = new HashMap<>();
    for (DynamicConfigurations dyn : dynamicConfigs) {
      if (!dyn.isPluginLevel()) {
        continue;
      }
      PluginKey key = new PluginKey(dyn.getPluginCategory(), dyn.getPluginName());
      if (latestPluginOverrides.containsKey(key)) {
        continue;
      }
      Map<String, String> overridesMap =
          store.loadPluginOverrides(key.pluginCategory, key.pluginName);
      latestPluginOverrides.put(key, Configurations.fromMap(overridesMap));
    }

    for (Map.Entry<PluginKey, Configurations> entry : latestPluginOverrides.entrySet()) {
      pluginOverrides
          .computeIfAbsent(entry.getKey(), k -> new AtomicReference<>(new Configurations()))
          .set(entry.getValue());
    }

    // 3) refresh all registered DynamicConfigurations instances
    for (DynamicConfigurations dyn : dynamicConfigs) {
      try {
        dyn.refreshFromManager();
      } catch (Throwable t) {
        LOG.warn(
            "Failed to refresh DynamicConfigurations for pluginCategory={}, pluginName={}",
            dyn.getPluginCategory(),
            dyn.getPluginName(),
            t);
      }
    }
  }

  private static final class PluginKey {
    private final String pluginCategory;
    private final String pluginName;

    private PluginKey(String pluginCategory, String pluginName) {
      this.pluginCategory = pluginCategory;
      this.pluginName = pluginName == null ? null : pluginName.trim();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PluginKey pluginKey = (PluginKey) o;
      return Objects.equals(pluginCategory, pluginKey.pluginCategory)
          && Objects.equals(pluginName, pluginKey.pluginName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(pluginCategory, pluginName);
    }
  }
}
