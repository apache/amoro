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

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.cache.CacheBuilder;
import org.apache.amoro.shade.guava32.com.google.common.cache.CacheLoader;
import org.apache.amoro.shade.guava32.com.google.common.cache.LoadingCache;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AuthenticatedFileIOs {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatedFileIOs.class);
  public static final boolean CLOSE_TRASH = true;

  // Cache for FileIO instances at the catalog level - key is the TableMetaStore instance
  // Expires entries after 1 hour of inactivity
  private static final LoadingCache<TableMetaStore, AuthenticatedHadoopFileIO> FILE_IO_CACHE =
      CacheBuilder.newBuilder()
          .expireAfterAccess(1, TimeUnit.HOURS)
          .build(
              new CacheLoader<TableMetaStore, AuthenticatedHadoopFileIO>() {
                @Override
                public AuthenticatedHadoopFileIO load(TableMetaStore metaStore) {
                  return createAuthenticatedHadoopFileIO(metaStore);
                }
              });

  // Helper method to create a new AuthenticatedHadoopFileIO instance
  private static AuthenticatedHadoopFileIO createAuthenticatedHadoopFileIO(
      TableMetaStore metaStore) {
    LOG.debug(
        "Creating new AuthenticatedHadoopFileIO for catalog with TableMetaStore {}",
        metaStore.getClass().getName());
    return new AuthenticatedHadoopFileIO(metaStore);
  }

  // Cache for recoverable FileIO instances with trash management
  // Expires entries after 1 hour of inactivity
  private static final LoadingCache<RecoverableFileIOCacheKey, RecoverableHadoopFileIO>
      RECOVERABLE_FILE_IO_CACHE =
          CacheBuilder.newBuilder()
              .expireAfterAccess(1, TimeUnit.HOURS)
              .build(
                  new CacheLoader<RecoverableFileIOCacheKey, RecoverableHadoopFileIO>() {
                    @Override
                    public RecoverableHadoopFileIO load(RecoverableFileIOCacheKey key) {
                      return createRecoverableHadoopFileIO(key);
                    }
                  });

  // Helper method to create a new RecoverableHadoopFileIO instance
  private static RecoverableHadoopFileIO createRecoverableHadoopFileIO(
      RecoverableFileIOCacheKey key) {
    LOG.debug("Creating new RecoverableHadoopFileIO for table {}", key.tableIdentifier);
    TableMetaStore tableMetaStore = key.tableMetaStore;
    AuthenticatedHadoopFileIO baseFileIO = buildHadoopFileIO(tableMetaStore);
    TableTrashManager trashManager =
        TableTrashManagers.build(
            key.tableIdentifier, key.tableLocation, key.tableProperties, baseFileIO);
    String trashFilePattern =
        PropertyUtil.propertyAsString(
            key.tableProperties,
            TableProperties.TABLE_TRASH_FILE_PATTERN,
            TableProperties.TABLE_TRASH_FILE_PATTERN_DEFAULT);
    return new RecoverableHadoopFileIO(tableMetaStore, trashManager, trashFilePattern);
  }

  public static AuthenticatedHadoopFileIO buildRecoverableHadoopFileIO(
      TableIdentifier tableIdentifier,
      String tableLocation,
      Map<String, String> tableProperties,
      TableMetaStore tableMetaStore,
      Map<String, String> catalogProperties) {
    tableProperties =
        MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(tableProperties, catalogProperties);
    if (!CLOSE_TRASH
        && PropertyUtil.propertyAsBoolean(
            tableProperties,
            TableProperties.ENABLE_TABLE_TRASH,
            TableProperties.ENABLE_TABLE_TRASH_DEFAULT)) {

      // Create a cache key using the dedicated key class
      RecoverableFileIOCacheKey cacheKey =
          new RecoverableFileIOCacheKey(
              tableIdentifier, tableLocation, tableProperties, tableMetaStore);

      try {
        // Get or create a cached instance automatically via LoadingCache
        RecoverableHadoopFileIO fileIO = RECOVERABLE_FILE_IO_CACHE.get(cacheKey);
        LOG.debug("Using RecoverableHadoopFileIO for table {}", tableIdentifier);
        return fileIO;
      } catch (ExecutionException e) {
        LOG.warn(
            "Failed to get RecoverableHadoopFileIO from cache for table {}", tableIdentifier, e);
        return createRecoverableHadoopFileIO(cacheKey);
      }
    } else {
      // Use the simple cached FileIO without trash management
      return buildHadoopFileIO(tableMetaStore);
    }
  }

  public static AuthenticatedHadoopFileIO buildHadoopFileIO(TableMetaStore tableMetaStore) {
    // Get or create a cached instance for this TableMetaStore
    try {
      return FILE_IO_CACHE.get(tableMetaStore);
    } catch (ExecutionException e) {
      LOG.warn("Failed to get AuthenticatedHadoopFileIO from cache, creating new instance", e);
      return createAuthenticatedHadoopFileIO(tableMetaStore);
    }
  }

  public static AuthenticatedFileIO buildAdaptIcebergFileIO(
      TableMetaStore tableMetaStore, FileIO io) {
    if (io instanceof HadoopFileIO) {
      return buildHadoopFileIO(tableMetaStore);
    } else if (io instanceof AuthenticatedFileIO) {
      return (AuthenticatedFileIO) io;
    } else {
      return new AuthenticatedFileIOAdapter(io);
    }
  }

  @VisibleForTesting
  public static void clearCache() {
    FILE_IO_CACHE.invalidateAll();
    RECOVERABLE_FILE_IO_CACHE.invalidateAll();
    LOG.info("Cleared FileIO cache");
  }

  private static final class RecoverableFileIOCacheKey {
    private final TableIdentifier tableIdentifier;
    private final String tableLocation;
    private final Map<String, String> tableProperties;
    private final TableMetaStore tableMetaStore;

    private RecoverableFileIOCacheKey(
        TableIdentifier tableIdentifier,
        String tableLocation,
        Map<String, String> tableProperties,
        TableMetaStore tableMetaStore) {
      this.tableIdentifier = tableIdentifier;
      this.tableLocation = tableLocation;
      this.tableProperties = tableProperties;
      this.tableMetaStore = tableMetaStore;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      RecoverableFileIOCacheKey that = (RecoverableFileIOCacheKey) o;
      return Objects.equals(tableIdentifier, that.tableIdentifier)
          && Objects.equals(tableLocation, that.tableLocation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(tableIdentifier, tableLocation);
    }
  }
}
