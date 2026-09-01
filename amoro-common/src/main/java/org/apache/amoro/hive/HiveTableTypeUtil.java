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

package org.apache.amoro.hive;

import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.apache.thrift.TException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Utilities for classifying Hive Metastore table objects. */
public final class HiveTableTypeUtil {

  private static final String VIRTUAL_VIEW_TYPE = "VIRTUAL_VIEW";
  private static final String MATERIALIZED_VIEW_TYPE = "MATERIALIZED_VIEW";
  private static final List<String> VIEW_TYPES =
      Collections.unmodifiableList(Arrays.asList(VIRTUAL_VIEW_TYPE, MATERIALIZED_VIEW_TYPE));

  private HiveTableTypeUtil() {}

  /** Returns the Hive table type names representing virtual or materialized views. */
  public static List<String> viewTypes() {
    return VIEW_TYPES;
  }

  /** Returns whether the Hive table is a virtual or materialized view. */
  public static boolean isView(Table table) {
    return table != null && isViewType(table.getTableType());
  }

  /** Returns whether the lightweight Hive table metadata describes a view. */
  public static boolean isView(TableMeta table) {
    return table != null && isViewType(table.getTableType());
  }

  /**
   * Returns the normalized names of Hive views among the candidate tables.
   *
   * <p>The lightweight table metadata API is preferred. Clients that do not implement that API fall
   * back to loading the candidate table objects.
   *
   * @param client Hive Metastore client
   * @param database database containing the candidate tables
   * @param candidateTableNames table names to inspect
   * @return lowercase names of virtual and materialized views
   */
  public static Set<String> listViewNames(
      HMSClient client, String database, List<String> candidateTableNames) throws TException {
    if (candidateTableNames == null || candidateTableNames.isEmpty()) {
      return Collections.emptySet();
    }

    List<TableMeta> views;
    try {
      views = client.getTableMeta(database, "*", viewTypes());
    } catch (UnsupportedOperationException e) {
      return listViewNamesFromTables(client, database, candidateTableNames);
    }

    if (views == null) {
      return listViewNamesFromTables(client, database, candidateTableNames);
    }
    return views.stream()
        .filter(HiveTableTypeUtil::isView)
        .map(TableMeta::getTableName)
        .filter(name -> name != null)
        .map(name -> name.toLowerCase(Locale.ROOT))
        .collect(Collectors.toCollection(HashSet::new));
  }

  private static Set<String> listViewNamesFromTables(
      HMSClient client, String database, List<String> candidateTableNames) throws TException {
    List<Table> tables = client.getTableObjectsByName(database, candidateTableNames);
    if (tables == null) {
      throw new IllegalStateException(
          "Hive Metastore returned null while loading table objects from database: " + database);
    }
    return tables.stream()
        .filter(HiveTableTypeUtil::isView)
        .map(Table::getTableName)
        .filter(name -> name != null)
        .map(name -> name.toLowerCase(Locale.ROOT))
        .collect(Collectors.toCollection(HashSet::new));
  }

  private static boolean isViewType(String tableType) {
    return VIRTUAL_VIEW_TYPE.equalsIgnoreCase(tableType)
        || MATERIALIZED_VIEW_TYPE.equalsIgnoreCase(tableType);
  }
}
