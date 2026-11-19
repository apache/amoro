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

import org.apache.amoro.NoSuchDatabaseException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.apache.thrift.TException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Utilities for classifying Hive Metastore table objects. */
public final class HiveTableTypeUtil {

  private static final String VIRTUAL_VIEW_TYPE = "VIRTUAL_VIEW";
  private static final String MATERIALIZED_VIEW_TYPE = "MATERIALIZED_VIEW";
  private static final List<String> VIEW_TYPES =
      Collections.unmodifiableList(Arrays.asList(VIRTUAL_VIEW_TYPE, MATERIALIZED_VIEW_TYPE));

  private static final String MIXED_TABLE_FLAG = "arctic.enabled";
  private static final String SPARK_SOURCE_PROVIDER = "spark.sql.sources.provider";
  private static final String FLINK_CONNECTOR = "connector";
  private static final String TABLE_TYPE = "table_type";
  private static final Set<String> DECLARED_NON_HIVE_FORMATS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList("PAIMON", "ICEBERG", "LANCE")));

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

    List<TableMeta> views = client.getTableMeta(database, "*", viewTypes());
    if (views == null) {
      throw new IllegalStateException(
          "Hive Metastore returned null while loading table metadata from database: " + database);
    }
    return views.stream()
        .filter(HiveTableTypeUtil::isView)
        .map(TableMeta::getTableName)
        .filter(name -> name != null)
        .map(name -> name.toLowerCase(Locale.ROOT))
        .collect(Collectors.toCollection(HashSet::new));
  }

  private static boolean isViewType(String tableType) {
    return VIRTUAL_VIEW_TYPE.equalsIgnoreCase(tableType)
        || MATERIALIZED_VIEW_TYPE.equalsIgnoreCase(tableType);
  }

  /** Returns whether a physical table in Hive Metastore belongs to the HIVE format. */
  public static boolean isHiveTable(Table table) {
    if (table == null || isView(table)) {
      return false;
    }

    Map<String, String> parameters = table.getParameters();
    if (parameters == null) {
      return true;
    }

    String declaredFormat = parameters.get(TABLE_TYPE);
    if (declaredFormat != null
        && DECLARED_NON_HIVE_FORMATS.contains(declaredFormat.toUpperCase(Locale.ROOT))) {
      return false;
    }

    return !Boolean.parseBoolean(parameters.get(MIXED_TABLE_FLAG)) && !isHudi(parameters);
  }

  /** Lists HIVE format table names of a database, excluding views. */
  public static List<String> listHiveTables(HMSClientPool clients, String database) {
    try {
      return clients.run(
          client -> {
            List<String> tableNames = client.getAllTables(database);
            if (tableNames == null || tableNames.isEmpty()) {
              return Collections.emptyList();
            }

            return client.getTableObjectsByName(database, tableNames).stream()
                .filter(HiveTableTypeUtil::isHiveTable)
                .map(Table::getTableName)
                .collect(Collectors.toList());
          });
    } catch (NoSuchObjectException e) {
      throw new NoSuchDatabaseException("Database " + database + " does not exist", e);
    } catch (TException e) {
      throw new RuntimeException("Failed to list tables of database " + database, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted in call to list tables of " + database, e);
    }
  }

  public static boolean isHudi(Map<String, String> parameters) {
    return parameters != null
        && ("hudi".equalsIgnoreCase(parameters.get(SPARK_SOURCE_PROVIDER))
            || "hudi".equalsIgnoreCase(parameters.get(FLINK_CONNECTOR)));
  }
}
