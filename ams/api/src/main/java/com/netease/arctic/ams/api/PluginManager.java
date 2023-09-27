package com.netease.arctic.ams.api;

import java.util.List;

/**
 * Plugin manager interface for all pluggable services
 * @param <T> plugin type
 */
public interface PluginManager<T extends AmoroPlugin> {

  /**
   * Install a plugin
   * @param pluginName related to Plugin.name()
   */
  void install(String pluginName);

  /**
   * Uninstall a plugin
   * @param pluginName related to Plugin.name()
   */
  void uninstall(String pluginName);

  /**
   * List all installed plugins
   * @return list of installed plugins
   */
  List<T> list();

  /**
   * Get a plugin by its name
   * @param pluginName related to Plugin.name()
   * @return plugin instance
   */
  T get(String pluginName);

  /**
   * Close the plugin manager and trigger all plugins to close if necessary
   */
  void close();
}
