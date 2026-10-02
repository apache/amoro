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

package org.apache.amoro.io.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.MetricsModes;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.TestHelpers;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.Record;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestSortedPosDeleteWriterHeapFlush extends TableTestBase {

  public TestSortedPosDeleteWriterHeapFlush() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false));
  }

  @BeforeAll
  public static void startAms() throws Exception {
    TEST_AMS.before();
  }

  @AfterAll
  public static void stopAms() {
    TEST_AMS.after();
  }

  @BeforeEach
  public void setup() throws Exception {
    temp.create();
    setupCatalog();
    setupTable();
  }

  @AfterEach
  public void tearDown() {
    if (getCatalogMeta() != null) {
      dropTable();
      dropCatalog();
    }
    temp.delete();
  }

  @Test
  public void testFlushTriggeredByHeapUsage() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            0.8d,
            2,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 90L;
              }
            });

    writer.delete("file-a", 1L);
    writer.delete("file-a", 2L);
    writer.delete("file-a", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(2, deleteFiles.size());
    List<Long> recordCounts =
        deleteFiles.stream().map(DeleteFile::recordCount).sorted().collect(Collectors.toList());
    assertEquals(Arrays.asList(1L, 2L), recordCounts);
  }

  @Test
  public void testHeapFlushDisabledByRatio() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            0d,
            1,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 100L;
              }
            });

    writer.delete("file-b", 1L);
    writer.delete("file-b", 2L);
    writer.delete("file-b", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(1, deleteFiles.size());
    assertEquals(3L, deleteFiles.get(0).recordCount());
  }

  @Test
  public void testHeapFlushDisabledByRatioAtLeastOne() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            1.0d,
            1,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 100L;
              }
            });

    writer.delete("file-c", 1L);
    writer.delete("file-c", 2L);
    writer.delete("file-c", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(1, deleteFiles.size());
    assertEquals(3L, deleteFiles.get(0).recordCount());
  }

  @Test
  public void testHeapFlushDisabledByNegativeRatio() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            -0.1d,
            1,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 100L;
              }
            });

    writer.delete("file-d", 1L);
    writer.delete("file-d", 2L);
    writer.delete("file-d", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(1, deleteFiles.size());
    assertEquals(3L, deleteFiles.get(0).recordCount());
  }

  @Test
  public void testHeapFlushDisabledWhenMaxMemoryUnavailable() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            0.8d,
            1,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 0L;
              }

              @Override
              public long usedMemory() {
                return 100L;
              }
            });

    writer.delete("file-e", 1L);
    writer.delete("file-e", 2L);
    writer.delete("file-e", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(1, deleteFiles.size());
    assertEquals(3L, deleteFiles.get(0).recordCount());
  }

  @Test
  public void testHeapFlushWhenMinRecordsZero() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            0.8d,
            0,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 90L;
              }
            });

    writer.delete("file-f", 1L);
    writer.delete("file-f", 2L);
    writer.delete("file-f", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(3, deleteFiles.size());
    assertEquals(Arrays.asList(1L, 1L, 1L), recordCounts(deleteFiles));
  }

  @Test
  public void testMultipleHeapFlushCycles() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            0.8d,
            2,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 90L;
              }
            });

    writer.delete("file-g", 1L);
    writer.delete("file-g", 2L);
    writer.delete("file-g", 3L);
    writer.delete("file-g", 4L);
    writer.delete("file-g", 5L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(3, deleteFiles.size());
    assertEquals(Arrays.asList(1L, 2L, 2L), recordCounts(deleteFiles));
  }

  @Test
  public void testRecordsNumThresholdTriggersWithoutHeapSampling() throws IOException {
    SortedPosDeleteWriter<Record> writer =
        newWriter(
            3L,
            0.8d,
            10,
            new SortedPosDeleteWriter.HeapUsageProvider() {
              @Override
              public long maxMemory() {
                return 100L;
              }

              @Override
              public long usedMemory() {
                return 90L;
              }
            });

    writer.delete("file-h", 1L);
    writer.delete("file-h", 2L);
    writer.delete("file-h", 3L);

    List<DeleteFile> deleteFiles = writer.complete();
    assertEquals(1, deleteFiles.size());
    assertEquals(3L, deleteFiles.get(0).recordCount());
  }

  private SortedPosDeleteWriter<Record> newWriter(
      double heapUsageRatioThreshold,
      int heapFlushMinRecords,
      SortedPosDeleteWriter.HeapUsageProvider heapUsageProvider) {
    return newWriter(
        Long.MAX_VALUE, heapUsageRatioThreshold, heapFlushMinRecords, heapUsageProvider);
  }

  private SortedPosDeleteWriter<Record> newWriter(
      long recordsNumThreshold,
      double heapUsageRatioThreshold,
      int heapFlushMinRecords,
      SortedPosDeleteWriter.HeapUsageProvider heapUsageProvider) {
    UnkeyedTable base = getBaseStore();
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(base.schema(), base.spec());
    appenderFactory.setAll(getMixedTable().properties());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX
            + MetadataColumns.DELETE_FILE_PATH.name(),
        MetricsModes.Full.get().toString());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX
            + MetadataColumns.DELETE_FILE_POS.name(),
        MetricsModes.Full.get().toString());

    StructLike partitionData = TestHelpers.Row.of();
    return new SortedPosDeleteWriter<>(
        appenderFactory,
        new CommonOutputFileFactory(
            base.location(),
            base.spec(),
            FileFormat.PARQUET,
            getMixedTable().io(),
            base.encryption(),
            0,
            0,
            0L),
        getMixedTable().io(),
        FileFormat.PARQUET,
        0,
        0,
        partitionData,
        recordsNumThreshold,
        heapUsageRatioThreshold,
        heapFlushMinRecords,
        heapUsageProvider);
  }

  private List<Long> recordCounts(List<DeleteFile> deleteFiles) {
    return deleteFiles.stream().map(DeleteFile::recordCount).sorted().collect(Collectors.toList());
  }
}
