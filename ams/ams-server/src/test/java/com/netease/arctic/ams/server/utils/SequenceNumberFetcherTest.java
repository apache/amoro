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

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestBase;
import jline.internal.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.Tables;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.ArrayUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SequenceNumberFetcherTest {
  private static final Logger LOG = LoggerFactory.getLogger(SequenceNumberFetcherTest.class);
  private Table table;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void init() {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");

    String path = tempFolder.getRoot().getPath();
    Log.info(path);
    table = hadoopTables.create(TableTestBase.TABLE_SCHEMA, PartitionSpec.unpartitioned(), tableProperties,
        path + "/test/table1");

  }

  @After
  public void clean() {

  }

  @Test
  public void testUnpartitionedTable() throws IOException {
    Map<String, Long> checkedDeletes = Maps.newHashMap();
    Map<String, Long> checkedDataFiles = Maps.newHashMap();

    List<DataFile> dataFiles1 = insertDataFiles(table, 10);
    checkNewFileSequenceNumber(checkedDeletes, checkedDataFiles, 1);

    insertEqDeleteFiles(table, 1);
    checkNewFileSequenceNumber(checkedDeletes, checkedDataFiles, 2);

    insertPosDeleteFiles(table, dataFiles1);
    checkNewFileSequenceNumber(checkedDeletes, checkedDataFiles, 3);

    List<DataFile> dataFiles2 = insertDataFiles(table, 1);
    checkNewFileSequenceNumber(checkedDeletes, checkedDataFiles, 4);

    overwriteDataFiles(table, 1, dataFiles2);
    checkNewFileSequenceNumber(checkedDeletes, checkedDataFiles, 5);

  }

  private void checkNewFileSequenceNumber(Map<String, Long> checkedDeletes, Map<String, Long> checkedDataFiles,
                                          long expectSequence) {
    SequenceNumberFetcher sequenceNumberFetcher;
    sequenceNumberFetcher = SequenceNumberFetcher.with(table, table.currentSnapshot().snapshotId());
    for (FileScanTask fileScanTask : table.newScan().planFiles()) {
      String path = fileScanTask.file().path().toString();
      long sequenceNumber = sequenceNumberFetcher.sequenceNumberOf(path);
      if (checkedDataFiles.containsKey(path)) {
        Assert.assertEquals((long) checkedDataFiles.get(path), sequenceNumber);
      } else {
        LOG.info("get sequence {} of {}", sequenceNumber, path);
        checkedDataFiles.put(path, sequenceNumber);
        Assert.assertEquals(expectSequence, sequenceNumber);
      }
      List<DeleteFile> deletes = fileScanTask.deletes();
      for (DeleteFile delete : deletes) {
        path = delete.path().toString();
        sequenceNumber = sequenceNumberFetcher.sequenceNumberOf(path);
        if (checkedDeletes.containsKey(path)) {
          Assert.assertEquals((long) checkedDeletes.get(path), sequenceNumber);
        } else {
          LOG.info("get sequence {} of {}", sequenceNumber, path);
          checkedDeletes.put(path, sequenceNumber);
          Assert.assertEquals(expectSequence, sequenceNumber);
        }
      }
    }
  }

  private List<DataFile> getCurrentAllDataFiles() {
    return Lists.newArrayList(CloseableIterable.transform(table.newScan().planFiles(), FileScanTask::file));
  }

  private List<DataFile> insertDataFiles(Table arcticTable, int length) throws IOException {
    List<DataFile> result = writeNewDataFiles(arcticTable, length);

    AppendFiles baseAppend = arcticTable.newAppend();
    result.forEach(baseAppend::appendFile);
    baseAppend.commit();

    return result;
  }

  private List<DataFile> overwriteDataFiles(Table arcticTable, int length, List<DataFile> dataFiles) throws IOException {
    List<DataFile> result = writeNewDataFiles(arcticTable, length);

    OverwriteFiles overwrite = arcticTable.newOverwrite();
    result.forEach(overwrite::addFile);
    dataFiles.forEach(overwrite::deleteFile);
    overwrite.commit();

    return result;
  }

  @NotNull
  private List<DataFile> writeNewDataFiles(Table arcticTable, int length) throws IOException {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile();

    long smallSizeByBytes = PropertyUtil.propertyAsLong(arcticTable.properties(),
        com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
        com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT);
    List<DataFile> result = new ArrayList<>();
    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.PARQUET, null);

    for (int i = 1; i < length * 10; i = i + length) {
      for (Record record : baseRecords(i, length, arcticTable.schema())) {
        if (writer.length() > smallSizeByBytes || result.size() > 0) {
          writer.close();
          result.add(writer.toDataFile());
          EncryptedOutputFile newOutputFile = outputFileFactory.newOutputFile();
          writer = appenderFactory
              .newDataWriter(newOutputFile, FileFormat.PARQUET, null);
        }
        writer.write(record);
      }
    }
    writer.close();
    result.add(writer.toDataFile());
    return result;
  }

  private void insertEqDeleteFiles(Table arcticTable, int length) throws IOException {
    Record tempRecord = baseRecords(0, 1, arcticTable.schema()).get(0);
    PartitionKey partitionKey = new PartitionKey(arcticTable.spec(), arcticTable.schema());
    partitionKey.partition(tempRecord);
    List<Integer> equalityFieldIds = Lists.newArrayList(arcticTable.schema().findField("id").fieldId());
    Schema eqDeleteRowSchema = arcticTable.schema().select("id");
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec(),
            ArrayUtil.toIntArray(equalityFieldIds), eqDeleteRowSchema, null);
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(arcticTable.spec(), partitionKey);

    List<DeleteFile> result = new ArrayList<>();
    EqualityDeleteWriter<Record> writer = appenderFactory
        .newEqDeleteWriter(outputFile, FileFormat.PARQUET, partitionKey);

    for (int i = 1; i < length * 10; i = i + length) {
      List<Record> records = baseRecords(i, length, arcticTable.schema());
      for (int j = 0; j < records.size(); j++) {
        if (j % 2 == 0) {
          writer.write(records.get(j));
        }
      }
    }
    writer.close();
    result.add(writer.toDeleteFile());

    RowDelta rowDelta = arcticTable.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  private void insertPosDeleteFiles(Table arcticTable, List<DataFile> dataFiles) throws IOException {
    Record tempRecord = baseRecords(0, 1, arcticTable.schema()).get(0);
    PartitionKey partitionKey = new PartitionKey(arcticTable.spec(), arcticTable.schema());
    partitionKey.partition(tempRecord);
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(arcticTable.schema(), arcticTable.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(arcticTable, arcticTable.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(arcticTable.spec(), partitionKey);

    List<DeleteFile> result = new ArrayList<>();
    PositionDeleteWriter<Record> writer = appenderFactory
        .newPosDeleteWriter(outputFile, FileFormat.PARQUET, partitionKey);
    for (int i = 0; i < dataFiles.size(); i++) {
      DataFile dataFile = dataFiles.get(i);
      if (i % 2 == 0) {
        PositionDelete<Record> positionDelete = PositionDelete.create();
        positionDelete.set(dataFile.path().toString(), 0L, null);
        writer.write(positionDelete);
      }
    }
    writer.close();
    result.add(writer.toDeleteFile());

    RowDelta rowDelta = arcticTable.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  private List<Record> baseRecords(int start, int length, Schema tableSchema) {
    GenericRecord record = GenericRecord.create(tableSchema);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = start; i < start + length; i++) {
      builder.add(record.copy(ImmutableMap.of("id", i, "name", "name",
          "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0))));
    }

    return builder.build();
  }
}