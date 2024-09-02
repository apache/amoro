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
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hudi.client.common.HoodieJavaEngineContext;
import org.apache.hudi.common.config.HoodieMetadataConfig;
import org.apache.hudi.common.fs.FSUtils;
import org.apache.hudi.config.HoodieWriteConfig;
import org.apache.hudi.table.HoodieJavaTable;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HudiHiveCatalog implements FormatCatalog {
  private final TableMetaStore metaStore;
  private final String catalog;
  private final Map<String, String> properties;
  private final CachedHiveClientPool hiveClientPool;

  protected HudiHiveCatalog(
      String catalog, Map<String, String> catalogProperties, TableMetaStore metaStore) {
    this.catalog = catalog;
    this.metaStore = metaStore;
    this.properties =
        catalogProperties == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(catalogProperties);
    this.hiveClientPool = new CachedHiveClientPool(metaStore, catalogProperties);
  }

  @Override
  public List<String> listDatabases() {
    try {
      return hiveClientPool.run(HMSClient::getAllDatabases);
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to fetch database from HMS", e);
    }
  }

  @Override
  public boolean databaseExists(String database) {
    try {
      hiveClientPool.run(client -> client.getDatabase(database));
      return true;
    } catch (NoSuchObjectException e) {
      return false;
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to fetch database from HMS", e);
    }
  }

  @Override
  public boolean tableExists(String database, String table) {
    return loadHoodieHiveTable(database, table).isPresent();
  }

  @Override
  public void createDatabase(String database) {
    try {
      Database hiveDatabase = new Database();
      hiveDatabase.setName(database);
      hiveClientPool.run(
          client -> {
            client.createDatabase(hiveDatabase);
            return null;
          });
    } catch (AlreadyExistsException e) {
      throw new org.apache.amoro.AlreadyExistsException("Database:" + database + " already exists");
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to create database from HMS", e);
    }
  }

  @Override
  public void dropDatabase(String database) {
    try {
      hiveClientPool.run(
          client -> {
            client.dropDatabase(database, false, false, false);
            return null;
          });
    } catch (NoSuchObjectException e) {
      // pass
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to drop database from HMS", e);
    }
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    Optional<Table> optHiveTable = loadHoodieHiveTable(database, table);
    if (!optHiveTable.isPresent()) {
      return false;
    }
    Table hiveTable = optHiveTable.get();
    String location = hiveTable.getSd().getLocation();
    try {
      hiveClientPool.run(
          client -> {
            client.dropTable(database, table, purge, true);
            return null;
          });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to drop database from HMS", e);
    }
    if (purge) {
      metaStore.doAs(
          () -> {
            Path tableLocation = new Path(location);
            FileSystem fs = FSUtils.getFs(tableLocation, metaStore.getConfiguration());
            fs.delete(tableLocation, true);
            return null;
          });
    }
    return true;
  }

  @Override
  public List<String> listTables(String database) {
    List<String> hiveTableLists;
    try {
      hiveTableLists = hiveClientPool.run(client -> client.getAllTables(database));
    } catch (NoSuchObjectException e) {
      throw new NoSuchDatabaseException("Database: " + database + " dose not exists.");
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to drop database from HMS", e);
    }
    return hiveTableLists.stream()
        .filter(table -> loadHoodieHiveTable(database, table).isPresent())
        .collect(Collectors.toList());
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    Optional<Table> hoodieHiveTable = loadHoodieHiveTable(database, table);
    if (!hoodieHiveTable.isPresent()) {
      throw new NoSuchDatabaseException(
          "Hoodie table: " + database + "." + table + " dose not exists");
    }
    Table hiveTable = hoodieHiveTable.get();
    String tableLocation = hiveTable.getSd().getLocation();
    HoodieJavaEngineContext context = new HoodieJavaEngineContext(metaStore.getConfiguration());
    HoodieWriteConfig config =
        HoodieWriteConfig.newBuilder()
            .withPath(tableLocation)
            .withMetadataConfig(HoodieMetadataConfig.newBuilder().enable(true).build())
            .build();

    return metaStore.doAs(
        () -> {
          HoodieJavaTable hoodieTable = HoodieJavaTable.create(config, context);
          TableIdentifier identifier = TableIdentifier.of(catalog, database, table);
          Map<String, String> tableProperties = hiveTable.getParameters();
          tableProperties = filterEngineProperties(tableProperties);
          Map<String, String> properties =
              CatalogUtil.mergeCatalogPropertiesToTable(tableProperties, this.properties);
          return new HudiTable(identifier, hoodieTable, properties);
        });
  }

  private Optional<Table> loadHoodieHiveTable(String database, String table) {
    try {
      Table hiveTable = hiveClientPool.run(client -> client.getTable(database, table));
      if (isHoodieTable(hiveTable)) {
        return Optional.of(hiveTable);
      } else {
        return Optional.empty();
      }
    } catch (NoSuchObjectException e) {
      return Optional.empty();
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to fetch database from HMS", e);
    }
  }

  static final String SPARK_SOURCE_PROVIDER = "spark.sql.sources.provider";
  static final String FLINK_CONNECTOR = "connector";

  private boolean isHoodieTable(Table table) {

    return "hudi".equalsIgnoreCase(table.getParameters().getOrDefault(SPARK_SOURCE_PROVIDER, ""))
        || "hudi".equalsIgnoreCase(table.getParameters().getOrDefault(FLINK_CONNECTOR, ""));
  }

  private Map<String, String> filterEngineProperties(Map<String, String> properties) {
    Set<String> enginePropertyKeys =
        Sets.newHashSet(
            "transient_lastDdlTime",
            "spark.sql.sources.schema.part.",
            "spark.sql.sources.schema.partCol.",
            "spark.sql.sources.schema.numPartCols",
            "spark.sql.sources.schema.numParts",
            "spark.sql.sources.provider",
            "spark.sql.create.version");
    Map<String, String> filteredProperties = new HashMap<>();
    properties.forEach(
        (k, v) -> {
          boolean exists = enginePropertyKeys.stream().anyMatch(k::startsWith);
          if (!exists) {
            filteredProperties.put(k, v);
          }
        });
    return filteredProperties;
  }
}
