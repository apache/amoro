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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/** Configuration of a plugin. */
public class PluginConfiguration {
  private static final String NAME = "name";
  private static final String ENABLED = "enabled";
  private static final String PROPERTIES = "properties";
  private final String name;
  private final boolean enabled;
  private final Map<String, String> properties;

  public PluginConfiguration(String name, boolean enabled, Map<String, String> properties) {
    this.name = name;
    this.enabled = enabled;
    this.properties = properties;
  }

  public static PluginConfiguration fromJSONObject(JSONObject configOptions) {
    Preconditions.checkArgument(configOptions.containsKey(NAME), "plugin name is required");
    String name = configOptions.get(NAME).toString();
    boolean enabled = Optional.ofNullable(configOptions.getBoolean(ENABLED)).orElse(true);
    Map<String, String> props =
        configOptions.getObject(PROPERTIES, new TypeReference<Map<String, String>>() {});
    if (props == null) {
      props = ImmutableMap.of();
    }
    return new PluginConfiguration(name, enabled, props);
  }

  /** @return Plugin name. */
  public String getName() {
    return name;
  }

  /** @return True if plugin will be installed. */
  public boolean isEnabled() {
    return enabled;
  }

  /** @return Plugin installation properties. */
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(properties);
  }
}
