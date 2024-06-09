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

import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hudi.common.fs.FSUtils;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HudiHiveCatalog extends HudiCatalogBase {
  private final CachedHiveClientPool hiveClientPool;

  protected HudiHiveCatalog(String catalog, Map<String, String> catalogProperties, TableMetaStore metaStore) {
    super(catalog, catalogProperties, metaStore);
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
  public boolean exist(String database) {
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
  public boolean exist(String database, String table) {
    return loadHoodieHiveTable(database, table).isPresent();
  }

  @Override
  public void createDatabase(String database) {
    try {
      Database hiveDatabase = new Database();
      hiveDatabase.setName(database);
      hiveClientPool.run(client -> {
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
      hiveClientPool.run(client -> {
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
      hiveClientPool.run(client -> {
        client.dropTable(database, table, purge, true);
        return null;
      });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to drop database from HMS", e);
    }
    if (purge) {
      metaStore.doAs(() -> {
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
  protected String loadTableLocation(String database, String table) {
    Optional<Table> hoodieHiveTable = loadHoodieHiveTable(database, table);
    return hoodieHiveTable.map(t -> t.getSd().getLocation())
        .orElseThrow(() -> new NoSuchTableException(
            "Hoodie table: " + database + "." + table + " dose not exists"));
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

  private boolean isHoodieTable(Table table) {
    final String SPARK_SOURCE_PROVIDER = "spark.sql.sources.provider";
    final String FLINK_CONNECTOR = "connector";
    return "hudi".equalsIgnoreCase(
        table.getParameters().getOrDefault(SPARK_SOURCE_PROVIDER, ""))
        || "hudi".equalsIgnoreCase(table.getParameters().getOrDefault(FLINK_CONNECTOR, ""));
  }
}
