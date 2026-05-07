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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class TestMixedTreeNodePosDeleteWriter extends TableTestBase {

  public static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)),
        Arguments.of(
            new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)));
  }

  @ParameterizedTest(name = "{0},{1}")
  @MethodSource("parameters")
  public void test(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper)
      throws IOException {
    setupTable(catalogTestHelper, tableTestHelper);
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

    Assertions.assertEquals(complete.size(), 4);
  }
}
