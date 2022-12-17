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

import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.table.WriteOperationKind;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface TestOptimizeBase {
  List<Record> baseRecords(int start, int length, Schema tableSchema);

  default Pair<Snapshot, List<DataFile>> insertTableBaseDataFiles(ArcticTable arcticTable, Long transactionId) throws IOException {
    TaskWriter<Record> writer = arcticTable.isKeyedTable() ?
        AdaptHiveGenericTaskWriterBuilder.builderFor(arcticTable)
        .withTransactionId(transactionId)
        .buildWriter(BaseLocationKind.INSTANT) :
        AdaptHiveGenericTaskWriterBuilder.builderFor(arcticTable)
            .buildWriter(BaseLocationKind.INSTANT) ;

    List<DataFile> baseDataFiles = insertBaseDataFiles(writer, arcticTable.schema());
    UnkeyedTable baseTable = arcticTable.isKeyedTable() ?
        arcticTable.asKeyedTable().baseTable() : arcticTable.asUnkeyedTable();
    AppendFiles baseAppend = baseTable.newAppend();
    baseDataFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();

    baseTable.refresh();
    Snapshot snapshot = baseTable.currentSnapshot();

    return Pair.of(snapshot, baseDataFiles);
  }

  default List<DataFile> insertOptimizeTargetDataFiles(ArcticTable arcticTable,
                                                       OptimizeType optimizeType,
                                                       long transactionId) throws IOException {
    WriteOperationKind writeOperationKind = WriteOperationKind.MAJOR_OPTIMIZE;
    switch (optimizeType) {
      case FullMajor:
        writeOperationKind = WriteOperationKind.FULL_OPTIMIZE;
        break;
      case Major:
        break;
      case Minor:
          writeOperationKind = WriteOperationKind.MINOR_OPTIMIZE;
          break;
    }
    TaskWriter<Record> writer = arcticTable.isKeyedTable() ?
        AdaptHiveGenericTaskWriterBuilder.builderFor(arcticTable)
            .withTransactionId(transactionId)
            .buildWriter(writeOperationKind) :
        AdaptHiveGenericTaskWriterBuilder.builderFor(arcticTable)
            .buildWriter(writeOperationKind);

    return insertBaseDataFiles(writer, arcticTable.schema());
  }

  default Pair<Snapshot, List<DeleteFile>> insertBasePosDeleteFiles(ArcticTable arcticTable,
                                                    Long transactionId,
                                                    List<DataFile> dataFiles,
                                                    Set<DataTreeNode> targetNodes) throws IOException {
    if (arcticTable.isKeyedTable()) {
      Preconditions.checkNotNull(transactionId);
    } else {
      Preconditions.checkArgument(transactionId == null);
    }
    Map<StructLike, List<DataFile>> dataFilesPartitionMap =
        new HashMap<>(dataFiles.stream().collect(Collectors.groupingBy(ContentFile::partition)));
    List<DeleteFile> deleteFiles = new ArrayList<>();
    for (Map.Entry<StructLike, List<DataFile>> dataFilePartitionMap : dataFilesPartitionMap.entrySet()) {
      StructLike partition = dataFilePartitionMap.getKey();
      List<DataFile> partitionFiles = dataFilePartitionMap.getValue();
      Map<DataTreeNode, List<DataFile>> nodeFilesPartitionMap = new HashMap<>(partitionFiles.stream()
          .collect(Collectors.groupingBy(dataFile ->
              TableFileUtils.parseFileNodeFromFileName(dataFile.path().toString()))));
      for (Map.Entry<DataTreeNode, List<DataFile>> nodeFilePartitionMap : nodeFilesPartitionMap.entrySet()) {
        DataTreeNode key = nodeFilePartitionMap.getKey();
        List<DataFile> nodeFiles = nodeFilePartitionMap.getValue();
        if (!targetNodes.contains(key)) {
          continue;
        }

        // write pos delete
        SortedPosDeleteWriter<Record> posDeleteWriter = AdaptHiveGenericTaskWriterBuilder.builderFor(arcticTable)
            .withTransactionId(transactionId)
            .buildBasePosDeleteWriter(key.mask(), key.index(), partition);
        for (DataFile nodeFile : nodeFiles) {
          posDeleteWriter.delete(nodeFile.path(), 0);
        }
        deleteFiles.addAll(posDeleteWriter.complete());
      }
    }

    UnkeyedTable baseTable = arcticTable.isKeyedTable() ?
        arcticTable.asKeyedTable().baseTable() : arcticTable.asUnkeyedTable();
    RowDelta rowDelta = baseTable.newRowDelta();
    deleteFiles.forEach(rowDelta::addDeletes);
    rowDelta.commit();

    baseTable.refresh();
    Snapshot snapshot = baseTable.currentSnapshot();

    return Pair.of(snapshot, deleteFiles);
  }

  default List<DeleteFile> insertOptimizeTargetDeleteFiles(ArcticTable arcticTable,
                                                           List<DataFile> dataFiles,
                                                           long transactionId) throws IOException {
    Map<StructLike, List<DataFile>> dataFilesPartitionMap =
        new HashMap<>(dataFiles.stream().collect(Collectors.groupingBy(ContentFile::partition)));
    List<DeleteFile> deleteFiles = new ArrayList<>();
    for (Map.Entry<StructLike, List<DataFile>> dataFilePartitionMap : dataFilesPartitionMap.entrySet()) {
      StructLike partition = dataFilePartitionMap.getKey();
      List<DataFile> partitionFiles = dataFilePartitionMap.getValue();
      Map<DataTreeNode, List<DataFile>> nodeFilesPartitionMap = new HashMap<>(partitionFiles.stream()
          .collect(Collectors.groupingBy(dataFile ->
              TableFileUtils.parseFileNodeFromFileName(dataFile.path().toString()))));
      for (Map.Entry<DataTreeNode, List<DataFile>> nodeFilePartitionMap : nodeFilesPartitionMap.entrySet()) {
        DataTreeNode key = nodeFilePartitionMap.getKey();
        List<DataFile> nodeFiles = nodeFilePartitionMap.getValue();

        // write pos delete
        SortedPosDeleteWriter<Record> posDeleteWriter = AdaptHiveGenericTaskWriterBuilder.builderFor(arcticTable)
            .withTransactionId(transactionId)
            .buildBasePosDeleteWriter(key.mask(), key.index(), partition);
        for (DataFile nodeFile : nodeFiles) {
          posDeleteWriter.delete(nodeFile.path(), 0);
        }
        deleteFiles.addAll(posDeleteWriter.complete());
      }
    }

    return deleteFiles;
  }

  default List<DataFile> insertBaseDataFiles(TaskWriter<Record> writer, Schema schema) throws IOException {
    List<DataFile> baseDataFiles = new ArrayList<>();
    // write 1000 records to 1 partitions(name="name)
    int length = 100;
    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length, schema)) {
        writer.write(record);
      }
      WriteResult result = writer.complete();
      baseDataFiles.addAll(Arrays.asList(result.dataFiles()));
    }

    return baseDataFiles;
  }
}
