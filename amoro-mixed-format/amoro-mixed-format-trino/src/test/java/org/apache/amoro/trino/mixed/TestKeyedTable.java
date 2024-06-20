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

package org.apache.amoro.trino.mixed;

import static org.apache.amoro.MockAmoroManagementServer.TEST_CATALOG_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import io.trino.testing.QueryRunner;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class TestKeyedTable extends TableTestBaseWithInitDataForTrino {

  private static final String CATALOG_DATABASE_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX + "test_db.";
  private static final String PK_TABLE_FULL_NAME = CATALOG_DATABASE_NAME + "test_pk_table";
  private static final String PK_TABLE_BASE_NAME = CATALOG_DATABASE_NAME + "\"test_pk_table#base\"";
  private static final String PK_TABLE_CHANGE_NAME =
      CATALOG_DATABASE_NAME + "\"test_pk_table#change\"";

  @Override
  protected QueryRunner createQueryRunner() throws Exception {
    CatalogTestHelper testCatalog = TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG);
    setupCatalog(testCatalog);

    setupTables();
    initData();
    return MixedFormatQueryRunner.builder()
        .setIcebergProperties(
            ImmutableMap.of(
                "amoro.url",
                String.format("thrift://localhost:%s/%s", AMS.port(), TEST_CATALOG_NAME),
                "mixed-format.enable-split-task-by-delete-ratio",
                "true"))
        .build();
  }

  @Test
  public void testStats() {
    assertThat(query("SHOW STATS FOR " + PK_TABLE_FULL_NAME))
        .skippingTypesCheck()
        .matches(
            "VALUES "
                + "('id', NULL, NULL, 0e0, NULL, '1', '4'), "
                + "('name$name', 548e0, NULL, 0e0, NULL, NULL, NULL), "
                + "('op_time', NULL, NULL, 0e0, NULL, '2022-01-01 12:00:00.000000', '2022-01-04 12:00:00.000000'), "
                + "(NULL, NULL, NULL, NULL, 4e0, NULL, NULL)");
  }

  @Test
  public void tableMOR() throws InterruptedException {
    assertQuery("select id from " + PK_TABLE_FULL_NAME, "VALUES 1, 2, 3, 6");
  }

  @Test
  public void tableMORWithProject() throws InterruptedException {
    assertQuery(
        "select op_time, \"name$name\" from " + PK_TABLE_FULL_NAME,
        "VALUES (TIMESTAMP '2022-01-01 12:00:00', 'john'), "
            + "(TIMESTAMP'2022-01-02 12:00:00', 'lily'), "
            + "(TIMESTAMP'2022-01-03 12:00:00', 'jake'), "
            + "(TIMESTAMP'2022-01-01 12:00:00', 'mack')");
  }

  @Test
  public void baseQuery() {
    assertQuery("select id from " + PK_TABLE_BASE_NAME, "VALUES 1, 2, 3");
  }

  @Test
  public void baseQueryWhenTableNameContainCatalogAndDataBase() {
    assertQuery(
        "select id from "
            + CATALOG_DATABASE_NAME
            + "\"test_mixed_format_catalog.test_db.test_pk_table#base\"",
        "VALUES 1, 2, 3");
  }

  @Test
  public void baseQueryWhenTableNameContainDataBase() {
    assertQuery(
        "select id from " + CATALOG_DATABASE_NAME + "\"test_db.test_pk_table#base\"",
        "VALUES 1, 2, 3");
  }

  @Test
  public void changeQuery() {
    assertQuery(
        "select * from " + PK_TABLE_CHANGE_NAME,
        "VALUES (6,'mack',TIMESTAMP '2022-01-01 12:00:00.000000' ,3,1,'INSERT'),"
            + "(5,'mary',TIMESTAMP '2022-01-01 12:00:00.000000',2,1,'INSERT'),"
            + "(5,'mary',TIMESTAMP '2022-01-01 12:00:00.000000',4,1,'DELETE')");
  }

  @Test
  public void changeQueryWhenTableNameContainCatalogAndDataBase() {
    assertQuery(
        "select * from "
            + CATALOG_DATABASE_NAME
            + "\"test_mixed_format_catalog.test_db.test_pk_table#change\"",
        "VALUES (6,'mack',TIMESTAMP '2022-01-01 12:00:00.000000' ,3,1,'INSERT'),"
            + "(5,'mary',TIMESTAMP '2022-01-01 12:00:00.000000',2,1,'INSERT'),"
            + "(5,'mary',TIMESTAMP '2022-01-01 12:00:00.000000',4,1,'DELETE')");
  }

  @Test
  public void changeQueryWhenTableNameContainDataBase() {
    assertQuery(
        "select * from " + CATALOG_DATABASE_NAME + "\"test_db.test_pk_table#change\"",
        "VALUES (6,'mack',TIMESTAMP '2022-01-01 12:00:00.000000' ,3,1,'INSERT'),"
            + "(5,'mary',TIMESTAMP '2022-01-01 12:00:00.000000',2,1,'INSERT'),"
            + "(5,'mary',TIMESTAMP '2022-01-01 12:00:00.000000',4,1,'DELETE')");
  }

  @AfterClass(alwaysRun = true)
  public void clear() {
    clearTable();
  }
}
