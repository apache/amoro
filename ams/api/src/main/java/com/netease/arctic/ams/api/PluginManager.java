package com.netease.arctic.ams.api;

import java.util.List;

/**
 * Plugin manager interface for all pluggable services
 * @param <T> plugin type
 */
public interface PluginManager<T extends AmoroPlugin> {

  void install(String pluginName);

  void uninstall(String pluginName);

  List<T> list();

  T get(String pluginName);

  void close();
}
