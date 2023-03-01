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

package com.netease.arctic.ams.server.utils;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.io.writer.GenericBaseTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.utils.FileUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnKeyedTableUtilTest extends TableTestBase {

  @Test
  public void testGetAllContentFilePath() throws Exception {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable.asKeyedTable())
        .withTransactionId(1L).buildBaseWriter();

    for (Record record : writeRecords()) {
      writer.write(record);
    }

    // DataFiles
    Set<String> s1FilePath = new HashSet<>();
    WriteResult result = writer.complete();
    AppendFiles appendFiles = testKeyedTable.asKeyedTable().baseTable().newAppend();
    for (DataFile dataFile : result.dataFiles()) {
      appendFiles.appendFile(dataFile);
      s1FilePath.add(FileUtil.getUriPath(dataFile.path().toString()));
    }
    appendFiles.commit();

    // DeleteFiles
    DataFile dataFile = result.dataFiles()[0];
    SortedPosDeleteWriter<Record> posDeleteWriter = GenericTaskWriters.builderFor(testKeyedTable.asKeyedTable())
        .withTransactionId(1L).buildBasePosDeleteWriter(2, 1, dataFile.partition());

    posDeleteWriter.delete(dataFile.path(), 1);
    posDeleteWriter.delete(dataFile.path(), 3);
    posDeleteWriter.delete(dataFile.path(), 5);
    List<DeleteFile> posDeleteResult = posDeleteWriter.complete();
    Assert.assertEquals(1, posDeleteResult.size());
    RowDelta rowDelta = testKeyedTable.asKeyedTable().baseTable().newRowDelta();
    for (DeleteFile deleteFile : posDeleteResult) {
      rowDelta.addDeletes(deleteFile);
      s1FilePath.add(FileUtil.getUriPath(deleteFile.path().toString()));
    }
    rowDelta.commit();

    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(testKeyedTable.asKeyedTable().baseTable()));
  }

  @Test
  public void testGetAllContentFilePathWithDelete() throws Exception {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable.asKeyedTable())
        .withTransactionId(1L).buildBaseWriter();

    for (Record record : writeRecords()) {
      writer.write(record);
    }

    Set<String> s1FilePath = new HashSet<>();
    WriteResult result = writer.complete();
    AppendFiles appendFiles = testKeyedTable.asKeyedTable().baseTable().newAppend();
    for (DataFile dataFile : result.dataFiles()) {
      appendFiles.appendFile(dataFile);
      s1FilePath.add(FileUtil.getUriPath(dataFile.path().toString()));
    }
    appendFiles.commit();

    DeleteFiles deleteFile = testKeyedTable.asKeyedTable().baseTable().newDelete();
    deleteFile.deleteFile(result.dataFiles()[0]).commit();
    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(testKeyedTable.asKeyedTable().baseTable()));
  }

  @Test
  public void testGetAllContentFilePathWithExpire() throws Exception {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(testKeyedTable.asKeyedTable())
        .withTransactionId(1L).buildBaseWriter();

    for (Record record : writeRecords()) {
      writer.write(record);
    }

    Set<String> s1FilePath = new HashSet<>();
    WriteResult result = writer.complete();
    AppendFiles appendFiles = testKeyedTable.asKeyedTable().baseTable().newAppend();
    for (DataFile dataFile : result.dataFiles()) {
      appendFiles.appendFile(dataFile);
      s1FilePath.add(FileUtil.getUriPath(dataFile.path().toString()));
    }
    appendFiles.commit();

    DeleteFiles deleteFile = testKeyedTable.asKeyedTable().baseTable().newDelete();
    deleteFile.deleteFile(result.dataFiles()[0]).commit();

    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(testKeyedTable.asKeyedTable().baseTable()));
    testKeyedTable.asKeyedTable().baseTable().newAppend().commit();
    testKeyedTable.asKeyedTable().baseTable().expireSnapshots()
        .retainLast(1).expireOlderThan(System.currentTimeMillis()).cleanExpiredFiles(true).commit();

    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(testKeyedTable.asKeyedTable().baseTable()));
  }

  private List<Record> writeRecords() {
    GenericRecord record = GenericRecord.create(testKeyedTable.schema());

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = 0; i < 10; i++) {
      builder.add(record.copy(ImmutableMap.of("id", i, "name", "name",
          "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0))));
    }

    return builder.build();
  }
}
