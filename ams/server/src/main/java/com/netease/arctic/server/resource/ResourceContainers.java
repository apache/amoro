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
import com.netease.arctic.ams.api.resource.ResourceContainer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceContainers {
  public static final String EXTERNAL_CONTAINER_NAME = "external";
  private static final Map<String, ContainerWrapper> globalContainers = Maps.newHashMap();
  private static volatile boolean isInitialized = false;

  static {
    ContainerMetadata metadata = new ContainerMetadata(EXTERNAL_CONTAINER_NAME, "");
    ContainerWrapper externalContainer = new ContainerWrapper(metadata, null);
    globalContainers.put(EXTERNAL_CONTAINER_NAME, externalContainer);
  }

  public static void init(List<ContainerMetadata> containerList) {
    Preconditions.checkState(!isInitialized, "OptimizerContainers has been initialized");
    Preconditions.checkNotNull(containerList, "containerList is null");
    containerList.forEach(
        metadata -> globalContainers.put(metadata.getName(), new ContainerWrapper(metadata)));
    isInitialized = true;
  }

  public static ResourceContainer get(String name) {
    checkInitialized();
    return Optional.ofNullable(globalContainers.get(name))
        .map(ContainerWrapper::getContainer)
        .orElseThrow(() -> new IllegalArgumentException("ResourceContainer not found: " + name));
  }

  public static List<ContainerMetadata> getMetadataList() {
    Preconditions.checkState(isInitialized, "OptimizerContainers not been initialized");
    return globalContainers.values().stream()
        .map(ContainerWrapper::getMetadata)
        .collect(Collectors.toList());
  }

  private static void checkInitialized() {
    Preconditions.checkState(isInitialized, "OptimizerContainers not been initialized");
  }

  public static boolean contains(String name) {
    checkInitialized();
    return globalContainers.containsKey(name);
  }

  private static class ContainerWrapper {
    private final ResourceContainer container;
    private final ContainerMetadata metadata;

    public ContainerWrapper(ContainerMetadata metadata) {
      this.metadata = metadata;
      this.container = loadResourceContainer(metadata.getImplClass());
    }

    ContainerWrapper(ContainerMetadata metadata, ResourceContainer container) {
      this.metadata = metadata;
      this.container = container;
    }

    public ResourceContainer getContainer() {
      return container;
    }

    public ContainerMetadata getMetadata() {
      return metadata;
    }

    private ResourceContainer loadResourceContainer(String implClass) {
      try {
        Class<?> clazz = Class.forName(implClass);
        ResourceContainer resourceContainer = (ResourceContainer) clazz.newInstance();
        resourceContainer.init(metadata.getName(), metadata.getProperties());
        return resourceContainer;
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        throw new IllegalStateException("can not init container " + implClass, e);
      }
    }
  }
}
