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

package org.apache.amoro.formats.lance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.FormatCatalogFactory;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.ipc.ArrowReader;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lance.Dataset;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

class TestLanceFormatIntegrationTest {

  private static final String CATALOG_NAME = "lance";
  private static final String DATABASE = "default";
  private static final String TABLE = "orders";

  @TempDir private Path warehouse;

  @Test
  void catalogReadsWrittenDatasetAndExposesDescriptorMetadata() throws IOException {
    writeDataset();

    LanceDirectoryV1Catalog catalog = newCatalog();
    assertEquals(Collections.singletonList(DATABASE), catalog.listDatabases());
    assertTrue(catalog.databaseExists(DATABASE));
    assertTrue(catalog.listTables(DATABASE).contains(TABLE));
    assertTrue(catalog.tableExists(DATABASE, TABLE));

    AmoroTable<?> table = catalog.loadTable(DATABASE, TABLE);
    Dataset dataset = (Dataset) table.originalTable();
    try {
      assertEquals(3L, dataset.countRows());

      LanceTableDescriptor descriptor = new LanceTableDescriptor();
      ServerTableMeta metadata = descriptor.getTableDetail(table);
      assertEquals("LANCE", metadata.getTableType());
      assertEquals(1, metadata.getSchema().size());
      assertEquals("id", metadata.getSchema().get(0).getField());
      assertEquals(3L, metadata.getTableSummary().getRecords());

      List<AmoroSnapshotsOfTable> snapshots = descriptor.getSnapshots(table, null, null);
      assertFalse(snapshots.isEmpty());
      assertEquals(3L, snapshots.get(0).getRecords());

      List<PartitionFileBaseInfo> files = descriptor.getTableFiles(table, null, null);
      assertFalse(files.isEmpty());
      assertEquals("DATA_FILE", files.get(0).getFileType());

      List<PartitionFileBaseInfo> snapshotFiles =
          descriptor.getSnapshotDetail(table, snapshots.get(0).getSnapshotId(), null);
      assertFalse(snapshotFiles.isEmpty());
    } finally {
      dataset.close();
    }

    assertTrue(catalog.dropTable(DATABASE, TABLE, true));
    assertFalse(catalog.tableExists(DATABASE, TABLE));
  }

  @Test
  void serviceLoaderFindsLanceProviders() {
    assertTrue(
        ServiceLoader.load(FormatCatalogFactory.class).stream()
            .map(ServiceLoader.Provider::type)
            .anyMatch(LanceCatalogFactory.class::equals));
    assertTrue(
        ServiceLoader.load(FormatTableDescriptor.class).stream()
            .map(ServiceLoader.Provider::type)
            .anyMatch(LanceTableDescriptor.class::equals));
  }

  private LanceDirectoryV1Catalog newCatalog() {
    return new LanceDirectoryV1Catalog(
        CATALOG_NAME,
        Collections.singletonMap(CatalogMetaProperties.KEY_WAREHOUSE, warehouse.toString()));
  }

  private void writeDataset() throws IOException {
    try (BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE);
        ArrowReader reader = new SingleBatchArrowReader(allocator);
        Dataset ignored =
            Dataset.write()
                .allocator(allocator)
                .reader(reader)
                .uri(warehouse.resolve(TABLE + ".lance").toString())
                .execute()) {
      // Dataset creation is completed by the write builder.
    }
  }

  private static class SingleBatchArrowReader extends ArrowReader {

    private static final Schema SCHEMA =
        new Schema(
            Collections.singletonList(
                new Field("id", FieldType.nullable(new ArrowType.Int(32, true)), null)));

    private boolean emitted;

    private SingleBatchArrowReader(BufferAllocator allocator) {
      super(allocator);
    }

    @Override
    public boolean loadNextBatch() throws IOException {
      if (emitted) {
        return false;
      }

      IntVector vector = (IntVector) getVectorSchemaRoot().getVector("id");
      vector.allocateNew(3);
      vector.setSafe(0, 10);
      vector.setSafe(1, 20);
      vector.setSafe(2, 30);
      vector.setValueCount(3);
      getVectorSchemaRoot().setRowCount(3);
      emitted = true;
      return true;
    }

    @Override
    public long bytesRead() {
      return 0L;
    }

    @Override
    protected void closeReadSource() {}

    @Override
    protected Schema readSchema() {
      return SCHEMA;
    }
  }
}
