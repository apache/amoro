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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.FileIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestAuthenticatedHadoopFileIO {

  private static final String CATALOG_USER = "amoro-service";
  private static final String TABLE_OWNER = "table-owner";

  @Test
  public void testUsesCatalogUserByDefault() {
    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(
            newSimpleMetaStore(), new HadoopFileIO(new Configuration()));

    assertEquals(CATALOG_USER, fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));
  }

  @Test
  public void testUsesTableOwnerOnCallerAndExecutorThreads() throws Exception {
    AuthenticatedFileIO fileIO =
        buildForOptimizingCommit(
            newSimpleMetaStore(),
            new HadoopFileIO(new Configuration()),
            Map.of(TableProperties.OWNER, TABLE_OWNER),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true"));

    assertEquals(TABLE_OWNER, fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      assertEquals(
          TABLE_OWNER,
          executor.submit(() -> fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser)).get());
    } finally {
      executor.shutdownNow();
      assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }
  }

  @Test
  public void testTableSettingOverridesCatalogSetting() {
    AuthenticatedFileIO fileIO =
        buildForOptimizingCommit(
            newSimpleMetaStore(),
            new HadoopFileIO(new Configuration()),
            Map.of(TableProperties.HDFS_IMPERSONATION_ENABLED, "false"),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true"));

    assertEquals(CATALOG_USER, fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));
  }

  @Test
  public void testTableSettingEnablesWhenCatalogDisabled() {
    AuthenticatedFileIO fileIO =
        buildForOptimizingCommit(
            newSimpleMetaStore(),
            new HadoopFileIO(new Configuration()),
            Map.of(
                TableProperties.OWNER,
                TABLE_OWNER,
                TableProperties.HDFS_IMPERSONATION_ENABLED,
                "true"),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "false"));

    assertEquals(TABLE_OWNER, fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));
  }

  @Test
  public void testCatalogTableDefaultEnablesImpersonation() {
    AuthenticatedFileIO fileIO =
        buildForOptimizingCommit(
            newSimpleMetaStore(),
            new HadoopFileIO(new Configuration()),
            Map.of(TableProperties.OWNER, TABLE_OWNER),
            Map.of(
                CatalogMetaProperties.TABLE_PROPERTIES_PREFIX
                    + TableProperties.HDFS_IMPERSONATION_ENABLED,
                "true"));

    assertEquals(TABLE_OWNER, fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));
  }

  @Test
  public void testRejectsMissingOwnerAndRestoresContext() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                buildForOptimizingCommit(
                    newSimpleMetaStore(),
                    new HadoopFileIO(new Configuration()),
                    Map.of(),
                    Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true")));

    assertTrue(exception.getMessage().contains("table owner is missing"));

    AuthenticatedFileIO ordinaryFileIO =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(
            newSimpleMetaStore(),
            new HadoopFileIO(new Configuration()),
            Map.of(),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true"));
    assertEquals(CATALOG_USER, ordinaryFileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));
  }

  @Test
  public void testRejectsUnsupportedAuthentication() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                buildForOptimizingCommit(
                    TableMetaStore.EMPTY,
                    new HadoopFileIO(new Configuration()),
                    Map.of(TableProperties.OWNER, TABLE_OWNER),
                    Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true")));

    assertTrue(exception.getMessage().contains("SIMPLE or KERBEROS"));
  }

  @Test
  public void testRejectsNonHadoopFileIO() {
    FileIO fileIO = mock(FileIO.class);
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                buildForOptimizingCommit(
                    newSimpleMetaStore(),
                    fileIO,
                    Map.of(TableProperties.OWNER, TABLE_OWNER),
                    Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true")));

    assertTrue(exception.getMessage().contains("requires HadoopFileIO"));
  }

  @Test
  public void testCatalogSettingDoesNotAffectOrdinaryTableLoads() {
    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(
            newSimpleMetaStore(),
            new HadoopFileIO(new Configuration()),
            Map.of(TableProperties.OWNER, TABLE_OWNER),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true"));

    assertEquals(CATALOG_USER, fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));
  }

  @Test
  public void testProxyFailureDoesNotFallBackToCatalogUser() {
    TableMetaStore metaStore = mock(TableMetaStore.class);
    when(metaStore.getConfiguration()).thenReturn(new Configuration());
    when(metaStore.supportsHadoopImpersonation()).thenReturn(true);
    SecurityException failure = new SecurityException("proxy denied");
    doThrow(failure).when(metaStore).doAsImpersonating(eq(TABLE_OWNER), any());
    AuthenticatedFileIO fileIO =
        buildForOptimizingCommit(
            metaStore,
            new HadoopFileIO(new Configuration()),
            Map.of(TableProperties.OWNER, TABLE_OWNER),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true"));

    SecurityException thrown =
        assertThrows(
            SecurityException.class, () -> fileIO.doAs(TestAuthenticatedHadoopFileIO::currentUser));

    assertSame(failure, thrown);
    verify(metaStore).doAsImpersonating(eq(TABLE_OWNER), any());
    verify(metaStore, never()).doAs(any());
  }

  @Test
  public void testFileIOEntryUsesProxyUser() {
    TableMetaStore metaStore = mock(TableMetaStore.class);
    when(metaStore.getConfiguration()).thenReturn(new Configuration());
    when(metaStore.supportsHadoopImpersonation()).thenReturn(true);
    doAnswer(invocation -> ((Callable<?>) invocation.getArgument(1)).call())
        .when(metaStore)
        .doAsImpersonating(eq(TABLE_OWNER), any());
    AuthenticatedFileIO fileIO =
        buildForOptimizingCommit(
            metaStore,
            new HadoopFileIO(new Configuration()),
            Map.of(TableProperties.OWNER, TABLE_OWNER),
            Map.of(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true"));

    fileIO.newOutputFile("file:///tmp/amoro-owner-test.metadata.json");

    verify(metaStore).doAsImpersonating(eq(TABLE_OWNER), any());
    verify(metaStore, never()).doAs(any());
  }

  private static AuthenticatedFileIO buildForOptimizingCommit(
      TableMetaStore metaStore,
      FileIO fileIO,
      Map<String, String> tableProperties,
      Map<String, String> catalogProperties) {
    return AuthenticatedFileIOs.withOptimizingCommitImpersonation(
        () ->
            AuthenticatedFileIOs.buildAdaptIcebergFileIO(
                metaStore, fileIO, tableProperties, catalogProperties));
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
