/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic;

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.file.FileNameGenerator;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;

import static com.netease.arctic.utils.ConvertStructUtil.partitionToPath;

public class DataFileInfoUtils {
  public static DataFileInfo convertToDatafileInfo(DataFile dataFile, Snapshot snapshot, ArcticTable arcticTable,
      boolean isChange) {
    DataFileInfo dataFileInfo = new DataFileInfo();
    dataFileInfo.setSize(dataFile.fileSizeInBytes());
    dataFileInfo.setPath((String) dataFile.path());
    dataFileInfo.setPartition(partitionToPath(arcticTable.spec(), dataFile.partition()));
    dataFileInfo.setSpecId(arcticTable.spec().specId());
    dataFileInfo.setRecordCount(dataFile.recordCount());
    if (arcticTable.isKeyedTable()) {
      DataFileType dataFileType;
      if (isChange) {
        dataFileType = FileNameGenerator.parseFileTypeForChange(dataFile.path().toString());
      } else {
        dataFileType = FileNameGenerator.parseFileTypeForBase(dataFile.path().toString());
      }
      DataTreeNode node = FileNameGenerator.parseFileNodeFromFileName(dataFile.path().toString());
      dataFileInfo.setType(dataFileType.name());
      dataFileInfo.setType(dataFileType.name());
      dataFileInfo.setIndex(node.index());
      dataFileInfo.setMask(node.mask());
    } else {
      dataFileInfo.setType(DataFileType.BASE_FILE.name());
      dataFileInfo.setIndex(0);
      dataFileInfo.setMask(0);
    }
    dataFileInfo.setCommitTime(snapshot.timestampMillis());
    dataFileInfo.setSequence(snapshot.sequenceNumber());
    return dataFileInfo;
  }

  public static DataFileInfo convertToDatafileInfo(DeleteFile deleteFile, Snapshot snapshot, ArcticTable arcticTable) {
    DataFileInfo dataFileInfo = new DataFileInfo();
    dataFileInfo.setSize(deleteFile.fileSizeInBytes());
    dataFileInfo.setPath(deleteFile.path().toString());
    dataFileInfo.setPartition(partitionToPath(arcticTable.spec(), deleteFile.partition()));
    dataFileInfo.setSpecId(arcticTable.spec().specId());
    dataFileInfo.setRecordCount(deleteFile.recordCount());
    dataFileInfo.setType(DataFileType.POS_DELETE_FILE.name());
    DataTreeNode node = FileNameGenerator.parseFileNodeFromFileName(deleteFile.path().toString());
    dataFileInfo.setIndex(node.getIndex());
    dataFileInfo.setMask(node.getMask());
    dataFileInfo.setCommitTime(snapshot.timestampMillis());
    dataFileInfo.setSequence(snapshot.sequenceNumber());
    return dataFileInfo;
  }


  private static <T> T get(StructLike data, int pos, Class<?> javaClass) {
    return data.get(pos, (Class<T>) javaClass);
  }
}
