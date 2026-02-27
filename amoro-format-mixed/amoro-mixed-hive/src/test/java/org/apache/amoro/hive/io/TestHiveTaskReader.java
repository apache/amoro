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

package org.apache.amoro.hive.io;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.io.TestTaskReader;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Set;

@RunWith(Parameterized.class)
public class TestHiveTaskReader extends TestTaskReader {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestHiveTaskReader(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper, boolean useDiskMap) {
    super(catalogTestHelper, tableTestHelper, useDiskMap);
  }

  @Parameterized.Parameters(name = "useDiskMap = {2}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true),
        false
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true),
        true
      },
      // test primary key with timestamp type
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(
            HiveTableTestHelper.HIVE_TABLE_SCHEMA,
            PrimaryKeySpec.builderFor(HiveTableTestHelper.HIVE_TABLE_SCHEMA)
                .addColumn("id")
                .addColumn("op_time")
                .build(),
            HiveTableTestHelper.HIVE_SPEC,
            Maps.newHashMap()),
        false
      }
    };
  }

  @Test
  public void testMergeOnReadFilterTimestamptzType() {
    // where op_time_wz = '2022-01-01T12:00:00'
    Set<Record> records =
        Sets.newHashSet(
            tableTestHelper()
                .readKeyedTable(
                    getMixedTable().asKeyedTable(),
                    Expressions.equal("op_time_wz", "2022-01-01T12:00:00Z"),
                    null,
                    isUseDiskMap(),
                    false));
    // expect: (id=1),(id=6), change store cannot be filtered by partition now.
    Set<Record> expectRecords = Sets.newHashSet();
    expectRecords.add(allRecords.get(0));
    expectRecords.add(allRecords.get(5));
    Assert.assertEquals(expectRecords, records);

    // where op_time_wz > '2022-01-10T12:00:00'
    records =
        Sets.newHashSet(
            tableTestHelper()
                .readKeyedTable(
                    getMixedTable().asKeyedTable(),
                    Expressions.greaterThan("op_time_wz", "2022-02-01T12:00:00Z"),
                    null,
                    isUseDiskMap(),
                    false));
    // expect: (id=6), change store cannot be filtered by partition now.
    expectRecords.clear();
    expectRecords.add(allRecords.get(5));
    Assert.assertEquals(expectRecords, records);
  }

  @Test
  public void testMergeOnReadFilterPartitionValue() {
    // where op_time_day > '2022-01-10'
    Set<Record> records =
        Sets.newHashSet(
            tableTestHelper()
                .readKeyedTable(
                    getMixedTable().asKeyedTable(),
                    Expressions.greaterThan("op_time_day", "2022-01-10"),
                    null,
                    isUseDiskMap(),
                    false));
    // expect: empty, change store can only be filtered by partition expression now.
    Set<Record> expectRecords = Sets.newHashSet();
    Assert.assertEquals(expectRecords, records);
  }
}
