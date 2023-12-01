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

package com.netease.arctic.server.util;

import com.netease.arctic.io.IcebergDataTestHelpers;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.mixed.MixedTables;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestIcebergTableUtil {
  private static final Schema schema =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()));
  private static final PrimaryKeySpec keySpec =
      PrimaryKeySpec.builderFor(schema).addColumn("id").build();

  @TempDir File warehouse;

  public static Stream<Function<Catalog, Table>> testFetchAllTableManifestFiles() {
    return Stream.of(
        (catalog) -> newIcebergTable(catalog),
        catalog -> newMixedTable(catalog, false).asUnkeyedTable(),
        catalog -> newMixedTable(catalog, true).asKeyedTable().baseTable(),
        catalog -> newMixedTable(catalog, true).asKeyedTable().changeTable());
  }

  @ParameterizedTest
  @MethodSource
  public void testFetchAllTableManifestFiles(Function<Catalog, Table> tableProvider)
      throws IOException {
    HadoopCatalog catalog = new HadoopCatalog(new Configuration(), warehouse.getAbsolutePath());
    Table table = tableProvider.apply(catalog);

    List<Record> records =
        IntStream.of(100)
            .boxed()
            .map(i -> MixedDataTestHelpers.createRecord(schema, i, UUID.randomUUID().toString()))
            .collect(Collectors.toList());
    for (Record r : records) {
      IcebergDataTestHelpers.append(table, Lists.newArrayList(r));
    }

    Set<String> expectManifestFiles =
        Lists.newArrayList(table.snapshots()).stream()
            .flatMap(snapshot -> snapshot.allManifests(table.io()).stream())
            .map(ManifestFile::path)
            .collect(Collectors.toSet());

    Set<String> actualManifestFiles = IcebergTableUtil.getAllManifestFiles(table);
    Assertions.assertEquals(expectManifestFiles, actualManifestFiles);
  }

  private static Table newIcebergTable(Catalog catalog) {
    return catalog.createTable(TableIdentifier.of("db", "table"), schema);
  }

  private static ArcticTable newMixedTable(Catalog catalog, boolean withKey) {
    MixedTables mixedTables = new MixedTables(TableMetaStore.EMPTY, Maps.newHashMap(), catalog);
    return mixedTables.createTable(
        com.netease.arctic.table.TableIdentifier.of("cata", "db", "table"),
        schema,
        PartitionSpec.unpartitioned(),
        withKey ? keySpec : PrimaryKeySpec.noPrimaryKey(),
        Maps.newHashMap());
  }
}
