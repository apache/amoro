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
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticatedFileIOs {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticatedFileIOs.class);
  public static final boolean CLOSE_TRASH = true;

  // Cache for FileIO instances at the catalog level - key is the TableMetaStore instance
  private static final ConcurrentHashMap<TableMetaStore, AuthenticatedHadoopFileIO> FILE_IO_CACHE =
      new ConcurrentHashMap<>();

  // Cache for recoverable FileIO instances with trash management
  private static final ConcurrentHashMap<String, RecoverableHadoopFileIO>
      RECOVERABLE_FILE_IO_CACHE = new ConcurrentHashMap<>();

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

      // Create a cache key based on table identifier and location
      String cacheKey = tableIdentifier.toString() + "|" + tableLocation;

      // Check if we already have a cached instance
      RecoverableHadoopFileIO cachedIO = RECOVERABLE_FILE_IO_CACHE.get(cacheKey);
      if (cachedIO != null) {
        LOG.debug("Reusing cached RecoverableHadoopFileIO for table {}", tableIdentifier);
        return cachedIO;
      }

      // If not in cache, create a new instance and cache it
      AuthenticatedHadoopFileIO baseFileIO = buildHadoopFileIO(tableMetaStore);
      TableTrashManager trashManager =
          TableTrashManagers.build(tableIdentifier, tableLocation, tableProperties, baseFileIO);
      String trashFilePattern =
          PropertyUtil.propertyAsString(
              tableProperties,
              TableProperties.TABLE_TRASH_FILE_PATTERN,
              TableProperties.TABLE_TRASH_FILE_PATTERN_DEFAULT);

      RecoverableHadoopFileIO fileIO =
          new RecoverableHadoopFileIO(tableMetaStore, trashManager, trashFilePattern);
      RECOVERABLE_FILE_IO_CACHE.put(cacheKey, fileIO);
      LOG.debug("Created and cached new RecoverableHadoopFileIO for table {}", tableIdentifier);
      return fileIO;
    } else {
      // Use the simple cached FileIO without trash management
      return buildHadoopFileIO(tableMetaStore);
    }
  }

  public static AuthenticatedHadoopFileIO buildHadoopFileIO(TableMetaStore tableMetaStore) {
    // Check if we already have a cached instance for this TableMetaStore
    return FILE_IO_CACHE.computeIfAbsent(
        tableMetaStore,
        metaStore -> {
          LOG.debug(
              "Creating new AuthenticatedHadoopFileIO for catalog with TableMetaStore {}",
              metaStore.getClass().getName());
          return new AuthenticatedHadoopFileIO(metaStore);
        });
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
    FILE_IO_CACHE.clear();
    RECOVERABLE_FILE_IO_CACHE.clear();
    LOG.info("Cleared FileIO cache");
  }
}
