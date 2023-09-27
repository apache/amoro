package com.netease.arctic.ams.api.plugin;

import java.util.List;

public interface PluginManager<T extends Plugin> {

  void install(String pluginName);

  void uninstall(String pluginName);

  List<T> list();

  T get(String pluginName);

  void close();
}
