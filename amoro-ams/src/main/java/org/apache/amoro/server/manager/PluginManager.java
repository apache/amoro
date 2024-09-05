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

package org.apache.amoro.server.manager;

import org.apache.amoro.AmoroPlugin;

import java.util.List;

/**
 * Plugin manager interface for all pluggable services
 *
 * @param <T> plugin type
 */
public interface PluginManager<T extends AmoroPlugin> {

  /**
   * Install a plugin
   *
   * @param pluginName related to Plugin.name()
   */
  void install(String pluginName);

  /**
   * Uninstall a plugin
   *
   * @param pluginName related to Plugin.name()
   */
  void uninstall(String pluginName);

  /**
   * Get a plugin by its name
   *
   * @param pluginName related to Plugin.name()
   * @return plugin instance
   */
  T get(String pluginName);

  /**
   * Get all installed plugins
   *
   * @return all installed plugins
   */
  List<T> installedPlugins();

  /** Close the plugin manager and trigger all plugins to close if necessary */
  void close();
}
