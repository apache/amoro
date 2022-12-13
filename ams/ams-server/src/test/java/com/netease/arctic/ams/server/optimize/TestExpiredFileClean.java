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

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.server.service.impl.TableExpireService;
import com.netease.arctic.ams.server.util.DataFileInfoUtils;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.op.UpdatePartitionProperties;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TestExpiredFileClean extends TableTestBase {

  private final List<DataFileInfo> changeTableFilesInfo = new ArrayList<>();

  @Test
  public void testDeleteChangeFiles() throws Exception {
    List<DataFile> s1Files = insertChangeDataFiles(1);
    List<StructLike> partitions = new ArrayList<>(s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    Assert.assertEquals(2, partitions.size());

    UpdatePartitionProperties updateProperties = testKeyedTable.baseTable().updatePartitionProperties(null);
    updateProperties.set(partitions.get(0), TableProperties.PARTITION_MAX_TRANSACTION_ID, "3");
    updateProperties.set(partitions.get(1), TableProperties.PARTITION_MAX_TRANSACTION_ID, "0");
    updateProperties.commit();
    List<DataFile> existedDataFiles = new ArrayList<>();
    try (CloseableIterable<FileScanTask> fileScanTasks = testKeyedTable.changeTable().newScan().planFiles()) {
      fileScanTasks.forEach(fileScanTask -> existedDataFiles.add(fileScanTask.file()));
    }
    Assert.assertEquals(4, existedDataFiles.size());

    TableExpireService.deleteChangeFile(testKeyedTable, changeTableFilesInfo);
    List<DataFile> currentDataFiles = new ArrayList<>();
    try (CloseableIterable<FileScanTask> fileScanTasks = testKeyedTable.changeTable().newScan().planFiles()) {
      fileScanTasks.forEach(fileScanTask -> currentDataFiles.add(fileScanTask.file()));
    }
    Assert.assertEquals(2, currentDataFiles.size());
  }

  @Test
  public void testExpireTableFiles() throws Exception {
    List<DataFile> s1Files = insertChangeDataFiles(1);
    List<StructLike> partitions = new ArrayList<>(s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    Assert.assertEquals(2, partitions.size());

    UpdatePartitionProperties updateProperties = testKeyedTable.baseTable().updatePartitionProperties(null);
    updateProperties.set(partitions.get(0), TableProperties.PARTITION_MAX_TRANSACTION_ID, "3");
    updateProperties.set(partitions.get(1), TableProperties.PARTITION_MAX_TRANSACTION_ID, "1");
    updateProperties.commit();
    Assert.assertTrue(testKeyedTable.io().exists((String) s1Files.get(0).path()));
    TableExpireService.deleteChangeFile(testKeyedTable, changeTableFilesInfo);
    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));

    insertChangeDataFiles(2);
    TableExpireService.expireSnapshots(testKeyedTable.changeTable(), System.currentTimeMillis(), new HashSet<>());
    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
    Assert.assertFalse(testKeyedTable.io().exists((String) s1Files.get(0).path()));
  }

  private List<DataFile> insertChangeDataFiles(long transactionId) throws IOException {
    GenericChangeTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
        .withChangeAction(ChangeAction.INSERT)
        .withTransactionId(transactionId).buildChangeWriter();

    List<DataFile> changeInsertFiles = new ArrayList<>();
    // write 4 file to 2 partitions(2022-1-1\2022-1-2)
    int length = 100;
    for (int i = 1; i < length; i = i + length) {
      for (Record record : baseRecords(i, length)) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      changeInsertFiles.addAll(Arrays.asList(result.dataFiles()));
    }
    AppendFiles baseAppend = testKeyedTable.changeTable().newAppend();
    changeInsertFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
    testKeyedTable.changeTable().refresh();
    Snapshot snapshot = testKeyedTable.changeTable().currentSnapshot();

    changeTableFilesInfo.addAll(changeInsertFiles.stream()
        .map(dataFile -> DataFileInfoUtils.convertToDatafileInfo(dataFile, snapshot, testKeyedTable))
        .collect(Collectors.toList()));

    return changeInsertFiles;
  }

  private List<Record> baseRecords(int start, int length) {
    GenericRecord record = GenericRecord.create(TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = start; i < start + length; i++) {
      builder.add(record.copy(ImmutableMap.of("id", i, "name", "name" + i, "op_time",
          LocalDateTime.of(2022, 1, i % 2 + 1, 12, 0, 0))));
    }

    return builder.build();
  }
}
