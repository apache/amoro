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

package com.netease.arctic.ams.server.repair;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.ams.server.TestAmsResource;
import com.netease.arctic.ams.server.util.DerbyTestUtil;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.catalog.CatalogTestHelpers;
import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Types;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;

public class TestRepairEnv {
  @ClassRule
  public static TestAmsResource TEST_AMS = new TestAmsResource();
  private static String WAREHOUSE = "/tmp/test_repair";
  protected static final String TEST_CATALOG_NAME = "test_repair_catalog";
  protected static final String TEST_DATABASE_NAME = "test_repair_db";
  public static ArcticCatalog catalog;

  public static final TableIdentifier TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DATABASE_NAME, "test_table");
  public static final TableIdentifier PK_TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DATABASE_NAME, "test_pk_table");

  public static final TableIdentifier UN_PARTITION_TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DATABASE_NAME, "un_partition_test_table");
  public static final TableIdentifier UN_PARTITION_PK_TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DATABASE_NAME, "un_partition_test_pk_table");

  protected static final List<TableIdentifier> ALL_TABLES = Lists.newArrayList(TABLE_ID, PK_TABLE_ID,
      UN_PARTITION_TABLE_ID, UN_PARTITION_PK_TABLE_ID);

  public static final Schema TABLE_SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "op_time", Types.TimestampType.withoutZone()),
      Types.NestedField.required(3, "op_time_with_zone", Types.TimestampType.withZone()),
      Types.NestedField.required(4, "d", Types.DecimalType.of(10, 0)),
      Types.NestedField.required(5, "name", Types.StringType.get())
  );

  protected static final PartitionSpec SPEC =
      PartitionSpec.builderFor(TABLE_SCHEMA).identity("name").build();
  protected static final PrimaryKeySpec PRIMARY_KEY_SPEC = PrimaryKeySpec.builderFor(TABLE_SCHEMA)
      .addColumn("id").build();

  protected UnkeyedTable testTable;
  protected KeyedTable testKeyedTable;

  protected UnkeyedTable testUnPartitionTable;
  protected KeyedTable testUnPartitionKeyedTable;

  @BeforeClass
  public static void beforeClass() {
    Map<String, String> storageConfig = new HashMap<>();
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.STORAGE_CONFIGS_VALUE_TYPE_HDFS);
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE, MockArcticMetastoreServer.getHadoopSite());
    storageConfig.put(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE, MockArcticMetastoreServer.getHadoopSite());

    Map<String, String> authConfig = new HashMap<>();
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
        CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE);
    authConfig.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
        System.getProperty("user.name"));

    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogMetaProperties.KEY_WAREHOUSE, WAREHOUSE);

    CatalogMeta catalogMeta = new CatalogMeta(TEST_CATALOG_NAME, CATALOG_TYPE_HADOOP,
        storageConfig, authConfig, catalogProperties);
    TEST_AMS.getAmsHandler().createCatalog(catalogMeta);
    catalog = CatalogLoader.load(TEST_AMS.getUrl(TEST_CATALOG_NAME));
    catalog.createDatabase(TEST_DATABASE_NAME);
  }

  @Before
  public void before() {
    testTable = catalog
        .newTableBuilder(TABLE_ID, TABLE_SCHEMA)
        .withPartitionSpec(SPEC)
        .create().asUnkeyedTable();

    testUnPartitionTable = catalog
        .newTableBuilder(UN_PARTITION_TABLE_ID, TABLE_SCHEMA)
        .create().asUnkeyedTable();

    testKeyedTable = catalog
        .newTableBuilder(PK_TABLE_ID, TABLE_SCHEMA)
        .withPartitionSpec(SPEC)
        .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
        .create().asKeyedTable();

    testUnPartitionKeyedTable = catalog
        .newTableBuilder(UN_PARTITION_PK_TABLE_ID, TABLE_SCHEMA)
        .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
        .create().asKeyedTable();
  }

  @After
  public void after() throws IOException {
    ALL_TABLES.forEach(e -> catalog.dropTable(e, true));
    DerbyTestUtil.deleteIfExists(WAREHOUSE);
  }
}
