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
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.io.reader.CombinedDeleteFilter;
import org.apache.amoro.io.reader.DeleteCache;
import org.apache.amoro.io.reader.GenericCombinedIcebergDataReader;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.TestHelpers;
import org.apache.iceberg.data.FileHelpers;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.types.TypeUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Tests for StructProjection hoisting in CombinedDeleteFilter.initializeBloomFilter().
 *
 * <p>Verifies that hoisting StructProjection.create() outside the per-record loop (reusing the
 * projection object via .wrap()) produces correct bloom filter initialization and equality-delete
 * filtering results, including the multiple-delete-schema scenario.
 */
@RunWith(Parameterized.class)
public class TestCombinedDeleteFilterStructProjection extends TableTestBase {

  private final FileFormat fileFormat;

  @Parameterized.Parameters(name = "fileFormat = {0}")
  public static Object[][] parameters() {
    return new Object[][] {{FileFormat.PARQUET}, {FileFormat.AVRO}, {FileFormat.ORC}};
  }

  public TestCombinedDeleteFilterStructProjection(FileFormat fileFormat) {
    super(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(false, false, buildTableProperties(fileFormat)));
    this.fileFormat = fileFormat;
    System.setProperty(DeleteCache.DELETE_CACHE_ENABLED, "false");
  }

  private static Map<String, String> buildTableProperties(FileFormat fileFormat) {
    Map<String, String> props = Maps.newHashMapWithExpectedSize(3);
    props.put(TableProperties.FORMAT_VERSION, "2");
    props.put(TableProperties.DEFAULT_FILE_FORMAT, fileFormat.name());
    props.put(TableProperties.DELETE_DEFAULT_FILE_FORMAT, fileFormat.name());
    return props;
  }

  /**
   * Lower the bloom-filter threshold so the filter is activated with a small dataset (< 3 data
   * records) while still having > threshold eq-delete records.
   */
  private static final long BLOOM_TRIGGER = 2L;

  @Before
  public void resetBloomFilterThreshold() {
    CombinedDeleteFilter.FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = BLOOM_TRIGGER;
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private OutputFileFactory outputFileFactory() {
    return OutputFileFactory.builderFor(getMixedTable().asUnkeyedTable(), 0, 1)
        .format(fileFormat)
        .build();
  }

  private DataFile writeDataFile(List<Record> records) throws IOException {
    return FileHelpers.writeDataFile(
        getMixedTable().asUnkeyedTable(),
        outputFileFactory().newOutputFile(TestHelpers.Row.of()).encryptingOutputFile(),
        TestHelpers.Row.of(),
        records);
  }

  private DeleteFile writeEqDeleteFile(List<Record> records, Schema deleteSchema)
      throws IOException {
    return FileHelpers.writeDeleteFile(
        getMixedTable().asUnkeyedTable(),
        outputFileFactory().newOutputFile(TestHelpers.Row.of()).encryptingOutputFile(),
        TestHelpers.Row.of(),
        records,
        deleteSchema);
  }

  private GenericCombinedIcebergDataReader buildReader(RewriteFilesInput input) {
    return new GenericCombinedIcebergDataReader(
        getMixedTable().io(),
        getMixedTable().schema(),
        getMixedTable().spec(),
        getMixedTable().asUnkeyedTable().encryption(),
        null,
        false,
        IdentityPartitionConverters::convertConstant,
        false,
        null,
        input,
        "");
  }

  // ---------------------------------------------------------------------------
  // Test 1: bloom filter is active and correctly identifies equality-deleted rows
  //         with a single delete schema (hoisted StructProjection reuse path)
  // ---------------------------------------------------------------------------

  /**
   * Verifies that with the bloom-filter path active, records matched by equality-delete files are
   * filtered out and the remainder survives — exercising the hoisted StructProjection code.
   */
  @Test
  public void testBloomFilterWithHoistedProjection_singleDeleteSchema() throws IOException {
    // 3 data rows: id=1,2,3
    DataFile dataFile =
        writeDataFile(
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "alice", 10L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "bob", 20L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "carol", 30L, "1970-01-01T08:00:00")));

