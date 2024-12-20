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
import org.apache.amoro.spark.test.SparkTestBase;
import org.apache.amoro.spark.test.TestIdentifier;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.Table;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UnifiedCatalogTestSuites extends SparkTestBase {

  @Override
  protected Map<String, String> sparkSessionConfig() {
    return ImmutableMap.of(
        "spark.sql.catalog.spark_catalog",
        "org.apache.amoro.spark.SparkUnifiedSessionCatalog",
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
            + "id int not null, "
            + "data string, "
            + "pt string"
            + pkDDL(format)
            + ") USING "
            + provider(format)
            + " PARTITIONED BY (pt) ";
    sql(sqlText);
    int expect = 0;
    if (!TableFormat.PAIMON.equals(format) || !spark().version().startsWith("3.1")) {
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

    // create and drop namespace
    testNamespaceWithSql();

    // visit sub tables.
    testVisitSubTable(format, sessionCatalog);

    // call procedure
    testCallProcedure(format);

    // alter table test
    testIcebergAlterTable(format, target(), "id");
    testIcebergUpdate(format, target());
    testIcebergDeleteInSubQuery(format, target());
    testIcebergDropPartitionField(format, target(), "pt");
    testIcebergMetadata(format, target());

    sql("DROP TABLE " + target() + " PURGE");
    Assertions.assertFalse(unifiedCatalog().tableExists(target().database, target().table));
  }

  private void testNamespaceWithSql() {
    // Use SparkTestBase::sql method to test SparkUnifiedCatalog instead of CommonUnifiedCatalog.
    String createDatabase = "CREATE DATABASE test_unified_catalog";
    sql(createDatabase);
    Assertions.assertTrue(unifiedCatalog().databaseExists("test_unified_catalog"));

    String dropDatabase = "DROP DATABASE test_unified_catalog";
    sql(dropDatabase);
    Assertions.assertFalse(unifiedCatalog().databaseExists("test_unified_catalog"));
  }

  private String pkDDL(TableFormat format) {
    if (TableFormat.MIXED_HIVE.equals(format) || TableFormat.MIXED_ICEBERG.equals(format)) {
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
    if (TableFormat.ICEBERG.equals(format)) {
      subTableNames = icebergInspectTableNames();
    } else if (format.in(TableFormat.MIXED_HIVE, TableFormat.MIXED_ICEBERG)) {
      subTableNames = mixedFormatSubTableNames();
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

  private void testIcebergAlterTable(
      TableFormat format, TestIdentifier targetTable, String fieldName) {
    if (TableFormat.ICEBERG != format) {
      // only tests for iceberg
      return;
    }
    // set identifier fields
    String sqlText =
        String.format("alter table %s set identifier fields  %s", targetTable, fieldName);
    Dataset<Row> rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    AmoroTable<?> icebergTable =
        unifiedCatalog().loadTable(targetTable.database, targetTable.table);
    Table table = (Table) icebergTable.originalTable();
    Assertions.assertTrue(table.schema().identifierFieldNames().contains(fieldName));

    // drop identifier fields/
    sqlText = String.format("alter table %s DROP IDENTIFIER FIELDS  %s", targetTable, fieldName);
    rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    icebergTable = unifiedCatalog().loadTable(targetTable.database, targetTable.table);
    table = (Table) icebergTable.originalTable();
    Assertions.assertFalse(table.schema().identifierFieldNames().contains(fieldName));
  }

  private void testIcebergUpdate(TableFormat format, TestIdentifier targetTable) {
    if (TableFormat.ICEBERG != format) {
      // only tests for iceberg
      return;
    }
    String afterValue = "update-xxx";
    // set identifier fields
    String sqlText = String.format("update %s set data='%s' where id=1", targetTable, afterValue);
    Dataset<Row> rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    // drop identifier fields/
    sqlText = String.format("select * from  %s where id=1", targetTable);
    rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 3 && rs.head().getString(1).equals(afterValue));
  }

  private void testIcebergDeleteInSubQuery(TableFormat format, TestIdentifier targetTable) {
    if (TableFormat.ICEBERG != format) {
      // only tests for iceberg
      return;
    }
    // set identifier fields
    String sqlText = String.format("select min(id) from %s", targetTable);
    Dataset<Row> rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 1);
    Integer minId = rs.head().getInt(0);

    // set identifier fields
    sqlText =
        String.format(
            "delete from %s where id=(select min(id) from %s);", targetTable, targetTable);
    rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    // drop identifier fields/
    sqlText = String.format("select * from  %s where id=%d", targetTable, minId);
    rs = sql(sqlText);
    Assertions.assertTrue(rs.count() == 0);
  }

  private void testIcebergDropPartitionField(
      TableFormat format, TestIdentifier targetTable, String fieldName) {
    if (TableFormat.ICEBERG != format) {
      // only tests for iceberg
      return;
    }
    // drop partition field
    String sqlText =
        String.format("alter table %s drop partition field  %s", targetTable, fieldName);
    Dataset<Row> rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    AmoroTable<?> icebergTable =
        unifiedCatalog().loadTable(targetTable.database, targetTable.table);
    Table table = (Table) icebergTable.originalTable();
    Assertions.assertTrue(
        Optional.empty()
            .equals(
                table.spec().fields().stream()
                    .filter(item -> item.name().equals(fieldName))
                    .findAny()));
  }

  private void testIcebergMetadata(TableFormat format, TestIdentifier targetTable) {
    if (TableFormat.ICEBERG != format) {
      // only tests for iceberg
      return;
    }
    // drop partition field
    String tagKey = "tag-unittest";
    String sqlText =
        String.format("alter table %s create tag if not exists `%s`", targetTable, tagKey);
    Dataset<Row> rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    AmoroTable<?> icebergTable =
        unifiedCatalog().loadTable(targetTable.database, targetTable.table);
    Table table = (Table) icebergTable.originalTable();
    Map<String, SnapshotRef> refs = table.refs();
    Assertions.assertTrue(refs != null && refs.containsKey(tagKey));

    sqlText = String.format("alter table %s drop tag `%s`", targetTable, tagKey);
    rs = sql(sqlText);
    Assertions.assertTrue(rs.columns().length == 0);

    icebergTable = unifiedCatalog().loadTable(targetTable.database, targetTable.table);
    table = (Table) icebergTable.originalTable();
    refs = table.refs();
    Assertions.assertTrue(refs != null && !refs.containsKey(tagKey));
  }
}
