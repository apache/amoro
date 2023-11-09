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

package com.netease.arctic.scan;

import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;

import java.util.List;
import java.util.stream.Collectors;

public class UnkeyedTableFileScanHelper extends IcebergTableFileScanHelper {

  public UnkeyedTableFileScanHelper(UnkeyedTable table, Long snapshotId) {
    super(table, snapshotId);
  }

  @Override
  protected FileScanResult buildFileScanResult(FileScanTask fileScanTask) {
    DataFile dataFile = wrapBaseFile(fileScanTask.file());
    List<ContentFile<?>> deleteFiles =
        fileScanTask.deletes().stream().map(this::wrapDeleteFile).collect(Collectors.toList());
    return new FileScanResult(dataFile, deleteFiles);
  }

  private DataFile wrapBaseFile(DataFile dataFile) {
    return DefaultKeyedFile.parseBase(dataFile);
  }

  private ContentFile<?> wrapDeleteFile(DeleteFile deleteFile) {
    if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
      throw new UnsupportedOperationException(
          "optimizing unkeyed table not support equality-delete");
    }
    return deleteFile;
  }
}
