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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.client.ClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.table.TableProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestLasCatalogSynchronizer {

  @Test
  public void testParseNamespaceUsesFirstSeparator() {
    Assertions.assertEquals(
        "tenant-a", LasCatalogSynchronizer.parseNamespace("tenant-a@las@archive").orElse(null));
    Assertions.assertFalse(LasCatalogSynchronizer.parseNamespace("las").isPresent());
    Assertions.assertFalse(LasCatalogSynchronizer.parseNamespace("@las").isPresent());
    Assertions.assertFalse(LasCatalogSynchronizer.parseNamespace("tenant-a@").isPresent());
    Assertions.assertFalse(
        LasCatalogSynchronizer.parseNamespace(
                "tenant-a@" + String.join("", Collections.nCopies(64, "x")))
            .isPresent());
    Assertions.assertFalse(LasCatalogSynchronizer.parseNamespace(null).isPresent());
  }

  @Test
  public void testSyncUsesOneSnapshotAndProtectsManualAndFilteredCatalogs() throws Exception {
    HMSClient hmsSdk = mock(HMSClient.class);
    when(hmsSdk.getCatalogs())
        .thenReturn(
            Arrays.asList("tenant-a@las", "tenant-a@manual", "tenant-b@keep", "invalid-catalog"));
    CatalogManager catalogManager = mock(CatalogManager.class);
    when(catalogManager.listNamespaces()).thenReturn(Collections.singletonList("tenant-a"));
    CatalogMeta manual = catalog("tenant-a@manual", "tenant-a", false);
    CatalogMeta removedFromAllowlist = catalog("tenant-b@keep", "tenant-b", true);
    CatalogMeta stale = catalog("tenant-z@gone", "tenant-z", true);
    when(catalogManager.listCatalogMetas())
        .thenReturn(Arrays.asList(manual, removedFromAllowlist, stale));

    LasCatalogSynchronizer synchronizer = synchronizer(hmsSdk, catalogManager);
    synchronizer.syncOnce();

    verify(hmsSdk, times(1)).getCatalogs();
    ArgumentCaptor<CatalogMeta> created = ArgumentCaptor.forClass(CatalogMeta.class);
    verify(catalogManager).createCatalog(created.capture());
    Assertions.assertEquals("tenant-a@las", created.getValue().getCatalogName());
    Assertions.assertEquals(
        CatalogMetaProperties.CATALOG_TYPE_HIVE, created.getValue().getCatalogType());
    Assertions.assertEquals(
        "tenant-a", created.getValue().getCatalogProperties().get(CatalogMetaProperties.NAMESPACE));
    Assertions.assertEquals(
        "false",
        created
            .getValue()
            .getCatalogProperties()
            .get(
                CatalogMetaProperties.TABLE_PROPERTIES_PREFIX
                    + TableProperties.ENABLE_SELF_OPTIMIZING));
    Configuration hiveConfiguration =
        decodeConfiguration(
            created
                .getValue()
                .getStorageConfigs()
                .get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE));
    Assertions.assertEquals(
        "thrift://hms-service:9083", hiveConfiguration.get("hive.metastore.uris"));
    Assertions.assertEquals("tenant-a@las", hiveConfiguration.get("metastore.catalog.default"));
    Configuration tosConfiguration =
        decodeConfiguration(
            created
                .getValue()
                .getStorageConfigs()
                .get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE));
    Assertions.assertEquals(
        "https://tos-cn-beijing.volces.com", tosConfiguration.get("fs.tos.endpoint"));
    Assertions.assertEquals(
        "io.proton.tos.iam.AssumeIamRoleCredentialProvider",
        tosConfiguration.get("fs.tos.credentials.provider"));
    Assertions.assertEquals(
        "trn:iam::tenant-a:role/ServiceRoleForLAS",
        tosConfiguration.get("fs.tos.credential.sts.iam-role-trn"));
    verify(catalogManager).dropCatalog("tenant-z@gone");
    verify(catalogManager, never()).dropCatalog("tenant-b@keep");
    verify(catalogManager, never()).updateCatalog(manual);
  }

  @Test
  public void testManagedCatalogIsUpdatedIdempotently() throws Exception {
    HMSClient hmsSdk = mock(HMSClient.class);
    when(hmsSdk.getCatalogs()).thenReturn(Collections.singletonList("tenant-a@las"));
    CatalogManager catalogManager = mock(CatalogManager.class);
    when(catalogManager.listNamespaces()).thenReturn(Collections.singletonList("tenant-a"));
    CatalogMeta existing = catalog("tenant-a@las", "tenant-a", true);
    existing.getCatalogProperties().put("preserved", "value");
    when(catalogManager.listCatalogMetas()).thenReturn(Collections.singletonList(existing));

    LasCatalogSynchronizer synchronizer = synchronizer(hmsSdk, catalogManager);
    synchronizer.syncOnce();

    ArgumentCaptor<CatalogMeta> updated = ArgumentCaptor.forClass(CatalogMeta.class);
    verify(catalogManager).updateCatalog(updated.capture());
    Assertions.assertEquals("value", updated.getValue().getCatalogProperties().get("preserved"));
    Assertions.assertEquals(
        "ICEBERG,PAIMON",
        updated.getValue().getCatalogProperties().get(CatalogMetaProperties.TABLE_FORMATS));
    when(catalogManager.listCatalogMetas())
        .thenReturn(Collections.singletonList(updated.getValue()));
    synchronizer.syncOnce();
    verify(catalogManager, times(1)).updateCatalog(any());
    verify(catalogManager, never()).createCatalog(any());
    verify(catalogManager, never()).dropCatalog(any());
  }

  @Test
  public void testHmsFailureDoesNotMutateCatalogState() throws Exception {
    HMSClient hmsSdk = mock(HMSClient.class);
    when(hmsSdk.getCatalogs()).thenThrow(new TException("unavailable"));
    CatalogManager catalogManager = mock(CatalogManager.class);
    LasCatalogSynchronizer synchronizer = synchronizer(hmsSdk, catalogManager);

    Assertions.assertThrows(TException.class, synchronizer::syncOnce);
    verifyNoInteractions(catalogManager);
  }

  @Test
  public void testInvalidHmsSnapshotDoesNotMutateCatalogState() throws Exception {
    HMSClient hmsSdk = mock(HMSClient.class);
    when(hmsSdk.getCatalogs()).thenReturn(Arrays.asList("tenant-a@las", null));
    CatalogManager catalogManager = mock(CatalogManager.class);
    LasCatalogSynchronizer synchronizer = synchronizer(hmsSdk, catalogManager);

    Assertions.assertThrows(IllegalStateException.class, synchronizer::syncOnce);
    verifyNoInteractions(catalogManager);
  }

  @Test
  public void testFirstRunIsDelayedByOneFiveMinuteInterval() {
    HMSClient hmsSdk = mock(HMSClient.class);
    CatalogManager catalogManager = mock(CatalogManager.class);
    ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    LasIntegrationContext context =
        LasIntegrationContext.initialize(TestLasIntegrationContext.validConfigurations());
    LasCatalogSynchronizer synchronizer =
        new LasCatalogSynchronizer(
            context,
            new LasHmsClient(directPool(hmsSdk), ignored -> directPool(hmsSdk)),
            catalogManager,
            scheduler);

    synchronizer.start();
    synchronizer.start();

    verify(scheduler, times(1))
        .scheduleWithFixedDelay(
            any(Runnable.class), eq(300_000L), eq(300_000L), eq(TimeUnit.MILLISECONDS));
    verifyNoInteractions(hmsSdk);
  }

  private static LasCatalogSynchronizer synchronizer(
      HMSClient hmsSdk, CatalogManager catalogManager) {
    LasIntegrationContext context =
        LasIntegrationContext.initialize(TestLasIntegrationContext.validConfigurations());
    HMSClientPool pool = directPool(hmsSdk);
    return new LasCatalogSynchronizer(
        context, new LasHmsClient(pool, ignored -> pool), catalogManager);
  }

  private static CatalogMeta catalog(String name, String namespace, boolean managed) {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.NAMESPACE, namespace);
    if (managed) {
      properties.put(
          LasCatalogSynchronizer.CATALOG_SOURCE, LasCatalogSynchronizer.CATALOG_SOURCE_LAS_HMS);
    }
    return new CatalogMeta(
        name,
        CatalogMetaProperties.CATALOG_TYPE_HIVE,
        new HashMap<>(),
        new HashMap<>(),
        properties);
  }

  private static HMSClientPool directPool(HMSClient client) {
    return new HMSClientPool() {
      @Override
      public <R> R run(ClientPool.Action<R, HMSClient, TException> action) throws TException {
        return action.run(client);
      }

      @Override
      public <R> R run(ClientPool.Action<R, HMSClient, TException> action, boolean retry)
          throws TException {
        return action.run(client);
      }
    };
  }

  private static Configuration decodeConfiguration(String encoded) {
    Configuration configuration = new Configuration(false);
    configuration.addResource(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
    return configuration;
  }
}
