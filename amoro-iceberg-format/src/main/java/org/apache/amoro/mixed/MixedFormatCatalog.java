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

package org.apache.amoro.mixed;

import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.table.blocker.TableBlockerManager;
import org.apache.iceberg.Schema;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.exceptions.NoSuchTableException;

import java.util.List;
import java.util.Map;

/** Catalog for mixed-format table create, drop, and load. */
public interface MixedFormatCatalog {

  /**
   * Return name of catalog
   *
   * @return catalog's name
   */
  String name();

  /**
   * Initialize a catalog given a custom name and a map of catalog properties. all catalog implement
   * must be no-args construct. Catalogs will call this method after implement object created.
   *
   * @param name name of catalog
   * @param properties client side catalog properties
   * @param metaStore auth context.
   */
  void initialize(String name, Map<String, String> properties, TableMetaStore metaStore);

  /**
   * Show database list of catalog.
   *
   * @return database list of catalog
   */
  List<String> listDatabases();

  /**
   * create database catalog.
   *
   * @param databaseName database name
   * @throws AlreadyExistsException when database already exist.
   * @throws NoSuchNamespaceException
   */
  void createDatabase(String databaseName);

  /**
   * drop database from catalog.
   *
   * @param databaseName database name
   */
  void dropDatabase(String databaseName);

  /**
   * Show table list of database.
   *
   * @param database database name
   * @return Table list of database in catalog
   */
  List<TableIdentifier> listTables(String database);

  /**
   * Get an mixed-format table by table identifier.
   *
   * @param tableIdentifier a table identifier
   * @return instance of {@link UnkeyedTable} or {@link KeyedTable} implementation referred by
   *     {@code tableIdentifier}
   */
  MixedTable loadTable(TableIdentifier tableIdentifier);

  /**
   * Check whether table exists.
   *
   * @param tableIdentifier a table identifier
   * @return true if the table exists, false otherwise
   */
  default boolean tableExists(TableIdentifier tableIdentifier) {
    try {
      loadTable(tableIdentifier);
      return true;
    } catch (NoSuchTableException e) {
      return false;
    }
  }

  /**
   * Rename a table.
   *
   * @param from identifier of the table to rename
   * @param newTableName new table name
   * @throws NoSuchTableException if the from table does not exist
   * @throws AlreadyExistsException if the to table already exists
   */
  void renameTable(TableIdentifier from, String newTableName);

  /**
   * Drop a table and delete all data and metadata files.
   *
   * @param tableIdentifier a table identifier
   * @param purge if true, delete all data and metadata files in the table
   * @return true if the table was dropped, false if the table did not exist
   */
  boolean dropTable(TableIdentifier tableIdentifier, boolean purge);

  /**
   * Instantiate a builder to build a table.
   *
   * @param identifier a table identifier
   * @param schema a schema
   * @return the builder to build a table
   */
  TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema);

  /**
   * Return a table blocker manager.
   *
   * @param tableIdentifier a table identifier
   * @return A Table Blocker Mana
   */
  TableBlockerManager getTableBlockerManager(TableIdentifier tableIdentifier);

  /**
   * Return catalog properties.
   *
   * @return properties
   */
  Map<String, String> properties();
}
