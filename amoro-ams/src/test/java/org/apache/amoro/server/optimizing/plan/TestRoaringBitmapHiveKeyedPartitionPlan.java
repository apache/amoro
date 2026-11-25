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
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.optimizing.RewriteStageTask;
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
 * Runs all {@link TestHiveKeyedPartitionPlan} cases with RoaringBitmap planning enabled, and adds
 * a move-files-only regression case: when {@link
 * TableProperties#SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES} is disabled, files already in the hive
 * location must be removed from the rewrite plan, in RoaringBitmap mode as well as in list mode.
 */
@RunWith(Parameterized.class)
public class TestRoaringBitmapHiveKeyedPartitionPlan extends TestHiveKeyedPartitionPlan {

  public TestRoaringBitmapHiveKeyedPartitionPlan(
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
  public void testMoveFilesOnlySkipsFilesInHiveLocation() {
    updateChangeHashBucket(1);
    closeFullOptimizingInterval();
    closeMinorOptimizingInterval();
    // write two change insert files, which are treated as fragments and trigger the full
    // optimizing through hasNewHiveData regardless of the hive location
    List<Record> newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    OptimizingTestHelpers.appendChange(
        getMixedTable(),
        tableTestHelper()
            .writeChangeStore(
                getMixedTable(), beginTransaction(), ChangeAction.INSERT, newRecords, false));
    OptimizingTestHelpers.appendChange(
        getMixedTable(),
        tableTestHelper()
            .writeChangeStore(
                getMixedTable(), beginTransaction(), ChangeAction.INSERT, newRecords, false));

    // The change files are written by the mixed engine under the change-store location. Point
    // the hive location at the change-store root so both files are seen as files already in
    // the hive location, as they would be after a previous optimizing moved (or the hive
    // engine wrote) files there. This is done after writing so the write paths stay at
    // their default layout.
    updateTableProperty(
        HiveTableProperties.BASE_HIVE_LOCATION_ROOT,
        getMixedTable().asKeyedTable().changeTable().location() + "/");

    // trigger full optimizing through the hive refresh interval
    updateTableProperty(HiveTableProperties.REFRESH_HIVE_INTERVAL, 1 + "");

    // with rewrite-all-files enabled, the two change files are both rewritten
    List<RewriteStageTask> taskDescriptors = planWithCurrentFiles();
    Assert.assertEquals(1, taskDescriptors.size());

    // In move-files-only mode the two files are already in the hive location, so beforeSplit()
    // must exclude them from the rewrite plan in RoaringBitmap mode as well as in list mode.
    // Before the fix, the bitmap-based rewrite maps were never filtered and this planning
    // still produced 1 task rewriting both files.
    updateTableProperty(TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES, "false");
    Assert.assertEquals(0, planWithCurrentFiles().size());
  }
}
