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

package org.apache.amoro.formats.hive;

import org.apache.amoro.AlreadyExistsException;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.DatabaseNotEmptyException;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.client.ClientPool;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.HiveTableTypeUtil;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.InvalidOperationException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

public class HiveCatalog implements FormatCatalog {

  private final String name;
  private final HMSClientPool clients;

  public HiveCatalog(String name, TableMetaStore metaStore, Map<String, String> properties) {
    this.name = name;
    this.clients = new CachedHiveClientPool(metaStore, properties);
  }

  @Override
  public List<String> listDatabases() {
    try {
      return run("listDatabases", clients -> clients.getAllDatabases());
    } catch (TException e) {
      throw new RuntimeException("Failed to list all databases", e);
    }
  }

  @Override
  public boolean databaseExists(String database) {
    try {
      run("databaseExists", client -> client.getDatabase(database));
      return true;
    } catch (NoSuchObjectException e) {
      return false;
    } catch (TException e) {
      throw new RuntimeException("Failed to check database " + database, e);
    }
  }

  @Override
  public boolean tableExists(String database, String table) {
    try {
      return HiveTableTypeUtil.isHiveTable(
          run("tableExists", client -> client.getTable(database, table)));
    } catch (NoSuchObjectException e) {
      return false;
    } catch (TException e) {
      throw new RuntimeException("Failed to check table " + database + "." + table, e);
    }
  }

  @Override
  public List<String> listTables(String database) {
    return HiveTableTypeUtil.listHiveTables(clients, database);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    try {
      Table hmsTable = run("loadTable", client -> client.getTable(database, table));
      if (!HiveTableTypeUtil.isHiveTable(hmsTable)) {
        throw new NoSuchTableException(
            "Table " + database + "." + table + " is not a HIVE format table");
      }

      return new HiveTable(TableIdentifier.of(name, database, table), hmsTable);
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException("Table " + database + "." + table + " does not exist", e);
    } catch (TException e) {
      throw new RuntimeException("Failed to load table " + database + "." + table, e);
    }
  }

  @Override
  public void createDatabase(String database) {
    try {
      Database meta = new Database();
      meta.setName(database);
      run(
          "createDatabase",
          client -> {
            client.createDatabase(meta);
            return null;
          });
    } catch (org.apache.hadoop.hive.metastore.api.AlreadyExistsException e) {
      throw new AlreadyExistsException("Database " + database + " already exists", e);
    } catch (TException e) {
      throw new RuntimeException("Failed to create database " + database, e);
    }
  }

  @Override
  public void dropDatabase(String database) {
    try {
      run(
          "dropDatabase",
          client -> {
            client.dropDatabase(database, false, false, false);
            return null;
          });
    } catch (NoSuchObjectException e) {
      throw new NoSuchDatabaseException("Database " + database + " does not exist", e);
    } catch (InvalidOperationException e) {
      throw new DatabaseNotEmptyException("Database " + database + " is not empty", e);
    } catch (TException e) {
      throw new RuntimeException("Failed to drop database " + database, e);
    }
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    try {
      run(
          "dropTable",
          client -> {
            client.dropTable(database, table, purge, false);
            return null;
          });
      return true;
    } catch (NoSuchObjectException e) {
      return false;
    } catch (TException e) {
      throw new RuntimeException("Failed to drop table " + database + "." + table, e);
    }
  }

  private <R> R run(String operation, ClientPool.Action<R, HMSClient, TException> action)
      throws TException {
    try {
      return clients.run(action);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to " + operation, e);
    }
  }
}
