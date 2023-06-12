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

package com.netease.arctic.catalog;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableFormatOperations;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;

import java.util.List;

public interface CatalogOperations {

  /**
   * Show database list of metastore.
   *
   * @return database list of metastore
   */
  List<String> listDatabases();


  /**
   * Check whether database exists.
   *
   * @param database a database name
   * @return true if the database exists, false otherwise
   */
  boolean exist(String database);

  /**
   * Check whether table exists.
   *
   * @param database a database name
   * @param table a table name
   * @return true if the table exists, false otherwise
   */
  boolean exist(String database, String table);


  /**
   * create database catalog.
   *
   * @param database database name
   * @throws AlreadyExistsException when database already exists.
   */
  void createDatabase(String database);

  /**
   * drop database from catalog.
   *
   * @param database database name
   * @throws NoSuchNamespaceException when database not exists.
   */
  void dropDatabase(String database);


  /**
   * get the table list in database
   * @param database - database to list from
   * @return table meta lists
   */
  List<CatalogTableMeta>  listTables(String database);

  /**
   * get the format of table.
   * @param database a database name
   * @param table a table name
   * @return the table-format of given table.
   * @throws org.apache.iceberg.exceptions.NoSuchTableException when table not exists.
   */
  TableFormat tableFormat(String database, String table);

  /**
   * load table
   * @param database a database name
   * @param table a table name
   * @return instance of {@link com.netease.arctic.table.ArcticTable}
   *   implementation referred by {@code database}.{@code table}
   */
  ArcticTable loadTable(String database, String table);

  /**
   * get the table format operations for this format
   * @param format table format
   * @return table format operations
   */
  TableFormatOperations formatOperations(TableFormat format);
}
