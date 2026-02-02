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

/**
 * Central entry for dynamic configuration.
 *
 * <p>The configuration manager is responsible for periodically loading dynamic overrides from the
 * backing store (database) and providing them to {@link DynamicConfigurations} instances. All
 * {@link DynamicConfigurations} objects must be registered via {@link
 * #registerDynamicConfig(DynamicConfigurations)} so that they can be refreshed when the manager
 * performs a periodic reload.
 */
public interface ConfigurationManager {

  /**
   * Get the latest AMS service level configuration overrides loaded from database.
   *
   * <p>The returned {@link Configurations} typically contains only the override key/value pairs
   * (not the static base configuration). Callers should treat it as immutable.
   */
  Configurations getServerConfigurations();

  /**
   * Get the latest configuration overrides for a specific plugin.
   *
   * @param pluginCategory plugin category, such as {@code "metric-reporters"}
   * @param pluginName plugin configuration name
   * @return overrides for the given plugin, never {@code null}
   */
  Configurations getPluginConfigurations(String pluginCategory, String pluginName);

  /** Start periodic refresh from the backing store. */
  void start();

  /** Stop periodic refresh and release internal resources. */
  void stop();

  /**
   * Register a {@link DynamicConfigurations} instance so that it can be refreshed when the manager
   * reloads overrides.
   */
  void registerDynamicConfig(DynamicConfigurations dyn);
}
