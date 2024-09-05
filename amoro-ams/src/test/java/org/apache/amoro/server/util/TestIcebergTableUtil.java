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

package org.apache.amoro.server.util;

import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.mixed.MixedTables;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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

    // prepare snapshots
    List<Record> records =
        IntStream.of(1, 100)
            .boxed()
            .map(i -> MixedDataTestHelpers.createRecord(schema, i, UUID.randomUUID().toString()))
            .collect(Collectors.toList());
    for (Record r : records) {
      IcebergDataTestHelpers.append(table, Lists.newArrayList(r));
    }
    // expected manifest files
    Set<String> expectManifestFiles =
        Lists.newArrayList(table.snapshots()).stream()
            .flatMap(snapshot -> snapshot.allManifests(table.io()).stream())
            .map(ManifestFile::path)
            .collect(Collectors.toSet());

    // reload table
    TableIdentifier identifier = TableIdentifier.parse(table.name());
    identifier = TableIdentifier.of("db", identifier.name());
    Table reloadTable = catalog.loadTable(identifier);

    // get all manifest path by util method. assert result is right.
    Set<String> actualManifestFiles = IcebergTableUtil.getAllManifestFiles(reloadTable);
    Assertions.assertEquals(expectManifestFiles, actualManifestFiles);

    // Verify that the tested method does not trigger the snapshot's cache logic.
    List<Snapshot> snapshots = Lists.newArrayList(reloadTable.snapshots());
    for (Snapshot snapshot : snapshots) {
      try {
        Field allManifests = snapshot.getClass().getDeclaredField("allManifests");
        allManifests.setAccessible(true);
        Object value = allManifests.get(snapshot);
        Assertions.assertNull(value);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Table newIcebergTable(Catalog catalog) {
    return catalog.createTable(TableIdentifier.of("db", "table"), schema);
  }

  private static MixedTable newMixedTable(Catalog catalog, boolean withKey) {
    MixedTables mixedTables = new MixedTables(TableMetaStore.EMPTY, Maps.newHashMap(), catalog);
    return mixedTables.createTable(
        org.apache.amoro.table.TableIdentifier.of("cata", "db", "table"),
        schema,
        PartitionSpec.unpartitioned(),
        withKey ? keySpec : PrimaryKeySpec.noPrimaryKey(),
        Maps.newHashMap());
  }
}
