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

package org.apache.amoro.server.optimizing.plan;

import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

/**
 * Runs all {@link TestHiveUnkeyedPartitionPlan} cases with RoaringBitmap planning enabled, and
 * adds a move-files-only regression case to make sure files written outside the hive location are
 * kept in the rewrite plan when {@link
 * TableProperties#SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES} is disabled, in RoaringBitmap mode as
 * well as in list mode. Unkeyed base files are written by the mixed engine outside the hive
 * location, so they are moved to the hive location instead of being filtered out.
 */
@RunWith(Parameterized.class)
public class TestRoaringBitmapHiveUnkeyedPartitionPlan extends TestHiveUnkeyedPartitionPlan {

  public TestRoaringBitmapHiveUnkeyedPartitionPlan(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Before
  @Override
  public void mock() {
    super.mock();
    enableRoaringBitmapInPlanning();
  }

  @Test
  public void testMoveFilesOnlyKeepsFilesOutsideHiveLocation() {
    closeFullOptimizingInterval();
    closeMinorOptimizingInterval();
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    OptimizingTestHelpers.appendBase(
        getMixedTable(),
        tableTestHelper()
            .writeBaseStore(getMixedTable(), beginTransaction(), newRecords, false));
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 5, 8, "2022-01-01T12:00:00");
    OptimizingTestHelpers.appendBase(
        getMixedTable(),
        tableTestHelper()
            .writeBaseStore(getMixedTable(), beginTransaction(), newRecords, false));

    // trigger full optimizing through the hive refresh interval
    updateTableProperty(HiveTableProperties.REFRESH_HIVE_INTERVAL, 1 + "");

    // with rewrite-all-files enabled, the two files outside the hive location are both rewritten
    List<RewriteStageTask> taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());

    // In move-files-only mode the two files are still outside the hive location, so
    // beforeSplit() must keep them in the rewrite plan in RoaringBitmap mode as well as in
    // list mode; they are moved to the hive location instead of being rewritten.
    updateTableProperty(TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES, "false");
    taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());
    Assert.assertEquals(
        "true",
        taskDescriptors.get(0).getProperties().get(TaskProperties.MOVE_FILE_TO_HIVE_LOCATION));
  }
}
