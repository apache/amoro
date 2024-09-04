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

package org.apache.amoro.spark.test;

import org.apache.amoro.table.TableIdentifier;

public class TestIdentifier {
  public static final String SOURCE_TYPE_HIVE = "hive";
  public static final String SOURCE_TYPE_ARCTIC = "arctic";
  public static final String SOURCE_TYPE_VIEW = "view";

  public final String database;
  public final String table;
  public final String catalog;
  public final String sourceType;
  private final String amsCatalogName;

  private TestIdentifier(
      String catalog, String amsCatalogName, String database, String table, String sourceType) {
    this.database = database;
    this.table = table;
    this.catalog = catalog;
    this.sourceType = sourceType;
    this.amsCatalogName = amsCatalogName;
  }

  public static TestIdentifier ofHiveSource(String database, String table) {
    return new TestIdentifier(null, null, database, table, SOURCE_TYPE_HIVE);
  }

  public static TestIdentifier ofViewSource(String table) {
    return new TestIdentifier(null, null, null, table, SOURCE_TYPE_VIEW);
  }

  public static TestIdentifier ofDataLake(
      String sparkCatalog, String amsCatalog, String database, String table, boolean isSource) {
    String source = isSource ? SOURCE_TYPE_ARCTIC : null;
    return new TestIdentifier(sparkCatalog, amsCatalog, database, table, source);
  }

  public TableIdentifier toAmoroIdentifier() {
    return TableIdentifier.of(amsCatalogName, database, table);
  }

  public org.apache.spark.sql.connector.catalog.Identifier toSparkIdentifier() {
    return org.apache.spark.sql.connector.catalog.Identifier.of(new String[] {database}, table);
  }

  @Override
  public String toString() {
    if (SOURCE_TYPE_VIEW.equalsIgnoreCase(sourceType)) {
      return table;
    }
    return database + "." + table;
  }
}
