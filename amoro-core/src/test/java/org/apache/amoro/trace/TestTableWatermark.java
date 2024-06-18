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

package org.apache.amoro.trace;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class TestTableWatermark extends TableTestBase {

  private final boolean onBaseTable;

  private UnkeyedTable operationTable;

  public TestTableWatermark(boolean keyedTable, boolean onBaseTable) {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(keyedTable, true));
    this.onBaseTable = onBaseTable;
  }

  @Parameterized.Parameters(name = "keyedTable = {0}, onBaseTable = {1}")
  public static Object[][] parameters() {
    return new Object[][] {{true, true}, {true, false}, {false, true}};
  }

  private UnkeyedTable getOperationTable() {
    if (operationTable == null) {
      MixedTable mixedTable = getMixedTable();
      if (isKeyedTable()) {
        if (onBaseTable) {
          operationTable = mixedTable.asKeyedTable().baseTable();
        } else {
          operationTable = mixedTable.asKeyedTable().changeTable();
        }
      } else {
        if (onBaseTable) {
          operationTable = mixedTable.asUnkeyedTable();
        } else {
          throw new IllegalArgumentException("Unkeyed table do not have change store");
        }
      }
    }
    return operationTable;
  }

  @Test
  public void testChangeWatermarkWithAppendFiles() {
    testTableWatermark(
        addFile -> {
          getOperationTable().newAppend().appendFile(addFile).commit();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithAppendFilesInTx() {
    testTableWatermark(
        addFile -> {
          Transaction transaction = getOperationTable().newTransaction();
          transaction.newAppend().appendFile(addFile).commit();
          transaction.commitTransaction();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithOverwriteFiles() {
    testTableWatermark(
        addFile -> {
          getOperationTable().newOverwrite().addFile(addFile).commit();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithOverwriteFilesInTx() {
    testTableWatermark(
        addFile -> {
          Transaction transaction = getOperationTable().newTransaction();
          transaction.newOverwrite().addFile(addFile).commit();
          transaction.commitTransaction();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithReplacePartitions() {
    testTableWatermark(
        addFile -> {
          getOperationTable().newReplacePartitions().addFile(addFile).commit();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithReplacePartitionsInTx() {
    testTableWatermark(
        addFile -> {
          Transaction transaction = getOperationTable().newTransaction();
          transaction.newReplacePartitions().addFile(addFile).commit();
          transaction.commitTransaction();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithRowDelta() {
    testTableWatermark(
        addFile -> {
          getOperationTable().newRowDelta().addRows(addFile).commit();
          return null;
        });
  }

  @Test
  public void testChangeWatermarkWithRowDeltaFilesInTx() {
    testTableWatermark(
        addFile -> {
          Transaction transaction = getOperationTable().newTransaction();
          transaction.newRowDelta().addRows(addFile).commit();
          transaction.commitTransaction();
          return null;
        });
  }

  private void testTableWatermark(Function<DataFile, Void> tableOperation) {
    long start = System.currentTimeMillis();
    getMixedTable()
        .updateProperties()
        .set(TableProperties.TABLE_EVENT_TIME_FIELD, "op_time")
        .set(TableProperties.TABLE_WATERMARK_ALLOWED_LATENESS, "10")
        .commit();

    Map<Integer, ByteBuffer> lowerBounds = Maps.newHashMap();
    Map<Integer, ByteBuffer> upperBounds = Maps.newHashMap();
    lowerBounds.put(4, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start - 30000));
    upperBounds.put(4, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start - 10000));

    Metrics metrics =
        new Metrics(
            2L,
            Maps.newHashMap(),
            Maps.newHashMap(),
            Maps.newHashMap(),
            null,
            lowerBounds,
            upperBounds);

    DataFile file1 =
        DataFiles.builder(getMixedTable().spec())
            .withPath("/path/to/file1.parquet")
            .withFileSizeInBytes(0)
            .withPartitionPath("op_time_day=2022-01-01")
            .withMetrics(metrics)
            .build();
    tableOperation.apply(file1);
    Assert.assertEquals(
        start - 20000, TablePropertyUtil.getTableWatermark(getMixedTable().properties()));
  }
}
