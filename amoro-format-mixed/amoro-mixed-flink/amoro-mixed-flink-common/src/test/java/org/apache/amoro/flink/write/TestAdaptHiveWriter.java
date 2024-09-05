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

package org.apache.amoro.flink.write;

import static org.apache.amoro.table.TableProperties.FILE_FORMAT_ORC;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.flink.read.AdaptHiveFlinkParquetReaders;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.hive.table.HiveLocationKind;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterators;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BaseLocationKind;
import org.apache.amoro.table.ChangeLocationKind;
import org.apache.amoro.table.LocationKind;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.WriteOperationKind;
import org.apache.flink.table.data.DecimalData;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.data.FlinkOrcReader;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestAdaptHiveWriter extends TableTestBase {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestAdaptHiveWriter(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true, FILE_FORMAT_ORC)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false, FILE_FORMAT_ORC)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true, FILE_FORMAT_ORC)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false, FILE_FORMAT_ORC)
      }
    };
  }

  @Test
  public void testKeyedTableWriteTypeFromOperateKind() {
    Assume.assumeTrue(isKeyedTable());
    MixedTable testKeyedHiveTable = getMixedTable();
    FlinkTaskWriterBuilder builder =
        FlinkTaskWriterBuilder.buildFor(testKeyedHiveTable)
            .withFlinkSchema(FlinkSchemaUtil.convert(testKeyedHiveTable.schema()));

    Assert.assertTrue(
        builder.buildWriter(ChangeLocationKind.INSTANT) instanceof FlinkChangeTaskWriter);
    Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);

    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.APPEND) instanceof FlinkChangeTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.OVERWRITE) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.MINOR_OPTIMIZE) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.MAJOR_OPTIMIZE) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.FULL_OPTIMIZE) instanceof FlinkBaseTaskWriter);
  }

  @Test
  public void testUnKeyedTableWriteTypeFromOperateKind() {
    Assume.assumeFalse(isKeyedTable());
    MixedTable testHiveTable = getMixedTable();
    FlinkTaskWriterBuilder builder =
        FlinkTaskWriterBuilder.buildFor(testHiveTable)
            .withFlinkSchema(FlinkSchemaUtil.convert(testHiveTable.schema()));

    Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);

    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.APPEND) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.OVERWRITE) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.MAJOR_OPTIMIZE) instanceof FlinkBaseTaskWriter);
    Assert.assertTrue(
        builder.buildWriter(WriteOperationKind.FULL_OPTIMIZE) instanceof FlinkBaseTaskWriter);
  }

  @Test
  public void testKeyedTableChangeWriteByLocationKind() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
  }

  @Test
  public void testKeyedTableBaseWriteByLocationKind() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @Test
  public void testKeyedTableHiveWriteByLocationKind() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  @Test
  public void testUnPartitionKeyedTableChangeWriteByLocationKind() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
  }

  @Test
  public void testUnPartitionKeyedTableBaseWriteByLocationKind() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @Test
  public void testUnPartitionKeyedTableHiveWriteByLocationKind() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  @Test
  public void testUnKeyedTableChangeWriteByLocationKind() throws IOException {
    Assume.assumeFalse(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    try {
      testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testUnKeyedTableBaseWriteByLocationKind() throws IOException {
    Assume.assumeFalse(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @Test
  public void testUnKeyedTableHiveWriteByLocationKind() throws IOException {
    Assume.assumeFalse(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  @Test
  public void testUnPartitionUnKeyedTableChangeWriteByLocationKind() throws IOException {
    Assume.assumeFalse(isKeyedTable());
    Assume.assumeFalse(isPartitionedTable());
    try {
      testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testUnPartitionUnKeyedTableBaseWriteByLocationKind() throws IOException {
    Assume.assumeFalse(isKeyedTable());
    Assume.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @Test
  public void testUnPartitionUnKeyedTableHiveWriteByLocationKind() throws IOException {
    Assume.assumeFalse(isKeyedTable());
    Assume.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  public void testWrite(
      MixedTable table, LocationKind locationKind, List<RowData> records, String pathFeature)
      throws IOException {
    FlinkTaskWriterBuilder builder =
        FlinkTaskWriterBuilder.buildFor(table)
            .withFlinkSchema(FlinkSchemaUtil.convert(table.schema()));

    TaskWriter<RowData> changeWrite = builder.buildWriter(locationKind);
    for (RowData record : records) {
      changeWrite.write(record);
    }
    WriteResult complete = changeWrite.complete();
    Arrays.stream(complete.dataFiles())
        .forEach(s -> Assert.assertTrue(s.path().toString().contains(pathFeature)));
    CloseableIterable<RowData> concat =
        CloseableIterable.concat(
            Arrays.stream(complete.dataFiles())
                .map(
                    s -> {
                      switch (s.format()) {
                        case PARQUET:
                          return readParquet(table.schema(), s.path().toString());
                        case ORC:
                          return readOrc(table.schema(), s.path().toString());
                        default:
                          throw new UnsupportedOperationException(
                              "Cannot read unknown format: " + s.format());
                      }
                    })
                .collect(Collectors.toList()));
    Set<RowData> result = new HashSet<>();
    Iterators.addAll(result, concat.iterator());
    Assert.assertEquals(result, records.stream().collect(Collectors.toSet()));
  }

  private CloseableIterable<RowData> readParquet(Schema schema, String path) {
    AdaptHiveParquet.ReadBuilder builder =
        AdaptHiveParquet.read(Files.localInput(path))
            .project(schema)
            .createReaderFunc(
                fileSchema ->
                    AdaptHiveFlinkParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
            .caseSensitive(false);

    CloseableIterable<RowData> iterable = builder.build();
    return iterable;
  }

  private CloseableIterable<RowData> readOrc(Schema schema, String path) {
    ORC.ReadBuilder builder =
        ORC.read(Files.localInput(path))
            .project(schema)
            .createReaderFunc(fileSchema -> new FlinkOrcReader(schema, fileSchema, new HashMap<>()))
            .caseSensitive(false);

    CloseableIterable<RowData> iterable = builder.build();
    return iterable;
  }

  private List<RowData> geneRowData() {
    return Lists.newArrayList(geneRowData(1, "lily", 0, "2022-01-02T12:00:00"));
  }

  private RowData geneRowData(int id, String name, long ts, String timestamp) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    return GenericRowData.of(
        id,
        StringData.fromString(name),
        ts,
        TimestampData.fromLocalDateTime(LocalDateTime.parse(timestamp, formatter)),
        TimestampData.fromLocalDateTime(LocalDateTime.parse(timestamp, formatter)),
        DecimalData.fromBigDecimal(new BigDecimal("0"), 10, 0),
        StringData.fromString(timestamp.substring(0, 10)));
  }
}
