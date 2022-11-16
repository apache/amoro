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

package com.netease.arctic.trace;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;

public class TableWatermarkTest extends TableTestBase {

  @Test
  public void testChangeWatermarkWithAppendFiles() {
    testTableWatermark((arcticTable, addFile) -> {
      arcticTable.asUnkeyedTable().newAppend().appendFile(addFile).commit();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithAppendFilesInTx() {
    testTableWatermark((arcticTable, addFile) -> {
      Transaction transaction = arcticTable.asUnkeyedTable().newTransaction();
      transaction.newAppend().appendFile(addFile).commit();
      transaction.commitTransaction();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithOverwriteFiles() {
    testTableWatermark((arcticTable, addFile) -> {
      arcticTable.asUnkeyedTable().newOverwrite().addFile(addFile).commit();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithOverwriteFilesInTx() {
    testTableWatermark((arcticTable, addFile) -> {
      Transaction transaction = arcticTable.asUnkeyedTable().newTransaction();
      transaction.newOverwrite().addFile(addFile).commit();
      transaction.commitTransaction();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithReplacePartitions() {
    testTableWatermark((arcticTable, addFile) -> {
      arcticTable.asUnkeyedTable().newReplacePartitions().addFile(addFile).commit();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithReplacePartitionsInTx() {
    testTableWatermark((arcticTable, addFile) -> {
      Transaction transaction = arcticTable.asUnkeyedTable().newTransaction();
      transaction.newReplacePartitions().addFile(addFile).commit();
      transaction.commitTransaction();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithRowDelta() {
    testTableWatermark((arcticTable, addFile) -> {
      arcticTable.asUnkeyedTable().newRowDelta().addRows(addFile).commit();
      return null;
    });
  }

  @Test
  public void testChangeWatermarkWithRowDeltaFilesInTx() {
    testTableWatermark((arcticTable, addFile) -> {
      Transaction transaction = arcticTable.asUnkeyedTable().newTransaction();
      transaction.newRowDelta().addRows(addFile).commit();
      transaction.commitTransaction();
      return null;
    });
  }

  @Test
  public void testChangeKeyedTableWatermark() {
    long start = System.currentTimeMillis();
    testKeyedTable.updateProperties().set(TableProperties.TABLE_EVENT_TIME_FIELD, "op_time")
        .set(TableProperties.TABLE_WATERMARK_ALLOWED_LATENESS, "10").commit();

    Map<Integer, ByteBuffer> lowerBounds = Maps.newHashMap();
    Map<Integer, ByteBuffer> upperBounds = Maps.newHashMap();
    lowerBounds.put(3, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start - 30000));
    upperBounds.put(3, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start - 10000));

    Metrics metrics = new Metrics(2L, Maps.newHashMap(), Maps.newHashMap(),
        Maps.newHashMap(), null, lowerBounds, upperBounds);

    DataFile file1 = DataFiles.builder(SPEC)
        .withPath("/path/to/file1.parquet")
        .withFileSizeInBytes(0)
        .withPartitionPath("op_time_day=2022-01-01")
        .withMetrics(metrics)
        .build();
    testKeyedTable.changeTable().newAppend().appendFile(file1).commit();
    Assert.assertEquals(start - 20000, TablePropertyUtil.getTableWatermark(testKeyedTable.properties()));

    lowerBounds.put(3, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start));
    upperBounds.put(3, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start));
    metrics = new Metrics(2L, Maps.newHashMap(), Maps.newHashMap(),
        Maps.newHashMap(), null, lowerBounds, upperBounds);

    DataFile file2 = DataFiles.builder(SPEC)
        .withPath("/path/to/file2.parquet")
        .withFileSizeInBytes(0)
        .withPartitionPath("op_time_day=2022-01-01")
        .withMetrics(metrics)
        .build();
    testKeyedTable.baseTable().newOverwrite().addFile(file2).commit();
    Assert.assertEquals(start - 10000, TablePropertyUtil.getTableWatermark(testKeyedTable.properties()));
  }

  private void testTableWatermark(BiFunction<ArcticTable, DataFile, Void> tableOperation) {
    long start = System.currentTimeMillis();
    testTable.asUnkeyedTable().updateProperties().set(TableProperties.TABLE_EVENT_TIME_FIELD, "op_time")
        .set(TableProperties.TABLE_WATERMARK_ALLOWED_LATENESS, "10").commit();

    Map<Integer, ByteBuffer> lowerBounds = Maps.newHashMap();
    Map<Integer, ByteBuffer> upperBounds = Maps.newHashMap();
    lowerBounds.put(3, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start - 30000));
    upperBounds.put(3, Conversions.toByteBuffer(Types.TimestampType.withoutZone(), start - 10000));

    Metrics metrics = new Metrics(2L, Maps.newHashMap(), Maps.newHashMap(),
        Maps.newHashMap(), null, lowerBounds, upperBounds);

    DataFile file1 = DataFiles.builder(SPEC)
        .withPath("/path/to/file1.parquet")
        .withFileSizeInBytes(0)
        .withPartitionPath("op_time_day=2022-01-01")
        .withMetrics(metrics)
        .build();
    tableOperation.apply(testTable, file1);
    Assert.assertEquals(start - 20000, TablePropertyUtil.getTableWatermark(testTable.properties()));
  }
}
