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

package org.apache.amoro.formats.lance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.hive.HMSMockServer;
import org.apache.amoro.table.TableMetaStore;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lance.Dataset;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.hive3.Hive3NamespaceConfig;
import org.lance.namespace.model.DeclareTableRequest;
import org.lance.namespace.model.DeclareTableResponse;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DeregisterTableResponse;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestLanceHiveCatalog {

  private static final String S3_ENDPOINT = "http://127.0.0.1:1";
  private static HMSMockServer hms;
  private static RootAllocator allocator;
  private static LanceHiveCatalog catalog;
  private String database;

  private static final Schema SCHEMA =
      new Schema(
          Collections.singletonList(
              new Field("id", FieldType.nullable(new ArrowType.Int(32, true)), null)));

  /** Registers the table in HMS and writes a Lance dataset at the returned location. */
  private String createTable(String table) {
    DeclareTableResponse response =
        catalog.namespace.declareTable(
            new DeclareTableRequest()
                .id(Arrays.asList(Hive3NamespaceConfig.CATALOG_NAME, database, table)));
    String location = response.getLocation();
    Dataset.write().allocator(allocator).uri(location).schema(SCHEMA).execute().close();
    return location;
  }

  private static boolean dataExists(String location) {
    return new File(URI.create(location)).exists();
  }

  @BeforeAll
  public static void startMetastore() {
    hms = new HMSMockServer(new File("target/hive_warehouse"));
    hms.start();
    allocator = new RootAllocator(Long.MAX_VALUE);
    Map<String, String> properties = new HashMap<>();
    properties.put("warehouse", hms.getWareHouseLocation());
    properties.put("uri", hms.hiveConf().get(HiveConf.ConfVars.METASTOREURIS.varname));
    properties.put(S3FileIOProperties.ENDPOINT, S3_ENDPOINT);
    properties.put(S3FileIOProperties.ACCESS_KEY_ID, "test-key");
    properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, "test-secret");
    catalog = new LanceHiveCatalog("lance_hive", properties, TableMetaStore.EMPTY);
  }

  @BeforeEach
  public void createDatabase() {
    database = "db_" + UUID.randomUUID().toString().replace("-", "");
    catalog.createDatabase(database);
  }

  @AfterEach
  public void cleanUp() throws Exception {
    hms.getClient().dropDatabase(database, true, true, true);
    FileUtils.deleteQuietly(new File(hms.getDatabasePath(database)));
  }

  @AfterAll
  public static void stopMetastore() {
    if (allocator != null) {
      allocator.close();
    }
    hms.stop();
    FileUtils.deleteQuietly(new File("target/hive_warehouse"));
  }

  @Test
  public void testDropTable() {
    // 1、purge=true removes both the metastore entry and the Lance dataset directory.
    String location = createTable("t1");
    Assertions.assertTrue(catalog.dropTable(database, "t1", true));
    Assertions.assertFalse(catalog.tableExists(database, "t1"));
    Assertions.assertFalse(dataExists(location));
    // A second drop finds no metastore entry and reports false instead of failing.
    Assertions.assertFalse(catalog.dropTable(database, "t1", true));

    // 2、purge=false only deregisters the metastore entry, so the table data must still be there.
    location = createTable("t1");
    Assertions.assertTrue(catalog.dropTable(database, "t1", false));
    Assertions.assertFalse(catalog.tableExists(database, "t1"));
    Assertions.assertTrue(dataExists(location));
  }

  /** A failed data deletion must be reported with the location left behind. */
  @Test
  public void testPurgeFailureIsReported() throws Exception {
    String location = createTable("t2");
    // Replace the dataset directory with a file so Dataset.drop fails deterministically.
    FileUtils.deleteDirectory(new File(URI.create(location)));
    FileUtils.writeStringToFile(new File(URI.create(location)), "not a dataset", "UTF-8");

    Exception error =
        Assertions.assertThrows(Exception.class, () -> catalog.dropTable(database, "t2", true));
    // The entry is already dropped, so the message must carry the location to clean up.
    Assertions.assertTrue(error.getMessage().contains(location), error.getMessage());
    Assertions.assertFalse(catalog.tableExists(database, "t2"));
  }

  /** Purge must reach the catalog-only endpoint instead of an empty option map. */
  @Test
  public void testPurgeUsesCatalogStorageOptions() throws Exception {
    LanceNamespace stub = mock(LanceNamespace.class);
    when(stub.deregisterTable(any(DeregisterTableRequest.class)))
        .thenReturn(new DeregisterTableResponse().location("s3://bucket/t.lance"));
    java.lang.reflect.Field field = AbstractLanceCatalog.class.getDeclaredField("namespace");
    field.setAccessible(true);
    Object original = field.get(catalog);
    field.set(catalog, stub);
    try {
      Exception error =
          Assertions.assertThrows(
              IllegalStateException.class, () -> catalog.dropTable(database, "t1", true));
      // Only the catalog-configured endpoint can put this host into the S3 error.
      Assertions.assertTrue(
          error.getCause().getMessage().contains(S3_ENDPOINT), error.getCause().getMessage());
    } finally {
      field.set(catalog, original);
    }
  }

  @Test
  public void testDropDatabase() {
    // drop empty database
    catalog.dropDatabase(database);
    Assertions.assertFalse(catalog.databaseExists(database));

    // drop no empty database.
    catalog.createDatabase(database);
    String location = createTable("t1");
    Assertions.assertThrows(IllegalStateException.class, () -> catalog.dropDatabase(database));
    // A rejected drop must leave the database, its table and the data untouched.
    Assertions.assertTrue(catalog.databaseExists(database));
    Assertions.assertTrue(catalog.tableExists(database, "t1"));
    Assertions.assertTrue(dataExists(location));
  }

  /** A non-Lance table in the same database must be left untouched by purge. */
  @Test
  public void testDropNonLanceTableReturnsFalse() throws Exception {
    Table table = new Table();
    table.setDbName(database);
    table.setTableName("hive_tbl");
    table.setTableType("EXTERNAL_TABLE");
    StorageDescriptor sd = new StorageDescriptor();
    sd.setLocation(hms.getTablePath(database, "hive_tbl"));
    sd.setCols(Collections.emptyList());
    sd.setSerdeInfo(new SerDeInfo());
    table.setSd(sd);
    hms.getClient().createTable(table);

    Assertions.assertFalse(catalog.dropTable(database, "hive_tbl", true));
    Assertions.assertTrue(hms.getClient().tableExists(database, "hive_tbl"));
  }
}
