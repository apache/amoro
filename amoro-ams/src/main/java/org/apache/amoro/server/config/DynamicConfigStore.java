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

package org.apache.amoro.server.config;

import org.apache.amoro.server.persistence.DynamicConfigEntry;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.DynamicConfigMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence helper for dynamic configuration.
 *
 * <p>This class is a thin wrapper around {@link DynamicConfigMapper} providing convenient methods
 * to load configuration overrides for the AMS service and individual plugins.
 */
public class DynamicConfigStore extends PersistentBase {

  private static final String AMS_CONF_GROUP = "AMS";

  /** Load all key/value overrides for AMS service level (conf_group = 'AMS'). */
  public Map<String, String> loadServerOverrides() {
    List<DynamicConfigEntry> entries =
        getAs(DynamicConfigMapper.class, DynamicConfigMapper::selectServerConfigs);
    Map<String, String> overrides = new HashMap<>();
    for (DynamicConfigEntry entry : entries) {
      if (entry.getConfKey() != null && entry.getConfValue() != null) {
        overrides.put(entry.getConfKey(), entry.getConfValue());
      }
    }
    return overrides;
  }

  /**
   * Load all key/value overrides for a specific plugin.
   *
   * @param category plugin category, e.g. {@code "metric-reporters"} or {@code "event-listeners"}
   * @param pluginName plugin configuration name
   */
  public Map<String, String> loadPluginOverrides(String category, String pluginName) {
    String confGroup = buildPluginConfGroup(category);
    List<DynamicConfigEntry> entries =
        getAs(DynamicConfigMapper.class, m -> m.selectPluginConfigs(confGroup, pluginName));
    Map<String, String> overrides = new HashMap<>();
    for (DynamicConfigEntry entry : entries) {
      if (entry.getConfKey() != null && entry.getConfValue() != null) {
        overrides.put(entry.getConfKey(), entry.getConfValue());
      }
    }
    return overrides;
  }

  private String buildPluginConfGroup(String category) {
    if (category == null || category.isEmpty()) {
      return "PLUGIN";
    }
    return "PLUGIN_" + category;
  }
}
