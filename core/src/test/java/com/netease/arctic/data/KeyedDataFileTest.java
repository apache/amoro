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

package com.netease.arctic.data;

import com.netease.arctic.TableTestBase;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

public class KeyedDataFileTest extends TableTestBase {

  @Test
  public void testDefaultKeyedFile() {
    List<DataFile> writeFiles = writeChange(PK_TABLE_ID, ChangeAction.INSERT, writeRecords());

    Assert.assertEquals(1, writeFiles.size());
    DefaultKeyedFile defaultKeyedFile = new DefaultKeyedFile(writeFiles.get(0));
    Assert.assertEquals(DataFileType.INSERT_FILE, defaultKeyedFile.type());
    Assert.assertEquals(3, defaultKeyedFile.node().mask());
    Long txId = AMS.handler().getTableCurrentTxId(PK_TABLE_ID.buildTableIdentifier());
    Assert.assertEquals(0, defaultKeyedFile.node().index());
    Assert.assertEquals(txId, defaultKeyedFile.transactionId());

  }

  private List<Record> writeRecords() {
    GenericRecord record = GenericRecord.create(TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 1, "name", "john", "op_time",
        LocalDateTime.of(2022, 1, 1, 12, 0, 0))));
    builder.add(record.copy(ImmutableMap.of("id", 1, "name", "lily", "op_time",
        LocalDateTime.of(2022, 1, 1, 12, 0, 0))));

    return builder.build();
  }
}
