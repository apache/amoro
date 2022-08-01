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

package com.netease.arctic.ams.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.AmsClientPools;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.api.OptimizeRangeType;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.handler.impl.OptimizeManagerHandler;
import com.netease.arctic.ams.server.model.AMSColumnInfo;
import com.netease.arctic.ams.server.model.FilesStatistics;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.model.PartitionBaseInfo;
import com.netease.arctic.ams.server.model.PartitionFileBaseInfo;
import com.netease.arctic.ams.server.model.ServerTableMeta;
import com.netease.arctic.ams.server.model.TableBasicInfo;
import com.netease.arctic.ams.server.model.TableStatistics;
import com.netease.arctic.ams.server.model.TransactionsOfTable;
import com.netease.arctic.ams.server.optimize.OptimizeService;
import com.netease.arctic.ams.server.service.MetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.service.impl.TableBaseInfoService;
import com.netease.arctic.ams.server.util.DerbyTestUtil;
import com.netease.arctic.ams.server.utils.AmsUtils;
import com.netease.arctic.ams.server.utils.CatalogUtil;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import io.javalin.testtools.JavalinTest;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_CATALOG_NAME;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static com.netease.arctic.ams.server.util.DerbyTestUtil.get;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class,
    ArcticMetaStore.class,
    ServiceContainer.class,
    CatalogUtil.class,
    MetaService.class,
    ArcticCatalog.class,
    ArcticTable.class,
    PartitionSpec.class,
    FileInfoCacheService.class,
    CatalogMetadataService.class,
    OptimizeManagerHandler.class
})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class TableControllerTest {

  private final Logger LOG = LoggerFactory.getLogger("TableControllerTest");

  private static final File testBaseDir = new File("unit_test_base_tmp");

  private static final MockArcticMetastoreServer ams = MockArcticMetastoreServer.getInstance();

  private static volatile SqlSessionFactory sqlSessionFactory;

  private static final TableIdentifier TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, "test_db", "test_table");
  private final TableIdentifier PK_TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, "test_db", "test_pk_table");
  private static final Schema TABLE_SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "name", Types.StringType.get()),
      Types.NestedField.required(3, "op_time", Types.TimestampType.withoutZone())
  );
  private static final PartitionSpec SPEC = PartitionSpec.builderFor(TABLE_SCHEMA)
      .day("op_time").build();
  private static final PrimaryKeySpec PRIMARY_KEY_SPEC = PrimaryKeySpec.builderFor(TABLE_SCHEMA)
      .addColumn("id").build();

  private static String catalogName;
  private static String database;
  private static final String table = "test_table";

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  static {
    try {
      DerbyTestUtil.deleteIfExists(DerbyTestUtil.path + "mydb1");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Before
  public void startMetastore() throws Exception {
    FileUtils.deleteQuietly(testBaseDir);
    testBaseDir.mkdirs();

    AmsClientPools.cleanAll();
    if (!ams.isStarted()) {
      ams.start();
    }
    catalogName = TEST_CATALOG_NAME;
    database = MockArcticMetastoreServer.TEST_DB_NAME;

    mockStatic(JDBCSqlSessionFactoryProvider.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenAnswer((Answer<SqlSessionFactory>) invocation ->
        get());
    DerbyTestUtil derbyTestUtil = new DerbyTestUtil();
    derbyTestUtil.createTestTable();
    mockStatic(ArcticMetaStore.class);
    com.netease.arctic.ams.server.config.Configuration configuration =
        new com.netease.arctic.ams.server.config.Configuration();
    configuration.setString(ArcticMetaStoreConf.DB_TYPE, "derby");
    configuration.setString(ArcticMetaStoreConf.THRIFT_BIND_HOST, "127.0.0.1");
    configuration.setInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT, ams.port());
    ArcticMetaStore.conf = configuration;
    TableMeta tableMeta = new TableMeta();
    tableMeta.tableIdentifier = new com.netease.arctic.ams.api.TableIdentifier(catalogName, database, table);
    ams.handler().createTableMeta(tableMeta);
  }

  @After
  public void stopMetastore() throws IOException, SQLException {
    AmsClientPools.cleanAll();
  }

  @Test
  public void testGetCatalogs() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
            app.get("/", TableController::getCatalogs);
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetDatabaseList() {
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/", TableController::getDatabaseList);
      final okhttp3.Response resp = client.get("/" + catalogName + "/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetTableList() {
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/", TableController::getTableList);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetTableDetail() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/{table}/", TableController::getTableDetail);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/" + table + "/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetOptimizeInfo() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/{table}/", TableController::getOptimizeInfo);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/" + table + "/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetTableTransactions() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/{table}/", TableController::getTableTransactions);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/" + table + "/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetTransactionDetail() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/{table}/{transactionId}/", TableController::getTransactionDetail);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/" + table + "/1/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetTablePartitions() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/{table}/", TableController::getTablePartitions);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/" + table + "/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetPartitionFileListInfo() throws Exception {
    mockService(catalogName, database, table);
    JavalinTest.test((app, client) -> {
      app.get("/{catalog}/{db}/{table}/{partition}/", TableController::getPartitionFileListInfo);
      final okhttp3.Response resp = client.get("/" + catalogName + "/" + database + "/" + table + "/dt/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  private void mockService(String catalog, String db, String table)
      throws Exception {
    // need to mock out since FileSystem.create calls UGI, which occasionally has issues on some
    // systems
    mockStatic(ServiceContainer.class);
    mockStatic(CatalogUtil.class);
    mockStatic(MetaService.class);

    ArcticCatalog arcticCatalog = mock(ArcticCatalog.class);
    when(CatalogUtil.getArcticCatalog(ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST),
        ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT), catalogName)).
        thenReturn(arcticCatalog);
    ArcticTable arcticTable = mock(ArcticTable.class);
    PartitionSpec partitionSpec = mock(PartitionSpec.class);
    when(arcticCatalog.loadTable(TableIdentifier.of(catalog, db, table))).thenReturn(arcticTable);
    when(arcticTable.spec()).thenReturn(partitionSpec);
    when(partitionSpec.isUnpartitioned()).thenReturn(false);
    TableBaseInfoService tableBaseInfoService = mock(TableBaseInfoService.class);
    when(ServiceContainer.getTableInfoService()).thenReturn(tableBaseInfoService);
    when(tableBaseInfoService.getTableBasicInfo(TableIdentifier.of(catalog, db, table)))
        .thenReturn(mockTableBasicInfo(catalog, db, table));
    FileInfoCacheService fileInfoCacheService = mock(FileInfoCacheService.class);
    when(ServiceContainer.getFileInfoCacheService()).thenReturn(fileInfoCacheService);
    when(fileInfoCacheService.getWatermark(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)),Constants.INNER_TABLE_CHANGE))
        .thenReturn(1000L);
    when(fileInfoCacheService.getWatermark(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)), Constants.INNER_TABLE_BASE))
        .thenReturn(1000L);
    when(fileInfoCacheService.getTransactions(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table))))
        .thenReturn(mockTableTransactions());
    when(fileInfoCacheService.getDatafilesInfo(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)), 1L))
        .thenReturn(mockDataFileInfos());
    when(fileInfoCacheService.getPartitionBaseInfoList(
        AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table))))
        .thenReturn(mockPartitionBaseInfos());
    when(fileInfoCacheService.getPartitionFileList(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)), "dt"))
        .thenReturn(mockPartitionFileBaseInfos());
    OptimizeService optimizeService = mock(OptimizeService.class);
    when(ServiceContainer.getOptimizeService()).thenReturn(optimizeService);
    when(optimizeService.getOptimizeHistory(TableIdentifier.of(catalog, db, table)))
        .thenReturn(mockOptimizeHistories(catalog, db, table));
    when(MetaService.getServerTableMeta(arcticCatalog, TableIdentifier.of(catalog, db, table)))
        .thenReturn(mockServerTableMeta(catalog, db, table));
    CatalogMetadataService catalogMetadataService = mock(CatalogMetadataService.class);
    when(ServiceContainer.getCatalogMetadataService()).thenReturn(catalogMetadataService);
    when(catalogMetadataService.getCatalogs()).thenReturn(mockCatalogMetas());
  }

  private TableBasicInfo mockTableBasicInfo(String catalog, String db, String table) {
    TableBasicInfo tableBasicInfo = new TableBasicInfo();
    tableBasicInfo.setTableIdentifier(TableIdentifier.of(catalog, db, table));
    TableStatistics tableStatistics = new TableStatistics();
    tableStatistics.setTableIdentifier(TableIdentifier.of(catalog, db, table));
    FilesStatistics filesStatistics = new FilesStatistics();
    filesStatistics.setAverageSize(100);
    filesStatistics.setFileCnt(200);
    filesStatistics.setTotalSize(200 * 100);
    tableStatistics.setTotalFilesStat(filesStatistics);
    Map<String, String> testMap = new HashMap<>();
    testMap.put("test", "test");
    tableStatistics.setSummary(testMap);
    tableBasicInfo.setBaseStatistics(tableStatistics);
    tableBasicInfo.setTableStatistics(tableStatistics);
    tableBasicInfo.setChangeStatistics(tableStatistics);
    return tableBasicInfo;
  }

  private ServerTableMeta mockServerTableMeta(String catalog, String db, String table) {
    ServerTableMeta serverTableMeta = new ServerTableMeta();
    serverTableMeta.setTableIdentifier(TableIdentifier.of(catalog, db, table));
    List<AMSColumnInfo> schema = new ArrayList<>();
    schema.add(new AMSColumnInfo("id", "string", ""));
    schema.add(new AMSColumnInfo("name", "string", ""));
    serverTableMeta.setSchema(schema);
    List<AMSColumnInfo> pkList = new ArrayList<>();
    pkList.add(new AMSColumnInfo("id", "string", ""));
    serverTableMeta.setPkList(pkList);
    return serverTableMeta;
  }

  private List<OptimizeHistory> mockOptimizeHistories (String catalog, String db, String table) {
    List<OptimizeHistory> optimizeHistories = new ArrayList<>();
    OptimizeHistory optimizeHistory = new OptimizeHistory();
    optimizeHistory.setTableIdentifier(TableIdentifier.of(catalog, db, table));
    optimizeHistory.setVisibleTime(1656855463563L);
    optimizeHistory.setPlanTime(1656855463563L);
    optimizeHistory.setDuration(1656855463563L);
    optimizeHistory.setCommitTime(1656855463563L);
    optimizeHistory.setRecordId(1L);
    optimizeHistory.setOptimizeRange(OptimizeRangeType.Table);
    FilesStatistics filesStatistics = new FilesStatistics();
    filesStatistics.setAverageSize(100);
    filesStatistics.setFileCnt(100);
    filesStatistics.setTotalSize(100 * 100);
    optimizeHistory.setTotalFilesStatBeforeOptimize(filesStatistics);
    optimizeHistory.setTotalFilesStatAfterOptimize(filesStatistics);
    optimizeHistories.add(optimizeHistory);
    return optimizeHistories;
  }

  private List<TransactionsOfTable> mockTableTransactions() {
    List<TransactionsOfTable> transactions = new ArrayList<>();
    transactions.add(new TransactionsOfTable(1L, 100, 100L, 1656855463563L));
    transactions.add(new TransactionsOfTable(2L, 100, 200L, 1656855463563L));
    return transactions;
  }

  private List<DataFileInfo> mockDataFileInfos() {
    List<DataFileInfo> dataFileInfos = new ArrayList<>();
    DataFileInfo dataFileInfo = new DataFileInfo();
    dataFileInfo.setPath("/home/");
    dataFileInfo.setPartition("id");
    dataFileInfo.setType("BASE_FILE");
    dataFileInfo.setSize(100L);
    dataFileInfo.setCommitTime(1656855463563L);
    dataFileInfos.add(dataFileInfo);
    return dataFileInfos;
  }

  private List<PartitionBaseInfo> mockPartitionBaseInfos() {
    List<PartitionBaseInfo> partitionBaseInfos = new ArrayList<>();
    partitionBaseInfos.add(new PartitionBaseInfo("dt", 100, 100, new Timestamp(1656855463563L)));
    return partitionBaseInfos;
  }

  private List<PartitionFileBaseInfo> mockPartitionFileBaseInfos() {
    List<PartitionFileBaseInfo> partitionFileBaseInfos = new ArrayList<>();
    partitionFileBaseInfos.add(new PartitionFileBaseInfo("1", "BASE_FILE", 1656855463563L, "dt", "/home", 100L));
    return partitionFileBaseInfos;
  }

  private List<CatalogMeta> mockCatalogMetas() {
    List<CatalogMeta> catalogMetas = new ArrayList<>();
    Map<String, String> storageConfig = new HashMap<>();
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HDFS);
    String hadoopSite = Base64.getEncoder().encodeToString("<configuration></configuration>"
        .getBytes(StandardCharsets.UTF_8));
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, hadoopSite);
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, hadoopSite);

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
        System.getProperty("user.name"));

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogMetaProperties.KEY_WAREHOUSE_DIR, "/tmp");

    CatalogMeta catalogMeta = new CatalogMeta(TEST_CATALOG_NAME, CATALOG_TYPE_HADOOP,
        storageConfig, authConfig, catalogProperties);
    catalogMetas.add(catalogMeta);
    return catalogMetas;
  }
}