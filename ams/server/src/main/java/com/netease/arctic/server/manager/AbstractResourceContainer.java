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

import com.netease.arctic.ams.api.OptimizerProperties;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.ams.api.resource.ResourceContainer;
import com.netease.arctic.ams.api.resource.ResourceStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractResourceContainer implements ResourceContainer {
  private String containerName;
  private Map<String, String> containerProperties;

  protected String amsHome;
  protected String amsOptimizingUrl;

  @Override
  public String name() {
    return containerName;
  }

  @Override
  public void init(String name, Map<String, String> containerProperties) {
    this.containerName = name;
    this.containerProperties = containerProperties;
    this.amsHome = containerProperties.get(OptimizerProperties.AMS_HOME);
    this.amsOptimizingUrl = containerProperties.get(OptimizerProperties.AMS_OPTIMIZER_URI);
    Preconditions.checkNotNull(
        this.amsHome, "Container Property: %s is required", OptimizerProperties.AMS_HOME);
    Preconditions.checkNotNull(
        this.amsOptimizingUrl,
        "Container Property: %s is required",
        OptimizerProperties.AMS_OPTIMIZER_URI);
  }

  @Override
  public void requestResource(Resource resource) {
    Map<String, String> startupStats = doScaleOut(resource);
    resource.getProperties().putAll(startupStats);
  }

  protected abstract Map<String, String> doScaleOut(Resource resource);

  protected String getOptimizingUri(Map<String, String> resourceProperties) {
    String optimizingUrl =
        resourceProperties.getOrDefault(OptimizerProperties.AMS_OPTIMIZER_URI, null);
    if (StringUtils.isNotEmpty(optimizingUrl)) {
      return optimizingUrl;
    }
    return amsOptimizingUrl;
  }

  public Map<String, String> getContainerProperties() {
    return containerProperties;
  }

  protected String buildOptimizerStartupArgsString(Resource resource) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append(" -a ")
        .append(getOptimizingUri(resource.getProperties()))
        .append(" -p ")
        .append(resource.getThreadCount())
        .append(" -g ")
        .append(resource.getGroupName());
    if (resource.getProperties().containsKey(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL)) {
      stringBuilder
          .append(" -hb ")
          .append(resource.getProperties().get(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL));
    }
    if (org.apache.iceberg.util.PropertyUtil.propertyAsBoolean(
        resource.getProperties(),
        OptimizerProperties.OPTIMIZER_EXTEND_DISK_STORAGE,
        OptimizerProperties.OPTIMIZER_EXTEND_DISK_STORAGE_DEFAULT)) {
      stringBuilder
          .append(" -eds -dsp ")
          .append(resource.getRequiredProperty(OptimizerProperties.OPTIMIZER_DISK_STORAGE_PATH));
      if (resource.getProperties().containsKey(OptimizerProperties.OPTIMIZER_MEMORY_STORAGE_SIZE)) {
        stringBuilder
            .append(" -msz ")
            .append(
                resource.getProperties().get(OptimizerProperties.OPTIMIZER_MEMORY_STORAGE_SIZE));
      }
    }
    if (StringUtils.isNotEmpty(resource.getResourceId())) {
      stringBuilder.append(" -id ").append(resource.getResourceId());
    }
    return stringBuilder.toString();
  }

  protected List<String> exportSystemProperties() throws IOException {
    List<String> cmds = new ArrayList<>();
    if (containerProperties != null) {
      for (Map.Entry<String, String> entry : containerProperties.entrySet()) {
        if (entry.getKey().startsWith(OptimizerProperties.EXPORT_PROPERTY_PREFIX)) {
          String exportPropertyName =
              entry.getKey().substring(OptimizerProperties.EXPORT_PROPERTY_PREFIX.length());
          String exportValue = entry.getValue();
          cmds.add(String.format("export %s=%s", exportPropertyName, exportValue));
        }
      }
    }
    return cmds;
  }

  @Override
  public ResourceStatus getStatus(String resourceId) {
    return null;
  }
}
