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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.junit.Before;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class TableDataTestBase extends TableTestBase {

  // 6 records, (id=1),(id=2),(id=3),(id=4),(id=5),(id=6)
  protected List<Record> allRecords;

  protected DataFile dataFileForPositionDelete;
  protected DeleteFile deleteFileOfPositionDelete;

  public TableDataTestBase(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  public TableDataTestBase() {
    this(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  protected List<Record> baseRecords(List<Record> records) {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(records.get(0));
    builder.add(records.get(1));
    builder.add(records.get(2));
    builder.add(records.get(3));

    return builder.build();
  }

  protected List<Record> changeInsertRecords(List<Record> records) {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(records.get(4));
    builder.add(records.get(5));
    return builder.build();
  }

  protected List<Record> changeDeleteRecords(List<Record> records) {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(records.get(4));
    return builder.build();
  }

  @Before
  public void initData() throws IOException {
    allRecords = Lists.newArrayListWithCapacity(6);
    allRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    allRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    allRecords.add(tableTestHelper().generateTestRecord(3, "jake", 0, "2022-01-03T12:00:00"));
    allRecords.add(tableTestHelper().generateTestRecord(4, "sam", 0, "2022-01-04T12:00:00"));
    allRecords.add(tableTestHelper().generateTestRecord(5, "mary", 0, "2022-01-01T12:00:00"));
    allRecords.add(tableTestHelper().generateTestRecord(6, "mack", 0, "2022-01-01T12:00:00"));

    // write base with transaction id:1, (id=1),(id=2),(id=3),(id=4)
    List<DataFile> baseFiles =
        tableTestHelper()
            .writeBaseStore(getMixedTable().asKeyedTable(), 1L, baseRecords(allRecords), false);
    dataFileForPositionDelete =
        baseFiles.stream()
            .filter(s -> s.path().toString().contains("op_time_day=2022-01-04"))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Cannot find data file to delete"));
    AppendFiles baseAppend = getMixedTable().asKeyedTable().baseTable().newAppend();
    baseFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();

    // write position with transaction id:4, (id=4)
    DeleteFile posDeleteFiles =
        MixedDataTestHelpers.writeBaseStorePosDelete(
                getMixedTable(), 4L, dataFileForPositionDelete, Collections.singletonList(0L))
            .stream()
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Cannot get delete file from writer"));

    this.deleteFileOfPositionDelete = posDeleteFiles;
    getMixedTable().asKeyedTable().baseTable().newRowDelta().addDeletes(posDeleteFiles).commit();

    // write change insert with transaction id:2, (id=5),(id=6)
    writeChangeStore(2L, ChangeAction.INSERT, changeInsertRecords(allRecords));

    // write change delete with transaction id:3, (id=5)
    writeChangeStore(3L, ChangeAction.DELETE, changeDeleteRecords(allRecords));
  }

  protected void writeChangeStore(Long txId, ChangeAction insert, List<Record> records) {
    List<DataFile> insertFiles =
        tableTestHelper()
            .writeChangeStore(getMixedTable().asKeyedTable(), txId, insert, records, false);
    AppendFiles changeAppendInsert = getMixedTable().asKeyedTable().changeTable().newAppend();
    insertFiles.forEach(changeAppendInsert::appendFile);
    changeAppendInsert.commit();
  }
}
