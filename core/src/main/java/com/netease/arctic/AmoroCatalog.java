/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic;

import java.util.List;

/** Base Catalog interface for Amoro. */
public interface AmoroCatalog {

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
   * @throws NoSuchDatabaseException when database not exists.
   */
  void dropDatabase(String database);

  /**
   * load table from catalog.
   *
   * @param database database name
   * @param table table name
   * @return table instance
   * @throws NoSuchDatabaseException when database not exists.
   * @throws NoSuchTableException when table not exists.
   */
  AmoroTable<?> loadTable(String database, String table);
}
