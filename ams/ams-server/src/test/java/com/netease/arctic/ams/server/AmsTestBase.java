/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.handler.impl.ArcticTableMetastoreHandler;
import com.netease.arctic.ams.server.handler.impl.OptimizeManagerHandler;
import com.netease.arctic.ams.server.service.MetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.DDLTracerService;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.service.impl.JDBCMetaService;
import com.netease.arctic.ams.server.util.DerbyTestUtil;
import com.netease.arctic.ams.server.utils.CatalogUtil;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_CATALOG_NAME;
import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_DB_NAME;
import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static com.netease.arctic.ams.server.util.DerbyTestUtil.get;
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
public class AmsTestBase {

  private static final File testTableBaseDir = new File("/tmp");
  private static final File testBaseDir = new File("unit_test_base_tmp");
  protected static final MockArcticMetastoreServer ams = MockArcticMetastoreServer.getInstance();
  protected static ArcticTableMetastoreHandler amsHandler;
  protected static ArcticCatalog catalog;
  protected static final String AMS_TEST_CATALOG_NAME = "ams_test_catalog";
  protected static final String AMS_TEST_DB_NAME = "ams_test_db";

  protected static final TableIdentifier TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_table");
  protected static final TableIdentifier PK_TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_pk_table");
  protected static final Schema TABLE_SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "name", Types.StringType.get()),
      Types.NestedField.required(3, "op_time", Types.TimestampType.withoutZone())
  );
  protected static final Schema POS_DELETE_SCHEMA = new Schema(
      MetadataColumns.DELETE_FILE_PATH,
      MetadataColumns.DELETE_FILE_POS
  );
  protected static final PartitionSpec SPEC = PartitionSpec.builderFor(TABLE_SCHEMA)
      .day("op_time").build();
  protected static final PrimaryKeySpec PRIMARY_KEY_SPEC = PrimaryKeySpec.builderFor(TABLE_SCHEMA)
      .addColumn("id").build();
  protected File tableDir = null;
  protected UnkeyedTable testTable;
  protected KeyedTable testKeyedTable;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Before
  public void beforeAll() throws IOException {
    tableDir = temp.newFolder();
    testTable = catalog
        .newTableBuilder(TABLE_ID, TABLE_SCHEMA)
        .withProperty(TableProperties.LOCATION, tableDir.getPath() + "/table")
        .withPartitionSpec(SPEC)
        .create().asUnkeyedTable();

    testKeyedTable = catalog
        .newTableBuilder(PK_TABLE_ID, TABLE_SCHEMA)
        .withProperty(TableProperties.LOCATION, tableDir.getPath() + "/pk_table")
        .withPartitionSpec(SPEC)
        .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
        .create().asKeyedTable();
  }

  @BeforeClass
  public static void beforeAllTest() throws Exception {
    System.setProperty("HADOOP_USER_NAME", System.getProperty("user.name"));
    FileUtils.deleteQuietly(testBaseDir);
    FileUtils.deleteQuietly(testTableBaseDir);
    testBaseDir.mkdirs();

    try {
      DerbyTestUtil.deleteIfExists(DerbyTestUtil.path + "mydb1");
    } catch (IOException e) {
      e.printStackTrace();
    }

    mockStatic(JDBCSqlSessionFactoryProvider.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenAnswer((Answer<SqlSessionFactory>) invocation ->
        get());
    DerbyTestUtil derbyTestUtil = new DerbyTestUtil();
    derbyTestUtil.createTestTable();
    mockStatic(ArcticMetaStore.class);
    mockStatic(ServiceContainer.class);
    mockStatic(CatalogMetadataService.class);

    //mock service
    CatalogMetadataService catalogMetadataService = new CatalogMetadataService();
    when(ServiceContainer.getCatalogMetadataService()).thenReturn(catalogMetadataService);
    JDBCMetaService metaService = new JDBCMetaService();
    when(ServiceContainer.getMetaService()).thenReturn(metaService);
    DDLTracerService ddlTracerService = new DDLTracerService();
    when(ServiceContainer.getDdlTracerService()).thenReturn(ddlTracerService);
    FileInfoCacheService fileInfoCacheService = new FileInfoCacheService();
    when(ServiceContainer.getFileInfoCacheService()).thenReturn(fileInfoCacheService);

    //mock handler
    amsHandler = new ArcticTableMetastoreHandler(ServiceContainer.getMetaService());
    when(ServiceContainer.getTableMetastoreHandler()).thenReturn(amsHandler);

    //set handler config
    com.netease.arctic.ams.server.config.Configuration configuration =
        new com.netease.arctic.ams.server.config.Configuration();
    configuration.setString(ArcticMetaStoreConf.DB_TYPE, "derby");
    configuration.setString(ArcticMetaStoreConf.THRIFT_BIND_HOST, "127.0.0.1");
    configuration.setInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT, ams.port());
    ArcticMetaStore.conf = configuration;

    //create
    createCatalog();
  }

  private static void createCatalog() {
    Map<String, String> storageConfig = new HashMap<>();
    storageConfig.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HDFS);
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, MockArcticMetastoreServer.getHadoopSite());
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, MockArcticMetastoreServer.getHadoopSite());
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE, "");

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put(
        CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    authConfig.put(
        CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
        System.getProperty("user.name"));

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogMetaProperties.KEY_WAREHOUSE_DIR, "/tmp");
    CatalogMeta catalogMeta = new CatalogMeta(AMS_TEST_CATALOG_NAME, CATALOG_TYPE_HADOOP,
        storageConfig, authConfig, catalogProperties);
    List<CatalogMeta> catalogMetas = Lists.newArrayList(catalogMeta);
    ServiceContainer.getCatalogMetadataService().addCatalog(catalogMetas);
    catalog = CatalogLoader.load(amsHandler, AMS_TEST_CATALOG_NAME);
    catalog.createDatabase(AMS_TEST_DB_NAME);
  }

  @AfterClass
  public static void afterAllTest() {
    FileUtils.deleteQuietly(testBaseDir);
    FileUtils.deleteQuietly(testTableBaseDir);
    testBaseDir.mkdirs();

    try {
      DerbyTestUtil.deleteIfExists(DerbyTestUtil.path + "mydb1");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
