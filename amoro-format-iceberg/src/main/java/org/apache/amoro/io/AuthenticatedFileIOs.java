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

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;
import java.util.function.Supplier;

public class AuthenticatedFileIOs {

  public static final boolean CLOSE_TRASH = true;
  private static final ThreadLocal<Boolean> OPTIMIZING_COMMIT_IMPERSONATION = new ThreadLocal<>();

  public static AuthenticatedHadoopFileIO buildRecoverableHadoopFileIO(
      TableIdentifier tableIdentifier,
      String tableLocation,
      Map<String, String> tableProperties,
      TableMetaStore tableMetaStore,
      Map<String, String> catalogProperties) {
    return buildRecoverableHadoopFileIO(
        tableIdentifier,
        tableLocation,
        tableProperties,
        tableMetaStore,
        catalogProperties,
        tableProperties.get(TableProperties.OWNER));
  }

  public static AuthenticatedHadoopFileIO buildRecoverableHadoopFileIO(
      TableIdentifier tableIdentifier,
      String tableLocation,
      Map<String, String> tableProperties,
      TableMetaStore tableMetaStore,
      Map<String, String> catalogProperties,
      String tableOwner) {
    String proxyUser =
        resolveProxyUser(tableProperties, catalogProperties, tableMetaStore, tableOwner);
    Map<String, String> effectiveTableProperties =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(tableProperties, catalogProperties);
    if (!CLOSE_TRASH
        && PropertyUtil.propertyAsBoolean(
            effectiveTableProperties,
            TableProperties.ENABLE_TABLE_TRASH,
            TableProperties.ENABLE_TABLE_TRASH_DEFAULT)) {
      AuthenticatedHadoopFileIO fileIO = new AuthenticatedHadoopFileIO(tableMetaStore, proxyUser);
      TableTrashManager trashManager =
          TableTrashManagers.build(
              tableIdentifier, tableLocation, effectiveTableProperties, fileIO);
      String trashFilePattern =
          PropertyUtil.propertyAsString(
              effectiveTableProperties,
              TableProperties.TABLE_TRASH_FILE_PATTERN,
              TableProperties.TABLE_TRASH_FILE_PATTERN_DEFAULT);

      return new RecoverableHadoopFileIO(tableMetaStore, trashManager, trashFilePattern, proxyUser);
    } else {
      return new AuthenticatedHadoopFileIO(tableMetaStore, proxyUser);
    }
  }

  public static AuthenticatedHadoopFileIO buildHadoopFileIO(TableMetaStore tableMetaStore) {
    return new AuthenticatedHadoopFileIO(tableMetaStore);
  }

  public static AuthenticatedFileIO buildAdaptIcebergFileIO(
      TableMetaStore tableMetaStore, FileIO io) {
    return buildAdaptIcebergFileIOWithProxyUser(tableMetaStore, io, null);
  }

  public static AuthenticatedFileIO buildAdaptIcebergFileIO(
      TableMetaStore tableMetaStore,
      FileIO io,
      Map<String, String> tableProperties,
      Map<String, String> catalogProperties) {
    String tableOwner = tableProperties == null ? null : tableProperties.get(TableProperties.OWNER);
    return buildAdaptIcebergFileIO(
        tableMetaStore, io, tableProperties, catalogProperties, tableOwner);
  }

  public static AuthenticatedFileIO buildAdaptIcebergFileIO(
      TableMetaStore tableMetaStore,
      FileIO io,
      Map<String, String> tableProperties,
      Map<String, String> catalogProperties,
      String tableOwner) {
    return buildAdaptIcebergFileIOWithProxyUser(
        tableMetaStore,
        io,
        resolveProxyUser(tableProperties, catalogProperties, tableMetaStore, tableOwner));
  }

  private static AuthenticatedFileIO buildAdaptIcebergFileIOWithProxyUser(
      TableMetaStore tableMetaStore, FileIO io, String proxyUser) {
    if (io instanceof HadoopFileIO) {
      return new AuthenticatedHadoopFileIO(tableMetaStore, proxyUser);
    } else {
      Preconditions.checkArgument(
          proxyUser == null,
          "HDFS impersonation requires HadoopFileIO, but the table uses %s",
          io.getClass().getName());
      return new AuthenticatedFileIOAdapter(io);
    }
  }

  /**
   * Runs a table loader in the optimizing-commit scope. FileIOs created during the load retain the
   * resolved table owner after the scope is restored.
   */
  public static <T> T withOptimizingCommitImpersonation(Supplier<T> tableLoader) {
    Boolean previous = OPTIMIZING_COMMIT_IMPERSONATION.get();
    OPTIMIZING_COMMIT_IMPERSONATION.set(true);
    try {
      return tableLoader.get();
    } finally {
      if (previous == null) {
        OPTIMIZING_COMMIT_IMPERSONATION.remove();
      } else {
        OPTIMIZING_COMMIT_IMPERSONATION.set(previous);
      }
    }
  }

  static boolean isOptimizingCommitImpersonationActive() {
    return Boolean.TRUE.equals(OPTIMIZING_COMMIT_IMPERSONATION.get());
  }

  /** Returns whether HDFS impersonation is enabled for the current optimizing-commit table load. */
  public static boolean isHdfsImpersonationEnabledForOptimizingCommit(
      Map<String, String> tableProperties, Map<String, String> catalogProperties) {
    if (!isOptimizingCommitImpersonationActive()) {
      return false;
    }
    if (tableProperties != null
        && tableProperties.containsKey(TableProperties.HDFS_IMPERSONATION_ENABLED)) {
      return Boolean.parseBoolean(tableProperties.get(TableProperties.HDFS_IMPERSONATION_ENABLED));
    }
    if (catalogProperties != null
        && catalogProperties.containsKey(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED)) {
      return Boolean.parseBoolean(
          catalogProperties.get(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED));
    }
    return catalogProperties != null
        && Boolean.parseBoolean(
            catalogProperties.get(
                CatalogMetaProperties.TABLE_PROPERTIES_PREFIX
                    + TableProperties.HDFS_IMPERSONATION_ENABLED));
  }

  private static String resolveProxyUser(
      Map<String, String> tableProperties,
      Map<String, String> catalogProperties,
      TableMetaStore tableMetaStore,
      String tableOwner) {
    if (!isHdfsImpersonationEnabledForOptimizingCommit(tableProperties, catalogProperties)) {
      return null;
    }
    Preconditions.checkArgument(
        tableMetaStore.supportsHadoopImpersonation(),
        "HDFS impersonation requires SIMPLE or KERBEROS catalog authentication");
    Preconditions.checkArgument(
        StringUtils.isNotBlank(tableOwner),
        "HDFS impersonation is enabled, but the table owner is missing");
    return tableOwner;
  }
}
