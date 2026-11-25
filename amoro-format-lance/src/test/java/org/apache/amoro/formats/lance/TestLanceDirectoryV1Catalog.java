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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TagOrBranchInfo;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowReader;
import org.apache.arrow.vector.types.FloatingPointPrecision;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lance.Dataset;
import org.lance.WriteParams;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Covers the directory (v1) catalog, the only Lance catalog that resolves its tables through the
 * namespace implementation bundled in lance-core instead of {@code lance-namespace-hive3}.
 *
 * <p>It is also the test that notices lance-core drifting from lance-namespace-core: such a
 * mismatch is invisible to the compiler and to {@link TestLanceHiveCatalog}, and shows up as an
 * {@code UnsupportedOperationException} thrown by a namespace default method instead.
 */
public class TestLanceDirectoryV1Catalog {

  private static final String CATALOG_NAME = "lance_dir";
  private static final String DATABASE = "default";

  /** More tables than any page size the namespace may start to apply by default. */
  private static final int MANY_TABLES = 300;

  private static final Schema SCHEMA =
      new Schema(
          Arrays.asList(
              new Field("id", new FieldType(true, new ArrowType.Int(32, true), null), null),
              new Field("name", new FieldType(true, new ArrowType.Utf8(), null), null),
              new Field(
                  "score",
                  new FieldType(
                      true, new ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE), null),
                  null)),
          Collections.emptyMap());

  @TempDir Path warehouse;

  private BufferAllocator allocator;
  private LanceDirectoryV1Catalog catalog;

  @BeforeEach
  public void createCatalog() {
    allocator = new RootAllocator(Long.MAX_VALUE);
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, warehouse.toUri().toString());
    catalog = new LanceDirectoryV1Catalog(CATALOG_NAME, properties);
  }

  @AfterEach
  public void closeAllocator() {
    allocator.close();
  }

  @Test
  public void testTableLifecycle() throws Exception {
    String t1 = write("t1", 0);
    write("t2", 100);

    Assertions.assertEquals(Collections.singletonList(DATABASE), catalog.listDatabases());
    Assertions.assertTrue(catalog.databaseExists(DATABASE));
    Assertions.assertFalse(catalog.databaseExists("other"));
    Assertions.assertEquals(Arrays.asList("t1", "t2"), listTables());
    Assertions.assertTrue(catalog.listTables("other").isEmpty());

    Assertions.assertTrue(catalog.tableExists(DATABASE, "t1"));
    Assertions.assertFalse(catalog.tableExists(DATABASE, "t3"));

    AmoroTable<?> table = catalog.loadTable(DATABASE, "t1");
    Assertions.assertEquals(TableIdentifier.of(CATALOG_NAME, DATABASE, "t1"), table.id());
    Assertions.assertInstanceOf(Dataset.class, table.originalTable());
    Assertions.assertThrows(NoSuchTableException.class, () -> catalog.loadTable(DATABASE, "t3"));

    Assertions.assertTrue(catalog.dropTable(DATABASE, "t1", true));
    Assertions.assertFalse(catalog.tableExists(DATABASE, "t1"));
    Assertions.assertFalse(Files.exists(Path.of(t1)), "purge must remove the dataset directory");
    Assertions.assertEquals(Collections.singletonList("t2"), listTables());
  }

  /** The catalog takes a single page from the namespace, so no table may be cut off. */
  @Test
  public void testListsEveryTable() throws Exception {
    List<String> expected = new ArrayList<>();
    for (int i = 0; i < MANY_TABLES; i++) {
      String table = String.format("tbl_%03d", i);
      write(table, i);
      expected.add(table);
    }

    Assertions.assertEquals(expected, listTables());
  }

  /** The descriptor reads metadata only; pin down what it reports for a small version history. */
  @Test
  public void testTableDescriptor() throws Exception {
    String uri = warehouse.resolve("sample.lance").toUri().toString();
    append(uri, WriteParams.WriteMode.CREATE, 0);
    append(uri, WriteParams.WriteMode.APPEND, 3);
    append(uri, WriteParams.WriteMode.APPEND, 6);
    try (Dataset dataset = Dataset.open().uri(uri).build()) {
      dataset.delete("id < 2");
      dataset.tags().create("tag-one", 1);
      dataset.tags().create("tag-two", dataset.getVersion().getId());
    }
    AmoroTable<?> table = catalog.loadTable(DATABASE, "sample");
    LanceTableDescriptor descriptor = new LanceTableDescriptor();

    ServerTableMeta meta = descriptor.getTableDetail(table);
    Assertions.assertEquals("LANCE", meta.getTableType());
    Assertions.assertEquals(uri, meta.getBaseLocation());
    Assertions.assertEquals(
        Arrays.asList("id", "name", "score"),
        meta.getSchema().stream().map(AMSColumnInfo::getField).collect(Collectors.toList()));
    // 3 data files of one write each, plus the deletion file of version 4.
    Assertions.assertEquals(4, meta.getTableSummary().getFile());
    Assertions.assertEquals(7, meta.getTableSummary().getRecords());
    Assertions.assertEquals(3L, meta.getBaseMetrics().get("fileCount"));
    Assertions.assertEquals(1L, meta.getChangeMetrics().get("deletionFileCount"));
    Assertions.assertEquals(2L, meta.getChangeMetrics().get("deletedRows"));

    List<AmoroSnapshotsOfTable> snapshots = descriptor.getSnapshots(table, null, null);
    Assertions.assertEquals(4, snapshots.size());
    Map<String, Long> recordsByVersion = new HashMap<>();
    for (AmoroSnapshotsOfTable snapshot : snapshots) {
      Assertions.assertEquals("WRITE", snapshot.getOperation());
      recordsByVersion.put(snapshot.getSnapshotId(), snapshot.getRecords());
    }
    Assertions.assertEquals(
        Arrays.asList("1", "2", "3", "4"),
        recordsByVersion.keySet().stream().sorted().collect(Collectors.toList()));
    Assertions.assertEquals(Long.valueOf(3), recordsByVersion.get("1"));
    Assertions.assertEquals(Long.valueOf(6), recordsByVersion.get("2"));
    Assertions.assertEquals(Long.valueOf(9), recordsByVersion.get("3"));
    Assertions.assertEquals(Long.valueOf(7), recordsByVersion.get("4"));

    // Every write added one data file; the deletion file only exists from version 4 on.
    Assertions.assertEquals(1, descriptor.getSnapshotDetail(table, "1", null).size());
    Assertions.assertEquals(2, descriptor.getSnapshotDetail(table, "2", null).size());
    List<PartitionFileBaseInfo> version3 = descriptor.getSnapshotDetail(table, "3", null);
    Assertions.assertEquals(3, version3.size());
    Assertions.assertTrue(version3.stream().allMatch(f -> "DATA_FILE".equals(f.getFileType())));
    List<PartitionFileBaseInfo> head = descriptor.getSnapshotDetail(table, "4", null);
    Assertions.assertEquals(4, head.size());
    Assertions.assertEquals(
        3, head.stream().filter(f -> "DATA_FILE".equals(f.getFileType())).count());
    Assertions.assertEquals(
        1, head.stream().filter(f -> "DELETION_FILE".equals(f.getFileType())).count());
    Assertions.assertTrue(descriptor.getSnapshotDetail(table, "not-a-version", null).isEmpty());
    Assertions.assertEquals(3, descriptor.getTableFiles(table, null, null).size());

    Assertions.assertEquals(
        Arrays.asList("tag-one->1", "tag-two->4"),
        descriptor.getTableTags(table).stream()
            .map(tag -> tag.getName() + "->" + tag.getSnapshotId())
            .sorted()
            .collect(Collectors.toList()));
    Assertions.assertEquals(
        Collections.singletonList(TagOrBranchInfo.MAIN_BRANCH.getName()),
        descriptor.getTableBranches(table).stream()
            .map(TagOrBranchInfo::getName)
            .collect(Collectors.toList()));
  }

  private List<String> listTables() {
    List<String> tables = new ArrayList<>(catalog.listTables(DATABASE));
    Collections.sort(tables);
    return tables;
  }

  /** Creates the dataset of a table holding one batch of rows; returns its location. */
  private String write(String table, int firstId) throws Exception {
    String uri = warehouse.resolve(table + ".lance").toUri().toString();
    append(uri, WriteParams.WriteMode.CREATE, firstId);
    return uri;
  }

  private void append(String uri, WriteParams.WriteMode mode, int firstId) throws Exception {
    try (ArrowReader reader = new SingleBatchReader(allocator, firstId, 3);
        Dataset ignored =
            Dataset.write()
                .allocator(allocator)
                .uri(uri)
                .mode(mode)
                .schema(SCHEMA)
                .reader(reader)
                .execute()) {}
  }

  private static void fill(VectorSchemaRoot root, int firstId, int rows) {
    IntVector ids = (IntVector) root.getVector("id");
    VarCharVector names = (VarCharVector) root.getVector("name");
    Float8Vector scores = (Float8Vector) root.getVector("score");
    root.allocateNew();
    for (int i = 0; i < rows; i++) {
      ids.setSafe(i, firstId + i);
      names.setSafe(i, ("n" + (firstId + i)).getBytes(StandardCharsets.UTF_8));
      scores.setSafe(i, (firstId + i) * 0.5);
    }
    root.setRowCount(rows);
  }

  /**
   * Serves one in-memory batch. It deliberately skips Arrow IPC: the module's test classpath
   * resolves an old flatbuffers, under which {@code ArrowStreamReader}/{@code ArrowStreamWriter}
   * fail with a {@code NoSuchMethodError}.
   */
  private static final class SingleBatchReader extends ArrowReader {
    private boolean served;

    SingleBatchReader(BufferAllocator allocator, int firstId, int rows) {
      super(allocator);
      try {
        ensureInitialized();
        fill(getVectorSchemaRoot(), firstId, rows);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    @Override
    public boolean loadNextBatch() {
      if (served) {
        return false;
      }
      served = true;
      return true;
    }

    @Override
    public long bytesRead() {
      return 0;
    }

    @Override
    protected Schema readSchema() {
      return SCHEMA;
    }

    @Override
    protected void closeReadSource() {}
  }
}
