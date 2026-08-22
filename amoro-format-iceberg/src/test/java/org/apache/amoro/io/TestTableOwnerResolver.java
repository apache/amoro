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

package org.apache.amoro.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.iceberg.Table;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TestTableOwnerResolver {

  private static final TableIdentifier TABLE_IDENTIFIER =
      TableIdentifier.of("catalog", "database", "table");
  private static final String CATALOG_USER = "catalog-user";

  @Test
  public void testRefreshesSettingAndOwnerInCatalogContext() {
    AtomicBoolean refreshed = new AtomicBoolean();
    AtomicReference<String> refreshUser = new AtomicReference<>();
    Table table = mock(Table.class);
    doAnswer(
            ignored -> {
              refreshUser.set(currentUser());
              refreshed.set(true);
              return null;
            })
        .when(table)
        .refresh();
    when(table.properties())
        .thenAnswer(
            ignored ->
                Map.of(
                    TableProperties.OWNER,
                    refreshed.get() ? "current-owner" : "stale-owner",
                    TableProperties.HDFS_IMPERSONATION_ENABLED,
                    Boolean.toString(refreshed.get())));

    String owner =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                TableOwnerResolver.resolve(
                    CatalogMetaProperties.CATALOG_TYPE_HADOOP,
                    TABLE_IDENTIFIER,
                    table,
                    Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "false"),
                    newSimpleMetaStore()));

    assertEquals("current-owner", owner);
    assertEquals(CATALOG_USER, refreshUser.get());
    verify(table).refresh();
  }

  @Test
  public void testOrdinaryLoadDoesNotRefreshIcebergMetadata() {
    Table table = mock(Table.class);

    String owner =
        TableOwnerResolver.resolve(
            CatalogMetaProperties.CATALOG_TYPE_HADOOP,
            TABLE_IDENTIFIER,
            table,
            enabledCatalogProperties(),
            TableMetaStore.EMPTY);

    assertNull(owner);
    verify(table, never()).refresh();
    verify(table, never()).properties();
  }

  @Test
  public void testHiveCatalogUsesHiveOwner() {
    String owner =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                TableOwnerResolver.resolve(
                    CatalogMetaProperties.CATALOG_TYPE_HIVE,
                    TABLE_IDENTIFIER,
                    Map.of(TableProperties.OWNER, "metadata-owner"),
                    enabledCatalogProperties(),
                    () -> "hms-owner"));

    assertEquals("hms-owner", owner);
  }

  @Test
  public void testNonHiveCatalogUsesIcebergOwner() {
    AtomicBoolean hiveOwnerLoaded = new AtomicBoolean();
    String owner =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                TableOwnerResolver.resolve(
                    CatalogMetaProperties.CATALOG_TYPE_HADOOP,
                    TABLE_IDENTIFIER,
                    Map.of(TableProperties.OWNER, "metadata-owner"),
                    enabledCatalogProperties(),
                    () -> {
                      hiveOwnerLoaded.set(true);
                      return "hms-owner";
                    }));

    assertEquals("metadata-owner", owner);
    assertFalse(hiveOwnerLoaded.get());
  }

  @Test
  public void testDisabledCatalogDoesNotLoadOwner() {
    AtomicBoolean hiveOwnerLoaded = new AtomicBoolean();
    String owner =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                TableOwnerResolver.resolve(
                    CatalogMetaProperties.CATALOG_TYPE_HIVE,
                    TABLE_IDENTIFIER,
                    Map.of(),
                    Map.of(),
                    () -> {
                      hiveOwnerLoaded.set(true);
                      return "hms-owner";
                    }));

    assertNull(owner);
    assertFalse(hiveOwnerLoaded.get());
  }

  @Test
  public void testTableSettingDisablesHiveOwnerLoad() {
    AtomicBoolean hiveOwnerLoaded = new AtomicBoolean();
    String owner =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                TableOwnerResolver.resolve(
                    CatalogMetaProperties.CATALOG_TYPE_HIVE,
                    TABLE_IDENTIFIER,
                    Map.of(TableProperties.HDFS_IMPERSONATION_ENABLED, "false"),
                    enabledCatalogProperties(),
                    () -> {
                      hiveOwnerLoaded.set(true);
                      return "hms-owner";
                    }));

    assertNull(owner);
    assertFalse(hiveOwnerLoaded.get());
  }

  private static Map<String, String> enabledCatalogProperties() {
    return Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true");
  }

  private static TableMetaStore newSimpleMetaStore() {
    return TableMetaStore.builder()
        .withCoreSite(new byte[0])
        .withHdfsSite(new byte[0])
        .withSimpleAuth(CATALOG_USER)
        .build();
  }

  private static String currentUser() throws IOException {
    return UserGroupInformation.getCurrentUser().getShortUserName();
  }
}
