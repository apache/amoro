package com.netease.arctic.ams.server;

import com.google.common.collect.Lists;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.model.SesssionInfo;
import com.netease.arctic.ams.server.model.SqlResult;
import com.netease.arctic.ams.server.model.SqlStatus;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.TerminalService;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.table.WriteOperationKind;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class OptimizeIntegrationTest {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizeIntegrationTest.class);
  private static AmsEnvironment amsEnvironment;
  private static final long TIMEOUT = 30_000;

  private static final String CATALOG = "local_catalog";
  private static final String DATABASE = "test_db";
  private static final TableIdentifier TB_1 = TableIdentifier.of(CATALOG, DATABASE, "test_table1");
  private static final TableIdentifier TB_2 = TableIdentifier.of(CATALOG, DATABASE, "test_table2");
  private static final TableIdentifier TB_3 = TableIdentifier.of(CATALOG, DATABASE, "test_table3");
  private static final TableIdentifier TB_4 = TableIdentifier.of(CATALOG, DATABASE, "test_table4");

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
    LOG.info("TEMP folder {}", tempFolder.getRoot().getAbsolutePath());
    catalogsCache.clear();
    amsEnvironment = new AmsEnvironment(tempFolder.getRoot().getAbsolutePath());
    amsEnvironment.start();
    catalog(CATALOG).createDatabase(DATABASE);
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
        newRecord(table, 3, "aaa", quickDateWithZone(3)),
        newRecord(table, 4, "bbb", quickDateWithZone(3)),
        newRecord(table, 5, "eee", quickDateWithZone(4)),
        newRecord(table, 6, "ddd", quickDateWithZone(4))
    ));

    // Step 2: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(table, 7, "fff", quickDateWithZone(3)),
        newRecord(table, 8, "ggg", quickDateWithZone(3)),
        newRecord(table, 9, "hhh", quickDateWithZone(4)),
        newRecord(table, 10, "iii", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 2, 1);

    // Step 3: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(table, 11, "jjj", quickDateWithZone(3)),
        newRecord(table, 12, "kkk", quickDateWithZone(3)),
        newRecord(table, 13, "lll", quickDateWithZone(4)),
        newRecord(table, 14, "mmm", quickDateWithZone(4))
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
        newRecord(table, 3, "aaa", quickDateWithZone(3)),
        newRecord(table, 4, "bbb", quickDateWithZone(3)),
        newRecord(table, 5, "eee", quickDateWithZone(4)),
        newRecord(table, 6, "ddd", quickDateWithZone(4))
    ));

    // Step 2: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(table, 7, "fff", quickDateWithZone(3)),
        newRecord(table, 8, "ggg", quickDateWithZone(3)),
        newRecord(table, 9, "hhh", quickDateWithZone(4)),
        newRecord(table, 10, "iii", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 4, 2);

    // Step 3: insert data
    writeBase(table, Lists.newArrayList(
        newRecord(table, 11, "jjj", quickDateWithZone(3)),
        newRecord(table, 12, "kkk", quickDateWithZone(3)),
        newRecord(table, 13, "lll", quickDateWithZone(4)),
        newRecord(table, 14, "mmm", quickDateWithZone(4))
    ));
    // wait Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 2);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 4, 2);
  }

  private void testKeyedTableContinueOptimizing(KeyedTable table) {
    TableIdentifier tb = table.id();
    long startId = getOptimizeHistoryStartId();
    // Step1: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(table, 3, "aaa", quickDateWithZone(3)),
        newRecord(table, 4, "bbb", quickDateWithZone(3)),
        newRecord(table, 5, "eee", quickDateWithZone(4)),
        newRecord(table, 6, "ddd", quickDateWithZone(4))
    ), null);

    // wait Minor Optimize result, no major optimize because there is only 1 base file for each node
    OptimizeHistory optimizeHistory = waitOptimizeResult(tb, startId + 1);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 4, 4);

    // Step2: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(table, 7, "fff", quickDateWithZone(3)),
        newRecord(table, 8, "ggg", quickDateWithZone(3)),
        newRecord(table, 9, "hhh", quickDateWithZone(4)),
        newRecord(table, 10, "iii", quickDateWithZone(4))
    ), null);
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 2);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 8, 4);
    optimizeHistory = waitOptimizeResult(tb, startId + 3);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 8, 4);

    // Step3: delete change data
    writeChange(table, null, Lists.newArrayList(
        newRecord(table, 7, "fff", quickDateWithZone(3)),
        newRecord(table, 8, "ggg", quickDateWithZone(3))
    ));
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 4);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 4, 2);
    optimizeHistory = waitOptimizeResult(tb, startId + 5);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 4, 2);

    // Step4: update change data
    writeChange(table, Lists.newArrayList(
        newRecord(table, 9, "hhh_new", quickDateWithZone(4)),
        newRecord(table, 10, "iii_new", quickDateWithZone(4))
    ), Lists.newArrayList(
        newRecord(table, 9, "fff", quickDateWithZone(4)),
        newRecord(table, 10, "ggg", quickDateWithZone(4))
    ));
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 6);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 6, 4);
    optimizeHistory = waitOptimizeResult(tb, startId + 7);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 10, 4);

    // Step5: delete all change data
    writeChange(table, null, Lists.newArrayList(
        newRecord(table, 3, "aaa", quickDateWithZone(3)),
        newRecord(table, 4, "bbb", quickDateWithZone(3)),
        newRecord(table, 5, "eee", quickDateWithZone(4)),
        newRecord(table, 6, "ddd", quickDateWithZone(4)),
        newRecord(table, 9, "hhh_new", quickDateWithZone(4)),
        newRecord(table, 10, "iii_new", quickDateWithZone(4))
    ));
    // wait Minor/Major Optimize result
    optimizeHistory = waitOptimizeResult(tb, startId + 8);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 10, 4);
    optimizeHistory = waitOptimizeResult(tb, startId + 9);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Major, 8, 0);

    // Step6: insert change data
    writeChange(table, Lists.newArrayList(
        newRecord(table, 11, "jjj", quickDateWithZone(3))
    ), null);
    // wait Minor Optimize result, no major optimize because there is only 1 base file for each node
    optimizeHistory = waitOptimizeResult(tb, startId + 10);
    assertOptimizeHistory(optimizeHistory, OptimizeType.Minor, 1, 1);
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

  private boolean waitSessionResult(SesssionInfo sessionInfo) throws TimeoutException {
    return waitUntilFinish(() -> checkSessionResult(sessionInfo), TIMEOUT);
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

  private Status checkSessionResult(SesssionInfo sessionInfo) {
    List<SqlResult> sqlStatus = TerminalService.getSqlStatus(sessionInfo.getSessionId());
    if (Objects.isNull(sqlStatus)) {
      return Status.FAILED;
    }
    boolean notFinish = false;
    if (sqlStatus.size() == 0) {
      notFinish = true;
      LOG.info("status : empty");
    }
    for (SqlResult status : sqlStatus) {
      LOG.info("status : {}", status.getStatus());
      if (!Objects.equals(status.getStatus(), SqlStatus.FINISHED.getName())) {
        if (!Objects.equals(status.getStatus(), SqlStatus.RUNNING.getName()) &&
            !Objects.equals(status.getStatus(), SqlStatus.CREATED.getName())) {
          return Status.FAILED;
        }
        notFinish = true;
        break;
      }
    }
    return notFinish ? Status.RUNNING : Status.SUCCESS;
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

  public static GenericRecord newRecord(ArcticTable table, Object... val) {
    GenericRecord writeRecord = GenericRecord.create(table.schema());
    for (int i = 0; i < val.length; i++) {
      writeRecord.set(i, val[i]);
    }
    return writeRecord;
  }

  @AfterClass
  public static void afterClass() {
    amsEnvironment.stop();
    catalogsCache.clear();
  }
}
