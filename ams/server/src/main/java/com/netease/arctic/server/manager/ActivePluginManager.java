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
import com.netease.arctic.ams.api.PluginManager;
import com.netease.arctic.server.exception.LoadingPluginException;
import com.netease.arctic.server.utils.PreconditionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ActivePluginManager<T extends ActivePlugin>
    implements PluginManager<T>, Iterable<T> {

  protected static final String PUGIN_IMPLEMENTION_CLASS = "impl";

  private final Map<String, T> installedPlugins = new ConcurrentHashMap<>();

  protected ActivePluginManager() {
  }

  protected abstract Map<String, String> loadProperties(String pluginName);

  @SuppressWarnings("unchecked")
  @Override
  public void install(String pluginName) {
    PreconditionUtils.checkNotExist(installedPlugins.containsKey(pluginName),
        "Plugin " + pluginName);
    Map<String, String> properties = loadProperties(pluginName);
    String pluginClass = properties.get(PUGIN_IMPLEMENTION_CLASS);
    try {
      Class<?> clazz = Class.forName(pluginClass);
      T plugin = (T) clazz.newInstance();
      installedPlugins.computeIfAbsent(pluginName, k -> {
        plugin.open(properties);
        return plugin;
      });
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new LoadingPluginException("Cannot load plugin " + pluginName, e);
    }
  }

  @Override
  public void uninstall(String pluginName) {
    PreconditionUtils.checkExist(installedPlugins.containsKey(pluginName),
        "Plugin " + pluginName);
    T plugin = installedPlugins.remove(pluginName);
    if (plugin != null) {
      plugin.close();
    }
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      final Iterator<T> iterator = installedPlugins.values().iterator();
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public T next() {
        return iterator.next();
      }
    };
  }

  @Override
  public T get(String pluginName) {
    return installedPlugins.get(pluginName);
  }

  @Override
  public void close() {
    installedPlugins.values().forEach(ActivePlugin::close);
    installedPlugins.clear();
  }
}
