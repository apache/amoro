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

package org.apache.amoro.spark.test.unified;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.UnifiedCatalog;
import org.apache.amoro.UnifiedCatalogLoader;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.spark.SparkUnifiedSessionCatalog;
import org.apache.amoro.spark.test.SparkTestBase;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Map;

public class UnifiedCatalogTestSuites extends SparkTestBase {

  @Override
  protected Map<String, String> sparkSessionConfig() {
    return ImmutableMap.of(
        "spark.sql.catalog.spark_catalog",
        SparkUnifiedSessionCatalog.class.getName(),
        "spark.sql.catalog.spark_catalog.uri",
        CONTEXT.amsCatalogUrl(null));
  }

  public static List<Arguments> testTableFormats() {
    return Lists.newArrayList(
        Arguments.of(TableFormat.ICEBERG, false),
        Arguments.of(TableFormat.MIXED_ICEBERG, false),
        Arguments.of(TableFormat.MIXED_HIVE, false),
        Arguments.of(TableFormat.PAIMON, false),
        Arguments.of(TableFormat.ICEBERG, true),
        Arguments.of(TableFormat.MIXED_ICEBERG, true),
        Arguments.of(TableFormat.MIXED_HIVE, true),
        Arguments.of(TableFormat.PAIMON, true));
  }

  public void testTableFormats(TableFormat format, boolean sessionCatalog) {
    setCatalog(format, sessionCatalog);
    String sqlText =
        "CREATE TABLE "
            + target()
            + " ( "
            + "id int, "
            + "data string, "
            + "pt string"
            + pkDDL(format)
            + ") USING "
            + provider(format)
            + " PARTITIONED BY (pt) ";
    sql(sqlText);
    int expect = 0;
    if (TableFormat.PAIMON != format || !spark().version().startsWith("3.1")) {
      // write is not supported in spark3-1
      sqlText =
          "INSERT INTO "
              + target()
              + " VALUES "
              + "(1, 'a', '2020-01-01'), (2, 'b', '2020-01-02'), (3, 'c', '2020-01-03')";
      sql(sqlText);
      expect = 3;
    }

    sqlText = "SELECT * FROM " + target();
    long count = sql(sqlText).count();
    Assertions.assertEquals(expect, count);

    // visit sub tables.
    testVisitSubTable(format, sessionCatalog);

    // call procedure
    testCallProcedure(format);

    sql("DROP TABLE " + target() + " PURGE");
    Assertions.assertFalse(unifiedCatalog().tableExists(target().database, target().table));
  }

  private String pkDDL(TableFormat format) {
    if (TableFormat.MIXED_HIVE == format || TableFormat.MIXED_ICEBERG == format) {
      return ", primary key(id)";
    }
    return "";
  }

  private List<String> icebergInspectTableNames() {
    List<String> inspectTableNames =
        Lists.newArrayList(
            "history",
            "metadata_log_entries",
            "snapshots",
            "files",
            "manifests",
            "partitions",
            "all_data_files",
            "all_manifests",
            "refs");

    inspectTableNames.add("at_timestamp_" + System.currentTimeMillis());

    AmoroTable<?> table = unifiedCatalog().loadTable(target().database, target().table);
    String snapshotId = table.currentSnapshot().id();
    inspectTableNames.add("snapshot_id_" + snapshotId);
    return inspectTableNames;
  }

  private List<String> mixedFormatSubTableNames() {
    return Lists.newArrayList("change");
  }

  private void testVisitSubTable(TableFormat format, boolean sessionCatalog) {
    if (spark().version().startsWith("3.1") && sessionCatalog) {
      // sub table identifier is not supported in spark 3.1 session catalog
      return;
    }

    List<String> subTableNames = Lists.newArrayList();
    switch (format) {
      case ICEBERG:
        subTableNames = icebergInspectTableNames();
        break;
      case MIXED_ICEBERG:
      case MIXED_HIVE:
        subTableNames = mixedFormatSubTableNames();
        break;
    }

    for (String inspectTableName : subTableNames) {
      Dataset<Row> rs = sql("SELECT * FROM " + target() + "." + inspectTableName);
      Assertions.assertTrue(rs.columns().length > 0);
    }
  }

  private void testCallProcedure(TableFormat format) {
    if (TableFormat.ICEBERG != format) {
      // only tests for iceberg
      return;
    }
    String sqlText = "CALL " + currentCatalog + ".system.remove_orphan_files('" + target() + "')";
    Dataset<Row> rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length > 0);
  }

  private void setCatalog(TableFormat format, boolean sessionCatalog) {
    if (sessionCatalog) {
      setCurrentCatalog(SPARK_SESSION_CATALOG);
    } else {
      String unifiedSparkCatalogName = "unified_" + format.name().toLowerCase();
      setCurrentCatalog(unifiedSparkCatalogName);
    }
    UnifiedCatalog unifiedCatalog =
        UnifiedCatalogLoader.loadUnifiedCatalog(
            CONTEXT.amsThriftUrl(), format.name().toLowerCase(), Maps.newHashMap());
    if (!unifiedCatalog().databaseExists(database())) {
      unifiedCatalog.createDatabase(database());
    }
  }
}
