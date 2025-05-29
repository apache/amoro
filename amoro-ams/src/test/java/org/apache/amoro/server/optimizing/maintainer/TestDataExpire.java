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

package org.apache.amoro.server.optimizing.maintainer;

import static org.apache.amoro.BasicTableTestHelper.PRIMARY_KEY_SPEC;
import static org.apache.amoro.BasicTableTestHelper.SPEC;
import static org.junit.Assume.assumeTrue;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.optimizing.scan.UnkeyedTableFileScanHelper;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.scheduler.inline.ExecutorTestBase;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.ContentFiles;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.MetricsModes;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestDataExpire extends ExecutorTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      // Mixed format partitioned by timestamp
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, false, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, true, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false, getDefaultProp())
      },
      // Mixed format partitioned by timestampz
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(TABLE_SCHEMA1, PRIMARY_KEY_SPEC, SPEC, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            TABLE_SCHEMA1, PRIMARY_KEY_SPEC, PartitionSpec.unpartitioned(), getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            TABLE_SCHEMA1, PrimaryKeySpec.noPrimaryKey(), SPEC, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            TABLE_SCHEMA1,
            PrimaryKeySpec.noPrimaryKey(),
            PartitionSpec.unpartitioned(),
            getDefaultProp())
      },
      // Mixed format partitioned by date string
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(TABLE_SCHEMA2, PRIMARY_KEY_SPEC, SPEC2, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            TABLE_SCHEMA2, PRIMARY_KEY_SPEC, PartitionSpec.unpartitioned(), getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            TABLE_SCHEMA2, PrimaryKeySpec.noPrimaryKey(), SPEC2, getDefaultProp())
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(
            TABLE_SCHEMA2,
            PrimaryKeySpec.noPrimaryKey(),
            PartitionSpec.unpartitioned(),
            getDefaultProp())
      }
    };
  }

  public static final Schema TABLE_SCHEMA1 =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name", Types.StringType.get()),
          Types.NestedField.required(3, "ts", Types.LongType.get()),
          Types.NestedField.required(4, "op_time", Types.TimestampType.withZone()));

  public static final Schema TABLE_SCHEMA2 =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name", Types.StringType.get()),
          Types.NestedField.required(3, "ts", Types.LongType.get()),
          Types.NestedField.required(4, "op_time", Types.StringType.get()));

  public static final PartitionSpec SPEC2 =
      PartitionSpec.builderFor(TABLE_SCHEMA2).identity("op_time").build();

  public TestDataExpire(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testPartitionLevel() {
    if (getMixedTable().isUnkeyedTable()) {
      testUnKeyedPartitionLevel();
    } else {
      testKeyedPartitionLevel();
    }
  }

  private void testUnKeyedPartitionLevel() {
    List<Record> records =
        Lists.newArrayList(
            createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"),
            createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
            createRecord(3, "333", parseMillis("2022-01-02T12:00:00"), "2022-01-02T12:00:00"),
            createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
    OptimizingTestHelpers.appendBase(
        getMixedTable(), tableTestHelper().writeBaseStore(getMixedTable(), 0, records, false));

    DataExpirationConfig config = parseDataExpirationConfig(getMixedTable());

    getMaintainerAndExpire(config, "2022-01-03T18:00:00.000");

    List<Record> result = readSortedBaseRecords(getMixedTable());

    List<Record> expected;
    if (tableTestHelper().partitionSpec().isPartitioned()) {
      // retention time is 1 day, expire partitions that order than 2022-01-02
      if (expireByStringDate()) {
        expected =
            Lists.newArrayList(
                createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"));
      } else {
        expected =
            Lists.newArrayList(
                createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
                createRecord(3, "333", parseMillis("2022-01-02T12:00:00"), "2022-01-02T12:00:00"),
                createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
      }
    } else {
      expected =
          Lists.newArrayList(
              createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"),
              createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
              createRecord(3, "333", parseMillis("2022-01-02T12:00:00"), "2022-01-02T12:00:00"),
              createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
    }
    Assert.assertEquals(expected, result);
  }

  private void testKeyedPartitionLevel() {
    KeyedTable keyedTable = getMixedTable().asKeyedTable();

    ArrayList<Record> baseRecords =
        Lists.newArrayList(
            createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"),
            createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"));
    OptimizingTestHelpers.appendBase(
        keyedTable, tableTestHelper().writeBaseStore(keyedTable, 0, baseRecords, false));

    ArrayList<Record> newRecords =
        Lists.newArrayList(
            createRecord(3, "333", parseMillis("2022-01-02T12:00:00"), "2022-01-02T12:00:00"),
            createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
    OptimizingTestHelpers.appendChange(
        keyedTable,
        tableTestHelper().writeChangeStore(keyedTable, 1L, ChangeAction.INSERT, newRecords, false));

    CloseableIterable<TableFileScanHelper.FileScanResult> scan = buildKeyedFileScanHelper().scan();
    assertScanResult(scan, 4, 0);

    // expire partitions that order than 2022-01-02 18:00:00.000
    DataExpirationConfig config = parseDataExpirationConfig(keyedTable);
    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(keyedTable);
    tableMaintainer.expireDataFrom(
        config,
        LocalDateTime.parse("2022-01-03T18:00:00.000")
            .atZone(
                IcebergTableMaintainer.getDefaultZoneId(
                    keyedTable.schema().findField(config.getExpirationField())))
            .toInstant());

    CloseableIterable<TableFileScanHelper.FileScanResult> scanAfterExpire =
        buildKeyedFileScanHelper().scan();
    if (tableTestHelper().partitionSpec().isPartitioned()) {
      if (expireByStringDate()) {
        assertScanResult(scanAfterExpire, 1, 0);
      } else {
        assertScanResult(scanAfterExpire, 3, 0);
      }
    } else {
      assertScanResult(scanAfterExpire, 4, 0);
    }

    List<Record> records = readSortedKeyedRecords(keyedTable);
    List<Record> expected;
    if (tableTestHelper().partitionSpec().isPartitioned()) {
      if (expireByStringDate()) {
        expected =
            Lists.newArrayList(
                createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"));
      } else {
        expected =
            Lists.newArrayList(
                createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
                createRecord(3, "333", parseMillis("2022-01-02T12:00:00"), "2022-01-02T12:00:00"),
                createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
      }
    } else {
      expected =
          Lists.newArrayList(
              createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"),
              createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
              createRecord(3, "333", parseMillis("2022-01-02T12:00:00"), "2022-01-02T12:00:00"),
              createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
    }
    Assert.assertEquals(expected, records);
  }

  @Test
  public void testFileLevel() {
    MixedTable table = getMixedTable();
    table
        .updateProperties()
        .set(TableProperties.DATA_EXPIRATION_LEVEL, DataExpirationConfig.ExpireLevel.FILE.name())
        .commit();
    if (table.isUnkeyedTable()) {
      testUnKeyedFileLevel();
    } else {
      testKeyedFileLevel();
    }
  }

  private void testKeyedFileLevel() {
    KeyedTable keyedTable = getMixedTable().asKeyedTable();

    ArrayList<Record> baseRecords =
        Lists.newArrayList(
            createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"),
            createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"));
    OptimizingTestHelpers.appendBase(
        keyedTable, tableTestHelper().writeBaseStore(keyedTable, 0, baseRecords, false));

    ArrayList<Record> newRecords =
        Lists.newArrayList(
            createRecord(3, "333", parseMillis("2022-01-02T18:00:00"), "2022-01-02T18:00:00"),
            createRecord(4, "444", parseMillis("2021-12-30T19:00:00"), "2021-12-30T19:00:00"));
    OptimizingTestHelpers.appendChange(
        keyedTable,
        tableTestHelper().writeChangeStore(keyedTable, 1L, ChangeAction.INSERT, newRecords, false));

    CloseableIterable<TableFileScanHelper.FileScanResult> scan = buildKeyedFileScanHelper().scan();
    assertScanResult(scan, 4, 0);

    // expire partitions that order than 2022-01-02 18:00:00.000
    DataExpirationConfig config = parseDataExpirationConfig(keyedTable);
    MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getMixedTable());
    mixedTableMaintainer.expireDataFrom(
        config,
        LocalDateTime.parse("2022-01-03T18:00:00.000")
            .atZone(
                IcebergTableMaintainer.getDefaultZoneId(
                    keyedTable.schema().findField(config.getExpirationField())))
            .toInstant());

    CloseableIterable<TableFileScanHelper.FileScanResult> scanAfterExpire =
        buildKeyedFileScanHelper().scan();
    assertScanResult(scanAfterExpire, 1, 0);

    List<Record> records = readSortedKeyedRecords(keyedTable);
    List<Record> expected =
        Lists.newArrayList(
            createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"));
    Assert.assertEquals(expected, records);
  }

  private void testUnKeyedFileLevel() {
    List<Record> records =
        Lists.newArrayList(
            createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"),
            createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
            createRecord(3, "333", parseMillis("2022-01-02T18:00:00"), "2022-01-02T18:00:00"),
            createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
    records.forEach(
        r ->
            OptimizingTestHelpers.appendBase(
                getMixedTable(),
                tableTestHelper()
                    .writeBaseStore(getMixedTable(), 0, Lists.newArrayList(r), false)));
    CloseableIterable<TableFileScanHelper.FileScanResult> scan = getTableFileScanHelper().scan();
    assertScanResult(scan, 4, 0);

    // expire partitions that order than 2022-01-02 18:00:00.000
    DataExpirationConfig config = parseDataExpirationConfig(getMixedTable());

    getMaintainerAndExpire(config, "2022-01-03T18:00:00.000");

    List<Record> result = readSortedBaseRecords(getMixedTable());

    List<Record> expected;
    if (expireByStringDate()) {
      expected =
          Lists.newArrayList(
              createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"));
    } else {
      expected =
          Lists.newArrayList(
              createRecord(2, "222", parseMillis("2022-01-03T12:00:00"), "2022-01-03T12:00:00"),
              createRecord(4, "444", parseMillis("2022-01-02T19:00:00"), "2022-01-02T19:00:00"));
    }
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testBaseOnRule() {
    List<Record> records =
        Lists.newArrayList(
            createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"));
    OptimizingTestHelpers.appendBase(
        getMixedTable(), tableTestHelper().writeBaseStore(getMixedTable(), 0, records, false));

    DataExpirationConfig config = parseDataExpirationConfig(getMixedTable());

    if (getTestFormat().equals(TableFormat.ICEBERG)) {
      Table table = getMixedTable().asUnkeyedTable();
      IcebergTableMaintainer icebergTableMaintainer = new IcebergTableMaintainer(table);
      Types.NestedField field = table.schema().findField(config.getExpirationField());
      long lastSnapshotTime = table.currentSnapshot().timestampMillis();
      long lastCommitTime = icebergTableMaintainer.expireBaseOnRule(config, field).toEpochMilli();
      Assert.assertEquals(lastSnapshotTime, lastCommitTime);
    } else {
      MixedTable mixedTable = getMixedTable();
      MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(mixedTable);
      Types.NestedField field = getMixedTable().schema().findField(config.getExpirationField());

      long lastSnapshotTime;
      if (mixedTable.isKeyedTable()) {
        List<Record> changeRecords =
            Lists.newArrayList(
                createRecord(2, "222", parseMillis("2022-01-01T12:00:05"), "2022-01-01T12:00:05"));
        KeyedTable keyedTable = mixedTable.asKeyedTable();
        OptimizingTestHelpers.appendChange(
            keyedTable,
            tableTestHelper()
                .writeChangeStore(keyedTable, 2L, ChangeAction.INSERT, changeRecords, false));
        lastSnapshotTime = keyedTable.changeTable().currentSnapshot().timestampMillis();
      } else {
        lastSnapshotTime = mixedTable.asUnkeyedTable().currentSnapshot().timestampMillis();
      }
      long lastCommitTime =
          mixedTableMaintainer.expireMixedBaseOnRule(config, field).toEpochMilli();
      Assert.assertEquals(lastSnapshotTime, lastCommitTime);
    }
  }

  protected void getMaintainerAndExpire(DataExpirationConfig config, String datetime) {
    if (getTestFormat().equals(TableFormat.ICEBERG)) {
      Table table = getMixedTable().asUnkeyedTable();
      IcebergTableMaintainer icebergTableMaintainer = new IcebergTableMaintainer(table);
      Types.NestedField field = table.schema().findField(config.getExpirationField());
      icebergTableMaintainer.expireDataFrom(
          config,
          StringUtils.isBlank(datetime)
              ? icebergTableMaintainer.expireBaseOnRule(config, field)
              : LocalDateTime.parse(datetime)
                  .atZone(
                      IcebergTableMaintainer.getDefaultZoneId(
                          getMixedTable().schema().findField(config.getExpirationField())))
                  .toInstant());
    } else {
      MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getMixedTable());
      Types.NestedField field = getMixedTable().schema().findField(config.getExpirationField());
      mixedTableMaintainer.expireDataFrom(
          config,
          StringUtils.isBlank(datetime)
              ? mixedTableMaintainer.expireMixedBaseOnRule(config, field)
              : LocalDateTime.parse(datetime)
                  .atZone(
                      IcebergTableMaintainer.getDefaultZoneId(
                          getMixedTable().schema().findField(config.getExpirationField())))
                  .toInstant());
    }
  }

  @Test
  public void testNormalFieldPartitionLevel() {
    getMixedTable().updateProperties().set(TableProperties.DATA_EXPIRATION_FIELD, "ts").commit();

    testPartitionLevel();
  }

  @Test
  public void testNormalFieldFileLevel() {
    getMixedTable().updateProperties().set(TableProperties.DATA_EXPIRATION_FIELD, "ts").commit();

    testFileLevel();
  }

  @Test
  public void testExpireByPartitionWhenMetricsModeIsNone() {
    assumeTrue(getMixedTable().format().in(TableFormat.MIXED_ICEBERG, TableFormat.ICEBERG));

    getMixedTable()
        .updateProperties()
        .set(
            org.apache.iceberg.TableProperties.DEFAULT_WRITE_METRICS_MODE,
            MetricsModes.None.get().toString())
        .commit();

    testPartitionLevel();
  }

  @Test
  public void testGcDisabled() {
    MixedTable testTable = getMixedTable();
    testTable.updateProperties().set("gc.enabled", "false").commit();

    ArrayList<Record> baseRecords =
        Lists.newArrayList(
            createRecord(1, "111", parseMillis("2022-01-01T12:00:00"), "2022-01-01T12:00:00"));
    OptimizingTestHelpers.appendBase(
        testTable, tableTestHelper().writeBaseStore(testTable, 0, baseRecords, false));

    CloseableIterable<TableFileScanHelper.FileScanResult> scan;
    if (isKeyedTable()) {
      scan = buildKeyedFileScanHelper().scan();
    } else {
      scan = getTableFileScanHelper().scan();
    }
    assertScanResult(scan, 1, 0);

    DataExpirationConfig config = parseDataExpirationConfig(testTable);
    MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getMixedTable());
    mixedTableMaintainer.expireDataFrom(
        config,
        LocalDateTime.parse("2024-01-01T00:00:00.000")
            .atZone(
                IcebergTableMaintainer.getDefaultZoneId(
                    testTable.schema().findField(config.getExpirationField())))
            .toInstant());

    CloseableIterable<TableFileScanHelper.FileScanResult> scanAfterExpire;
    if (isKeyedTable()) {
      scanAfterExpire = buildKeyedFileScanHelper().scan();
    } else {
      scanAfterExpire = getTableFileScanHelper().scan();
    }
    assertScanResult(scanAfterExpire, 0, 0);
  }

  protected Record createRecord(int id, String name, long ts, String opTime) {
    Object time;
    Schema schema = getMixedTable().schema();
    Type type = schema.findField("op_time").type();
    switch (type.typeId()) {
      case TIMESTAMP:
        if (((Types.TimestampType) type).shouldAdjustToUTC()) {
          time = opTime + "Z";
        } else {
          time = opTime;
        }
        break;
      case STRING:
        time =
            LocalDateTime.parse(opTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        break;
      case LONG:
        time =
            LocalDateTime.parse(opTime)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
        break;
      default:
        time = opTime;
    }

    return MixedDataTestHelpers.createRecord(getMixedTable().schema(), id, name, ts, time);
  }

  protected void assertScanResult(
      CloseableIterable<TableFileScanHelper.FileScanResult> result, int size, Integer deleteCnt) {
    int scanCnt = 0;
    for (TableFileScanHelper.FileScanResult fileScanResult : result) {
      ++scanCnt;
      if (deleteCnt != null) {
        Assert.assertEquals(deleteCnt.intValue(), fileScanResult.deleteFiles().size());
      }
      for (ContentFile<?> deleteFile : fileScanResult.deleteFiles()) {
        if (ContentFiles.isDataFile(deleteFile)) {
          Assert.assertTrue(deleteFile instanceof PrimaryKeyedFile);
          PrimaryKeyedFile primaryKeyedFile = (PrimaryKeyedFile) deleteFile;
          Assert.assertEquals(DataFileType.EQ_DELETE_FILE, primaryKeyedFile.type());
        } else {
          Assert.assertTrue(deleteFile instanceof DeleteFile);
        }
      }
    }

    Assert.assertEquals(size, scanCnt);
  }

  protected List<Record> readSortedKeyedRecords(KeyedTable keyedTable) {
    return tableTestHelper()
        .readKeyedTable(keyedTable, Expressions.alwaysTrue(), null, false, false).stream()
        .sorted(Comparator.comparing(o -> o.get(0, Integer.class)))
        .collect(Collectors.toList());
  }

  protected List<Record> readSortedBaseRecords(MixedTable table) {
    return tableTestHelper().readBaseStore(table, Expressions.alwaysTrue(), null, false).stream()
        .sorted(Comparator.comparing(o -> o.get(0, Integer.class)))
        .collect(Collectors.toList());
  }

  protected KeyedTableFileScanHelper buildKeyedFileScanHelper() {
    long baseSnapshotId =
        IcebergTableUtil.getSnapshotId(getMixedTable().asKeyedTable().baseTable(), true);
    long changeSnapshotId =
        IcebergTableUtil.getSnapshotId(getMixedTable().asKeyedTable().changeTable(), true);
    return new KeyedTableFileScanHelper(
        getMixedTable().asKeyedTable(), new KeyedTableSnapshot(baseSnapshotId, changeSnapshotId));
  }

  protected TableFileScanHelper getTableFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getMixedTable().asUnkeyedTable(), true);
    return new UnkeyedTableFileScanHelper(getMixedTable().asUnkeyedTable(), baseSnapshotId);
  }

  protected static Map<String, String> getDefaultProp() {
    Map<String, String> prop = new HashMap<>();
    prop.put(TableProperties.ENABLE_DATA_EXPIRATION, "true");
    prop.put(TableProperties.DATA_EXPIRATION_FIELD, "op_time");
    prop.put(TableProperties.DATA_EXPIRATION_RETENTION_TIME, "1d");
    return prop;
  }

  private static long parseMillis(String datetime) {
    return parseInstantWithZone(datetime, ZoneOffset.UTC).toEpochMilli();
  }

  private static Instant parseInstantWithZone(String datetime, ZoneId zoneId) {
    return LocalDateTime.parse(datetime).atZone(zoneId).toInstant();
  }

  private boolean expireByStringDate() {
    String expireField =
        CompatiblePropertyUtil.propertyAsString(
            getMixedTable().properties(), TableProperties.DATA_EXPIRATION_FIELD, "");
    return getMixedTable()
        .schema()
        .findField(expireField)
        .type()
        .typeId()
        .equals(Type.TypeID.STRING);
  }

  private static DataExpirationConfig parseDataExpirationConfig(MixedTable table) {
    Map<String, String> properties = table.properties();
    return TableConfigurations.parseDataExpirationConfig(properties);
  }
}
