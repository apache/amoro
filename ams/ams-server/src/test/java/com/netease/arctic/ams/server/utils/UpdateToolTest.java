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

package com.netease.arctic.ams.server.utils;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.ModifyChangeTableSequence;
import org.apache.iceberg.ModifyTableSequence;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class UpdateToolTest extends TableTestBase {

  @Parameterized.Parameters(name = "testFormat = {0}")
  public static Object[] parameters() {
    return new Object[] {TableFormat.MIXED_ICEBERG};
  }

  public UpdateToolTest(TableFormat testFormat) {
    super(new BasicCatalogTestHelper(testFormat), new BasicTableTestHelper(true, true));
  }

  @Test
  public void testModifyChangeTableSequence() {
    KeyedTable table = getArcticTable().asKeyedTable();
    long txId = getArcticTable().asKeyedTable().beginTransaction("begin");
    List<DataFile> writeFiles = DataTestHelpers.writeChangeStore(getArcticTable().asKeyedTable(), txId,
        ChangeAction.INSERT, writeRecords(), false);

    Snapshot snapshot = table.changeTable().currentSnapshot();
    long lastSeqNumber = snapshot.sequenceNumber();
    long modifiedSeqNumber = lastSeqNumber + 10;

    ModifyTableSequence modifyTableSequence = new ModifyChangeTableSequence(
        table.asKeyedTable().changeTable().name(),
        ((HasTableOperations) table.asKeyedTable().changeTable()).operations()
    );
    modifyTableSequence.sequence(modifiedSeqNumber);
    modifyTableSequence.commit();
    table.refresh();

    Assert.assertEquals(
        modifiedSeqNumber,
        table.asKeyedTable().changeTable().currentSnapshot().sequenceNumber()
    );
  }

  @Test
  public void testModifyChangeTableSequenceNoEffect() {
    KeyedTable table = getArcticTable().asKeyedTable();
    long txId = getArcticTable().asKeyedTable().beginTransaction("begin");
    List<DataFile> writeFiles = DataTestHelpers.writeChangeStore(getArcticTable().asKeyedTable(), txId,
        ChangeAction.INSERT, writeRecords(), false);

    Snapshot snapshot = table.changeTable().currentSnapshot();
    long lastSeqNumber = snapshot.sequenceNumber();
    long modifiedSeqNumber = lastSeqNumber - 10;

    ModifyTableSequence modifyTableSequence = new ModifyChangeTableSequence(
        table.asKeyedTable().changeTable().name(),
        ((HasTableOperations) table.asKeyedTable().changeTable()).operations()
    );
    modifyTableSequence.sequence(modifiedSeqNumber);
    modifyTableSequence.commit();
    table.refresh();

    Assert.assertTrue(
        lastSeqNumber <= table.asKeyedTable().changeTable().currentSnapshot().sequenceNumber()
    );
  }

  private List<Record> writeRecords() {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(DataTestHelpers.createRecord(1, "john", 0, "2022-08-30T12:00:00"));
    builder.add(DataTestHelpers.createRecord(1, "lily", 0, "2022-08-30T12:00:00"));
    return builder.build();
  }
}
