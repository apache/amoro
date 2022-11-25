package com.netease.arctic.ams.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.Tables;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.ArrayUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
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

  private static final ConcurrentHashMap<String, ArcticCatalog> catalogsCache = new ConcurrentHashMap<>();

  private static final Schema SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "name", Types.StringType.get()),
      Types.NestedField.required(3, "op_time", Types.TimestampType.withZone())
  );

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
    KeyedTable table = catalog(CATALOG).loadTable(TB_2).asKeyedTable();

    testKeyedTableContinueOptimizing(table);

  }

  @Test
  public void testPkPartitionTableOptimizing() {
    createPkPartitionArcticTable(TB_1);
    KeyedTable table = catalog(CATALOG).loadTable(TB_1).asKeyedTable();

    testKeyedTableContinueOptimizing(table);
  }

  @Test
  public void testNoPkTableOptimizing() {
    createNoPkArcticTable(TB_3);
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
  public void testIcebergTableOptimizing() throws IOException {
    Table table = createIcebergTable(TB_5);
    TableIdentifier tb = TB_5;
    long startId = getOptimizeHistoryStartId();

    // Step 1: insert 2 data file
    insertDataFiles(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ));

    insertDataFiles(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ));

    // wait Major Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);

    assertContainIdSet(readRecords(table), 0, 3, 4, 5, 6);

    // Step 2: insert delete file
    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3))
    ));

    // wait Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 2);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 2, 1);

    assertContainIdSet(readRecords(table), 0, 4, 5, 6);

  }

  private void testKeyedTableContinueOptimizing(KeyedTable table) {
    TableIdentifier tb = table.id();
    long startId = getOptimizeHistoryStartId();
    // Step1: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3)),
        newRecord(5, "eee", quickDateWithZone(4)),
        newRecord(6, "ddd", quickDateWithZone(4))
    ), null);

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
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA)
        .day("op_time").build();

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withPrimaryKeySpec(primaryKeySpec)
        .withPartitionSpec(spec)
        .withProperty(TableProperties.MINOR_OPTIMIZE_TRIGGER_MAX_INTERVAL, "1000")
        .withProperty(TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL, "1000");

    tableBuilder.create();
  }

  private void createPkArcticTable(TableIdentifier tableIdentifier) {
    PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(SCHEMA)
        .addColumn("id").build();

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withPrimaryKeySpec(primaryKeySpec)
        .withProperty(TableProperties.MINOR_OPTIMIZE_TRIGGER_MAX_INTERVAL, "1000")
        .withProperty(TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL, "1000");

    tableBuilder.create();
  }

  private void createNoPkArcticTable(TableIdentifier tableIdentifier) {

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withProperty(TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL, "1000");

    tableBuilder.create();
  }

  private Table createIcebergTable(TableIdentifier tableIdentifier) {
    Tables hadoopTables = new HadoopTables(new Configuration());
    Map<String, String> tableProperties = Maps.newHashMap();
    tableProperties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.MINOR_OPTIMIZE_TRIGGER_SMALL_FILE_COUNT, "2");

    return hadoopTables.create(SCHEMA, PartitionSpec.unpartitioned(), tableProperties,
        ICEBERG_CATALOG_DIR + "/" + tableIdentifier.getDatabase() + "/" + tableIdentifier.getTableName());
  }

  private void createNoPkPartitionArcticTable(TableIdentifier tableIdentifier) {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA)
        .day("op_time").build();

    TableBuilder tableBuilder = catalog(CATALOG).newTableBuilder(tableIdentifier, SCHEMA)
        .withPartitionSpec(spec)
        .withProperty(TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL, "1000");

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
    long txId = table.beginTransaction(System.currentTimeMillis() + "");
    List<DataFile> insertFiles = write(insertRows, table, txId, ChangeAction.INSERT);
    List<DataFile> deleteFiles = write(deleteRows, table, txId, ChangeAction.DELETE);
    AppendFiles appendFiles = table.changeTable().newAppend();
    insertFiles.forEach(appendFiles::appendFile);
    deleteFiles.forEach(appendFiles::appendFile);
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
  private List<DataFile> write(List<Record> rows, KeyedTable table, long txId, ChangeAction action) {
    if (rows != null && !rows.isEmpty()) {
      try (TaskWriter<Record> writer = GenericTaskWriters.builderFor(table)
          .withTransactionId(txId)
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

  public static OffsetDateTime ofDateWithZone(int year, int mon, int day, int hour) {
    LocalDateTime dateTime = LocalDateTime.of(year, mon, day, hour, 0);
    return OffsetDateTime.of(dateTime, ZoneOffset.ofHours(0));
  }

  public static OffsetDateTime quickDateWithZone(int day) {
    return ofDateWithZone(2022, 1, day, 0);
  }

  public static GenericRecord newRecord(Object... val) {
    GenericRecord writeRecord = GenericRecord.create(SCHEMA);
    for (int i = 0; i < val.length; i++) {
      writeRecord.set(i, val[i]);
    }
    return writeRecord;
  }

  private List<DataFile> insertDataFiles(Table table, List<Record> records) throws IOException {
    List<DataFile> result = writeNewDataFiles(table, records);

    AppendFiles baseAppend = table.newAppend();
    result.forEach(baseAppend::appendFile);
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

  @NotNull
  private List<DataFile> writeNewDataFiles(Table table, List<Record> records) throws IOException {
    GenericAppenderFactory appenderFactory = new GenericAppenderFactory(table.schema(), table.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table, table.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile();

    long smallSizeByBytes = PropertyUtil.propertyAsLong(table.properties(),
        com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
        com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT);
    List<DataFile> result = new ArrayList<>();
    DataWriter<Record> writer = appenderFactory
        .newDataWriter(outputFile, FileFormat.PARQUET, null);

    for (Record record : records) {
      if (writer.length() > smallSizeByBytes || result.size() > 0) {
        writer.close();
        result.add(writer.toDataFile());
        EncryptedOutputFile newOutputFile = outputFileFactory.newOutputFile();
        writer = appenderFactory
            .newDataWriter(newOutputFile, FileFormat.PARQUET, null);
      }
      writer.write(record);
    }
    writer.close();
    result.add(writer.toDataFile());
    return result;
  }

  private List<DeleteFile> insertEqDeleteFiles(Table table, List<Record> records) throws IOException {
    List<DeleteFile> result = writeEqDeleteFiles(table, records);

    RowDelta rowDelta = table.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();
    return result;
  }

  @NotNull
  private List<DeleteFile> writeEqDeleteFiles(Table table, List<Record> records) throws IOException {
    List<DeleteFile> result = new ArrayList<>();
    List<Integer> equalityFieldIds = org.apache.iceberg.relocated.com.google.common.collect.Lists.newArrayList(
        table.schema().findField("id").fieldId());
    Schema eqDeleteRowSchema = table.schema().select("id");
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec(),
            ArrayUtil.toIntArray(equalityFieldIds), eqDeleteRowSchema, null);
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table, table.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(table.spec(), null);

    EqualityDeleteWriter<Record> writer = appenderFactory
        .newEqDeleteWriter(outputFile, FileFormat.PARQUET, null);

    for (Record record : records) {
      writer.write(record);
    }
    writer.close();
    result.add(writer.toDeleteFile());
    return result;
  }

  private List<DeleteFile> insertPosDeleteFiles(Table table, List<DataFile> dataFiles) throws IOException {
    List<DeleteFile> result = writePosDeleteFiles(table, dataFiles);

    RowDelta rowDelta = table.newRowDelta();
    result.forEach(rowDelta::addDeletes);
    rowDelta.commit();
    return result;
  }

  @NotNull
  private List<DeleteFile> writePosDeleteFiles(Table table, List<DataFile> dataFiles) throws IOException {
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(table.schema(), table.spec());
    OutputFileFactory outputFileFactory =
        OutputFileFactory.builderFor(table, table.spec().specId(), 1)
            .build();
    EncryptedOutputFile outputFile = outputFileFactory.newOutputFile(table.spec(), null);

    List<DeleteFile> result = new ArrayList<>();
    PositionDeleteWriter<Record> writer = appenderFactory
        .newPosDeleteWriter(outputFile, FileFormat.PARQUET, null);
    for (int i = 0; i < dataFiles.size(); i++) {
      DataFile dataFile = dataFiles.get(i);
      if (i % 2 == 0) {
        PositionDelete<Record> positionDelete = PositionDelete.create();
        positionDelete.set(dataFile.path().toString(), 0L, null);
        writer.write(positionDelete);
      }
    }
    writer.close();
    result.add(writer.toDeleteFile());
    return result;
  }
  
  private CloseableIterable<Record> readRecords(Table table) {
    return IcebergGenerics.read(table).select("id").build();
  }

  public static void assertContainIdSet(CloseableIterable<Record> rows, int idIndex, Object... idList) {
    Set<Object> idSet = org.glassfish.jersey.internal.guava.Sets.newHashSet();
    rows.forEach(r -> idSet.add(r.get(idIndex)));
    for (Object id : idList) {
      if (!idSet.contains(id)) {
        throw new AssertionError("assert id contain " + id + ", but not found");
      }
    }
  }

  @AfterClass
  public static void afterClass() {
    amsEnvironment.stop();
    catalogsCache.clear();
  }
}
