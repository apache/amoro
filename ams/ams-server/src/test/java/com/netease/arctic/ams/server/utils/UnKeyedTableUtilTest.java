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

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.io.writer.GenericBaseTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.io.writer.SortedPosDeleteWriter;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.Tables;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Multimap;
import org.apache.iceberg.relocated.com.google.common.collect.Multimaps;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.netease.arctic.TableTestBase.newGenericRecord;
import static com.netease.arctic.TableTestBase.partitionData;
import static com.netease.arctic.TableTestBase.writeEqDeleteFile;
import static com.netease.arctic.TableTestBase.writeNewDataFile;
import static com.netease.arctic.TableTestBase.writePosDeleteFile;

public class UnKeyedTableUtilTest extends TableTestBase {
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  public UnKeyedTableUtilTest() {
    super(new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Test
  public void testGetAllContentFilePath() throws Exception {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(getArcticTable().asKeyedTable())
        .withTransactionId(1L).buildBaseWriter();

    for (Record record : writeRecords()) {
      writer.write(record);
    }

    // DataFiles
    Set<String> s1FilePath = new HashSet<>();
    WriteResult result = writer.complete();
    AppendFiles appendFiles = getArcticTable().asKeyedTable().baseTable().newAppend();
    for (DataFile dataFile : result.dataFiles()) {
      appendFiles.appendFile(dataFile);
      s1FilePath.add(TableFileUtils.getUriPath(dataFile.path().toString()));
    }
    appendFiles.commit();

    // DeleteFiles
    DataFile dataFile = result.dataFiles()[0];
    SortedPosDeleteWriter<Record> posDeleteWriter = GenericTaskWriters.builderFor(getArcticTable().asKeyedTable())
        .withTransactionId(1L).buildBasePosDeleteWriter(2, 1, dataFile.partition());

    posDeleteWriter.delete(dataFile.path(), 1);
    posDeleteWriter.delete(dataFile.path(), 3);
    posDeleteWriter.delete(dataFile.path(), 5);
    List<DeleteFile> posDeleteResult = posDeleteWriter.complete();
    Assert.assertEquals(1, posDeleteResult.size());
    RowDelta rowDelta = getArcticTable().asKeyedTable().baseTable().newRowDelta();
    for (DeleteFile deleteFile : posDeleteResult) {
      rowDelta.addDeletes(deleteFile);
      s1FilePath.add(TableFileUtils.getUriPath(deleteFile.path().toString()));
    }
    rowDelta.commit();

    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(getArcticTable().asKeyedTable().baseTable()));
  }

  @Test
  public void testGetAllContentFilePathWithDelete() throws Exception {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(getArcticTable().asKeyedTable())
        .withTransactionId(1L).buildBaseWriter();

    for (Record record : writeRecords()) {
      writer.write(record);
    }

    Set<String> s1FilePath = new HashSet<>();
    WriteResult result = writer.complete();
    AppendFiles appendFiles = getArcticTable().asKeyedTable().baseTable().newAppend();
    for (DataFile dataFile : result.dataFiles()) {
      appendFiles.appendFile(dataFile);
      s1FilePath.add(TableFileUtils.getUriPath(dataFile.path().toString()));
    }
    appendFiles.commit();

    DeleteFiles deleteFile = getArcticTable().asKeyedTable().baseTable().newDelete();
    deleteFile.deleteFile(result.dataFiles()[0]).commit();
    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(getArcticTable().asKeyedTable().baseTable()));
  }

