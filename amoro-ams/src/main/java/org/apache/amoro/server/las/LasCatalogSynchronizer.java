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

package org.apache.amoro.server.las;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.table.TableProperties;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/** Periodically projects allowlisted HMS3 catalogs into AMS CatalogMeta records. */
public final class LasCatalogSynchronizer implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(LasCatalogSynchronizer.class);

  static final String CATALOG_SOURCE = "catalog.source";
  static final String CATALOG_SOURCE_LAS_HMS = "las-hms-sync";

  private static final String HIVE_METASTORE_URIS = "hive.metastore.uris";
  private static final String METASTORE_CATALOG_DEFAULT = "metastore.catalog.default";
  private static final int MAX_CATALOG_NAME_LENGTH = 64;

  private final LasIntegrationContext context;
  private final LasHmsClient hmsClient;
  private final CatalogManager catalogManager;
  private final ScheduledExecutorService scheduler;
  private final long intervalMillis;
  private final AtomicBoolean started = new AtomicBoolean(false);

  public LasCatalogSynchronizer(
      LasIntegrationContext context, LasHmsClient hmsClient, CatalogManager catalogManager) {
    this(
        context,
        hmsClient,
        catalogManager,
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("las-hms-catalog-sync-%d")
                .build()));
  }

  LasCatalogSynchronizer(
      LasIntegrationContext context,
      LasHmsClient hmsClient,
      CatalogManager catalogManager,
      ScheduledExecutorService scheduler) {
    this.context = Objects.requireNonNull(context, "context");
    this.hmsClient = Objects.requireNonNull(hmsClient, "hmsClient");
    this.catalogManager = Objects.requireNonNull(catalogManager, "catalogManager");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.intervalMillis = context.catalogSyncInterval().toMillis();
  }

  /** Starts synchronization after one full interval; no HMS call is made during AMS startup. */
  public void start() {
    if (started.compareAndSet(false, true)) {
      scheduler.scheduleWithFixedDelay(
          this::syncSafely, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
      LOG.info("LAS HMS catalog synchronization scheduled every {} ms", intervalMillis);
    }
  }

  void syncOnce() throws Exception {
    List<String> discoveredCatalogs = hmsClient.listCatalogs();
    if (discoveredCatalogs == null || discoveredCatalogs.stream().anyMatch(Objects::isNull)) {
      throw new IllegalStateException("HMS getCatalogs returned an invalid snapshot");
    }

    Set<String> rawCatalogNames = new HashSet<>(discoveredCatalogs);
    Set<String> allowlistedNamespaces = new HashSet<>(catalogManager.listNamespaces());
    Map<String, CatalogMeta> existingCatalogs =
        catalogManager.listCatalogMetas().stream()
            .collect(Collectors.toMap(CatalogMeta::getCatalogName, catalog -> catalog));

    int created = 0;
    int updated = 0;
    int removed = 0;
    for (String physicalCatalogName : rawCatalogNames) {
      Optional<String> namespace = parseNamespace(physicalCatalogName);
      if (!namespace.isPresent() || !allowlistedNamespaces.contains(namespace.get())) {
        continue;
      }

      CatalogMeta existing = existingCatalogs.get(physicalCatalogName);
      if (existing == null) {
        catalogManager.createCatalog(createCatalogMeta(physicalCatalogName, namespace.get()));
        created++;
      } else if (isManagedByThisSynchronizer(existing)) {
        CatalogMeta refreshed = mergeCatalogMeta(existing, physicalCatalogName, namespace.get());
        if (!existing.equals(refreshed)) {
          catalogManager.updateCatalog(refreshed);
          updated++;
        }
      } else {
        LOG.warn(
            "Skip HMS catalog {} because an unmanaged catalog has the same name",
            physicalCatalogName);
      }
    }

    for (CatalogMeta existing : existingCatalogs.values()) {
      if (isManagedByThisSynchronizer(existing)
          && !rawCatalogNames.contains(existing.getCatalogName())) {
        catalogManager.dropCatalog(existing.getCatalogName());
        removed++;
      }
    }

    LOG.info(
        "LAS HMS catalog synchronization completed: discovered={}, allowlistedNamespaces={}, created={}, updated={}, removed={}",
        rawCatalogNames.size(),
        allowlistedNamespaces.size(),
        created,
        updated,
        removed);
  }

  static Optional<String> parseNamespace(String catalogName) {
    if (catalogName == null || catalogName.length() > MAX_CATALOG_NAME_LENGTH) {
      return Optional.empty();
    }
    int separator = catalogName.indexOf('@');
    if (separator <= 0 || separator == catalogName.length() - 1) {
      return Optional.empty();
    }
    return Optional.of(catalogName.substring(0, separator));
  }

  private CatalogMeta mergeCatalogMeta(
      CatalogMeta existing, String physicalCatalogName, String namespace) {
    CatalogMeta refreshed = createCatalogMeta(physicalCatalogName, namespace);
    Map<String, String> properties = new HashMap<>(existing.getCatalogProperties());
    properties.putAll(refreshed.getCatalogProperties());
    refreshed.setCatalogProperties(properties);
    return refreshed;
  }

  private CatalogMeta createCatalogMeta(String physicalCatalogName, String namespace) {
    CatalogMeta catalogMeta = new CatalogMeta();
    catalogMeta.setCatalogName(physicalCatalogName);
    catalogMeta.setCatalogType(CatalogMetaProperties.CATALOG_TYPE_HIVE);

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(
        CatalogMetaProperties.TABLE_FORMATS,
        TableFormat.ICEBERG.name() + "," + TableFormat.PAIMON.name());
    catalogProperties.put(CatalogMetaProperties.NAMESPACE, namespace);
    catalogProperties.put(CATALOG_SOURCE, CATALOG_SOURCE_LAS_HMS);
    catalogProperties.put(
        CatalogMetaProperties.TABLE_PROPERTIES_PREFIX + TableProperties.ENABLE_SELF_OPTIMIZING,
        "false");
    catalogMeta.setCatalogProperties(catalogProperties);

    Map<String, String> storageConfigs = new HashMap<>();
    storageConfigs.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HADOOP);
    storageConfigs.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE,
        encodeConfiguration(createHiveConfiguration(physicalCatalogName)));
    storageConfigs.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE,
        encodeConfiguration(createTosConfiguration(namespace)));
    storageConfigs.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE,
        encodeConfiguration(new Configuration(false)));
    catalogMeta.setStorageConfigs(storageConfigs);
    catalogMeta.setAuthConfigs(Collections.emptyMap());
    return catalogMeta;
  }

  private Configuration createHiveConfiguration(String physicalCatalogName) {
    Configuration configuration = new Configuration(false);
    configuration.set(HIVE_METASTORE_URIS, context.hmsUri().toString());
    configuration.set(METASTORE_CATALOG_DEFAULT, physicalCatalogName);
    return configuration;
  }

  private Configuration createTosConfiguration(String namespace) {
    Configuration configuration = new Configuration(false);
    configuration.set("fs.AbstractFileSystem.tos.impl", "io.proton.fs.ProtonFS");
    configuration.set("fs.tos.impl", "io.proton.fs.ProtonFileSystem");
    configuration.set("fs.tos.endpoint", context.tosEndpoint().toString());
    configuration.set("proton.cache.enable", "false");
    configuration.set(
        "mapreduce.outputcommitter.factory.class", "io.proton.commit.CommitterFactory");
    configuration.set(
        "fs.tos.credentials.provider", "io.proton.tos.iam.AssumeIamRoleCredentialProvider");
    configuration.set("fs.volc.openapi.host", context.iamEndpoint().getAuthority());
    configuration.set("fs.volc.openapi.region", context.region());
    configuration.set("fs.tos.http.maxConnections", "1024");
    configuration.set(
        "fs.tos.credential.sts.iam-role-trn",
        String.format("trn:iam::%s:role/%s", namespace, context.iamDataRoleName()));
    configuration.set(
        "fs.tos.credential.sts.token.time-to-live",
        String.valueOf(context.iamAssumeRoleTtl().getSeconds()));
    return configuration;
  }

  private static String encodeConfiguration(Configuration configuration) {
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      configuration.writeXml(output);
      return Base64.getEncoder().encodeToString(output.toByteArray());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static boolean isManagedByThisSynchronizer(CatalogMeta catalogMeta) {
    return catalogMeta.getCatalogProperties() != null
        && CATALOG_SOURCE_LAS_HMS.equals(catalogMeta.getCatalogProperties().get(CATALOG_SOURCE));
  }

  private void syncSafely() {
    try {
      syncOnce();
    } catch (Throwable t) {
      LOG.error("LAS HMS catalog synchronization failed", t);
    }
  }

  @Override
  public void close() {
    scheduler.shutdownNow();
  }
}
