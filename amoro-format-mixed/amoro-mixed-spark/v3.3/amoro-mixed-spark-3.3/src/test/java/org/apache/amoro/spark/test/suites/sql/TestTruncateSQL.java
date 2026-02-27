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

package org.apache.amoro.spark.test.suites.sql;

import org.apache.amoro.TableFormat;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.extensions.EnableCatalogSelect;
import org.apache.amoro.spark.test.utils.TableFiles;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestTruncateSQL extends MixedTableTestBase {

  public static Stream<Arguments> testTruncateTable() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", ""),
        Arguments.of(TableFormat.MIXED_HIVE, "", ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", ""),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", ""),
        Arguments.of(TableFormat.MIXED_HIVE, ", PRIMARY KEY(id)", " PARTITIONED BY (day)"),
        Arguments.of(TableFormat.MIXED_HIVE, "", " PARTITIONED BY (day)"),
        Arguments.of(TableFormat.MIXED_ICEBERG, ", PRIMARY KEY(id)", " PARTITIONED BY (day)"),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", " PARTITIONED BY (day)"));
  }

  @DisplayName("Test `test truncate table`")
  @ParameterizedTest
  @MethodSource
  public void testTruncateTable(TableFormat format, String primaryKeyDDL, String partitionDDL) {
    String sqlText =
        "CREATE TABLE "
            + target()
            + " ( \n"
            + "id int, data string, day string "
            + primaryKeyDDL
            + " ) using "
            + provider(format)
            + partitionDDL;
    sql(sqlText);
    sql(
        "insert into "
            + target().database
            + "."
            + target().table
            + " values (1, 'a', 'a'), (2, 'b', 'b'), (3, 'c', 'c')");
    TableFiles files = TestTableUtil.files(loadTable());
    Assertions.assertEquals(3, files.totalFileCount());

    sql("truncate table " + target().database + "." + target().table);
    Dataset<Row> sql = sql("select * from " + target().database + "." + target().table);
    Assertions.assertEquals(0, sql.collectAsList().size());
    files = TestTableUtil.files(loadTable());
    Assertions.assertEquals(0, files.totalFileCount());
  }
}
