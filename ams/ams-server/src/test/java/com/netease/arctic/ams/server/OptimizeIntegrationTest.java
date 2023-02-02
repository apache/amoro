package com.netease.arctic.ams.server;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.table.WriteOperationKind;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.Tables;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Multimap;
import org.apache.iceberg.relocated.com.google.common.collect.Multimaps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static com.netease.arctic.TableTestBase.partitionData;
import static com.netease.arctic.TableTestBase.writeEqDeleteFile;
import static com.netease.arctic.TableTestBase.writeNewDataFile;
import static com.netease.arctic.TableTestBase.writePosDeleteFile;

public class OptimizeIntegrationTest {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizeIntegrationTest.class);
  private static AmsEnvironment amsEnvironment;
  private static final long TIMEOUT = 30_000;

  private static final String CATALOG = "local_catalog1";
  private static final String ICEBERG_CATALOG = "iceberg_catalog";
  private static final String DATABASE = "test_db";
  private static String CATALOG_DIR;
  private static String ICEBERG_CATALOG_DIR;
  private static final TableIdentifier TB_1 = TableIdentifier.of(CATALOG, DATABASE, "test_table1");
  private static final TableIdentifier TB_2 = TableIdentifier.of(CATALOG, DATABASE, "test_table2");
  private static final TableIdentifier TB_3 = TableIdentifier.of(CATALOG, DATABASE, "test_table3");
  private static final TableIdentifier TB_4 = TableIdentifier.of(CATALOG, DATABASE, "test_table4");
  private static final TableIdentifier TB_5 = TableIdentifier.of(ICEBERG_CATALOG, DATABASE, "test_table5");
  private static final TableIdentifier TB_6 = TableIdentifier.of(ICEBERG_CATALOG, DATABASE, "test_table6");
  private static final TableIdentifier TB_7 = TableIdentifier.of(ICEBERG_CATALOG, DATABASE, "test_table7");
  private static final TableIdentifier TB_8 = TableIdentifier.of(ICEBERG_CATALOG, DATABASE, "test_table8");

  private static final ConcurrentHashMap<String, ArcticCatalog> catalogsCache = new ConcurrentHashMap<>();

  private static final Schema SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "name", Types.StringType.get()),
      Types.NestedField.required(3, "op_time", Types.TimestampType.withZone())
  );
  
  private static final PartitionSpec SPEC = PartitionSpec.builderFor(SCHEMA)
      .day("op_time").build();

  @ClassRule
  public static TemporaryFolder tempFolder = new TemporaryFolder();

  @BeforeClass
  public static void beforeClass() {
    String rootPath = tempFolder.getRoot().getAbsolutePath();
    CATALOG_DIR = rootPath + "/arctic/warehouse";
    ICEBERG_CATALOG_DIR = rootPath + "/iceberg/warehouse";
    LOG.info("TEMP folder {}", rootPath);
    catalogsCache.clear();
    amsEnvironment = new AmsEnvironment(rootPath);
    amsEnvironment.start();
    amsEnvironment.createLocalCatalog(CATALOG, CATALOG_DIR);
    amsEnvironment.createIcebergCatalog(ICEBERG_CATALOG, ICEBERG_CATALOG_DIR);
    catalog(CATALOG).createDatabase(DATABASE);
    catalog(ICEBERG_CATALOG).createDatabase(DATABASE);
  }

  @Test
  public void testPkTableOptimizing() {
    createPkArcticTable(TB_2);
    assertTableExist(TB_2);
    KeyedTable table = catalog(CATALOG).loadTable(TB_2).asKeyedTable();

    testKeyedTableContinueOptimizing(table);

  }

  private void assertTableExist(TableIdentifier tableIdentifier) {
    List<TableIdentifier> tableIdentifiers = amsEnvironment.refreshTables();
    Assert.assertTrue(tableIdentifiers.contains(tableIdentifier));
  }

  @Test
  public void testPkPartitionTableOptimizing() {
    createPkPartitionArcticTable(TB_1);
    assertTableExist(TB_1);
    KeyedTable table = catalog(CATALOG).loadTable(TB_1).asKeyedTable();

    testKeyedTableContinueOptimizing(table);
  }

  @Test
  public void testNoPkTableOptimizing() {
    createNoPkArcticTable(TB_3);
    assertTableExist(TB_3);
    UnkeyedTable table = catalog(CATALOG).loadTable(TB_3).asUnkeyedTable();

    TableIdentifier tb = table.id();
    long startId = getOptimizeHistoryStartId();

    // Step 1: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord( 4, "bbb", quickDateWithZone(3)),
        newRecord(5, "eee", quickDateWithZone(4)),
        newRecord( 6, "ddd", quickDateWithZone(4))
    ));

    // Step 2: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(7, "fff", quickDateWithZone(3)),
        newRecord(8, "ggg", quickDateWithZone(3)),
        newRecord(9, "hhh", quickDateWithZone(4)),
        newRecord(10, "iii", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 2, 1);

    // Step 3: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(11, "jjj", quickDateWithZone(3)),
        newRecord(12, "kkk", quickDateWithZone(3)),
        newRecord(13, "lll", quickDateWithZone(4)),
        newRecord(14, "mmm", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 2);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 2, 1);
  }

  @Test
  public void testNoPkPartitionTableOptimizing() {
    createNoPkPartitionArcticTable(TB_4);
    assertTableExist(TB_4);
    UnkeyedTable table = catalog(CATALOG).loadTable(TB_4).asUnkeyedTable();

    TableIdentifier tb = table.id();
    long startId = getOptimizeHistoryStartId();

    // Step 1: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3)),
        newRecord(5, "eee", quickDateWithZone(4)),
        newRecord(6, "ddd", quickDateWithZone(4))
    ));

    // Step 2: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(7, "fff", quickDateWithZone(3)),
        newRecord(8, "ggg", quickDateWithZone(3)),
        newRecord(9, "hhh", quickDateWithZone(4)),
        newRecord(10, "iii", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 4, 2);

    // Step 3: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(11, "jjj", quickDateWithZone(3)),
        newRecord(12, "kkk", quickDateWithZone(3)),
        newRecord(13, "lll", quickDateWithZone(4)),
        newRecord(14, "mmm", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 2);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 4, 2);
  }

  @Test
  public void testIcebergTableFullOptimize() throws IOException {
    Table table = createIcebergTable(TB_5, PartitionSpec.unpartitioned());
    TableIdentifier tb = TB_5;
    assertTableExist(TB_5);
    long startId = getOptimizeHistoryStartId();
    long offset = 1;
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "100");

    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    rowDelta(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    rowDelta(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    rowDeltaWithPos(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    int size = assertContainIdSet(readRecords(table), 0, 4);
    Assert.assertEquals(1, size);

    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0");

    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.FullMajor, 8, 1);

    size = assertContainIdSet(readRecords(table), 0, 4);
    Assert.assertEquals(1, size);
  }

  @Test
  public void testIcebergTableOptimizing() throws IOException {
    Table table = createIcebergTable(TB_6, PartitionSpec.unpartitioned());
    TableIdentifier tb = TB_6;
    assertTableExist(TB_6);
    long startId = getOptimizeHistoryStartId();
    long offset = 1;
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "bbb", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);
    int size = assertContainIdSet(readRecords(table), 0, 1, 2, 3, 4, 5, 6);
    Assert.assertEquals(6, size);

    // Step 2: insert delete file and Minor Optimize
    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);
    size = assertContainIdSet(readRecords(table), 0, 2, 3, 4, 5, 6);
    Assert.assertEquals(5, size);

    // Step 3: insert 2 delete file and Minor Optimize(big file)
    long dataFileSize = getDataFileSize(table);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / (dataFileSize - 100) + "");

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(2, "aaa", quickDateWithZone(3))
    ), partitionData);

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 3, 1);
    size = assertContainIdSet(readRecords(table), 0, 4, 5, 6);
    Assert.assertEquals(3, size);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "10");

    // Step 4: insert 1 delete and full optimize
    // insertEqDeleteFiles(table, Lists.newArrayList(
    //     newRecord(4, "bbb", quickDateWithZone(3))
    // ));
    rowDelta(table, Lists.newArrayList(
        newRecord(7, "aaa", quickDateWithZone(3)),
        newRecord(8, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0");

    // wait FullMajor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset);
    assertOptimizeHistory(optimizeHistory, OptimizeType.FullMajor, 4, 1);

    size = assertContainIdSet(readRecords(table), 0, 5, 6, 7, 8);
    Assert.assertEquals(4, size);
  }

  @Test
  public void testV1IcebergTableOptimizing() throws IOException {
    Table table = createIcebergV1Table(TB_8, PartitionSpec.unpartitioned());
    TableIdentifier tb = TB_8;
    assertTableExist(TB_8);
    long startId = getOptimizeHistoryStartId();
    long offset = 1;
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "bbb", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);
    int size = assertContainIdSet(readRecords(table), 0, 1, 2, 3, 4, 5, 6);
    Assert.assertEquals(6, size);

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(7, "ccc", quickDateWithZone(3)),
        newRecord(8, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);
    size = assertContainIdSet(readRecords(table), 0, 1, 2, 3, 4, 5, 6, 7, 8);
    Assert.assertEquals(8, size);
    
  }

  @Test
  public void testPartitionIcebergTableOptimizing() throws IOException {
    Table table = createIcebergTable(TB_7, SPEC);
    TableIdentifier tb = TB_7;
    assertTableExist(TB_7);
    long startId = getOptimizeHistoryStartId();
    long offset = 1;
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "bbb", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);
    int size = assertContainIdSet(readRecords(table), 0, 1, 2, 3, 4, 5, 6);
    Assert.assertEquals(6, size);

    // Step 2: insert delete file and Minor Optimize
    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);
    size = assertContainIdSet(readRecords(table), 0, 2, 3, 4, 5, 6);
    Assert.assertEquals(5, size);

    // Step 3: insert 2 delete file and Minor Optimize(big file)
    long dataFileSize = getDataFileSize(table);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / (dataFileSize - 100) + "");

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(2, "aaa", quickDateWithZone(3))
    ), partitionData);

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset++);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 3, 1);
    size = assertContainIdSet(readRecords(table), 0, 4, 5, 6);
    Assert.assertEquals(3, size);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "10");

    // Step 4: insert 1 delete and full optimize
    // insertEqDeleteFiles(table, Lists.newArrayList(
    //     newRecord(4, "bbb", quickDateWithZone(3))
    // ));
    rowDelta(table, Lists.newArrayList(
        newRecord(7, "aaa", quickDateWithZone(3)),
        newRecord(8, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0");

    // wait FullMajor Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + offset);
    assertOptimizeHistory(optimizeHistory, OptimizeType.FullMajor, 4, 1);

    size = assertContainIdSet(readRecords(table), 0, 5, 6, 7, 8);
    Assert.assertEquals(4, size);
  }

  private void testKeyedTableContinueOptimizing(KeyedTable table) {
    TableIdentifier tb = table.id();
    emptyCommit(table);
    emptyCommit(table);
    emptyCommit(table);
    emptyCommit(table);
    long startId = getOptimizeHistoryStartId();
    // Step1: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3)),
        newRecord(5, "eee", quickDateWithZone(4)),
        newRecord(6, "ddd", quickDateWithZone(4))
    ), null);

    amsEnvironment.syncTableFileCache(tb, Constants.INNER_TABLE_CHANGE);

    // wait Minor Optimize result, no major optimize because there is only 1 base file for each node
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 4, 4);

    // Step2: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(7, "fff", quickDateWithZone(3)),
        newRecord(8, "ggg", quickDateWithZone(3)),
        newRecord(9, "hhh", quickDateWithZone(4)),
        newRecord(10, "iii", quickDateWithZone(4))
    ), null);

    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 2);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 8, 4);
    optimizeHistory = waitOptimizeResult(tb, startId + 3);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 8, 4);

    // Step3: delete change data
    writeChange(table, null, Lists.newArrayList(
        newRecord(7, "fff", quickDateWithZone(3)),
        newRecord(8, "ggg", quickDateWithZone(3))
    ));
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 4);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 4, 2);

    // Step4: update change data
    writeChange(table, Lists.newArrayList(
        newRecord(9, "hhh_new", quickDateWithZone(4)),
        newRecord(10, "iii_new", quickDateWithZone(4))
    ), Lists.newArrayList(
        newRecord(9, "fff", quickDateWithZone(4)),
        newRecord(10, "ggg", quickDateWithZone(4))
    ));
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 5);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 6, 4);
    optimizeHistory = waitOptimizeResult(tb, startId + 6);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 6, 2);

    // Step5: delete all change data
    writeChange(table, null, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3)),
        newRecord(5, "eee", quickDateWithZone(4)),
        newRecord(6, "ddd", quickDateWithZone(4)),
        newRecord(9, "hhh_new", quickDateWithZone(4)),
        newRecord(10, "iii_new", quickDateWithZone(4))
    ));
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 7);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 12, 4);

    // Step6: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(11, "jjj", quickDateWithZone(3))
    ), null);
    // wait Minor Optimize result, no major optimize because there is only 1 base file for each node
    optimizeHistory = waitOptimizeResult(tb, startId + 8);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 3, 1);
    optimizeHistory = waitOptimizeResult(tb, startId + 9);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 3, 1);
  }

  private void assertOptimizeHistory(OptimizeHistory optimizeHistory,
                                     OptimizeType optimizeType,
                                     int fileCntBefore,
                                     int fileCntAfter) {
    Assert.assertNotNull(optimizeHistory);
    Assert.assertEquals(optimizeType, optimizeHistory.getOptimizeType());
    Assert.assertEquals(fileCntBefore, optimizeHistory.getTotalFilesStatBeforeOptimize().getFileCnt());
    Assert.assertEquals(fileCntAfter, optimizeHistory.getTotalFilesStatAfterOptimize().getFileCnt());
  }

  private static long getOptimizeHistoryStartId() {
    return ServiceContainer.getOptimizeService().maxOptimizeHistoryId();
  }

  private static ArcticCatalog catalog(String name) {
    return catalogsCache.computeIfAbsent(name, n -> CatalogLoader.load(amsEnvironment.getAmsUrl() + "/" + n));
  }

  private void createPkPartitionArcticTable(TableIdentifier tableIdentifier) {
    PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(SCHEMA)
        .addColumn("id").build();

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withPrimaryKeySpec(primaryKeySpec)
        .withPartitionSpec(SPEC)
        .withProperty(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "1000")
        .withProperty(TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_INTERVAL, "1000");

    tableBuilder.create();
  }

  private void createPkArcticTable(TableIdentifier tableIdentifier) {
    PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(SCHEMA)
        .addColumn("id").build();

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withPrimaryKeySpec(primaryKeySpec)
        .withProperty(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "1000")
        .withProperty(TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_INTERVAL, "1000");

    tableBuilder.create();
  }

  private void createNoPkArcticTable(TableIdentifier tableIdentifier) {

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withProperty(TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_INTERVAL, "1000");

    tableBuilder.create();
  }

  private Table createIcebergTable(TableIdentifier tableIdentifier, PartitionSpec partitionSpec) {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2");

    return hadoopTables.create(SCHEMA, partitionSpec, tableProperties,
        ICEBERG_CATALOG_DIR + "/" + tableIdentifier.getDatabase() + "/" + tableIdentifier.getTableName());
  }

  private Table createIcebergV1Table(TableIdentifier tableIdentifier, PartitionSpec partitionSpec) {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "1");
    tableProperties.put(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2");

    return hadoopTables.create(SCHEMA, partitionSpec, tableProperties,
        ICEBERG_CATALOG_DIR + "/" + tableIdentifier.getDatabase() + "/" + tableIdentifier.getTableName());
  }
  
  private void updateProperties(Table table, String key, String value) {
    UpdateProperties updateProperties = table.updateProperties();
    updateProperties.set(key, value);
    updateProperties.commit();
  }

  private void createNoPkPartitionArcticTable(TableIdentifier tableIdentifier) {
    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withPartitionSpec(SPEC)
        .withProperty(TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_INTERVAL, "1000");

    tableBuilder.create();
  }

  private OptimizeHistory waitOptimizeResult(TableIdentifier tableIdentifier, long expectRecordId) {
    boolean success;
    try {
      success = waitUntilFinish(() -> {
        List<OptimizeHistory> optimizeHistory =
            ServiceContainer.getOptimizeService().getOptimizeHistory(tableIdentifier);
        if (optimizeHistory == null || optimizeHistory.isEmpty()) {
          LOG.info("optimize history is empty");
          return Status.RUNNING;
        }
        Optional<OptimizeHistory> any =
            optimizeHistory.stream().filter(p -> p.getRecordId() == expectRecordId).findAny();

        if (any.isPresent()) {
          return Status.SUCCESS;
        } else {
          LOG.info("optimize history max recordId {}",
              optimizeHistory.stream().map(OptimizeHistory::getRecordId).max(Comparator.naturalOrder()).get());
          return Status.RUNNING;
        }
      }, TIMEOUT);
    } catch (TimeoutException e) {
      throw new IllegalStateException("wait optimize result timeout expectRecordId " + expectRecordId, e);
    }

    if (success) {
      List<OptimizeHistory> optimizeHistory = ServiceContainer.getOptimizeService().getOptimizeHistory(tableIdentifier);
      return optimizeHistory.stream().filter(p -> p.getRecordId() == expectRecordId).findAny().orElse(null);
    } else {
      return null;
    }
  }

  private boolean waitUntilFinish(Supplier<Status> statusSupplier, final long timeout) throws TimeoutException {
    long start = System.currentTimeMillis();
    while (true) {
      long duration = System.currentTimeMillis() - start;
      if (duration > timeout) {
        throw new TimeoutException("wait exceed timeout, " + duration + "ms > " + timeout + "ms");
      }
      Status status = statusSupplier.get();
      if (status == Status.FAILED) {
        return false;
      } else if (status == Status.RUNNING) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        return true;
      }
    }
  }

  private enum Status {
    SUCCESS,
    FAILED,
    RUNNING
  }

  public void writeBase(UnkeyedTable table, List<Record> insertRows) {
    List<DataFile> insertFiles = write(table, insertRows);
    AppendFiles appendFiles = table.newAppend();
    insertFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  public void writeChange(KeyedTable table, List<Record> insertRows, List<Record> deleteRows) {
    List<DataFile> insertFiles = write(insertRows, table, ChangeAction.INSERT);
    List<DataFile> deleteFiles = write(deleteRows, table, ChangeAction.DELETE);
    AppendFiles appendFiles = table.changeTable().newAppend();
    insertFiles.forEach(appendFiles::appendFile);
    deleteFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }
  
  public void emptyCommit(KeyedTable table) {
    AppendFiles appendFiles = table.changeTable().newAppend();
    appendFiles.commit();
  }

  private List<DataFile> write(UnkeyedTable table, List<Record> rows) {
    if (rows != null && !rows.isEmpty()) {
      try (TaskWriter<Record> writer = AdaptHiveGenericTaskWriterBuilder.builderFor(table)
          .withChangeAction(ChangeAction.INSERT)
          .buildWriter(WriteOperationKind.APPEND)) {
        rows.forEach(row -> {
          try {
            writer.write(row);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
        return Arrays.asList(writer.complete().dataFiles());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return Collections.emptyList();
  }

  // TODO use new writer in 0.3.1
  private List<DataFile> write(List<Record> rows, KeyedTable table, ChangeAction action) {
    if (rows != null && !rows.isEmpty()) {
      try (TaskWriter<Record> writer = GenericTaskWriters.builderFor(table)
          .withChangeAction(action)
          .buildChangeWriter()) {
        rows.forEach(row -> {
          try {
            writer.write(row);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
        return Arrays.asList(writer.complete().dataFiles());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return Collections.emptyList();
  }

  private static OffsetDateTime ofDateWithZone(int year, int mon, int day, int hour) {
    LocalDateTime dateTime = LocalDateTime.of(year, mon, day, hour, 0);
    return OffsetDateTime.of(dateTime, ZoneOffset.ofHours(0));
  }

  private static OffsetDateTime quickDateWithZone(int day) {
    return ofDateWithZone(2022, 1, day, 0);
  }

  private static Record newRecord(Object... val) {
    return TableTestBase.newGenericRecord(SCHEMA, val);
  }

  private DataFile insertDataFile(Table table, List<Record> records, StructLike partitionData) throws IOException {
    DataFile result = writeNewDataFile(table, records, partitionData);

    AppendFiles baseAppend = table.newAppend();
    baseAppend.appendFile(result);
    baseAppend.commit();

    return result;
  }

  private List<DataFile> overwriteDataFiles(Table table, List<DataFile> toDeleteDataFiles,
                                            List<DataFile> newDataFiles) {
    OverwriteFiles overwrite = table.newOverwrite();
    toDeleteDataFiles.forEach(overwrite::deleteFile);
    newDataFiles.forEach(overwrite::addFile);
    overwrite.commit();

    return newDataFiles;
  }

  private void rowDelta(Table table, List<Record> insertRecords, List<Record> deleteRecords, StructLike partitionData)
      throws IOException {
    DataFile dataFile = writeNewDataFile(table, insertRecords, partitionData);

    DeleteFile deleteFile = writeEqDeleteFile(table, deleteRecords, partitionData);
    RowDelta rowDelta = table.newRowDelta();
    rowDelta.addRows(dataFile);
    rowDelta.addDeletes(deleteFile);
    rowDelta.validateFromSnapshot(table.currentSnapshot().snapshotId());
    rowDelta.commit();
  }

  private void rowDeltaWithPos(Table table, List<Record> insertRecords, List<Record> deleteRecords,
                               StructLike partitionData) throws IOException {
    DataFile dataFile = writeNewDataFile(table, insertRecords, partitionData);

    DeleteFile deleteFile = writeEqDeleteFile(table, deleteRecords, partitionData);
    Multimap<String, Long>
        file2Positions = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
    file2Positions.put(dataFile.path().toString(), 0L);
    DeleteFile posDeleteFile = writePosDeleteFile(table, file2Positions, partitionData);
    RowDelta rowDelta = table.newRowDelta();
    rowDelta.addRows(dataFile);
    rowDelta.addDeletes(deleteFile);
    rowDelta.addDeletes(posDeleteFile);
    rowDelta.validateFromSnapshot(table.currentSnapshot().snapshotId());
    rowDelta.commit();
  }

  private List<DataFile> rewriteFiles(Table table, List<DataFile> toDeleteDataFiles, List<DataFile> newDataFiles,
                                      long sequence) {
    RewriteFiles rewriteFiles = table.newRewrite();
    rewriteFiles.rewriteFiles(Sets.newHashSet(toDeleteDataFiles), Sets.newHashSet(newDataFiles), sequence);
    rewriteFiles.validateFromSnapshot(table.currentSnapshot().snapshotId());
    rewriteFiles.commit();
    return newDataFiles;
  }

  private List<DeleteFile> rewriteFiles(Table table, Set<DeleteFile> toDeleteFiles,
                                        List<DeleteFile> newFiles) {
    RewriteFiles rewriteFiles = table.newRewrite();
    rewriteFiles.rewriteFiles(Collections.emptySet(), Sets.newHashSet(toDeleteFiles), Collections.emptySet(),
        Sets.newHashSet(newFiles));
    rewriteFiles.validateFromSnapshot(table.currentSnapshot().snapshotId());
    rewriteFiles.commit();
    return newFiles;
  }

  private DeleteFile insertEqDeleteFiles(Table table, List<Record> records, StructLike partitionData) throws IOException {
    DeleteFile result = writeEqDeleteFile(table, records, partitionData);

    RowDelta rowDelta = table.newRowDelta();
    rowDelta.addDeletes(result);
    rowDelta.commit();
    return result;
  }
  
  private CloseableIterable<Record> readRecords(Table table) {
    return IcebergGenerics.read(table).select("id").build();
  }

  public static int assertContainIdSet(CloseableIterable<Record> rows, int idIndex, Object... idList) {
    Set<Object> idSet = org.glassfish.jersey.internal.guava.Sets.newHashSet();
    int cnt = 0;
    for (Record r : rows) {
      idSet.add(r.get(idIndex));
      cnt++;
    }
    for (Object id : idList) {
      if (!idSet.contains(id)) {
        throw new AssertionError("assert id contain " + id + ", but not found");
      }
    }
    return cnt;
  }
  
  private long getDataFileSize(Table table) {
    table.refresh();
    try (CloseableIterable<FileScanTask> fileScanTasks = table.newScan().planFiles()) {
      DataFile file = fileScanTasks.iterator().next().file();
      LOG.info("get file size {} of {}", file.fileSizeInBytes(), file.path());
      return file.fileSizeInBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close table scan of " + table.name(), e);
    }
  }

  @AfterClass
  public static void afterClass() {
    amsEnvironment.stop();
    catalogsCache.clear();
  }
}
