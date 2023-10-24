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

import com.netease.arctic.ams.api.AmoroPlugin;
import com.netease.arctic.ams.api.PluginManager;
import com.netease.arctic.server.utils.PreconditionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin manager based on SPI
 *
 * @param <T> plugin type
 */
public abstract class SpiPluginManager<T extends AmoroPlugin> implements PluginManager<T> {

  private final Map<String, T> installedPlugins = new ConcurrentHashMap<>();
  private final ServiceLoader<T> pluginLoader;

  public SpiPluginManager() {
    this.pluginLoader = ServiceLoader.load(getPluginClass());
  }

  @SuppressWarnings("unchecked")
  private Class<T> getPluginClass() {
    try {
      Type type = getClass().getGenericSuperclass();
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] typeArguments = parameterizedType.getActualTypeArguments();
      return (Class<T>) typeArguments[0];
    } catch (Throwable throwable) {
      throw new IllegalStateException(
          "Cannot determine service type for " + getClass().getName(), throwable);
    }
  }

  @Override
  public void install(String pluginName) {
    PreconditionUtils.checkNotExist(
        installedPlugins.containsKey(pluginName), "Plugin " + pluginName);
    for (T plugin : pluginLoader) {
      if (plugin.name().equals(pluginName)) {
        installedPlugins.put(pluginName, plugin);
      }
    }
  }

  @Override
  public void uninstall(String pluginName) {
    PreconditionUtils.checkExist(installedPlugins.containsKey(pluginName), "Plugin " + pluginName);
    installedPlugins.remove(pluginName);
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
    installedPlugins.clear();
  }
}
