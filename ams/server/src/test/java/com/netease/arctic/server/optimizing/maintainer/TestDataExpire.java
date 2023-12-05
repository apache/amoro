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

package com.netease.arctic.server.optimizing.maintainer;

import static com.netease.arctic.BasicTableTestHelper.PRIMARY_KEY_SPEC;
import static com.netease.arctic.BasicTableTestHelper.SPEC;

import com.google.common.collect.Lists;
import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import com.netease.arctic.server.optimizing.scan.KeyedTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.UnkeyedTableFileScanHelper;
import com.netease.arctic.server.table.DataExpirationConfig;
import com.netease.arctic.server.table.KeyedTableSnapshot;
import com.netease.arctic.server.table.executor.ExecutorTestBase;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.ContentFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
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
    if (getArcticTable().isUnkeyedTable()) {
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
        getArcticTable(), tableTestHelper().writeBaseStore(getArcticTable(), 0, records, false));

    DataExpirationConfig config = new DataExpirationConfig(getArcticTable());

    getMaintainerAndExpire(config, "2022-01-03T18:00:00.000");

    List<Record> result = readSortedBaseRecords(getArcticTable());

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
    Assert.assertEquals(expected, result);
  }

  private void testKeyedPartitionLevel() {
    KeyedTable keyedTable = getArcticTable().asKeyedTable();

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
    DataExpirationConfig config = new DataExpirationConfig(keyedTable);
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
    ArcticTable table = getArcticTable();
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
    KeyedTable keyedTable = getArcticTable().asKeyedTable();

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
    DataExpirationConfig config = new DataExpirationConfig(keyedTable);
    MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getArcticTable());
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
                getArcticTable(),
                tableTestHelper()
                    .writeBaseStore(getArcticTable(), 0, Lists.newArrayList(r), false)));
    CloseableIterable<TableFileScanHelper.FileScanResult> scan = getTableFileScanHelper().scan();
    assertScanResult(scan, 4, 0);

    // expire partitions that order than 2022-01-02 18:00:00.000
    DataExpirationConfig config = new DataExpirationConfig(getArcticTable());

    getMaintainerAndExpire(config, "2022-01-03T18:00:00.000");

    List<Record> result = readSortedBaseRecords(getArcticTable());

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

  protected void getMaintainerAndExpire(DataExpirationConfig config, String datetime) {
    if (getTestFormat().equals(TableFormat.ICEBERG)) {
      IcebergTableMaintainer icebergTableMaintainer =
          new IcebergTableMaintainer(getArcticTable().asUnkeyedTable());
      icebergTableMaintainer.expireDataFrom(
          config,
          LocalDateTime.parse(datetime)
              .atZone(
                  IcebergTableMaintainer.getDefaultZoneId(
                      getArcticTable().schema().findField(config.getExpirationField())))
              .toInstant());
    } else {
      MixedTableMaintainer mixedTableMaintainer = new MixedTableMaintainer(getArcticTable());
      mixedTableMaintainer.expireDataFrom(
          config,
          LocalDateTime.parse(datetime)
              .atZone(
                  IcebergTableMaintainer.getDefaultZoneId(
                      getArcticTable().schema().findField(config.getExpirationField())))
              .toInstant());
    }
  }

  @Test
  public void testNormalFieldPartitionLevel() {
    getArcticTable().updateProperties().set(TableProperties.DATA_EXPIRATION_FIELD, "ts").commit();

    testPartitionLevel();
  }

  @Test
  public void testNormalFieldFileLevel() {
    getArcticTable().updateProperties().set(TableProperties.DATA_EXPIRATION_FIELD, "ts").commit();

    testFileLevel();
  }

  protected Record createRecord(int id, String name, long ts, String opTime) {
    Object time;
    Schema schema = getArcticTable().schema();
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

    return MixedDataTestHelpers.createRecord(getArcticTable().schema(), id, name, ts, time);
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

  protected List<Record> readSortedBaseRecords(ArcticTable table) {
    return tableTestHelper().readBaseStore(table, Expressions.alwaysTrue(), null, false).stream()
        .sorted(Comparator.comparing(o -> o.get(0, Integer.class)))
        .collect(Collectors.toList());
  }

  protected KeyedTableFileScanHelper buildKeyedFileScanHelper() {
    long baseSnapshotId =
        IcebergTableUtil.getSnapshotId(getArcticTable().asKeyedTable().baseTable(), true);
    long changeSnapshotId =
        IcebergTableUtil.getSnapshotId(getArcticTable().asKeyedTable().changeTable(), true);
    return new KeyedTableFileScanHelper(
        getArcticTable().asKeyedTable(), new KeyedTableSnapshot(baseSnapshotId, changeSnapshotId));
  }

  protected TableFileScanHelper getTableFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getArcticTable().asUnkeyedTable(), true);
    return new UnkeyedTableFileScanHelper(getArcticTable().asUnkeyedTable(), baseSnapshotId);
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
            getArcticTable().properties(), TableProperties.DATA_EXPIRATION_FIELD, "");
    return getArcticTable()
        .schema()
        .findField(expireField)
        .type()
        .typeId()
        .equals(Type.TypeID.STRING);
  }
}
