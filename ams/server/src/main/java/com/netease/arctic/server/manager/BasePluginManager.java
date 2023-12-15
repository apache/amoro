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
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Parent class of all plugin managers, which provide the common method to help load, install, and
 * visit plugins.
 *
 * @param <T> The plugin types.
 */
public abstract class BasePluginManager<T extends ActivePlugin> {
  private static final Logger LOG = LoggerFactory.getLogger(BasePluginManager.class);

  private final Map<String, T> installedPlugins = new ConcurrentHashMap<>();
  private final Map<String, T> foundedPlugins = new ConcurrentHashMap<>();

  private final List<PluginConfiguration> pluginConfigurations;

  private final Class<T> pluginType;

  private final Executor pluginExecutorPool;

  @SuppressWarnings("unchecked")
  public BasePluginManager(List<PluginConfiguration> pluginConfigurations) {
    Type superclass = this.getClass().getGenericSuperclass();
    Preconditions.checkArgument(
        superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
    pluginType = (Class<T>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
    this.pluginConfigurations = pluginConfigurations;

    // single thread pool, and min thread size is 1.
    this.pluginExecutorPool =
        new ThreadPoolExecutor(
            0,
            1,
            Long.MAX_VALUE,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            r -> {
              Thread thread = new Thread(r);
              thread.setName("Plugin-" + pluginCategory() + "-0");
              thread.setDaemon(true);
              return thread;
            });
  }

  /** Initialize the plugin manager, and install all plugins. */
  public void initialize() {
    foundAvailablePlugins();
    for (PluginConfiguration pluginConfig : pluginConfigurations) {
      if (!pluginConfig.isEnabled()) {
        continue;
      }
      Map<String, String> props = pluginConfig.getProperties();
      installedPlugins.computeIfAbsent(
          pluginConfig.getName(),
          name -> {
            T plugin = foundedPlugins.get(name);
            Preconditions.checkNotNull(
                plugin, "Cannot find am implement class for plugin:%s", name);
            plugin.open(props);
            return plugin;
          });
    }
  }

  /**
   * Plugin category to manger
   *
   * @return Category name of plugins.
   */
  protected abstract String pluginCategory();

  /**
   * Jars path for this plugin manager.
   *
   * @return plugins path.
   */
  protected String pluginPath() {
    return Environments.getPluginPath() + "/" + pluginCategory();
  }

  /**
   * Visit all installed plugins
   *
   * @param visitor function to visit all installed plugins.
   */
  protected void callPlugins(Consumer<T> visitor) {
    this.installedPlugins
        .values()
        .forEach(
            p -> {
              try (ClassLoaderContext ignored = new ClassLoaderContext(p)) {
                visitor.accept(p);
              } catch (Throwable throwable) {
                LOG.error("Error when call plugin: " + p.name(), throwable);
              }
            });
  }

  /**
   * Visit all installed plugins and non-block the current thread.
   *
   * @param visitor function to visit all installed plugins.
   */
  protected void callPluginsAsync(Consumer<T> visitor) {
    pluginExecutorPool.execute(() -> callPlugins(visitor));
  }

  private void foundAvailablePlugins() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    ServiceLoader<T> buildInLoader = ServiceLoader.load(pluginType, classLoader);
    addToFoundedPlugin(buildInLoader);

    try {
      Path pluginPath = Paths.get(pluginPath());
      Files.list(pluginPath)
          .map(Path::toFile)
          .forEach(
              f -> {
                if (f.isFile() && f.getName().endsWith(".jar")) {
                  findSingleJarExternalPlugins(f, classLoader);
                } else if (f.isDirectory()) {
                  findClasspathExternalPlugins(f, classLoader);
                }
              });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void findSingleJarExternalPlugins(File pluginJarFile, ClassLoader parentClassLoader) {
    try {
      ClassLoader pluginClassLoader =
          new URLClassLoader(
              new URL[] {new URL(pluginJarFile.getAbsolutePath())}, parentClassLoader);
      ServiceLoader<T> loader = ServiceLoader.load(pluginType, pluginClassLoader);
      addToFoundedPlugin(loader);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void findClasspathExternalPlugins(File pluginClasspath, ClassLoader parentClassLoader) {
    URL[] jarFiles =
        Optional.ofNullable(pluginClasspath.listFiles((dir, name) -> name.endsWith(".jar")))
            .map(Arrays::asList)
            .map(files -> files.stream().map(this::fileToURL).toArray(URL[]::new))
            .orElse(null);
    if (jarFiles != null) {
      ClassLoader pluginClassLoader = new URLClassLoader(jarFiles, parentClassLoader);
      ServiceLoader<T> loader = ServiceLoader.load(pluginType, pluginClassLoader);
      addToFoundedPlugin(loader);
    }
  }

  private URL fileToURL(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void addToFoundedPlugin(ServiceLoader<T> loader) {
    loader.forEach(
        p -> {
          T exists = foundedPlugins.putIfAbsent(p.name(), p);
          if (exists != null) {
            throw new IllegalStateException(
                "Plugin name "
                    + p.name()
                    + " conflict, current plugin class: "
                    + p.getClass().getName()
                    + ", existing plugin class"
                    + exists.getClass().getName());
          }
        });
  }
}
