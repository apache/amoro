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

package com.netease.arctic.spark.test.unified;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.test.SparkTestBase;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class UnifiedCatalogTestSuites extends SparkTestBase {

  public void testTableFormats(TableFormat format) {
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
    testVisitSubTable(format);

    // call procedure
    testCallProcedure(format);

    sql("DROP TABLE " + target() + " PURGE");
    Assertions.assertFalse(unifiedCatalog().exist(target().database, target().table));
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

  private void testVisitSubTable(TableFormat format) {
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
}
