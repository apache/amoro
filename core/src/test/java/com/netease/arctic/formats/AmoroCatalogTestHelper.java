/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats;

import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import org.apache.hadoop.conf.Configuration;

/**
 * Helper Interface for testing AmoroCatalog.
 */
public interface AmoroCatalogTestHelper<T> {

  /**
   * Table option key when using {@link #createTable(String, String)} to create table.
   */
  String DEFAULT_TABLE_OPTION_KEY = "amoro.test.key";

  /**
   * Table option value when using {@link #createTable(String, String)} to create table.
   */
  String DEFAULT_TABLE_OPTION_VALUE = "amoro.test.value";

  /**
   * Table column named 'id' when using {@link #createTable(String, String)} to create table.
   */
  String DEFAULT_SCHEMA_ID_NAME = "id";

  /**
   * Table column named 'name' when using {@link #createTable(String, String)} to create table.
   */
  String DEFAULT_SCHEMA_NAME_NAME = "name";

  /**
   * Table column named 'age' when using {@link #createTable(String, String)} to create table.
   */
  String DEFAULT_SCHEMA_AGE_NAME = "age";

  /**
   * Will be called first to inject the warehouse location.
   */
  void initWarehouse(String warehouseLocation);

  /**
   * Will be called first to inject the hive configuration.
   */
  void initHiveConf(Configuration hiveConf);

  /**
   * Get the {@link CatalogMeta} for the catalog.
   */
  CatalogMeta getCatalogMeta();

  /**
   * Get the {@link AmoroCatalog} for the catalog.
   */
  AmoroCatalog amoroCatalog();

  /**
   * Get the original catalog for the catalog. It will be paimon catalog or iceberg catalog or other.
   */
  T originalCatalog();

  /**
   * Get the catalog name.
   */
  String catalogName();

  /**
   * Clean the catalog. drop databases and tables.
   */
  void clean();

  /**
   * Create a table. The schema, properties, etc. of a table depend on its implementation.
   */
  void createTable(String db, String tableName)
      throws Exception;
}
