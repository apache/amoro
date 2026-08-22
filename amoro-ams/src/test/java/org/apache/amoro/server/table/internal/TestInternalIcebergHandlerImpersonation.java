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

package org.apache.amoro.server.table.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.utils.InternalTableUtil;
import org.apache.amoro.table.TableProperties;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TestInternalIcebergHandlerImpersonation {

  @Test
  public void testInternalHandlerUsesTableOwner() {
    String catalogUser = "amoro-service";
    String tableOwner = "table-owner";
    TableMetadata tableMetadata = mock(TableMetadata.class);
    CatalogMeta catalogMeta = newCatalogMeta(catalogUser);
    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.withOptimizingCommitImpersonation(
            () ->
                InternalTableUtil.newIcebergFileIo(
                    catalogMeta, Map.of(TableProperties.OWNER, tableOwner)));
    InternalIcebergHandler handler = new InternalIcebergHandler(tableMetadata, fileIO);
    try {
      AuthenticatedFileIO handlerFileIO = (AuthenticatedFileIO) handler.newTableOperator().io();
      assertEquals(tableOwner, handlerFileIO.doAs(this::currentUser));
    } finally {
      handler.close();
    }
  }

  private static CatalogMeta newCatalogMeta(String catalogUser) {
    String emptyConfiguration =
        Base64.getEncoder().encodeToString("<configuration/>".getBytes(StandardCharsets.UTF_8));
    Map<String, String> storageConfigs = new HashMap<>();
    storageConfigs.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HADOOP);
    storageConfigs.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, emptyConfiguration);
    storageConfigs.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, emptyConfiguration);

    Map<String, String> authConfigs = new HashMap<>();
    authConfigs.put(
        CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    authConfigs.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME, catalogUser);

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogMetaProperties.KEY_WAREHOUSE, "file:///tmp/amoro");
    catalogProperties.put(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true");
    return new CatalogMeta("test", "ams", storageConfigs, authConfigs, catalogProperties);
  }

  private String currentUser() throws IOException {
    return UserGroupInformation.getCurrentUser().getShortUserName();
  }
}
