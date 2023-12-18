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

package com.netease.arctic.ams.api.resource;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.Constants;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceGroup {
  private String name;
  private Set<Action> actions;
  private String container;
  private Map<String, String> properties;

  protected ResourceGroup() {}

  private ResourceGroup(String name, Set<Action> actions, String container) {
    this.name = name;
    this.actions = actions;
    this.container = container;
  }

  public String getName() {
    return name;
  }

  public Set<Action> getActions() {
    return actions;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  protected void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getContainer() {
    return container;
  }

  // generate inner builder class, use addProperties instead of set
  public static class Builder {
    private final String name;
    private final String container;
    private final Set<Action> actions = new HashSet<>();
    private final Map<String, String> properties = new HashMap<>();

    public Builder(String name, String container) {
      Preconditions.checkArgument(
          name != null && container != null,
          "Resource group name and container name can not be null");
      this.name = name;
      this.container = container;
    }

    public Builder(String name) {
      Preconditions.checkArgument(name != null, "Resource group name can not be null");
      this.name = name;
      this.container = Constants.EXTERNAL_RESOURCE_CONTAINER;
    }

    public String getName() {
      return name;
    }

    public String getContainer() {
      return container;
    }

    public Builder addProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
    }

    public Builder addAction(Action action) {
      actions.add(action);
      return this;
    }

    public Builder addProperties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    public ResourceGroup build() {
      ResourceGroup resourceGroup = new ResourceGroup(name, actions, container);
      resourceGroup.setProperties(properties);
      return resourceGroup;
    }
  }
}
