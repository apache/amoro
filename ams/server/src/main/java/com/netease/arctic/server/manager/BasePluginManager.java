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


package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.ActivePlugin;
import com.netease.arctic.server.Environments;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parent class of all plugin managers, which provide the common method to help load,
 * install, and visit plugins.
 * @param <T> The plugin types.
 */
public abstract class BasePluginManager<T extends ActivePlugin> {

  private final Map<String, T> installedPlugins = new ConcurrentHashMap<>();

  /**
   * Plugin type to manger
   * @return Type of plugins.
   */
  abstract protected String pluginType();

  /**
   * Jars path for this plugin manager.
   * @return plugins path.
   */
  protected String pluginPath() {
    return Environments.getPluginPath() + "/" + pluginType();
  }




}
