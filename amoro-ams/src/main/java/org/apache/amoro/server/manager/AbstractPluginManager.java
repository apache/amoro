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

import org.apache.amoro.ActivePlugin;
import org.apache.amoro.config.ConfigurationManager;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.AlreadyExistsException;
import org.apache.amoro.exception.LoadingPluginException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.Environments;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parent class of all plugin managers, which provide the common method to help load, install, and
 * visit plugins.
 *
 * @param <T> The plugin types.
 */
public abstract class AbstractPluginManager<T extends ActivePlugin> implements PluginManager<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPluginManager.class);
  private static final String PLUGIN_CONFIG_DIR_NAME = "plugins";

  private final Map<String, T> installedPlugins = new ConcurrentHashMap<>();
  private final Map<String, T> foundedPlugins = new ConcurrentHashMap<>();
  private final Map<String, PluginConfiguration> pluginConfigs = Maps.newLinkedHashMap();
  private final String pluginCategory;
  protected final Configurations serviceConfig;
  private final Class<T> pluginType;
  private final ConfigurationManager configurationManager;

  @SuppressWarnings("unchecked")
  public AbstractPluginManager(String pluginCategory) {
    this(pluginCategory, null, null);
  }

  @SuppressWarnings("unchecked")
  public AbstractPluginManager(String pluginCategory, ConfigurationManager configurationManager) {
    this(pluginCategory, null, configurationManager);
  }

  @SuppressWarnings("unchecked")
  public AbstractPluginManager(
      String pluginCategory,
      Configurations serviceConfig,
      ConfigurationManager configurationManager) {
    this.pluginCategory = pluginCategory;
    this.serviceConfig = serviceConfig;
    this.configurationManager = configurationManager;
    Type superclass = this.getClass().getGenericSuperclass();
    Preconditions.checkArgument(
        superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
    pluginType = (Class<T>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
  }

  /** Initialize the plugin manager, and install all plugins. */
  public void initialize() {
    List<PluginConfiguration> pluginConfigs = loadPluginConfigurations();
    pluginConfigs.stream()
        .sorted(Comparator.comparing(PluginConfiguration::getPriority))
        .forEach(
            config -> {
              PluginConfiguration exists = this.pluginConfigs.putIfAbsent(config.getName(), config);
              Preconditions.checkArgument(
                  exists == null, "Duplicate plugin name found: %s", config.getName());
            });

    foundAvailablePlugins();
    for (PluginConfiguration pluginConfig : pluginConfigs) {
      if (!pluginConfig.isEnabled()) {
        continue;
      }
      install(pluginConfig.getName());
    }
  }

  @Override
  public void install(String pluginName) {
    PluginConfiguration pluginConfig = this.pluginConfigs.get(pluginName);
    Preconditions.checkArgument(
        pluginConfig != null, "Plugin configuration is not found for %s", pluginName);

    AtomicBoolean exists = new AtomicBoolean(true);
    installedPlugins.computeIfAbsent(
        pluginConfig.getName(),
        name -> {
          T plugin = foundedPlugins.get(name);
          if (plugin == null) {
            throw new LoadingPluginException(
                "Cannot find an implement class for the plugin:" + name);
          }

          Map<String, String> properties = pluginConfig.getProperties();
          Map<String, String> augmentedProperties = Maps.newHashMap(properties);

          // determine property keys for plugin category and name, configurable via
          // AmoroManagementConf.PLUGIN_CATEGORY_PROPERTY_KEY and
          // AmoroManagementConf.PLUGIN_NAME_PROPERTY_KEY
          String categoryKey =
              serviceConfig.getString(AmoroManagementConf.PLUGIN_CATEGORY_PROPERTY_KEY);
          String nameKey = serviceConfig.getString(AmoroManagementConf.PLUGIN_NAME_PROPERTY_KEY);

          // expose key names to plugins so that they can resolve category/name in their open()
          // methods
          augmentedProperties.put(
              AmoroManagementConf.PLUGIN_CATEGORY_PROPERTY_KEY.key(), categoryKey);
          augmentedProperties.put(AmoroManagementConf.PLUGIN_NAME_PROPERTY_KEY.key(), nameKey);

          // inject identifiers so that plugins can locate their own dynamic configs
          augmentedProperties.put(categoryKey, pluginCategory());
          augmentedProperties.put(nameKey, pluginConfig.getName());

          if (configurationManager != null) {
            plugin.open(augmentedProperties, configurationManager);
          } else {
            plugin.open(augmentedProperties);
          }
          exists.set(false);
          return plugin;
        });
    if (exists.get()) {
      throw new AlreadyExistsException(
          "Plugin: " + pluginConfig.getName() + " has been already installed");
    }
  }

  @Override
  public void uninstall(String pluginName) {
    installedPlugins.computeIfPresent(
        pluginName,
        (name, plugin) -> {
          plugin.close();
          return null;
        });
  }

  @Override
  public T get(String pluginName) {
    return installedPlugins.get(pluginName);
  }

  /** Close all active plugin */
  @Override
  public void close() {
    forEach(plugin -> uninstall(plugin.name()));
  }

  @Override
  public List<T> installedPlugins() {
    return installedPluginsStream().collect(Collectors.toList());
  }

  protected Stream<T> installedPluginsStream() {
    return pluginConfigs.values().stream()
        .filter(p -> installedPlugins.containsKey(p.getName()))
        .map(p -> installedPlugins.get(p.getName()));
  }

  /**
   * Plugin category to manger
   *
   * @return Category name of plugins.
   */
  protected String pluginCategory() {
    return pluginCategory;
  }

  /**
   * Jars path for this plugin manager.
   *
   * @return plugins path.
   */
  protected String pluginPath() {
    return Environments.getPluginPath() + "/" + pluginCategory();
  }

  /**
   * Get plugin manger config file path
   *
   * @return path of plugin manger config path
   */
  protected Path pluginManagerConfigFilePath() {
    return Paths.get(
        Environments.getConfigPath(), PLUGIN_CONFIG_DIR_NAME, pluginCategory() + ".yaml");
  }

  /**
   * Visit all installed plugins.
   *
   * @param visitor function to visit all installed plugins.
   */
  protected void forEach(Consumer<? super T> visitor) {
    installedPluginsStream()
        .forEach(
            plugin -> {
              try (ClassLoaderContext ignored = new ClassLoaderContext(plugin)) {
                visitor.accept(plugin);
              } catch (Throwable throwable) {
                LOG.error("Error when call plugin: {}", plugin.name(), throwable);
              }
            });
  }

  private void foundAvailablePlugins() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    ServiceLoader<T> buildInLoader = ServiceLoader.load(pluginType, classLoader);
    addToFoundedPlugin(buildInLoader);

    try {
      Path pluginPath = Paths.get(pluginPath());
      if (!Files.exists(pluginPath) || !Files.isDirectory(pluginPath)) {
        return;
      }
      Files.list(pluginPath)
          .map(Path::toFile)
          .forEach(
              file -> {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                  findSingleJarExternalPlugins(file, classLoader);
                } else if (file.isDirectory()) {
                  findClasspathExternalPlugins(file, classLoader);
                }
              });
    } catch (IOException e) {
      throw new LoadingPluginException("Failed when discover available plugins", e);
    }
  }

  protected void findSingleJarExternalPlugins(File pluginJarFile, ClassLoader parentClassLoader) {
    try {
      ClassLoader pluginClassLoader =
          new URLClassLoader(new URL[] {pluginJarFile.toURI().toURL()}, parentClassLoader);
      ServiceLoader<T> loader = ServiceLoader.load(pluginType, pluginClassLoader);
      addToFoundedPlugin(loader);
    } catch (MalformedURLException e) {
      throw new LoadingPluginException("Failed when load plugin", e);
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

  @VisibleForTesting
  protected List<PluginConfiguration> loadPluginConfigurations() {
    JsonNode yamlConfig = null;
    Path mangerConfigPath = pluginManagerConfigFilePath();
    if (!Files.exists(mangerConfigPath)) {
      return ImmutableList.of();
    }
    try {
      Object yamlObj = new Yaml().loadAs(Files.newInputStream(mangerConfigPath), Object.class);
      if (yamlObj instanceof Map) {
        yamlConfig = JacksonUtil.fromObjects(yamlObj);
      }
    } catch (IOException e) {
      throw new LoadingPluginException(
          "Failed when load plugin configs from file: " + mangerConfigPath, e);
    }

    LOG.info("initializing plugin configuration for: {}", pluginCategory());
    String pluginListKey = pluginCategory();

    JsonNode pluginConfigList = yamlConfig != null ? yamlConfig.get(pluginListKey) : null;
    List<PluginConfiguration> configs = Lists.newArrayList();
    if (pluginConfigList != null && !pluginConfigList.isEmpty()) {
      for (int i = 0; i < pluginConfigList.size(); i++) {
        JsonNode pluginConfiguration = pluginConfigList.get(i);
        PluginConfiguration configuration = PluginConfiguration.fromJSONObject(pluginConfiguration);
        configs.add(configuration);
      }
    }
    return configs;
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
        plugin -> {
          T exists = foundedPlugins.putIfAbsent(plugin.name(), plugin);
          if (exists != null && !exists.getClass().equals(plugin.getClass())) {
            throw new IllegalStateException(
                "Plugin name "
                    + plugin.name()
                    + " conflict, current plugin class: "
                    + plugin.getClass().getName()
                    + ", existing plugin class"
                    + exists.getClass().getName());
          }
        });
  }
}
