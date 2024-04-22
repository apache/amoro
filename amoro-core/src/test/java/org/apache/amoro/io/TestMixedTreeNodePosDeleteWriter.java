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
import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.io.writer.MixedTreeNodePosDeleteWriter;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class TestMixedTreeNodePosDeleteWriter extends TableTestBase {

  public TestMixedTreeNodePosDeleteWriter(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{1},{2}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  @Test
  public void test() throws IOException {
    UnkeyedTable table = getMixedTable().asKeyedTable().baseTable();
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec());

    StructLike partitionData = GenericRecord.create(table.spec().schema());
    partitionData.set(0, 1);

    MixedTreeNodePosDeleteWriter<Record> writer =
        new MixedTreeNodePosDeleteWriter<>(
            appenderFactory,
            FileFormat.PARQUET,
            partitionData,
            table.io(),
            table.encryption(),
            1L,
            table.location(),
            table.spec());

    writer.setTreeNode(DataTreeNode.ofId(4));
    writer.delete("a", 0);

    writer.setTreeNode(DataTreeNode.ofId(5));
    writer.delete("b", 0);

    writer.setTreeNode(DataTreeNode.ofId(6));
    writer.delete("c", 0);

    writer.setTreeNode(DataTreeNode.ofId(7));
    writer.delete("d", 0);

    List<DeleteFile> complete = writer.complete();

    Assert.assertEquals(complete.size(), 4);
  }
}
