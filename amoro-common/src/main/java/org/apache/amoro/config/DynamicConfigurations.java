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

package org.apache.amoro.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * DynamicConfigurations represents a configuration view that can be updated at runtime.
 *
 * <p>Each instance keeps a reference to the initial static {@link Configurations} (baseConfig) and
 * asks a {@link ConfigurationManager} for service level or plugin level overrides during refresh.
 * The effective configuration is always
 *
 * <pre>
 *   currentConfig = merge(baseConfig, overridesFromManager)
 * </pre>
 *
 * where overrides come from:
 *
 * <ul>
 *   <li>AMS service level: {@link ConfigurationManager#getServerConfigurations()}.
 *   <li>Plugin level: {@link ConfigurationManager#getPluginConfigurations(String, String)}.
 * </ul>
 *
 * <p>This class extends {@link Configurations} so that it can be injected wherever a {@code
 * Configurations} is expected. All getter methods defined in {@link Configurations} operate on the
 * current merged configuration.
 */
public class DynamicConfigurations extends Configurations {

  public enum Type {
    SERVICE,
    PLUGIN
  }

  private final Configurations baseConfig;
  private final ConfigurationManager configurationManager;
  private final Type type;
  private final String pluginCategory;
  private final String pluginName;

  /**
   * Create a service level dynamic configuration.
   *
   * @param baseConfig static AMS service configuration
   * @param configurationManager configuration manager used to fetch overrides
   */
  public DynamicConfigurations(
      Configurations baseConfig, ConfigurationManager configurationManager) {
    this(baseConfig, configurationManager, Type.SERVICE, null, null);
  }

  /**
   * Create a plugin level dynamic configuration.
   *
   * @param baseConfig static plugin configuration
   * @param configurationManager configuration manager used to fetch overrides
   * @param pluginCategory plugin category, such as {@code "metric-reporters"}
   * @param pluginName plugin configuration name
   */
  public DynamicConfigurations(
      Configurations baseConfig,
      ConfigurationManager configurationManager,
      String pluginCategory,
      String pluginName) {
    this(baseConfig, configurationManager, Type.PLUGIN, pluginCategory, pluginName);
  }

  private DynamicConfigurations(
      Configurations baseConfig,
      ConfigurationManager configurationManager,
      Type type,
      String pluginCategory,
      String pluginName) {
    super(baseConfig);
    this.baseConfig = baseConfig.clone();
    this.configurationManager = configurationManager;
    this.type = type;
    this.pluginCategory = pluginCategory;
    this.pluginName = pluginName;
    if (this.configurationManager != null) {
      this.configurationManager.registerDynamicConfig(this);
      // build initial effective configuration from current overrides
      refreshFromManager();
    }
  }

  /** Whether this instance represents plugin level configuration. */
  public boolean isPluginLevel() {
    return type == Type.PLUGIN;
  }

  /** Plugin category, meaningful only for plugin level configurations. */
  public String getPluginCategory() {
    return pluginCategory;
  }

  /** Plugin name, meaningful only for plugin level configurations. */
  public String getPluginName() {
    return pluginName;
  }

  /**
   * Refresh the current configuration from the underlying {@link ConfigurationManager}.
   *
   * <p>This method is intended to be called by {@link ConfigurationManager} on a periodic basis.
   */
  public void refreshFromManager() {
    if (configurationManager == null) {
      return;
    }
    Configurations overrides;
    if (type == Type.SERVICE) {
      overrides = configurationManager.getServerConfigurations();
    } else {
      overrides = configurationManager.getPluginConfigurations(pluginCategory, pluginName);
    }
    if (overrides == null) {
      overrides = new Configurations();
    }

    Map<String, Object> merged = new HashMap<>();
    synchronized (baseConfig.confData) {
      merged.putAll(baseConfig.confData);
    }
    synchronized (overrides.confData) {
      merged.putAll(overrides.confData);
    }

    synchronized (this.confData) {
      this.confData.clear();
      this.confData.putAll(merged);
    }
  }

  /** Returns a snapshot of current configuration as a standalone {@link Configurations}. */
  public Configurations snapshot() {
    return this.clone();
  }

  // Convenience delegate methods mirroring Configurations API -------------------------------

  public <T> T get(ConfigOption<T> option) {
    return super.get(option);
  }

  public String getString(ConfigOption<String> option) {
    return super.getString(option);
  }

  public int getInteger(ConfigOption<Integer> option) {
    return super.getInteger(option);
  }

  public long getLong(ConfigOption<Long> option) {
    return super.getLong(option);
  }

  public boolean getBoolean(ConfigOption<Boolean> option) {
    return super.getBoolean(option);
  }

  public long getDurationInMillis(ConfigOption<Duration> option) {
    Duration duration = super.get(option);
    return duration.toMillis();
  }

  public String getValue(ConfigOption<?> option) {
    return super.getValue(option);
  }

  public <T extends Enum<T>> T getEnum(Class<T> enumClass, ConfigOption<String> option) {
    return super.getEnum(enumClass, option);
  }

  public <T> Optional<T> getOptional(ConfigOption<T> option) {
    return super.getOptional(option);
  }
}
