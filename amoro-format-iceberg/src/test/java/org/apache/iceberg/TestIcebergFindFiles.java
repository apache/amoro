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

package org.apache.iceberg;

import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class TestIcebergFindFiles extends TestBase {

  @Parameterized.Parameters(name = "formatVersion = {0}")
  public static List<Object> parameters() {
    return Arrays.asList(1, 2);
  }

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  public TestIcebergFindFiles(int formatVersion) {
    this.formatVersion = formatVersion;
  }

  @Before
  public void setUp() throws Exception {
    this.tableDir = tempDir.newFolder();
    this.tableDir.delete();
    super.setupTable();
  }

  @After
  public void cleanUp() throws Exception {
    super.cleanupTables();
  }

  @Test
  public void testBasicBehavior() {
    table.newAppend().appendFile(FILE_A).appendFile(FILE_B).commit();

    Iterable<ContentFile<?>> files = transform(new IcebergFindFiles(table).entries());

    Assert.assertEquals(pathSet(FILE_A, FILE_B), pathSet(files));
  }

  @Test
  public void testWithMetadataMatching() {
    table
        .newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .appendFile(FILE_C)
        .appendFile(FILE_D)
        .commit();

    Iterable<ContentFile<?>> files =
        transform(
            new IcebergFindFiles(table)
                .filterFiles(Expressions.startsWith("file_path", "/path/to/data-a"))
                .entries());
    Assert.assertEquals(pathSet(FILE_A), pathSet(files));
  }

  @Test
  public void testWithRecordsMatching() {
    table
        .newAppend()
        .appendFile(
            DataFiles.builder(SPEC)
                .withInputFile(Files.localInput("/path/to/data-e.parquet"))
                .withPartitionPath("data_bucket=4")
                .withMetrics(
                    new Metrics(
                        3L,
                        null, // no column sizes
                        ImmutableMap.of(1, 3L), // value count
                        ImmutableMap.of(1, 0L), // null count
                        null,
                        ImmutableMap.of(
                            1,
                            Conversions.toByteBuffer(Types.IntegerType.get(), 1)), // lower bounds
                        ImmutableMap.of(
                            1,
                            Conversions.toByteBuffer(Types.IntegerType.get(), 5)))) // lower bounds
                .build())
        .commit();

    final Iterable<ContentFile<?>> files =
        transform(new IcebergFindFiles(table).filterData(Expressions.equal("id", 1)).entries());

    Assert.assertEquals(Sets.newHashSet("/path/to/data-e.parquet"), pathSet(files));
  }

  @Test
  public void testInPartition() {
    table
        .newAppend()
        .appendFile(FILE_A) // bucket 0
        .appendFile(FILE_B) // bucket 1
        .appendFile(FILE_C) // bucket 2
        .appendFile(FILE_D) // bucket 3
        .commit();

    Iterable<ContentFile<?>> files =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(
                    table.spec(),
                    Lists.newArrayList(StaticDataTask.Row.of(1), StaticDataTask.Row.of(2)))
                .entries());

    Assert.assertEquals(pathSet(FILE_B, FILE_C), pathSet(files));
  }

  @Test
  public void testInPartitionForRemovePartitionField() {
    table
        .newAppend()
        .appendFile(FILE_A) // bucket 0
        .appendFile(FILE_B) // bucket 1
        .appendFile(FILE_C) // bucket 2
        .appendFile(FILE_D) // bucket 3
        .commit();
    table.updateSpec().removeField(Expressions.bucket("data", 16)).commit();

    DataFile fileDropPartitionField =
        DataFiles.builder(table.spec())
            .withPath("/path/to/data-drop-partition.parquet")
            .withFileSizeInBytes(10L)
            .withRecordCount(1L)
            .build();
    table
        .newAppend()
        .appendFile(fileDropPartitionField) // without partition field
        .commit();

    Iterable<ContentFile<?>> files =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(
                    table.specs().get(0),
                    Lists.newArrayList(StaticDataTask.Row.of(1), StaticDataTask.Row.of(2)))
                .entries());

    Assert.assertEquals(pathSet(FILE_B, FILE_C), pathSet(files));

    Iterable<ContentFile<?>> files2 =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(table.spec(), StaticDataTask.Row.of(new Object[] {null}))
                .entries());

    Assert.assertEquals(pathSet(fileDropPartitionField), pathSet(files2));
  }

  @Test
  public void testInPartitionForAddPartitionField() {
    table
        .newAppend()
        .appendFile(FILE_A) // bucket 0
        .appendFile(FILE_B) // bucket 1
        .appendFile(FILE_C) // bucket 2
        .appendFile(FILE_D) // bucket 3
        .commit();

    table.updateSpec().addField(Expressions.bucket("id", 16)).commit();

    DataFile fileAddPartitionField =
        DataFiles.builder(table.spec())
            .withPath("/path/to/data-add-partition.parquet")
            .withFileSizeInBytes(10)
            .withPartitionPath("data_bucket=0/id_bucket_16=1") // easy way to set partition data for
            // now
            .withRecordCount(1)
            .build();

    table.newAppend().appendFile(fileAddPartitionField).commit();

    Iterable<ContentFile<?>> files =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(
                    table.specs().get(0), StaticDataTask.Row.of(1), StaticDataTask.Row.of(2))
                .entries());

    Assert.assertEquals(pathSet(FILE_B, FILE_C), pathSet(files));

    Iterable<ContentFile<?>> files2 =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(table.specs().get(1), StaticDataTask.Row.of(0, 1))
                .entries());

    Assert.assertEquals(pathSet(fileAddPartitionField), pathSet(files2));
  }

  @Test
  public void testInPartitionForReplacePartitionField() {
    Assume.assumeTrue(formatVersion == 2);
    table
        .newAppend()
        .appendFile(FILE_A) // bucket 0
        .appendFile(FILE_B) // bucket 1
        .appendFile(FILE_C) // bucket 2
        .appendFile(FILE_D) // bucket 3
        .commit();

    table
        .updateSpec()
        .removeField(Expressions.bucket("data", 16))
        .addField(Expressions.bucket("id", 16))
        .commit();

    DataFile fileReplacePartitionField =
        DataFiles.builder(table.spec())
            .withPath("/path/to/data-add-partition.parquet")
            .withFileSizeInBytes(10)
            .withPartitionPath("id_bucket_16=1") // easy way to set partition data
            // for
            // now
            .withRecordCount(1)
            .build();

    table.newAppend().appendFile(fileReplacePartitionField).commit();

    Iterable<ContentFile<?>> files =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(
                    table.specs().get(0), StaticDataTask.Row.of(1), StaticDataTask.Row.of(2))
                .entries());

    Assert.assertEquals(pathSet(FILE_B, FILE_C), pathSet(files));

    Iterable<ContentFile<?>> files2 =
        transform(
            new IcebergFindFiles(table)
                .inPartitions(table.specs().get(1), StaticDataTask.Row.of(1))
                .entries());

    Assert.assertEquals(pathSet(fileReplacePartitionField), pathSet(files2));
  }

  @Test
  public void testAsOfTimestamp() {
    table.newAppend().appendFile(FILE_A).commit();

    table.newAppend().appendFile(FILE_B).commit();

    long timestamp = System.currentTimeMillis();

    table.newAppend().appendFile(FILE_C).commit();

    table.newAppend().appendFile(FILE_D).commit();

    Iterable<ContentFile<?>> files =
        transform(new IcebergFindFiles(table).asOfTime(timestamp).entries());

    Assert.assertEquals(pathSet(FILE_A, FILE_B), pathSet(files));
  }

  @Test
  public void testSnapshotId() {
    table.newAppend().appendFile(FILE_A).appendFile(FILE_B).commit();

    table.newAppend().appendFile(FILE_C).commit();

    long snapshotId = table.currentSnapshot().snapshotId();

    table.newAppend().appendFile(FILE_D).commit();

    Iterable<ContentFile<?>> files =
        transform(new IcebergFindFiles(table).inSnapshot(snapshotId).entries());

    Assert.assertEquals(pathSet(FILE_A, FILE_B, FILE_C), pathSet(files));
  }

  @Test
  public void testCaseSensitivity() {
    table
        .newAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .appendFile(FILE_C)
        .appendFile(FILE_D)
        .commit();

    Iterable<ContentFile<?>> files =
        transform(
            new IcebergFindFiles(table)
                .caseSensitive(false)
                .filterFiles(Expressions.startsWith("FILE_PATH", "/path/to/data-a"))
                .entries());

    Assert.assertEquals(pathSet(FILE_A), pathSet(files));
  }

  @Test
  public void testIncludeColumnStats() {
    table.newAppend().appendFile(FILE_WITH_STATS).commit();

    Iterable<ContentFile<?>> files =
        transform(new IcebergFindFiles(table).includeColumnStats().entries());
    final ContentFile<?> file = files.iterator().next();

    Assert.assertEquals(FILE_WITH_STATS.columnSizes(), file.columnSizes());
    Assert.assertEquals(FILE_WITH_STATS.valueCounts(), file.valueCounts());
    Assert.assertEquals(FILE_WITH_STATS.nullValueCounts(), file.nullValueCounts());
    Assert.assertEquals(FILE_WITH_STATS.nanValueCounts(), file.nanValueCounts());
    Assert.assertEquals(FILE_WITH_STATS.lowerBounds(), file.lowerBounds());
    Assert.assertEquals(FILE_WITH_STATS.upperBounds(), file.upperBounds());
  }

  @Test
  public void testFileContent() {
    Assume.assumeTrue(formatVersion == 2);
    table.newRowDelta().addRows(FILE_A).addDeletes(FILE_A_DELETES).commit();

    Iterable<ContentFile<?>> allFiles = transform(new IcebergFindFiles(table).entries());
    Assert.assertEquals(pathSet(FILE_A, FILE_A_DELETES), pathSet(allFiles));

    Iterable<ContentFile<?>> dataFiles =
        transform(new IcebergFindFiles(table).fileContent(ManifestContent.DATA).entries());
    Assert.assertEquals(pathSet(FILE_A), pathSet(dataFiles));

    Iterable<ContentFile<?>> deleteFiles =
        transform(new IcebergFindFiles(table).fileContent(ManifestContent.DELETES).entries());
    Assert.assertEquals(pathSet(FILE_A_DELETES), pathSet(deleteFiles));
  }

  @Test
  public void testNoSnapshot() {
    // a table has no snapshot when it just gets created and no data is loaded yet

    // if not handled properly, NPE will be thrown in collect()
    Iterable<IcebergFindFiles.IcebergManifestEntry> files = new IcebergFindFiles(table).entries();

    // verify an empty collection of data file is returned
    Assert.assertEquals(0, Sets.newHashSet(files).size());
  }

  private Set<String> pathSet(ContentFile<?>... files) {
    return Sets.newHashSet(
        Iterables.transform(Arrays.asList(files), file -> file.path().toString()));
  }

  private Set<String> pathSet(Iterable<ContentFile<?>> files) {
    return Sets.newHashSet(Iterables.transform(files, file -> file.path().toString()));
  }

  private Iterable<ContentFile<?>> transform(
      Iterable<IcebergFindFiles.IcebergManifestEntry> entries) {
    return Iterables.transform(entries, IcebergFindFiles.IcebergManifestEntry::getFile);
  }
}
