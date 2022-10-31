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

package com.netease.arctic.hive.catalog;

import com.google.common.collect.Maps;
import com.netease.arctic.CatalogMetaTestUtil;
import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.BaseIcebergCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.table.ArcticTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE_HIVE;

public class IcebergHiveCatalogTest extends HiveTableTestBase {

  private static final String ICEBERG_HIVE_CATALOG_NAME = "iceberg_hive";

  @BeforeClass
  public static void createIcebergCatalog() throws IOException {
    Map<String, String> storageConfig = new HashMap<>();
    storageConfig.put(
        CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HDFS);
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, MockArcticMetastoreServer.getHadoopSite());
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, MockArcticMetastoreServer.getHadoopSite());
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE,
        CatalogMetaTestUtil.encodingSite(hms.hiveConf()));

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
        System.getProperty("user.name"));

    tempFolder.create();
    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogMetaProperties.TABLE_FORMATS, "iceberg");

    CatalogMeta catalogMeta = new CatalogMeta(ICEBERG_HIVE_CATALOG_NAME, CATALOG_TYPE_HIVE,
        storageConfig, authConfig, catalogProperties);
    MockArcticMetastoreServer.getInstance().handler().createCatalog(catalogMeta);

    MockArcticMetastoreServer.getInstance().createCatalogIfAbsent(catalogMeta);
  }

  @Test
  public void testLoadCatalog() {
    ArcticCatalog icebergCatalog =
        CatalogLoader.load(MockArcticMetastoreServer.getInstance().getUrl(ICEBERG_HIVE_CATALOG_NAME));
    Assert.assertEquals(ICEBERG_HIVE_CATALOG_NAME, icebergCatalog.name());
    Assert.assertEquals(2, icebergCatalog.listDatabases().size());
  }

  @Test
  public void testCreateAndDropDatabase() {
    ArcticCatalog icebergCatalog =
        CatalogLoader.load(MockArcticMetastoreServer.getInstance().getUrl(ICEBERG_HIVE_CATALOG_NAME));
    icebergCatalog.createDatabase("db1");
    Assert.assertEquals(3, icebergCatalog.listDatabases().size());
    Assert.assertEquals("db1", icebergCatalog.listDatabases().get(0));
    icebergCatalog.dropDatabase("db1");
    Assert.assertEquals(2, icebergCatalog.listDatabases().size());
  }

  @Test
  public void testLoadIcebergTable() throws TException {
    ArcticCatalog icebergCatalog =
        CatalogLoader.load(MockArcticMetastoreServer.getInstance().getUrl(ICEBERG_HIVE_CATALOG_NAME));
    icebergCatalog.createDatabase("db2");
    CatalogMeta catalogMeta = MockArcticMetastoreServer.getInstance().handler().getCatalog(ICEBERG_HIVE_CATALOG_NAME);
    Map<String, String> catalogProperties = Maps.newHashMap(catalogMeta.getCatalogProperties());
    catalogProperties.put(ICEBERG_CATALOG_TYPE, ICEBERG_CATALOG_TYPE_HIVE);
    Catalog nativeIcebergTable = org.apache.iceberg.CatalogUtil.buildIcebergCatalog(
        ICEBERG_HIVE_CATALOG_NAME,
        catalogProperties, new Configuration());
    nativeIcebergTable.createTable(TableIdentifier.of("db2", "tb1"), TableTestBase.TABLE_SCHEMA);
    ArcticTable table = icebergCatalog.loadTable(
        com.netease.arctic.table.TableIdentifier.of(ICEBERG_HIVE_CATALOG_NAME, "db2", "tb1"));
    Assert.assertTrue(table instanceof BaseIcebergCatalog.BaseIcebergTable);
    Assert.assertEquals(true, table.isUnkeyedTable());
    Assert.assertEquals(TableTestBase.TABLE_SCHEMA.asStruct(), table.schema().asStruct());
    nativeIcebergTable.dropTable(TableIdentifier.of("db2", "tb1"), true);
    icebergCatalog.dropDatabase("db2");
  }

}
