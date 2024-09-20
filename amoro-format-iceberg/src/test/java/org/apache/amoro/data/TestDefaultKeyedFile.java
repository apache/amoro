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

package org.apache.amoro.data;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestDefaultKeyedFile extends TableTestBase {

  public TestDefaultKeyedFile() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Test
  public void testDefaultKeyedFile() {
    Long txId = getMixedTable().asKeyedTable().beginTransaction("begin");
    List<DataFile> writeFiles =
        MixedDataTestHelpers.writeChangeStore(
            getMixedTable().asKeyedTable(), txId, ChangeAction.INSERT, writeRecords(), false);

    Assert.assertEquals(1, writeFiles.size());
    DefaultKeyedFile defaultKeyedFile = DefaultKeyedFile.parseChange(writeFiles.get(0));
    Assert.assertEquals(DataFileType.INSERT_FILE, defaultKeyedFile.type());
    Assert.assertEquals(3, defaultKeyedFile.node().mask());
    Assert.assertEquals(0, defaultKeyedFile.node().index());
    // TODO check transactionId

  }

  private List<Record> writeRecords() {

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(MixedDataTestHelpers.createRecord(1, "john", 0, "2022-08-30T12:00:00"));
    builder.add(MixedDataTestHelpers.createRecord(1, "lily", 0, "2022-08-30T12:00:00"));

    return builder.build();
  }
}
