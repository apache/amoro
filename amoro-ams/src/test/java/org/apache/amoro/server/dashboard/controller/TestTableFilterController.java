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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TestTableFilterController {

  private CatalogManager manager;
  private ServerCatalog catalog;
  private Context ctx;
  private TableController controller;
  private CatalogMeta metadata;

  @BeforeEach
  public void setUp() {
    manager = mock(CatalogManager.class);
    catalog = mock(ServerCatalog.class);
    ctx = mock(Context.class);
    controller = new TableController(manager, null, null, null);
    metadata = new CatalogMeta();
    metadata.setCatalogProperties(new HashMap<>());
    when(ctx.pathParam("catalog")).thenReturn("test_catalog");
    when(ctx.pathParam("db")).thenReturn("db");
    when(manager.getServerCatalog("test_catalog")).thenReturn(catalog);
    when(catalog.getMetadata()).thenReturn(metadata);
  }

  @Test
  public void testDatabaseListBypassesCatalogFilters() {
    when(catalog.listDatabasesWithoutFilter()).thenReturn(Arrays.asList("db", "hidden_db"));
    controller.getTableFilterDatabaseList(ctx);
    assertEquals(Arrays.asList("db", "hidden_db"), result());
    verify(catalog, never()).listDatabases();
  }

  @Test
  public void testTableListIsUnfilteredSortedAndDeduplicated() {
    when(catalog.listTablesWithoutFilter("db"))
        .thenReturn(Arrays.asList(table("db", "b"), table("db", "a"), table("db", "a")));
    controller.getTableFilterTableList(ctx);
    assertEquals(Arrays.asList("a", "b"), result());
    verify(catalog, never()).listTables("db");
  }

  @Test
  public void testMultipleJavaRegexesMatchFullNames() {
    metadata.putToCatalogProperties(
        CatalogMetaProperties.KEY_TABLE_FILTER, "(?i)db\\.T_0[12]|other\\.exact");
    when(catalog.listDatabasesWithoutFilter()).thenReturn(Arrays.asList("db", "other"));
    when(catalog.listTablesWithoutFilter("db"))
        .thenReturn(Arrays.asList(table("db", "t_01"), table("db", "t_02"), table("db", "t_03")));
    when(catalog.listTablesWithoutFilter("other"))
        .thenReturn(Arrays.asList(table("other", "exact"), table("other", "exact_suffix")));
    controller.getTableFilterMatchedTables(ctx);
    assertEquals(Arrays.asList("db.t_01", "db.t_02", "other.exact"), result());
  }

  @Test
  public void testLegacyGroupedExactRules() {
    metadata.putToCatalogProperties(
        CatalogMetaProperties.KEY_TABLE_FILTER, "(db\\.a)|(other\\.b)|(db\\.c)");
    when(catalog.listDatabasesWithoutFilter()).thenReturn(Arrays.asList("db", "other"));
    when(catalog.listTablesWithoutFilter("db"))
        .thenReturn(Arrays.asList(table("db", "a"), table("db", "c"), table("db", "d")));
    when(catalog.listTablesWithoutFilter("other"))
        .thenReturn(Collections.singletonList(table("other", "b")));
    controller.getTableFilterMatchedTables(ctx);
    assertEquals(Arrays.asList("db.a", "db.c", "other.b"), result());
  }

  @Test
  public void testMissingFilterDoesNotScanCatalog() {
    controller.getTableFilterMatchedTables(ctx);
    assertEquals(Collections.emptyList(), result());
    verify(catalog, never()).listDatabasesWithoutFilter();
  }

  @Test
  public void testBlankDatabaseIsRejected() {
    when(ctx.pathParam("db")).thenReturn(" ");
    assertThrows(IllegalArgumentException.class, () -> controller.getTableFilterTableList(ctx));
    verify(manager, never()).getServerCatalog("test_catalog");
  }

  private TableIDWithFormat table(String database, String name) {
    return TableIDWithFormat.of(
        TableIdentifier.of("test_catalog", database, name), TableFormat.ICEBERG);
  }

  @SuppressWarnings("unchecked")
  private List<String> result() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(ctx).json(captor.capture());
    return ((OkResponse<List<String>>) captor.getValue()).getResult();
  }
}
