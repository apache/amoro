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

package org.apache.amoro.server.catalog;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.AlreadyExistsException;
import org.apache.amoro.exception.IllegalMetadataException;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.shade.guava32.com.google.common.cache.CacheBuilder;
import org.apache.amoro.shade.guava32.com.google.common.cache.CacheLoader;
import org.apache.amoro.shade.guava32.com.google.common.cache.LoadingCache;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DefaultCatalogManager extends PersistentBase implements CatalogManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCatalogManager.class);
  protected final Configurations serverConfiguration;
  private final LoadingCache<String, Optional<CatalogMeta>> metaCache;

  private final Map<String, ServerCatalog> serverCatalogMap = Maps.newConcurrentMap();

  public DefaultCatalogManager(Configurations serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
    Duration cacheTtl =
        serverConfiguration.get(AmoroManagementConf.CATALOG_META_CACHE_EXPIRATION_INTERVAL);
    metaCache =
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(cacheTtl)
            .build(
                new CacheLoader<String, Optional<CatalogMeta>>() {
                  @Override
                  public @NotNull Optional<CatalogMeta> load(@NotNull String key) throws Exception {
                    return Optional.ofNullable(
                        getAs(CatalogMetaMapper.class, mapper -> mapper.getCatalog(key)));
                  }
                });

    listCatalogMetas()
        .forEach(
            c -> {
              ServerCatalog serverCatalog =
                  CatalogBuilder.buildServerCatalog(c, serverConfiguration);
              serverCatalogMap.put(c.getCatalogName(), serverCatalog);
              metaCache.put(c.getCatalogName(), Optional.of(c));
              LOG.info("Load catalog {}, type:{}", c.getCatalogName(), c.getCatalogType());
            });
    LOG.info("DefaultCatalogManager initialized, total catalogs: {}", serverCatalogMap.size());
  }

  @Override
  public List<CatalogMeta> listCatalogMetas() {
    return getAs(CatalogMetaMapper.class, CatalogMetaMapper::getCatalogs).stream()
        .peek(c -> metaCache.put(c.getCatalogName(), Optional.of(c)))
        .collect(Collectors.toList());
  }

  @Override
  public CatalogMeta getCatalogMeta(String catalogName) {
    return getCatalogMetaOptional(catalogName)
        .orElseThrow(() -> new ObjectNotExistsException("Catalog " + catalogName));
  }

  private Optional<CatalogMeta> getCatalogMetaOptional(String catalogName) {
    try {
      return metaCache.get(catalogName);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean catalogExist(String catalogName) {
    return getCatalogMetaOptional(catalogName).isPresent();
  }

  @Override
  public ServerCatalog getServerCatalog(String catalogName) {
    Optional<CatalogMeta> catalogMeta = getCatalogMetaOptional(catalogName);
    if (!catalogMeta.isPresent()) {
      // remove if catalog is deleted
      disposeCatalog(catalogName);
      throw new ObjectNotExistsException("Catalog " + catalogName);
    }
    ServerCatalog serverCatalog =
        serverCatalogMap.computeIfAbsent(
            catalogName,
            n -> CatalogBuilder.buildServerCatalog(catalogMeta.get(), serverConfiguration));
    serverCatalog.reload(catalogMeta.get());
    return serverCatalog;
  }

  @Override
  public InternalCatalog getInternalCatalog(String catalogName) {
    ServerCatalog serverCatalog = getServerCatalog(catalogName);
    if (serverCatalog == null) {
      throw new ObjectNotExistsException("Catalog " + catalogName);
    }
    if (serverCatalog.isInternal()) {
      return (InternalCatalog) serverCatalog;
    }
    throw new ObjectNotExistsException("Catalog " + catalogName + " is not internal catalog");
  }

  @Override
  public List<ExternalCatalog> getExternalCatalogs() {
    return listCatalogMetas().stream()
        .filter(c -> !isInternal(c))
        .map(
            c -> {
              ServerCatalog serverCatalog =
                  serverCatalogMap.computeIfAbsent(
                      c.getCatalogName(),
                      n -> CatalogBuilder.buildServerCatalog(c, serverConfiguration));
              serverCatalog.reload(c);
              return serverCatalog;
            })
        .map(c -> (ExternalCatalog) c)
        .collect(Collectors.toList());
  }

  @Override
  public void createCatalog(CatalogMeta catalogMeta) {
    if (catalogExist(catalogMeta.getCatalogName())) {
      throw new AlreadyExistsException("Catalog " + catalogMeta.getCatalogName());
    }
    // Build to make sure the catalog is valid
    ServerCatalog catalog = CatalogBuilder.buildServerCatalog(catalogMeta, serverConfiguration);
    doAs(CatalogMetaMapper.class, mapper -> mapper.insertCatalog(catalog.getMetadata()));
    disposeCatalog(catalogMeta.getCatalogName());
    serverCatalogMap.put(catalogMeta.getCatalogName(), catalog);
    LOG.info(
        "Create catalog {}, type:{}", catalogMeta.getCatalogName(), catalogMeta.getCatalogType());
  }

  @Override
  public void dropCatalog(String catalogName) {
    doAs(
        CatalogMetaMapper.class,
        mapper -> {
          CatalogMeta meta = mapper.getCatalog(catalogName);
          if (isInternal(meta)) {
            int dbCount = mapper.selectDatabaseCount(catalogName);
            int tblCount = mapper.selectTableCount(catalogName);
            if (dbCount > 0 || tblCount > 0) {
              throw new IllegalMetadataException(
                  "Cannot drop internal catalog with databases or tables");
            }
          }
          mapper.deleteCatalog(catalogName);
          metaCache.invalidate(catalogName);
        });

    disposeCatalog(catalogName);
  }

  @Override
  public void updateCatalog(CatalogMeta catalogMeta) {
    ServerCatalog catalog = getServerCatalog(catalogMeta.getCatalogName());
    validateCatalogUpdate(catalog.getMetadata(), catalogMeta);

    metaCache.invalidate(catalogMeta.getCatalogName());
    catalog.updateMetadata(catalogMeta);
    LOG.info("Update catalog metadata: {}", catalogMeta.getCatalogName());
  }

  private void validateCatalogUpdate(CatalogMeta oldMeta, CatalogMeta newMeta) {
    if (!oldMeta.getCatalogType().equals(newMeta.getCatalogType())) {
      throw new IllegalMetadataException("Cannot update catalog type");
    }
  }

  private void disposeCatalog(String name) {
    serverCatalogMap.computeIfPresent(
        name,
        (n, c) -> {
          LOG.info("Dispose catalog: {}", n);
          c.dispose();
          return null;
        });
    metaCache.invalidate(name);
  }

  private static boolean isInternal(CatalogMeta meta) {
    return CatalogMetaProperties.CATALOG_TYPE_AMS.equalsIgnoreCase(meta.getCatalogType());
  }
}
