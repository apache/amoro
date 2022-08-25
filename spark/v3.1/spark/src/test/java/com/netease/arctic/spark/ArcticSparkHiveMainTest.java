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

package com.netease.arctic.spark;

import com.netease.arctic.spark.hive.TestCreateTableDDL;
import com.netease.arctic.spark.hive.TestMigrateHiveTable;
import com.netease.arctic.spark.source.TestKeyedTableDataFrameAPI;
import com.netease.arctic.spark.source.TestUnKeyedTableDataFrameAPI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestKeyedHiveInsertOverwriteDynamic.class,
    TestKeyedHiveInsertOverwriteStatic.class,
    TestUnkeyedHiveInsertOverwriteDynamic.class,
    TestUnkeyedHiveInsertOverwriteStatic.class,
    TestCreateTableDDL.class,
    TestMigrateHiveTable.class,
    TestHiveTableMergeOnRead.class,
    TestAlterKeyedTable.class,
    TestKeyedTableDDL.class,
    TestKeyedTableDML.class,
    TestKeyedTableDMLInsertOverwriteDynamic.class,
    TestKeyedTableDMLInsertOverwriteStatic.class,
    TestUnKeyedTableDDL.class,
    TestMigrateNonHiveTable.class,
    TestOptimizeWrite.class,
    TestUnKeyedTableDML.class,
    TestKeyedTableDataFrameAPI.class,
    TestUnKeyedTableDataFrameAPI.class,
    TestCreateKeyedTableAsSelect.class})
public class ArcticSparkHiveMainTest {

  @BeforeClass
  public static void suiteSetup() throws IOException {
    SparkTestContext.startAll();
  }

  @AfterClass
  public static void suiteTeardown() {
    SparkTestContext.stopAll();
  }
}
