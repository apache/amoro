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

package org.apache.amoro.io;

import static org.apache.amoro.table.TableProperties.FILE_FORMAT_ORC;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.DataFileTestHelpers;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.writer.GenericTaskWriters;
import org.apache.amoro.io.writer.SortedPosDeleteWriter;
import org.apache.amoro.scan.TableEntriesScan;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(Parameterized.class)
public class TestTaskWriter extends TableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true, FILE_FORMAT_ORC)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, false, FILE_FORMAT_ORC)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, true, FILE_FORMAT_ORC)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false, FILE_FORMAT_ORC)
      }
    };
  }

  public TestTaskWriter(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testBaseWriter() {
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "jake", 0, "2022-01-03T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(4, "sam", 0, "2022-01-04T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(5, "mary", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(6, "mack", 0, "2022-01-01T12:00:00"));

    List<DataFile> files =
        tableTestHelper().writeBaseStore(getMixedTable(), 1L, insertRecords, false);
    if (isKeyedTable()) {
      if (isPartitionedTable()) {
        Assert.assertEquals(5, files.size());
      } else {
        Assert.assertEquals(4, files.size());
      }
    } else {
      if (isPartitionedTable()) {
        Assert.assertEquals(4, files.size());
      } else {
        Assert.assertEquals(1, files.size());
      }
    }

    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    AppendFiles appendFiles = baseStore.newAppend();
    files.forEach(appendFiles::appendFile);
    appendFiles.commit();

    List<Record> readRecords =
        tableTestHelper().readBaseStore(getMixedTable(), Expressions.alwaysTrue(), null, false);
    Assert.assertEquals(Sets.newHashSet(insertRecords), Sets.newHashSet(readRecords));
  }

  @Test
  public void testBasePosDeleteWriter() throws IOException {
    String fileFormat =
        tableTestHelper()
            .tableProperties()
            .getOrDefault(
                TableProperties.DEFAULT_FILE_FORMAT, TableProperties.DEFAULT_FILE_FORMAT_DEFAULT);
    DataFile dataFile =
        DataFileTestHelpers.getFile(
            "/data",
            1,
            getMixedTable().spec(),
            isPartitionedTable() ? "op_time_day=2020-01-01" : null,
            null,
            false,
            FileFormat.valueOf(fileFormat.toUpperCase(Locale.ENGLISH)));
    GenericTaskWriters.Builder builder = GenericTaskWriters.builderFor(getMixedTable());
    if (isKeyedTable()) {
      builder.withTransactionId(1L);
    }
    SortedPosDeleteWriter<Record> writer =
        builder.buildBasePosDeleteWriter(0, 0, dataFile.partition());

    writer.delete(dataFile.path(), 1);
    writer.delete(dataFile.path(), 3);
    writer.delete(dataFile.path(), 5);
    List<DeleteFile> result = writer.complete();
    Assert.assertEquals(1, result.size());
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    RowDelta rowDelta = baseStore.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();

    // check lower bounds and upper bounds of file_path
    TableEntriesScan entriesScan =
        TableEntriesScan.builder(baseStore)
            .includeColumnStats()
            .includeFileContent(FileContent.POSITION_DELETES)
            .build();
    AtomicInteger cnt = new AtomicInteger();
    entriesScan
        .entries()
        .forEach(
            entry -> {
              cnt.getAndIncrement();
              ContentFile<?> file = entry.getFile();
              Map<Integer, ByteBuffer> lowerBounds = file.lowerBounds();
              Map<Integer, ByteBuffer> upperBounds = file.upperBounds();

              String pathLowerBounds =
                  new String(lowerBounds.get(MetadataColumns.DELETE_FILE_PATH.fieldId()).array());
              String pathUpperBounds =
                  new String(upperBounds.get(MetadataColumns.DELETE_FILE_PATH.fieldId()).array());

              // As ORC PositionDeleteWriter didn't add metricsConfig,
              // here can't get lower bounds and upper bounds of file_path accurately for orc file
              // format,
              // do not check lower bounds and upper bounds for orc
              if (!fileFormat.equals(FILE_FORMAT_ORC)) {
                Assert.assertEquals(dataFile.path().toString(), pathLowerBounds);
                Assert.assertEquals(dataFile.path().toString(), pathUpperBounds);
              }
            });
    Assert.assertEquals(1, cnt.get());
  }

  @Test
  public void testChangeWriter() {
    Assume.assumeTrue(isKeyedTable());
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "jake", 0, "2022-01-03T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(4, "sam", 0, "2022-01-04T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(5, "mary", 0, "2022-01-01T12:00:00"));

    List<DataFile> insertFiles =
        tableTestHelper()
            .writeChangeStore(
                getMixedTable().asKeyedTable(), 1L, ChangeAction.INSERT, insertRecords, false);
    Assert.assertEquals(4, insertFiles.size());

    List<Record> deleteRecords = Lists.newArrayList();
    deleteRecords.add(insertRecords.get(0));
    deleteRecords.add(insertRecords.get(1));

    List<DataFile> deleteFiles =
        tableTestHelper()
            .writeChangeStore(
                getMixedTable().asKeyedTable(), 2L, ChangeAction.DELETE, deleteRecords, false);
    Assert.assertEquals(2, deleteFiles.size());

    AppendFiles appendFiles = getMixedTable().asKeyedTable().changeTable().newAppend();
    insertFiles.forEach(appendFiles::appendFile);
    deleteFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();

    List<Record> readChangeRecords =
        tableTestHelper()
            .readChangeStore(getMixedTable().asKeyedTable(), Expressions.alwaysTrue(), null, false);
    List<Record> expectRecord = Lists.newArrayList();
    for (int i = 0; i < insertRecords.size(); i++) {
      expectRecord.add(
          MixedDataTestHelpers.appendMetaColumnValues(
              insertRecords.get(i), 1L, i + 1, ChangeAction.INSERT));
    }
    for (int i = 0; i < deleteRecords.size(); i++) {
      expectRecord.add(
          MixedDataTestHelpers.appendMetaColumnValues(
              deleteRecords.get(i), 2L, i + 1, ChangeAction.DELETE));
    }
    Assert.assertEquals(Sets.newHashSet(expectRecord), Sets.newHashSet(readChangeRecords));
  }

  @Test
  public void testOrderedWriterThrowException() {
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "jake", 0, "2022-02-01T23:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(4, "sam", 0, "2022-02-01T06:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(5, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(6, "lily", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(7, "jake", 0, "2022-02-01T23:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(8, "sam", 0, "2022-02-01T06:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(9, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(10, "lily", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(11, "jake", 0, "2022-02-01T23:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(12, "sam", 0, "2022-02-01T06:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(13, "john", 0, "2022-01-01T12:00:00"));

    Assert.assertThrows(
        IllegalStateException.class,
        () ->
            tableTestHelper()
                .writeBaseStore(getMixedTable().asKeyedTable(), 1L, insertRecords, true));

    Assume.assumeTrue(isKeyedTable());
    Assert.assertThrows(
        IllegalStateException.class,
        () ->
            tableTestHelper()
                .writeChangeStore(
                    getMixedTable().asKeyedTable(), 1L, ChangeAction.INSERT, insertRecords, true));
  }
}