  @Test
  public void testGetAllContentFilePathWithExpire() throws Exception {
    GenericBaseTaskWriter writer = GenericTaskWriters.builderFor(getArcticTable().asKeyedTable())
        .withTransactionId(1L).buildBaseWriter();

    for (Record record : writeRecords()) {
      writer.write(record);
    }

    Set<String> s1FilePath = new HashSet<>();
    WriteResult result = writer.complete();
    AppendFiles appendFiles = getArcticTable().asKeyedTable().baseTable().newAppend();
    for (DataFile dataFile : result.dataFiles()) {
      appendFiles.appendFile(dataFile);
      s1FilePath.add(TableFileUtils.getUriPath(dataFile.path().toString()));
    }
    appendFiles.commit();

    DeleteFiles deleteFile = getArcticTable().asKeyedTable().baseTable().newDelete();
    deleteFile.deleteFile(result.dataFiles()[0]).commit();

    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(getArcticTable().asKeyedTable().baseTable()));
    getArcticTable().asKeyedTable().baseTable().newAppend().commit();
    getArcticTable().asKeyedTable().baseTable().expireSnapshots()
        .retainLast(1).expireOlderThan(System.currentTimeMillis()).cleanExpiredFiles(true).commit();
    s1FilePath.remove(TableFileUtils.getUriPath(result.dataFiles()[0].path().toString()));
    Assert.assertEquals(s1FilePath, UnKeyedTableUtil.getAllContentFilePath(getArcticTable().asKeyedTable().baseTable()));
  }

  @Test
  public void testGetAllContentFilesForIcebergFormat() throws IOException {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");

    String path = tempFolder.getRoot().getPath();
    Table table = hadoopTables.create(com.netease.arctic.TableTestBase.TABLE_SCHEMA, PartitionSpec.unpartitioned(),
        tableProperties, path + "/test/table1");
    List<DataFile> dataFiles = insertDataFiles(table, 10);
    List<DeleteFile> eqDeleteFiles = insertEqDeleteFiles(table, 1);
    List<DeleteFile> posDeleteFiles = insertPosDeleteFiles(table, dataFiles);
    Set<String> realPaths = new HashSet<>();
    dataFiles.stream().map(DataFile::path).map(CharSequence::toString).forEach(realPaths::add);
    eqDeleteFiles.stream().map(DeleteFile::path).map(CharSequence::toString).forEach(realPaths::add);
    posDeleteFiles.stream().map(DeleteFile::path).map(CharSequence::toString).forEach(realPaths::add);
    Set<String> allContentFilePath = UnKeyedTableUtil.getAllContentFilePath(table);
    Assert.assertEquals(realPaths, allContentFilePath);
  }

  private static List<DataFile> insertDataFiles(Table table, int length) throws IOException {
    StructLike partitionData = partitionData(table.schema(), table.spec(), getOpTime());
    DataFile result = writeNewDataFile(table, records(0, length, table.schema()), partitionData);

    AppendFiles baseAppend = table.newAppend();
    baseAppend.appendFile(result);
    baseAppend.commit();

    return Collections.singletonList(result);
  }

  private static List<Record> records(int start, int length, Schema tableSchema) {
    List<Record> records = Lists.newArrayList();
    for (int i = start; i < start + length; i++) {
      records.add(newGenericRecord(tableSchema, i, "name", getOpTime()));
    }
    return records;
  }

  private static LocalDateTime getOpTime() {
    return LocalDateTime.of(2022, 1, 1, 12, 0, 0);
  }

  private List<DeleteFile> insertPosDeleteFiles(Table table, List<DataFile> dataFiles) throws IOException {
    Multimap<String, Long> file2Positions = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
    for (DataFile dataFile : dataFiles) {
      file2Positions.put(dataFile.path().toString(), 0L);
    }
    StructLike partitionData = partitionData(table.schema(), table.spec(), getOpTime());
    DeleteFile result = writePosDeleteFile(table, file2Positions, partitionData);
    RowDelta rowDelta = table.newRowDelta();
    rowDelta.addDeletes(result);
    rowDelta.commit();
    return Collections.singletonList(result);
  }

  private List<DeleteFile> insertEqDeleteFiles(Table table, int length) throws IOException {
    StructLike partitionData = partitionData(table.schema(), table.spec(), getOpTime());
    DeleteFile result = writeEqDeleteFile(table, records(0, length, table.schema()), partitionData);

    RowDelta rowDelta = table.newRowDelta();
    rowDelta.addDeletes(result);
    rowDelta.commit();
    return Collections.singletonList(result);
  }

  private List<Record> writeRecords() {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(DataTestHelpers.createRecord(2, "lily", 0, "2022-01-01T12:00:00"));
    builder.add(DataTestHelpers.createRecord(3, "jake", 0, "2022-02-01T23:00:00"));
    builder.add(DataTestHelpers.createRecord(4, "sam", 0, "2022-02-01T06:00:00"));
    builder.add(DataTestHelpers.createRecord(5, "john", 0, "2022-01-01T12:00:00"));

    return builder.build();
  }
}
