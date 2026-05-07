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
import org.apache.amoro.flink.FlinkTestBase;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.stream.Stream;

public class TestAdaptHiveWriter extends FlinkTestBase {

  static final TestHMS TEST_HMS = new TestHMS();

  @BeforeAll
  public static void startTestHms() throws Exception {
    TEST_HMS.before();
  }

  @AfterAll
  public static void stopTestHms() {
    TEST_HMS.after();
  }

  static Stream<Arguments> parameters() {
    return Stream.of(
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, false)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(false, true)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(false, false)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true, FILE_FORMAT_ORC)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, false, FILE_FORMAT_ORC)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(false, true, FILE_FORMAT_ORC)),
        Arguments.of(
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(false, false, FILE_FORMAT_ORC)));
  }

  private void setUpForParam(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper)
      throws Exception {
    initFlinkTestBase(catalogTestHelper, tableTestHelper);
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testKeyedTableWriteTypeFromOperateKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    MixedTable testKeyedHiveTable = getMixedTable();
    FlinkTaskWriterBuilder builder =
        FlinkTaskWriterBuilder.buildFor(testKeyedHiveTable)
            .withFlinkSchema(FlinkSchemaUtil.convert(testKeyedHiveTable.schema()));

    Assertions.assertTrue(
        builder.buildWriter(ChangeLocationKind.INSTANT) instanceof FlinkChangeTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(BaseLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(HiveLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);

    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.APPEND) instanceof FlinkChangeTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.OVERWRITE) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.MINOR_OPTIMIZE) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.MAJOR_OPTIMIZE) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.FULL_OPTIMIZE) instanceof FlinkBaseTaskWriter);
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnKeyedTableWriteTypeFromOperateKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    MixedTable testHiveTable = getMixedTable();
    FlinkTaskWriterBuilder builder =
        FlinkTaskWriterBuilder.buildFor(testHiveTable)
            .withFlinkSchema(FlinkSchemaUtil.convert(testHiveTable.schema()));

    Assertions.assertTrue(
        builder.buildWriter(BaseLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(HiveLocationKind.INSTANT) instanceof FlinkBaseTaskWriter);

    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.APPEND) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.OVERWRITE) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.MAJOR_OPTIMIZE) instanceof FlinkBaseTaskWriter);
    Assertions.assertTrue(
        builder.buildWriter(WriteOperationKind.FULL_OPTIMIZE) instanceof FlinkBaseTaskWriter);
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testKeyedTableChangeWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testKeyedTableBaseWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testKeyedTableHiveWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnPartitionKeyedTableChangeWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnPartitionKeyedTableBaseWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnPartitionKeyedTableHiveWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeTrue(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnKeyedTableChangeWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    try {
      testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
    } catch (Exception e) {
      Assertions.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnKeyedTableBaseWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnKeyedTableHiveWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeTrue(isPartitionedTable());
    testWrite(getMixedTable(), HiveLocationKind.INSTANT, geneRowData(), "hive");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnPartitionUnKeyedTableChangeWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
    try {
      testWrite(getMixedTable(), ChangeLocationKind.INSTANT, geneRowData(), "change");
    } catch (Exception e) {
      Assertions.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnPartitionUnKeyedTableBaseWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
    testWrite(getMixedTable(), BaseLocationKind.INSTANT, geneRowData(), "base");
  }

  @ParameterizedTest(name = "{0}, {1}")
  @MethodSource("parameters")
  public void testUnPartitionUnKeyedTableHiveWriteByLocationKind(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) throws Exception {
    setUpForParam(catalogTestHelper, tableTestHelper);

    Assumptions.assumeFalse(isKeyedTable());
    Assumptions.assumeFalse(isPartitionedTable());
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
        .forEach(s -> Assertions.assertTrue(s.path().toString().contains(pathFeature)));
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
    Assertions.assertEquals(result, records.stream().collect(Collectors.toSet()));
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
