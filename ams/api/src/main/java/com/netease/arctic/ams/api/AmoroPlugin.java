package com.netease.arctic.ams.api;

/**
 * Plugin interface for all pluggable services
 * including MetricEmitter, TableProcessFactory, etc.
 */
public interface AmoroPlugin {

  /**
   * @return plugin name
   */
  String name();
}
