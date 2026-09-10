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

package org.apache.amoro.hive.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.client.ClientPool;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestMixedHiveTablesImpersonation {

  @Test
  public void testUsesOwnerLoadedFromHiveMetastore() throws Exception {
    String catalogUser = "amoro-service";
    String tableOwner = "hms-owner";
    TableIdentifier identifier = TableIdentifier.of("catalog", "database", "table");
    TableMetaStore metaStore =
        TableMetaStore.builder()
            .withCoreSite(new byte[0])
            .withHdfsSite(new byte[0])
            .withSimpleAuth(catalogUser)
            .build();
    Map<String, String> catalogProperties =
        Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true");

    Table hiveTable = new Table();
    hiveTable.setOwner(tableOwner);
    HMSClient hiveClient = mock(HMSClient.class);
    when(hiveClient.getTable(identifier.getDatabase(), identifier.getTableName()))
        .thenReturn(hiveTable);
    CachedHiveClientPool hiveClientPool = mock(CachedHiveClientPool.class);
    doAnswer(
            invocation -> {
              ClientPool.Action<?, HMSClient, TException> action = invocation.getArgument(0);
              return action.run(hiveClient);
            })
        .when(hiveClientPool)
        .run(any());

    MixedHiveTables tables = new MixedHiveTables(catalogProperties, metaStore, hiveClientPool);
    String resolvedOwner =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () -> tables.loadHiveTableOwnerIfNeeded(identifier, Map.of()));
    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
                    identifier,
                    "file:///tmp/table",
                    Map.of(),
                    metaStore,
                    catalogProperties,
                    resolvedOwner));

    assertEquals(tableOwner, resolvedOwner);
    assertEquals(
        tableOwner, fileIO.doAs(() -> UserGroupInformation.getCurrentUser().getShortUserName()));
  }

  @Test
  public void testRestoresInterruptedStatusWhenOwnerLookupIsInterrupted() throws Exception {
    String catalogUser = "amoro-service";
    TableIdentifier identifier = TableIdentifier.of("catalog", "database", "table");
    TableMetaStore metaStore =
        TableMetaStore.builder()
            .withCoreSite(new byte[0])
            .withHdfsSite(new byte[0])
            .withSimpleAuth(catalogUser)
            .build();
    Map<String, String> catalogProperties =
        Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true");
    CachedHiveClientPool hiveClientPool = mock(CachedHiveClientPool.class);
    when(hiveClientPool.run(any()))
        .thenAnswer(
            invocation -> {
              throw new InterruptedException("interrupted");
            });
    MixedHiveTables tables = new MixedHiveTables(catalogProperties, metaStore, hiveClientPool);

    Thread.interrupted();
    try {
      IllegalStateException exception =
          assertThrows(
              IllegalStateException.class,
              () ->
                  AuthenticatedFileIOs.withOptimizingCommitImpersonation(
                      () -> tables.loadHiveTableOwnerIfNeeded(identifier, Map.of())));

      assertTrue(Thread.currentThread().isInterrupted());
      assertTrue(exception.getCause() instanceof InterruptedException);
    } finally {
      Thread.interrupted();
    }
  }
}
