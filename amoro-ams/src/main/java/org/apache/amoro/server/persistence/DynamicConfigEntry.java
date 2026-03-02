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

package org.apache.amoro.server.persistence;

/**
 * POJO that maps records in {@code dynamic_conf} table.
 *
 * <p>The dynamic configuration table is shared by AMS service level configuration and all plugin
 * categories. Records are partitioned by {@code confGroup} and {@code pluginName}:
 *
 * <ul>
 *   <li>Service level overrides: {@code conf_group = 'AMS'}, {@code plugin_name IS NULL}.
 *   <li>Plugin level overrides: {@code conf_group = 'PLUGIN_' + pluginCategory}, {@code plugin_name
 *       = pluginName}.
 * </ul>
 */
public class DynamicConfigEntry {

  /** Configuration key. */
  private String confKey;

  /** Configuration value stored as plain string. */
  private String confValue;

  /** Logical configuration group, such as {@code AMS} or {@code PLUGIN_metric-reporters}. */
  private String confGroup;

  /**
   * Plugin identifier. Only meaningful when {@code confGroup} starts with {@code PLUGIN_}. For AMS
   * service level configuration this field is usually {@code null}.
   */
  private String pluginName;

  public String getConfKey() {
    return confKey;
  }

  public void setConfKey(String confKey) {
    this.confKey = confKey;
  }

  public String getConfValue() {
    return confValue;
  }

  public void setConfValue(String confValue) {
    this.confValue = confValue;
  }

  public String getConfGroup() {
    return confGroup;
  }

  public void setConfGroup(String confGroup) {
    this.confGroup = confGroup;
  }

  public String getPluginName() {
    return pluginName;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }
}
