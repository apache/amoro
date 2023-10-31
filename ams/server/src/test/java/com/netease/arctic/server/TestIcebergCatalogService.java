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

package com.netease.arctic.server;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.io.IcebergDataTestHelpers;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.io.reader.GenericUnkeyedDataReader;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.apache.iceberg.rest.RESTCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestIcebergCatalogService extends InternalCatalogServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(TestIcebergCatalogService.class);

  private final Namespace ns = Namespace.of(database);
  private final TableIdentifier identifier = TableIdentifier.of(ns, table);

  @Override
  protected String catalogName() {
    return AmsEnvironment.INTERNAL_ICEBERG_CATALOG;
  }

  @Nested
  public class CatalogPropertiesTest {
    @Test
    public void testCatalogProperties() {
      CatalogMeta meta = serverCatalog.getMetadata();
      CatalogMeta oldMeta = meta.deepCopy();
      meta.putToCatalogProperties("cache-enabled", "false");
      meta.putToCatalogProperties("cache.expiration-interval-ms", "10000");
      serverCatalog.updateMetadata(meta);
      String warehouseInAMS = meta.getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);

      Map<String, String> clientSideConfiguration = Maps.newHashMap();
      clientSideConfiguration.put("cache-enabled", "true");

      try (RESTCatalog catalog = loadIcebergCatalog(clientSideConfiguration)) {
        Map<String, String> finallyConfigs = catalog.properties();
        // overwrites properties using value from ams
        Assertions.assertEquals(warehouseInAMS, finallyConfigs.get("warehouse"));
        // default properties using value from client then properties.
        Assertions.assertEquals("true", finallyConfigs.get("cache-enabled"));
        Assertions.assertEquals("10000", finallyConfigs.get("cache.expiration-interval-ms"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        serverCatalog.updateMetadata(oldMeta);
      }
    }
  }

  @Nested
  public class NamespaceTests {
    @Test
    public void testNamespaceOperations() throws IOException {
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
      nsCatalog.createNamespace(Namespace.of(database));
      Assertions.assertEquals(1, nsCatalog.listNamespaces().size());
      Assertions.assertEquals(0, nsCatalog.listNamespaces(Namespace.of(database)).size());
      nsCatalog.dropNamespace(Namespace.of(database));
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
    }
  }

  @Nested
  public class TableTests {
    List<Record> newRecords =
        Lists.newArrayList(
            MixedDataTestHelpers.createRecord(7, "777", 0, "2022-01-01T12:00:00"),
            MixedDataTestHelpers.createRecord(8, "888", 0, "2022-01-02T12:00:00"),
            MixedDataTestHelpers.createRecord(9, "999", 0, "2022-01-03T12:00:00"));

    @BeforeEach
    public void setup() {
      serverCatalog.createDatabase(database);
    }

    @AfterEach
    public void clean() {
      nsCatalog.dropTable(identifier);
      if (serverCatalog.exist(database, table)) {
        serverCatalog.dropTable(database, table);
      }
      serverCatalog.dropDatabase(database);
    }

    @Test
    public void testCreateTableAndListing() throws IOException {
      Assertions.assertTrue(nsCatalog.listTables(ns).isEmpty());

      LOG.info("Assert create iceberg table");
      nsCatalog.createTable(identifier, schema);
      Assertions.assertEquals(1, nsCatalog.listTables(ns).size());
      Assertions.assertEquals(identifier, nsCatalog.listTables(ns).get(0));

      LOG.info("Assert load iceberg table");
      Table tbl = nsCatalog.loadTable(identifier);
      Assertions.assertNotNull(tbl);
      Assertions.assertEquals(schema.asStruct(), tbl.schema().asStruct());
      Assertions.assertEquals(location, tbl.location());

      LOG.info("Assert table exists");
      Assertions.assertTrue(nsCatalog.tableExists(identifier));
      nsCatalog.dropTable(identifier);
      Assertions.assertFalse(nsCatalog.tableExists(identifier));
    }

    @Test
    public void testTableWriteAndCommit() throws IOException {
      Table tbl = nsCatalog.createTable(identifier, schema);
      WriteResult insert = IcebergDataTestHelpers.insert(tbl, newRecords);
      DataFile[] files = insert.dataFiles();
      AppendFiles appendFiles = tbl.newAppend();
      Arrays.stream(files).forEach(appendFiles::appendFile);
      appendFiles.commit();

      tbl = nsCatalog.loadTable(identifier);
      List<FileScanTask> tasks =
          Streams.stream(tbl.newScan().planFiles()).collect(Collectors.toList());
      Assertions.assertEquals(files.length, tasks.size());
    }

    @Test
    public void testTableTransaction() throws IOException {
      Table tbl = nsCatalog.createTable(identifier, schema, spec);
      WriteResult insert = IcebergDataTestHelpers.insert(tbl, newRecords);
      DataFile[] files = insert.dataFiles();

      Transaction tx = tbl.newTransaction();
      AppendFiles appendFiles = tx.newAppend();
      Arrays.stream(files).forEach(appendFiles::appendFile);
      appendFiles.commit();

      UpdateProperties properties = tx.updateProperties();
      properties.set("k1", "v1");
      properties.commit();

      Table loadedTable = nsCatalog.loadTable(identifier);
      Assertions.assertNull(loadedTable.currentSnapshot());
      List<FileScanTask> tasks =
          Streams.stream(tbl.newScan().planFiles()).collect(Collectors.toList());
      Assertions.assertEquals(0, tasks.size());

      tx.commitTransaction();
      loadedTable.refresh();

      Assertions.assertNotNull(loadedTable.currentSnapshot());
      Assertions.assertEquals("v1", loadedTable.properties().get("k1"));
      tasks = Streams.stream(tbl.newScan().planFiles()).collect(Collectors.toList());
      Assertions.assertEquals(files.length, tasks.size());
    }

    @Test
    public void testArcticCatalogLoader() throws IOException {
      Table tbl = nsCatalog.createTable(identifier, schema, spec);
      DataFile[] files = IcebergDataTestHelpers.insert(tbl, newRecords).dataFiles();
      AppendFiles appendFiles = tbl.newAppend();
      Arrays.stream(files).forEach(appendFiles::appendFile);
      appendFiles.commit();

      ArcticCatalog catalog = ams.catalog(AmsEnvironment.INTERNAL_ICEBERG_CATALOG);
      ArcticTable arcticTable =
          catalog.loadTable(
              com.netease.arctic.table.TableIdentifier.of(
                  AmsEnvironment.INTERNAL_ICEBERG_CATALOG, database, table));

      Assertions.assertEquals(TableFormat.ICEBERG, arcticTable.format());
      GenericUnkeyedDataReader reader =
          new GenericUnkeyedDataReader(
              arcticTable.io(),
              arcticTable.schema(),
              arcticTable.schema(),
              null,
              false,
              IdentityPartitionConverters::convertConstant,
              false);
      List<Record> records =
          MixedDataTestHelpers.readBaseStore(arcticTable, reader, Expressions.alwaysTrue());
      Assertions.assertEquals(newRecords.size(), records.size());
    }
  }
}
