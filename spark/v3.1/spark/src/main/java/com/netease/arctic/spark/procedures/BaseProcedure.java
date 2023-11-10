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

package com.netease.arctic.spark.procedures;

import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.utils.CompatibleHivePropertyUtil;
import com.netease.arctic.spark.actions.SparkActions;
import com.netease.arctic.spark.table.ArcticSparkTable;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.connector.catalog.CatalogPlugin;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.iceberg.catalog.Procedure;

abstract class BaseProcedure implements Procedure {

  protected final SparkSession spark;
  protected final TableCatalog tableCatalog;
  private SparkActions actions;

  protected BaseProcedure(TableCatalog tableCatalog) {
    this.spark = SparkSession.active();
    this.tableCatalog = tableCatalog;
  }

  protected SparkActions actions() {
    if (actions == null) {
      this.actions = SparkActions.get(spark);
    }
    return actions;
  }

  protected Identifier toIdentifier(String identifierAsString, String argName) {
    Spark3Util.CatalogAndIdentifier catalogAndIdentifier =
        toCatalogAndIdentifier(identifierAsString, argName, tableCatalog);

    Preconditions.checkArgument(
        catalogAndIdentifier.catalog().equals(tableCatalog),
        "Cannot run procedure in catalog '%s': '%s' is a table in catalog '%s'",
        tableCatalog.name(),
        identifierAsString,
        catalogAndIdentifier.catalog().name());

    return catalogAndIdentifier.identifier();
  }

  protected Spark3Util.CatalogAndIdentifier toCatalogAndIdentifier(
      String identifierAsString, String argName, CatalogPlugin catalog) {
    Preconditions.checkArgument(
        identifierAsString != null && !identifierAsString.isEmpty(),
        "Cannot handle an empty identifier for argument %s",
        argName);

    return Spark3Util.catalogAndIdentifier(
        "identifier for arg " + argName, spark, identifierAsString, catalog);
  }

  protected ArcticSparkTable loadSparkTable(Identifier ident) {
    try {
      Table table = tableCatalog.loadTable(ident);
      ValidationException.check(
          table instanceof ArcticSparkTable, "%s is not %s", ident, ArcticSparkTable.class.getName());
      return (ArcticSparkTable) table;
    } catch (NoSuchTableException e) {
      String errMsg =
          String.format("Couldn't load table '%s' in catalog '%s'", ident, tableCatalog.name());
      throw new RuntimeException(errMsg, e);
    }
  }

  protected boolean isArcticTable(Table table) {
    return table.properties() != null &&
        CompatibleHivePropertyUtil.propertyAsBoolean(
            table.properties(), HiveTableProperties.ARCTIC_TABLE_FLAG, false);
  }
}
