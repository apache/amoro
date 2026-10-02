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

package org.apache.amoro.server.dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.model.TableMeta;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestTableControllerTableList {

  private static final String CATALOG = "hive_catalog";
  private static final String DB = "db";

  private CatalogManager catalogManager;
  private ServerCatalog serverCatalog;
  private Context ctx;
  private TableController controller;

  @Test
  void managedAndUnmanagedHiveTablesAreSortedByTypeAndName() {
    setUp(CatalogMetaProperties.CATALOG_TYPE_HIVE);
    when(serverCatalog.listTables(DB))
        .thenReturn(
            Arrays.asList(
                table("paimon_a", TableFormat.PAIMON),
                table("iceberg_b", TableFormat.ICEBERG),
                table("arctic_b", TableFormat.MIXED_ICEBERG),
                table("arctic_a", TableFormat.MIXED_HIVE),
                table("iceberg_a", TableFormat.ICEBERG),
                // HMS also returns the managed table name "hive_a", so it must not
                // be listed twice (managed entry wins)
                table("hive_a", TableFormat.MIXED_ICEBERG)));
    when(ctx.queryParam("keywords")).thenReturn(null);

    try (MockedStatic<HiveTableUtil> hiveTableUtil = mockStatic(HiveTableUtil.class)) {
      hiveTableUtil
          .when(() -> HiveTableUtil.getAllHiveTables(any(HMSClientPool.class), eq(DB)))
          .thenReturn(Arrays.asList("hive_b", "hive_a"));
      controller.getTableList(ctx);
    }

    assertEquals(
        Arrays.asList(
            "arctic_a:ARCTIC",
            "arctic_b:ARCTIC",
            "hive_a:ARCTIC",
            "hive_b:HIVE",
            "iceberg_a:ICEBERG",
            "iceberg_b:ICEBERG",
            "paimon_a:PAIMON"),
        tableKeys());
  }

  @Test
  void keywordFilterKeepsFinalTypeAndNameOrder() {
    setUp(CatalogMetaProperties.CATALOG_TYPE_HIVE);
    when(serverCatalog.listTables(DB))
        .thenReturn(
            Arrays.asList(
                table("iceberg_b", TableFormat.ICEBERG), table("iceberg_a", TableFormat.ICEBERG)));
    when(ctx.queryParam("keywords")).thenReturn("a");

    try (MockedStatic<HiveTableUtil> hiveTableUtil = mockStatic(HiveTableUtil.class)) {
      hiveTableUtil
          .when(() -> HiveTableUtil.getAllHiveTables(any(HMSClientPool.class), eq(DB)))
          .thenReturn(Arrays.asList("hive_b", "hive_a"));
      controller.getTableList(ctx);
    }

    assertEquals(Arrays.asList("hive_a:HIVE", "iceberg_a:ICEBERG"), tableKeys());
  }

  @Test
  void nonHiveCatalogKeepsTypeAndNameOrderWithoutHmsAccess() {
    setUp("iceberg");
    when(serverCatalog.listTables(DB))
        .thenReturn(
            Arrays.asList(
                table("paimon_t", TableFormat.PAIMON),
                table("iceberg_t", TableFormat.ICEBERG),
                table("arctic_t", TableFormat.MIXED_HIVE),
                table("iceberg_a", TableFormat.ICEBERG)));
    when(ctx.queryParam("keywords")).thenReturn("");

    try (MockedStatic<HiveTableUtil> hiveTableUtil = mockStatic(HiveTableUtil.class)) {
      controller.getTableList(ctx);
      hiveTableUtil.verifyNoInteractions();
    }

    assertEquals(
        Arrays.asList(
            "arctic_t:ARCTIC", "iceberg_a:ICEBERG", "iceberg_t:ICEBERG", "paimon_t:PAIMON"),
        tableKeys());
  }

  private void setUp(String catalogType) {
    catalogManager = mock(CatalogManager.class);
    serverCatalog = mock(ServerCatalog.class);
    ctx = mock(Context.class);
    controller =
        new TableController(
            catalogManager,
            mock(TableManager.class),
            mock(ServerTableDescriptor.class),
            new Configurations());

    CatalogMeta catalogMeta = new CatalogMeta();
    catalogMeta.setCatalogType(catalogType);
    catalogMeta.setCatalogName(CATALOG);
    catalogMeta.setStorageConfigs(
        Collections.singletonMap(
            "storage.type", CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_LOCAL));
    catalogMeta.setCatalogProperties(Collections.emptyMap());

    when(catalogManager.getServerCatalog(CATALOG)).thenReturn(serverCatalog);
    when(serverCatalog.getMetadata()).thenReturn(catalogMeta);
    when(ctx.pathParam("catalog")).thenReturn(CATALOG);
    when(ctx.pathParam("db")).thenReturn(DB);
  }

  private static TableIDWithFormat table(String name, TableFormat format) {
    return TableIDWithFormat.of(TableIdentifier.of(CATALOG, DB, name), format);
  }

  private List<String> tableKeys() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(ctx).json(captor.capture());
    OkResponse<?> response = (OkResponse<?>) captor.getValue();
    @SuppressWarnings("unchecked")
    List<TableMeta> tables = (List<TableMeta>) response.getResult();
    return tables.stream()
        .map(table -> table.getName() + ":" + table.getType())
        .collect(Collectors.toList());
  }
}
