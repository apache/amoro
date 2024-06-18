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

package org.apache.amoro.spark.util;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.connector.catalog.CatalogManager;
import org.apache.spark.sql.connector.catalog.CatalogPlugin;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.TableCatalog;

import java.util.List;

/** Common util class of spark engines. */
public class SparkUtil {

  /**
   * A modified version of Spark's LookupCatalog.CatalogAndIdentifier.unapply Attempts to find the
   * catalog and identifier a multipart identifier represents
   *
   * @param spark Spark session to use for resolution
   * @param nameParts Multipart identifier representing a table
   * @param defaultCatalog Catalog to use if none is specified
   * @return The CatalogPlugin and Identifier for the table
   */
  public static TableCatalogAndIdentifier catalogAndIdentifier(
      SparkSession spark, List<String> nameParts, TableCatalog defaultCatalog) {
    CatalogManager catalogManager = spark.sessionState().catalogManager();

    String[] currentNamespace;
    if (defaultCatalog.equals(catalogManager.currentCatalog())) {
      currentNamespace = catalogManager.currentNamespace();
    } else {
      currentNamespace = defaultCatalog.defaultNamespace();
    }

    Preconditions.checkArgument(
        !nameParts.isEmpty(), "Cannot determine catalog and identifier from empty name");
    int lastElementIndex = nameParts.size() - 1;
    String name = nameParts.get(lastElementIndex);
    if (nameParts.size() == 1) {
      return new TableCatalogAndIdentifier(defaultCatalog, Identifier.of(currentNamespace, name));
    }

    CatalogPlugin catalogPlugin = null;
    try {
      catalogPlugin = catalogManager.catalog(nameParts.get(0));
    } catch (Exception e) {
      // pass
    }

    if (catalogPlugin == null) {
      // The first element was not a valid catalog, treat it like part of the namespace
      String[] namespace = nameParts.subList(0, lastElementIndex).toArray(new String[0]);
      return new TableCatalogAndIdentifier(defaultCatalog, Identifier.of(namespace, name));
    } else {
      // Assume the first element is a valid catalog
      TableCatalog tableCatalog = (TableCatalog) catalogPlugin;
      String[] namespace = nameParts.subList(1, lastElementIndex).toArray(new String[0]);
      return new TableCatalogAndIdentifier(tableCatalog, Identifier.of(namespace, name));
    }
  }

  /**
   * Util class to help resolve a multi name parts which represent both catalog and table identifier
   */
  public static class TableCatalogAndIdentifier {
    TableCatalog tableCatalog;
    Identifier identifier;

    public TableCatalogAndIdentifier(TableCatalog tableCatalog, Identifier identifier) {
      this.tableCatalog = tableCatalog;
      this.identifier = identifier;
    }

    public TableCatalog catalog() {
      return this.tableCatalog;
    }

    public Identifier identifier() {
      return this.identifier;
    }
  }
}
