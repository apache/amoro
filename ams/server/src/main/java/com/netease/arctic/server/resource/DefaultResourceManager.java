package com.netease.arctic.server.resource;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.ams.api.resource.ResourceManager;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.ResourceMapper;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.table.TableProperties;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultResourceManager extends StatedPersistentBase implements ResourceManager {

  private final TableService tableService;

  public DefaultResourceManager(TableService tableService) {
    this.tableService = tableService;
  }

  public void init(List<ResourceGroup> groups) {
    Set<String> oldGroups = listResourceGroups()
        .stream()
        .map(ResourceGroup::getName)
        .collect(Collectors.toSet());
    groups.forEach(group -> {
      if (oldGroups.contains(group.getName())) {
        updateResourceGroup(group);
      } else {
        createResourceGroup(group);
      }
    });
  }

  @Override
  public void createResourceGroup(ResourceGroup resourceGroup) {
    doAs(ResourceMapper.class, mapper -> mapper.insertResourceGroup(resourceGroup));
  }

  @Override
  public void deleteResourceGroup(String groupName) {
    if (canDeleteResourceGroup(groupName)) {
      doAs(ResourceMapper.class, mapper -> mapper.deleteResourceGroup(groupName));
    } else {
      throw new RuntimeException("Cannot delete resource group: " + groupName + " because it is in use.");
    }
  }

  public boolean canDeleteResourceGroup(String name) {
    for (CatalogMeta catalogMeta : tableService.listCatalogMetas()) {
      if (catalogMeta.getCatalogProperties() != null &&
          catalogMeta.getCatalogProperties()
              .getOrDefault(TableProperties.SELF_OPTIMIZING_GROUP, TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT)
              .equals(name)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void updateResourceGroup(ResourceGroup resourceGroup) {
    doAs(ResourceMapper.class, mapper -> mapper.updateResourceGroup(resourceGroup));
  }

  @Override
  public void createResource(Resource resource) {
    doAs(ResourceMapper.class, mapper -> mapper.insertResource(resource));
  }

  @Override
  public void deleteResource(String resourceId) {
    doAs(ResourceMapper.class, mapper -> mapper.deleteResource(resourceId));
  }

  @Override
  public List<ResourceGroup> listResourceGroups() {
    return getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups);
  }

  @Override
  public List<ResourceGroup> listResourceGroups(String containerName) {
    return getAs(ResourceMapper.class, ResourceMapper::selectResourceGroups).stream()
        .filter(group -> group.getContainer().equals(containerName))
        .collect(Collectors.toList());
  }

  @Override
  public ResourceGroup getResourceGroup(String groupName) {
    return getAs(ResourceMapper.class, mapper -> mapper.selectResourceGroup(groupName));
  }

  @Override
  public List<Resource> listResourcesByGroup(String groupName) {
    return getAs(ResourceMapper.class, mapper -> mapper.selectResourcesByGroup(groupName));
  }

  @Override
  public Resource getResource(String resourceId) {
    return getAs(ResourceMapper.class, mapper -> mapper.selectResource(resourceId));
  }
}
