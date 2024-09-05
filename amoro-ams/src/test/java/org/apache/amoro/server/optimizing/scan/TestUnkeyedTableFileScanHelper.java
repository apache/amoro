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

package org.apache.amoro.server.optimizing.scan;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.ExpressionUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class TestUnkeyedTableFileScanHelper extends TableFileScanHelperTestBase {
  public TestUnkeyedTableFileScanHelper(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      }
    };
  }

  @Test
  public void testScanEmpty() {
    List<TableFileScanHelper.FileScanResult> scan = scanFiles();
    assertScanResult(scan, 0);
  }

  @Test
  public void testScanEmptySnapshot() {
    OptimizingTestHelpers.appendBase(
        getMixedTable(),
        tableTestHelper().writeBaseStore(getMixedTable(), 0L, Collections.emptyList(), false));

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();
    assertScanResult(scan, 0);
  }

  @Test
  public void testScan() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-02T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), 0L, newRecords, false));
    // partition field = "2022-01-01T12:00:00"
    DataFile sampleFile = dataFiles.get(0);

    OptimizingTestHelpers.appendBase(
        getMixedTable(), tableTestHelper().writeBaseStore(getMixedTable(), 0L, newRecords, false));

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    if (isPartitionedTable()) {
      assertScanResult(scan, 4, null, 0);
    } else {
      assertScanResult(scan, 2, null, 0);
    }

    // test partition filter
    scan =
        scanFiles(
            buildFileScanHelper()
                .withPartitionFilter(
                    ExpressionUtil.convertPartitionDataToDataFilter(
                        getMixedTable(), sampleFile.specId(), sampleFile.partition())));
    assertScanResult(scan, 2, null, 0);
  }

  @Test
  public void testScanWithPosDelete() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), 0L, newRecords, false));
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              getMixedTable(), 0L, dataFile, Collections.singletonList(0L)));
    }
    OptimizingTestHelpers.appendBasePosDelete(getMixedTable(), posDeleteFiles);

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    assertScanResult(scan, 1, null, 1);
  }

  @Override
  protected UnkeyedTable getMixedTable() {
    return super.getMixedTable().asUnkeyedTable();
  }

  @Override
  protected TableFileScanHelper buildFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getMixedTable(), true);
    return new UnkeyedTableFileScanHelper(getMixedTable(), baseSnapshotId);
  }
}
