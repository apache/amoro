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

package org.apache.amoro.server.optimizing.maintainer;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.data.Record;
import org.junit.ClassRule;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.Map;

public class TestDataExpireHive extends TestDataExpire {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true, getDefaultProp())
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false, getDefaultProp())
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true, getDefaultProp())
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false, getDefaultProp())
      }
    };
  }

  public TestDataExpireHive(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Override
  protected Record createRecord(int id, String name, long ts, String opTime) {
    return MixedDataTestHelpers.createRecord(
        getMixedTable().schema(),
        id,
        name,
        ts,
        opTime,
        opTime + "Z",
        new BigDecimal("0"),
        opTime.substring(0, 10));
  }

  protected static Map<String, String> getDefaultProp() {
    return ImmutableMap.of(
        TableProperties.ENABLE_DATA_EXPIRATION, "true",
        TableProperties.DATA_EXPIRATION_FIELD, "op_time_day",
        TableProperties.DATA_EXPIRATION_RETENTION_TIME, "1d");
  }
}
