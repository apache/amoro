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

package org.apache.amoro.formats.paimon;

import org.apache.amoro.AlreadyExistsException;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.DatabaseNotEmptyException;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.table.TableIdentifier;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;

import java.util.List;

public class PaimonCatalog implements FormatCatalog {

  private final Catalog catalog;

  private final String name;

  public PaimonCatalog(Catalog catalog, String name) {
    this.catalog = catalog;
    this.name = name;
  }

  @Override
  public List<String> listDatabases() {
    return catalog.listDatabases();
  }

  @Override
  public boolean databaseExists(String database) {
    return catalog.databaseExists(database);
  }

  @Override
  public boolean tableExists(String database, String table) {
    return catalog.tableExists(Identifier.create(database, table));
  }

  @Override
  public void createDatabase(String database) {
    try {
      catalog.createDatabase(database, false);
    } catch (Catalog.DatabaseAlreadyExistException e) {
      throw new AlreadyExistsException(e);
    }
  }

  @Override
  public void dropDatabase(String database) {
    try {
      catalog.dropDatabase(database, false, false);
    } catch (Catalog.DatabaseNotExistException e) {
      throw new NoSuchDatabaseException(e);
    } catch (Catalog.DatabaseNotEmptyException e) {
      throw new DatabaseNotEmptyException(e);
    }
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    try {
      return new PaimonTable(
          TableIdentifier.of(name, database, table),
          catalog.getTable(Identifier.create(database, table)));
    } catch (Catalog.TableNotExistException e) {
      throw new NoSuchTableException(e);
    }
  }

  @Override
  public List<String> listTables(String database) {
    try {
      return catalog.listTables(database);
    } catch (Catalog.DatabaseNotExistException e) {
      throw new NoSuchDatabaseException(e);
    }
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    try {
      catalog.dropTable(Identifier.create(database, table), purge);
      return true;
    } catch (Catalog.TableNotExistException e) {
      return false;
    }
  }
}
