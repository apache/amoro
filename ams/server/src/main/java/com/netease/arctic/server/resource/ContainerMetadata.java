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

package com.netease.arctic.server.resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.Map;

public class ContainerMetadata {
  private final String name;
  private final String implClass;
  private Map<String, String> properties;

  public ContainerMetadata(String name, String implClass) {
    Preconditions.checkArgument(
        name != null && implClass != null,
        "Resource container name and implementation class can not be null");
    this.name = name;
    this.implClass = implClass;
    properties = Maps.newHashMap();
  }

  public String getName() {
    return name;
  }

  public String getImplClass() {
    return implClass;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
