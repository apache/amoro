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

package org.apache.amoro.server.dashboard;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Verifies the snapshot file statistics reported by {@link
 * MixedAndIcebergTableDescriptor#getSnapshots(AmoroTable, String, OperationType)} against a real
 * Iceberg table.
 */
public class TestSnapshotFileSize {

  private static final Schema SCHEMA =
      new Schema(Types.NestedField.required(1, "id", Types.IntegerType.get()));

  @TempDir File warehouse;

  @Test
  public void testAppendSnapshotsReportTotalFileSize() {
    Table icebergTable = newIcebergTable("append");
    icebergTable.newAppend().appendFile(dataFile("data-a.parquet", 100)).commit();
    long firstSnapshotId = icebergTable.currentSnapshot().snapshotId();
    icebergTable.newAppend().appendFile(dataFile("data-b.parquet", 200)).commit();
    long secondSnapshotId = icebergTable.currentSnapshot().snapshotId();

    List<AmoroSnapshotsOfTable> snapshots = getSnapshots(icebergTable);
    Assertions.assertEquals(2, snapshots.size());

    AmoroSnapshotsOfTable firstSnapshot = findSnapshot(snapshots, firstSnapshotId);
    Assertions.assertEquals(100L, firstSnapshot.getOriginalFileSize());
    Assertions.assertEquals(1, firstSnapshot.getFileCount());
    Assertions.assertEquals(10L, firstSnapshot.getRecords());

    AmoroSnapshotsOfTable secondSnapshot = findSnapshot(snapshots, secondSnapshotId);
    Assertions.assertEquals(300L, secondSnapshot.getOriginalFileSize());
    Assertions.assertEquals(2, secondSnapshot.getFileCount());
    Assertions.assertEquals(20L, secondSnapshot.getRecords());
  }

  @Test
  public void testRemoveSnapshotReportsRemainingTotalFileSize() {
    Table icebergTable = newIcebergTable("remove");
    DataFile removed = dataFile("data-a.parquet", 100);
    icebergTable.newAppend().appendFile(removed).commit();
    icebergTable.newAppend().appendFile(dataFile("data-b.parquet", 200)).commit();
    icebergTable.newDelete().deleteFile(removed).commit();
    long deleteSnapshotId = icebergTable.currentSnapshot().snapshotId();

    List<AmoroSnapshotsOfTable> snapshots = getSnapshots(icebergTable);

    AmoroSnapshotsOfTable deleteSnapshot = findSnapshot(snapshots, deleteSnapshotId);
    Assertions.assertEquals(200L, deleteSnapshot.getOriginalFileSize());
    Assertions.assertEquals(1, deleteSnapshot.getFileCount());
    Assertions.assertEquals(10L, deleteSnapshot.getRecords());
  }

  @Test
  public void testEmptyTableHasNoSnapshots() {
    Table icebergTable = newIcebergTable("empty");
    Assertions.assertTrue(getSnapshots(icebergTable).isEmpty());
  }

  private Table newIcebergTable(String name) {
    HadoopTables tables = new HadoopTables(new Configuration());
    return tables.create(
        SCHEMA, PartitionSpec.unpartitioned(), new File(warehouse, name).getAbsolutePath());
  }

  private DataFile dataFile(String path, long sizeInBytes) {
    return DataFiles.builder(PartitionSpec.unpartitioned())
        .withPath(path)
        .withFileSizeInBytes(sizeInBytes)
        .withRecordCount(10)
        .build();
  }

  private List<AmoroSnapshotsOfTable> getSnapshots(Table icebergTable) {
    AmoroTable<?> amoroTable =
        IcebergTable.newIcebergTable(
            TableIdentifier.of("catalog", "db", icebergTable.name()),
            icebergTable,
            TableMetaStore.builder().withConfiguration(new Configuration()).build(),
            Collections.emptyMap());
    MixedAndIcebergTableDescriptor descriptor = new MixedAndIcebergTableDescriptor();
    return descriptor.getSnapshots(amoroTable, null, OperationType.ALL);
  }

  /** Commit timestamps may tie, so snapshots are located by snapshot id. */
  private AmoroSnapshotsOfTable findSnapshot(
      List<AmoroSnapshotsOfTable> snapshots, long snapshotId) {
    return snapshots.stream()
        .filter(snapshot -> String.valueOf(snapshotId).equals(snapshot.getSnapshotId()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("snapshot " + snapshotId + " is missing"));
  }
}
