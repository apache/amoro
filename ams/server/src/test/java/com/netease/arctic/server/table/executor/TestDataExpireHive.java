package com.netease.arctic.server.table.executor;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.io.HiveDataTestHelpers;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.List;

@RunWith(Parameterized.class)
public class TestDataExpireHive extends TestDataExpire {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][]{
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
         new HiveTableTestHelper(true, true, getDefaultProp())},
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
         new HiveTableTestHelper(true, false, getDefaultProp())},
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
         new HiveTableTestHelper(false, true, getDefaultProp())},
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
         new HiveTableTestHelper(false, false, getDefaultProp())}};
  }

  public TestDataExpireHive(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Override
  protected Record createRecord(int id, String name, long ts, String opTime) {
    return DataTestHelpers.createRecord(getArcticTable().schema(), id, name, ts, opTime,
        opTime + "Z", new BigDecimal("0"), opTime.substring(0, 10));
  }

  public List<DataFile> writeAndCommitBaseAndHive(
      ArcticTable table, long txId, boolean writeHive) {
    String hiveSubDir = HiveTableUtil.newHiveSubdirectory(txId);
    List<DataFile> dataFiles = HiveDataTestHelpers.writeBaseStore(
        table, txId, createRecords(1, 100), false, writeHive, hiveSubDir);
    UnkeyedTable baseTable = table.isKeyedTable() ?
        table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    AppendFiles baseAppend = baseTable.newAppend();
    dataFiles.forEach(baseAppend::appendFile);
    baseAppend.commit();
    return dataFiles;
  }
}
