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

package org.apache.amoro.formats.hudi;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.DatabaseNotEmptyException;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hudi.client.common.HoodieJavaEngineContext;
import org.apache.hudi.common.fs.FSUtils;
import org.apache.hudi.config.HoodieWriteConfig;
import org.apache.hudi.table.HoodieJavaTable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Hudi catalog implement in hadoop filesystem. */
public class HudiHadoopCatalog implements FormatCatalog {
  private final TableMetaStore metaStore;
  private final String catalog;
  private final Map<String, String> properties;
  private final Path warehouse;

  protected HudiHadoopCatalog(
      String catalog, Map<String, String> catalogProperties, TableMetaStore metaStore) {
    this.catalog = catalog;
    this.metaStore = metaStore;
    this.properties =
        catalogProperties == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(catalogProperties);

    Preconditions.checkArgument(
        this.properties.containsKey(CatalogMetaProperties.KEY_WAREHOUSE),
        "Lack required property: {}",
        CatalogMetaProperties.KEY_WAREHOUSE);
    String warehosue = this.properties.get(CatalogMetaProperties.KEY_WAREHOUSE);
    this.warehouse = new Path(warehosue);
  }

  @Override
  public List<String> listDatabases() {
    return metaStore.doAs(
        () -> {
          FileSystem fs = fs();
          FileStatus[] fileStatuses = fs.listStatus(warehouse);
          if (fileStatuses == null || fileStatuses.length == 0) {
            return Lists.newArrayList();
          }
          List<String> databases = Lists.newArrayList();
          for (FileStatus s : fileStatuses) {
            if (!s.isDirectory()) {
              continue;
            }
            databases.add(s.getPath().getName());
          }
          return databases;
        });
  }

  @Override
  public boolean databaseExists(String database) {
    return metaStore.doAs(
        () -> {
          FileSystem fs = fs();
          return fs.exists(new Path(warehouse, database));
        });
  }

  @Override
  public boolean tableExists(String database, String table) {
    try {
      loadTableLocation(database, table);
      return true;
    } catch (NoSuchTableException e) {
      return false;
    }
  }

  @Override
  public void createDatabase(String database) {
    metaStore.doAs(
        () -> {
          FileSystem fs = fs();
          fs.mkdirs(new Path(warehouse, database));
          return null;
        });
  }

  @Override
  public void dropDatabase(String database) {
    List<String> tables = listTables(database);
    if (!tables.isEmpty()) {
      throw new DatabaseNotEmptyException("Database: " + database + " is not empty");
    }
    metaStore.doAs(
        () -> {
          FileSystem fs = fs();
          Path path = new Path(warehouse, database);
          fs.delete(path, true);
          return null;
        });
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    Path databasePath = new Path(warehouse, database);
    Path tablePath = new Path(databasePath, table);
    return metaStore.doAs(
        () -> {
          Path dropPath = new Path(tablePath, ".hoodie");
          if (purge) {
            dropPath = tablePath;
          }
          try {
            FileSystem fs = fs();
            return fs.delete(dropPath, true);
          } catch (IOException e) {
            return false;
          }
        });
  }

  @Override
  public List<String> listTables(String database) {
    return metaStore.doAs(
        () -> {
          FileSystem fs = fs();
          Path databasePath = new Path(warehouse, database);
          FileStatus[] items = fs.listStatus(databasePath);
          if (items == null || items.length == 0) {
            return Lists.newArrayList();
          }
          List<String> hoodieTables = Lists.newArrayList();
          for (FileStatus fileStatus : items) {
            if (fileStatus.isDirectory()) {
              Path tablePath = fileStatus.getPath();
              if (isHoodieTableBase(fs, tablePath)) {
                hoodieTables.add(tablePath.getName());
              }
            }
          }
          return hoodieTables;
        });
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    String tableLocation = loadTableLocation(database, table);
    HoodieJavaEngineContext context = new HoodieJavaEngineContext(metaStore.getConfiguration());
    HoodieWriteConfig config = HoodieWriteConfig.newBuilder().withPath(tableLocation).build();

    return metaStore.doAs(
        () -> {
          HoodieJavaTable hoodieTable = HoodieJavaTable.create(config, context);
          TableIdentifier identifier = TableIdentifier.of(catalog, database, table);
          Map<String, String> tableProperties =
              hoodieTable.getMetaClient().getTableConfig().propsMap();
          Map<String, String> properties =
              CatalogUtil.mergeCatalogPropertiesToTable(tableProperties, this.properties);
          return new HudiTable(identifier, hoodieTable, properties);
        });
  }

  private String loadTableLocation(String database, String table) {
    Path databasePath = new Path(warehouse, database);
    Path tablePath = new Path(databasePath, table);
    return metaStore.doAs(
        () -> {
          FileSystem fs = fs();
          if (isHoodieTableBase(fs, tablePath)) {
            return tablePath.toString();
          }
          throw new NoSuchTableException(database + "." + table + " is not exists");
        });
  }

  private boolean isHoodieTableBase(FileSystem fs, Path path) throws IOException {
    try {
      Path metadataPath = new Path(path, ".hoodie");
      FileStatus status = fs.getFileStatus(metadataPath);
      return status.isDirectory();
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  private FileSystem fs() {
    return FSUtils.getFs(warehouse, metaStore.getConfiguration());
  }
}
