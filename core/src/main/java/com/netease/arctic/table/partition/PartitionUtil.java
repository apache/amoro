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

package com.netease.arctic.table.partition;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.ManifestEntryFields;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.MetadataTableUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartitionUtil {

  public static final Logger LOG = LoggerFactory.getLogger(PartitionUtil.class);

  private static final Map<Integer, DataFileType> ICEBERG_FILE_TYPE_MAP = new HashMap<>();

  static {
    ICEBERG_FILE_TYPE_MAP.put(FileContent.DATA.id(), DataFileType.BASE_FILE);
    ICEBERG_FILE_TYPE_MAP.put(FileContent.POSITION_DELETES.id(), DataFileType.POS_DELETE_FILE);
    ICEBERG_FILE_TYPE_MAP.put(FileContent.EQUALITY_DELETES.id(), DataFileType.EQ_DELETE_FILE);
  }

  public static List<PartitionBaseInfo> getTablePartition(ArcticTable arcticTable) {
    if (arcticTable.spec().isUnpartitioned()) {
      return new ArrayList<>();
    }
    Map<String, PartitionBaseInfo> partitionBaseInfoHashMap = new HashMap<>();
    getTableFile(arcticTable, null).forEach(fileInfo -> {
      if (!partitionBaseInfoHashMap.containsKey(fileInfo.getPartitionName())) {
        partitionBaseInfoHashMap.put(fileInfo.getPartitionName(), new PartitionBaseInfo());
        partitionBaseInfoHashMap.get(fileInfo.getPartitionName()).setPartition(fileInfo.getPartitionName());
      }
      PartitionBaseInfo partitionInfo = partitionBaseInfoHashMap.get(fileInfo.getPartitionName());
      partitionInfo.setFileCount(partitionInfo.getFileCount() + 1);
      partitionInfo.setFileSize(partitionInfo.getFileSize() + fileInfo.getFileSize());
      partitionInfo.setLastCommitTime(partitionInfo.getLastCommitTime() > fileInfo.getCommitTime() ?
          partitionInfo.getLastCommitTime() :
          fileInfo.getCommitTime());
    });

    return new ArrayList<>(partitionBaseInfoHashMap.values());
  }

  public static List<PartitionFileBaseInfo> getTableFile(ArcticTable arcticTable, String partition) {
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    if (arcticTable.isKeyedTable()) {
      result.addAll(collectFileInfo(arcticTable.asKeyedTable().changeTable(), true, partition));
      result.addAll(collectFileInfo(arcticTable.asKeyedTable().baseTable(), false, partition));
    } else {
      result.addAll(collectFileInfo(arcticTable.asUnkeyedTable(), false, partition));
    }
    return result;
  }

  private static List<PartitionFileBaseInfo> collectFileInfo(Table table, boolean isChangeTable, String partition) {
    PartitionSpec spec = table.spec();
    List<PartitionFileBaseInfo> result = new ArrayList<>();
    Table entriesTable = MetadataTableUtils.createMetadataTableInstance(((HasTableOperations) table).operations(),
        table.name(), table.name() + "#ENTRIES",
        MetadataTableType.ENTRIES);
    try (CloseableIterable<Record> manifests = IcebergGenerics.read(entriesTable)
        .where(Expressions.notEqual(ManifestEntryFields.STATUS.name(), ManifestEntryFields.Status.DELETED.id()))
        .build()) {
      for (Record record : manifests) {
        long snapshotId = (long) record.getField(ManifestEntryFields.SNAPSHOT_ID.name());
        GenericRecord dataFile = (GenericRecord) record.getField(ManifestEntryFields.DATA_FILE_FIELD_NAME);
        Integer contentId = (Integer) dataFile.getField(DataFile.CONTENT.name());
        String filePath = (String) dataFile.getField(DataFile.FILE_PATH.name());
        String partitionPath = null;
        GenericRecord parRecord = (GenericRecord) dataFile.getField(DataFile.PARTITION_NAME);
        if (parRecord != null) {
          InternalRecordWrapper wrapper = new InternalRecordWrapper(parRecord.struct());
          partitionPath = spec.partitionToPath(wrapper.wrap(parRecord));
        }
        if (partition != null && spec.isPartitioned() && !partition.equals(partitionPath)) {
          continue;
        }
        Long fileSize = (Long) dataFile.getField(DataFile.FILE_SIZE.name());
        DataFileType dataFileType =
            isChangeTable ? FileNameRules.parseFileTypeForChange(filePath) : ICEBERG_FILE_TYPE_MAP.get(contentId);
        long commitTime = -1;
        if (table.snapshot(snapshotId) != null) {
          commitTime = table.snapshot(snapshotId).timestampMillis();
        }
        result.add(new PartitionFileBaseInfo(String.valueOf(snapshotId), dataFileType, commitTime,
            partitionPath, filePath, fileSize));
      }
    } catch (IOException exception) {
      LOG.error("close manifest file error", exception);
    }
    return result;
  }
}
