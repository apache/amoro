/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.io.IcebergDataTestHelpers;
import com.netease.arctic.io.MixedDataTestHelpers;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestMaintainerUtilMethods {

  @Test
  public void testFetchAllTableManifestFiles(@TempDir File tableLocation) throws IOException {
    HadoopTables tables = new HadoopTables();
    Schema schema =
        new Schema(
            Types.NestedField.required(1, "id", Types.IntegerType.get()),
            Types.NestedField.required(2, "data", Types.StringType.get()));
    Table icebergTable =
        tables.create(
            schema,
            PartitionSpec.unpartitioned(),
            SortOrder.unsorted(),
            Maps.newHashMap(),
            tableLocation.getAbsolutePath());

    List<Record> records =
        IntStream.of(100)
            .boxed()
            .map(i -> MixedDataTestHelpers.createRecord(schema, i, UUID.randomUUID().toString()))
            .collect(Collectors.toList());
    for (Record r : records) {
      IcebergDataTestHelpers.append(icebergTable, Lists.newArrayList(r));
    }

    Set<String> expectManifestFiles =
        Lists.newArrayList(icebergTable.snapshots()).stream()
            .flatMap(snapshot -> snapshot.allManifests(icebergTable.io()).stream())
            .map(ManifestFile::path)
            .collect(Collectors.toSet());

    Set<String> actualManifestFiles = IcebergTableMaintainer.allManifestFiles(icebergTable);
    Assertions.assertEquals(expectManifestFiles, actualManifestFiles);
  }
}
