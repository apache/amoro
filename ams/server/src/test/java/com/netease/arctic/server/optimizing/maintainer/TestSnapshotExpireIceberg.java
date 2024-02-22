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

package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.events.iceberg.ExpireSnapshotsResult;
import com.netease.arctic.ams.api.events.iceberg.ImmutableExpireSnapshotsResult;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import org.apache.iceberg.Table;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestSnapshotExpireIceberg extends TestSnapshotExpire {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)}
    };
  }

  public TestSnapshotExpireIceberg(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testIcebergExpireSnapshotsEvent() {
    Table table = getArcticTable().asUnkeyedTable();

    // commit two empty snapshots
    table.newAppend().commit();
    table.newAppend().commit();
    Assertions.assertEquals(2, Iterables.size(table.snapshots()));
    List<String> previousMetadataFiles =
        listChildFiles(getArcticTable().io(), getArcticTable().location() + "/metadata");

    IcebergTableMaintainer icebergTableMaintainer = new IcebergTableMaintainer(table);
    ExpireSnapshotsResult actualResult =
        icebergTableMaintainer
            .expireSnapshots(getArcticTable().id(), TableFormat.ICEBERG, System.currentTimeMillis())
            .getExpireResultAs(ExpireSnapshotsResult.class);
    Assertions.assertEquals(1, Iterables.size(table.snapshots()));

    List<String> removedMetadataFiles = Lists.newArrayList(previousMetadataFiles);
    removedMetadataFiles.removeAll(
        listChildFiles(getArcticTable().io(), getArcticTable().location() + "/metadata"));
    ExpireSnapshotsResult expectResult =
        ImmutableExpireSnapshotsResult.builder()
            .totalDuration(actualResult.totalDuration())
            .addAllDeletedMetadataFiles(removedMetadataFiles)
            .build();

    Assertions.assertEquals(expectResult, actualResult);
  }
}
