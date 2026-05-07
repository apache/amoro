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

package org.apache.amoro.flink.table;

import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.LOG_STORE_CATCH_UP;
import static org.apache.amoro.flink.table.descriptors.MixedFormatValidator.LOG_STORE_CATCH_UP_TIMESTAMP;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.flink.FlinkTestBase;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.UpdateProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

public class TestTableRefresh extends FlinkTestBase {
  static final TestHMS TEST_HMS = new TestHMS();

  static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true)),
        Arguments.of(
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)));
  }

  @BeforeAll
  public static void startTestHms() throws Exception {
    TEST_HMS.before();
  }

  @AfterAll
  public static void stopTestHms() {
    TEST_HMS.after();
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testRefresh(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper)
      throws Exception {
    initFlinkTestBase(catalogTestHelper, tableTestHelper);
    MixedFormatTableLoader tableLoader =
        MixedFormatTableLoader.of(TableTestHelper.TEST_TABLE_ID, catalogBuilder);

    tableLoader.open();
    MixedTable mixedTable = tableLoader.loadMixedFormatTable();
    boolean catchUp = true;
    String catchUpTs = "1";

    UpdateProperties updateProperties = mixedTable.updateProperties();
    updateProperties.set(LOG_STORE_CATCH_UP.key(), String.valueOf(catchUp));
    updateProperties.set(LOG_STORE_CATCH_UP_TIMESTAMP.key(), catchUpTs);
    updateProperties.commit();

    mixedTable.refresh();
    Map<String, String> properties = mixedTable.properties();
    Assertions.assertEquals(String.valueOf(catchUp), properties.get(LOG_STORE_CATCH_UP.key()));
    Assertions.assertEquals(catchUpTs, properties.get(LOG_STORE_CATCH_UP_TIMESTAMP.key()));
  }
}
