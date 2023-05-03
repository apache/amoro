package com.netease.arctic.ams.server.resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.resource.ResourceContainer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceContainers {
  private static final Map<String, ContainerWrapper> globalContainers = Maps.newHashMap();
  private static volatile boolean isInitialized = false;

  public static void init(List<ContainerMetadata> containerList) {
    Preconditions.checkState(!isInitialized, "OptimizerContainers has been initialized");
    Preconditions.checkNotNull(containerList, "containerList is null");
    containerList.forEach(metadata ->
        globalContainers.put(metadata.getName(), new ContainerWrapper(metadata)));
    isInitialized = true;
  }

  public static ResourceContainer get(String name) {
    checkInitialized();
    return Optional.ofNullable(globalContainers.get(name))
        .map(ContainerWrapper::getContainer)
        .orElseThrow(() -> new IllegalArgumentException("ResourceContainer not found: " + name));
  }

  public static List<ContainerMetadata> getMetadatas() {
    Preconditions.checkState(isInitialized, "OptimizerContainers not been initialized");
    return globalContainers.values()
        .stream()
        .map(ContainerWrapper::getMetadata)
        .collect(Collectors.toList());
  }

  private static void checkInitialized() {
    Preconditions.checkState(isInitialized, "OptimizerContainers not been initialized");
  }

  public static boolean contains(String name) {
    return get(name) != null;
  }

  private static class ContainerWrapper {
    private final ResourceContainer container;
    private final ContainerMetadata metadata;

    public ContainerWrapper(ContainerMetadata metadata) {
      this.metadata = metadata;
      this.container = loadResourceContainer(metadata.getImplClass());
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
