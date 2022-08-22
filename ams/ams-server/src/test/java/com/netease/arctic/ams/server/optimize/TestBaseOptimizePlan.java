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
import com.netease.arctic.ams.server.util.DataFileInfoUtils;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.io.writer.GenericBaseTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestBaseOptimizePlan extends TableTestBase {
  protected List<DataFileInfo> baseDataFilesInfo = new ArrayList<>();
  protected List<DataFileInfo> posDeleteFilesInfo = new ArrayList<>();

  protected List<DataFile> insertKeyedTableBaseDataFiles(long transactionId) throws IOException {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable)
        .withTransactionId(transactionId).buildBaseWriter();

    List<DataFile> baseDataFiles = new ArrayList<>();
    // write 1000 records to 2 partitions(2022-1-1\2022-1-2)
    int length = 100;
    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length)) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      baseDataFiles.addAll(Arrays.asList(result.dataFiles()));
    }
    AppendFiles baseAppend = testKeyedTable.baseTable().newAppend();
    baseDataFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
    long commitTime = System.currentTimeMillis();

    baseDataFilesInfo = baseDataFiles.stream()
        .map(dataFile -> DataFileInfoUtils.convertToDatafileInfo(dataFile, commitTime, testKeyedTable))
        .collect(Collectors.toList());
    return baseDataFiles;
  }

  protected List<DeleteFile> insertBasePosDeleteFiles(long transactionId) throws IOException {
    List<DataFile> dataFiles = insertKeyedTableBaseDataFiles(transactionId - 1);
    Map<StructLike, List<DataFile>> dataFilesPartitionMap =
        new HashMap<>(dataFiles.stream().collect(Collectors.groupingBy(ContentFile::partition)));
    List<DeleteFile> deleteFiles = new ArrayList<>();
    for (Map.Entry<StructLike, List<DataFile>> dataFilePartitionMap : dataFilesPartitionMap.entrySet()) {
      StructLike partition = dataFilePartitionMap.getKey();
      List<DataFile> partitionFiles = dataFilePartitionMap.getValue();
      Map<DataTreeNode, List<DataFile>> nodeFilesPartitionMap = new HashMap<>(partitionFiles.stream()
          .collect(Collectors.groupingBy(dataFile ->
              DefaultKeyedFile.parseMetaFromFileName(dataFile.path().toString()).node())));
      for (Map.Entry<DataTreeNode, List<DataFile>> nodeFilePartitionMap : nodeFilesPartitionMap.entrySet()) {
        DataTreeNode key = nodeFilePartitionMap.getKey();
        List<DataFile> nodeFiles = nodeFilePartitionMap.getValue();

        // write pos delete
        SortedPosDeleteWriter<Record> writer = GenericTaskWriters.builderFor(testKeyedTable)
            .withTransactionId(transactionId).buildBasePosDeleteWriter(key.getMask(), key.getIndex(), partition);
        for (DataFile nodeFile : nodeFiles) {
          writer.delete(nodeFile.path(), 0);
        }
        deleteFiles.addAll(writer.complete());
      }
    }
    RowDelta rowDelta = testKeyedTable.baseTable().newRowDelta();
    deleteFiles.forEach(rowDelta::addDeletes);
    rowDelta.commit();
    long commitTime = System.currentTimeMillis();

    posDeleteFilesInfo.addAll(deleteFiles.stream()
        .map(deleteFile -> DataFileInfoUtils.convertToDatafileInfo(deleteFile, commitTime, testKeyedTable))
        .collect(Collectors.toList()));

    return deleteFiles;
  }

  protected List<Record> baseRecords(int start, int length) {
    GenericRecord record = GenericRecord.create(TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = start; i < start + length; i++) {
      builder.add(record.copy(ImmutableMap.of("id", i, "name", "name" + i, "op_time",
          LocalDateTime.of(2022, 1, i % 2 + 1, 12, 0, 0))));
    }

    return builder.build();
  }
}