    // eq-delete on id: delete id=1 and id=2  (> BLOOM_TRIGGER records so filter activates)
    Schema idSchema = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRec = GenericRecord.create(idSchema);
    List<Record> deleteRecords = new ArrayList<>();
    deleteRecords.add(idRec.copy("id", 1));
    deleteRecords.add(idRec.copy("id", 2));
    deleteRecords.add(idRec.copy("id", 99)); // extra to push count above threshold
    DeleteFile eqDeleteFile = writeEqDeleteFile(deleteRecords, idSchema);

    RewriteFilesInput input =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {},
            new DeleteFile[] {MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile, 2L)},
            getMixedTable());

    GenericCombinedIcebergDataReader reader = buildReader(input);
    Assert.assertTrue("Bloom filter should be active", reader.getDeleteFilter().isFilterEqDelete());

    try (CloseableIterable<Record> surviving = reader.readData()) {
      List<Record> result = Lists.newArrayList(surviving);
      Assert.assertEquals("Only id=3 should survive", 1, result.size());
      Assert.assertEquals(3, result.get(0).get(0));
    }

    try (CloseableIterable<Record> deleted = reader.readDeletedData()) {
      Assert.assertEquals(
          "id=1 and id=2 should be reported as deleted", 2, Iterables.size(deleted));
    }

    reader.close();
  }

  // ---------------------------------------------------------------------------
  // Test 2: multiple delete schemas — the per-schema StructProjection loop in
  //         initializeBloomFilter must wrap() each record against each schema
  // ---------------------------------------------------------------------------

  /**
   * Uses two equality-delete files whose schemas differ (id-only vs id+name). Both must be put into
   * the bloom filter correctly so that applyEqDeletesForSchema can later verify membership.
   */
  @Test
  public void testBloomFilterWithHoistedProjection_multipleDeleteSchemas() throws IOException {
    DataFile dataFile =
        writeDataFile(
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "alice", 10L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "bob", 20L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "carol", 30L, "1970-01-01T08:00:00")));

    // Schema A: delete by id only
    Schema idSchema = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRec = GenericRecord.create(idSchema);
    List<Record> deleteByIdRecords = new ArrayList<>();
    IntStream.rangeClosed(1, 3).forEach(id -> deleteByIdRecords.add(idRec.copy("id", id)));
    DeleteFile eqDeleteById = writeEqDeleteFile(deleteByIdRecords, idSchema);

    // Schema B: delete by id + name (different schema → separate bloom-filter projection)
    Schema idNameSchema = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1, 2));
    GenericRecord idNameRec = GenericRecord.create(idNameSchema);
    List<Record> deleteByIdNameRecords = new ArrayList<>();
    IntStream.rangeClosed(1, 3)
        .forEach(
            id ->
                deleteByIdNameRecords.add(
                    idNameRec.copy("id", id, "name", id == 1 ? "alice" : "other")));
    DeleteFile eqDeleteByIdName = writeEqDeleteFile(deleteByIdNameRecords, idNameSchema);

    RewriteFilesInput input =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {},
            new DeleteFile[] {
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteById, 2L),
              MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteByIdName, 3L)
            },
            getMixedTable());

    GenericCombinedIcebergDataReader reader = buildReader(input);
    Assert.assertTrue("Bloom filter should be active", reader.getDeleteFilter().isFilterEqDelete());

    // id=1,2,3 are all deleted by eqDeleteById; none should survive
    try (CloseableIterable<Record> surviving = reader.readData()) {
      Assert.assertEquals("All records should be deleted", 0, Iterables.size(surviving));
    }

    try (CloseableIterable<Record> deleted = reader.readDeletedData()) {
      Assert.assertEquals("All 3 rows should appear as deleted", 3, Iterables.size(deleted));
    }

    reader.close();
  }

  // ---------------------------------------------------------------------------
  // Test 3: verify that the bloom filter does NOT wrongly exclude rows that are
  //         present in the data but absent from the eq-delete files
  // ---------------------------------------------------------------------------

  /**
   * Ensures false-negative freedom: records NOT covered by any equality-delete survive even when
   * the bloom filter path is active (i.e. the hoisted StructProjection wraps records faithfully).
   */
  @Test
  public void testBloomFilterWithHoistedProjection_noFalseNegatives() throws IOException {
    DataFile dataFile =
        writeDataFile(
            Arrays.asList(
                MixedDataTestHelpers.createRecord(10, "diana", 100L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(20, "eve", 200L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(30, "frank", 300L, "1970-01-01T08:00:00")));

    // eq-delete on id: only id=10 is deleted; insert extra entries to exceed bloom threshold
    Schema idSchema = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRec = GenericRecord.create(idSchema);
    List<Record> deleteRecords = new ArrayList<>();
    deleteRecords.add(idRec.copy("id", 10));
    // pad to exceed BLOOM_TRIGGER
    deleteRecords.add(idRec.copy("id", 999));
    deleteRecords.add(idRec.copy("id", 9999));
    DeleteFile eqDeleteFile = writeEqDeleteFile(deleteRecords, idSchema);

    RewriteFilesInput input =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {},
            new DeleteFile[] {MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile, 2L)},
            getMixedTable());

    GenericCombinedIcebergDataReader reader = buildReader(input);
    Assert.assertTrue("Bloom filter should be active", reader.getDeleteFilter().isFilterEqDelete());

    try (CloseableIterable<Record> surviving = reader.readData()) {
      List<Record> result = Lists.newArrayList(surviving);
      Assert.assertEquals("id=20 and id=30 should survive", 2, result.size());
    }

    reader.close();
  }

  // ---------------------------------------------------------------------------
  // Test 4: bloom filter inactive (below threshold) — non-bloom code path
  //         should also work correctly after the refactor
  // ---------------------------------------------------------------------------

  /**
   * Resets the threshold above the delete-record count so the bloom filter is not activated.
   * Confirms the non-bloom code path still correctly applies equality deletes.
   */
  @Test
  public void testEqualityDeleteWithoutBloomFilter() throws IOException {
    // Set threshold high so bloom filter is NOT activated
    CombinedDeleteFilter.FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 1_000_000L;

    DataFile dataFile =
        writeDataFile(
            Arrays.asList(
                MixedDataTestHelpers.createRecord(1, "alice", 10L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(2, "bob", 20L, "1970-01-01T08:00:00"),
                MixedDataTestHelpers.createRecord(3, "carol", 30L, "1970-01-01T08:00:00")));

    Schema idSchema = TypeUtil.select(BasicTableTestHelper.TABLE_SCHEMA, Sets.newHashSet(1));
    GenericRecord idRec = GenericRecord.create(idSchema);
    DeleteFile eqDeleteFile =
        writeEqDeleteFile(Collections.singletonList(idRec.copy("id", 2)), idSchema);

    RewriteFilesInput input =
        new RewriteFilesInput(
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DataFile[] {MixedDataTestHelpers.wrapIcebergDataFile(dataFile, 1L)},
            new DeleteFile[] {},
            new DeleteFile[] {MixedDataTestHelpers.wrapIcebergDeleteFile(eqDeleteFile, 2L)},
            getMixedTable());

    GenericCombinedIcebergDataReader reader = buildReader(input);
    Assert.assertFalse(
        "Bloom filter should NOT be active", reader.getDeleteFilter().isFilterEqDelete());

    try (CloseableIterable<Record> surviving = reader.readData()) {
      List<Record> result = Lists.newArrayList(surviving);
      Assert.assertEquals("id=1 and id=3 should survive", 2, result.size());
    }

    reader.close();
  }
}
