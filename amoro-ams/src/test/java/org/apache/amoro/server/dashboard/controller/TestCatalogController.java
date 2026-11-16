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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.CommonUnifiedCatalog;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.PlatformFileManager;
import org.apache.amoro.server.dashboard.model.CatalogRegisterInfo;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.utils.CloseableIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCatalogController {

  @TempDir Path tempDir;

  private final CatalogManager catalogManager = Mockito.mock(CatalogManager.class);
  private final PlatformFileManager fileManager = Mockito.mock(PlatformFileManager.class);
  private final CatalogController controller = new CatalogController(catalogManager, fileManager);

  @Test
  public void testRegisterLocalPaimonAndReadExistingTable() throws Exception {
    CatalogRegisterInfo info = registration("filesystem", "Local", "PAIMON");
    try (Catalog catalog =
        PaimonCatalogFactory.paimonCatalog(info.getProperties(), new Configuration())) {
      catalog.createDatabase("db", false);
      Identifier identifier = Identifier.create("db", "orders");
      catalog.createTable(
          identifier,
          Schema.newBuilder().column("id", DataTypes.INT()).option("bucket", "-1").build(),
          false);
      FileStoreTable table = (FileStoreTable) catalog.getTable(identifier);
      BatchWriteBuilder builder = table.newBatchWriteBuilder();
      try (BatchTableWrite write = builder.newWrite();
          BatchTableCommit commit = builder.newCommit()) {
        write.write(GenericRow.of(42));
        commit.commit(write.prepareCommit());
      }
    }

    CatalogMeta meta = register(info);
    Assertions.assertEquals(Map.of("storage.type", "Local"), meta.getStorageConfigs());
    Assertions.assertEquals(
        info.getProperties().get("warehouse"), meta.getCatalogProperties().get("warehouse"));
    Mockito.verifyNoInteractions(fileManager);

    // Use the same metadata and SPI loading path as AMS, including Local's auth handling.
    CommonUnifiedCatalog catalog = new CommonUnifiedCatalog(() -> meta, Map.of());
    Assertions.assertTrue(catalog.listDatabases().contains("db"));
    TableIdentifier identifier = TableIdentifier.of(info.getName(), "db", "orders");
    Assertions.assertEquals(1, catalog.listTables("db").size());
    Assertions.assertEquals(identifier, catalog.listTables("db").get(0).getIdentifier());
    Assertions.assertEquals(TableFormat.PAIMON, catalog.loadTable("db", "orders").format());
    FileStoreTable table = (FileStoreTable) catalog.loadTable("db", "orders").originalTable();
    ReadBuilder reader = table.newReadBuilder();
    try (CloseableIterator<InternalRow> rows =
        reader.newRead().createReader(reader.newScan().plan()).toCloseableIterator()) {
      Assertions.assertTrue(rows.hasNext());
      Assertions.assertEquals(42, rows.next().getInt(0));
      Assertions.assertFalse(rows.hasNext());
    }
  }

  @Test
  public void testUpdateLocalPaimon() {
    CatalogRegisterInfo info = registration("filesystem", "Local", "PAIMON");
    CatalogMeta original = register(info);
    Mockito.when(catalogManager.getCatalogMeta(info.getName())).thenReturn(original);
    info.setTableProperties(Map.of("snapshot.num-retained.min", "3"));
    Context ctx = context(info);

    controller.updateCatalog(ctx);

    ArgumentCaptor<CatalogMeta> captor = ArgumentCaptor.forClass(CatalogMeta.class);
    Mockito.verify(catalogManager).updateCatalog(captor.capture());
    CatalogMeta updated = captor.getValue();
    Assertions.assertEquals(original.getStorageConfigs(), updated.getStorageConfigs());
    Assertions.assertEquals("PAIMON", updated.getCatalogProperties().get("table-formats"));
    Assertions.assertEquals(
        "3", updated.getCatalogProperties().get("table.snapshot.num-retained.min"));
    Mockito.verify(ctx).json(Mockito.any());
    Mockito.verifyNoInteractions(fileManager);
  }

  @ParameterizedTest
  @CsvSource({"Local, ICEBERG", "Hadoop, ICEBERG", "Hadoop, PAIMON"})
  public void testExistingRegistrations(String storage, String format) {
    CatalogMeta meta = register(registration("filesystem", storage, format));
    Assertions.assertEquals(storage, meta.getStorageConfigs().get("storage.type"));
    Assertions.assertEquals(format, meta.getCatalogProperties().get("table-formats"));
  }

  @ParameterizedTest
  @CsvSource({"hive, PAIMON", "filesystem, MIXED_ICEBERG"})
  public void testUnsupportedLocalCombinationsRemainRejected(String type, String format) {
    Context ctx = context(registration(type, "Local", format));
    IllegalArgumentException error =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> controller.createCatalog(ctx));
    Assertions.assertTrue(error.getMessage().contains("Not support"));
    Mockito.verifyNoInteractions(catalogManager, fileManager);
  }

  @Test
  public void testLocalPaimonStillRequiresWarehouse() {
    CatalogRegisterInfo info = registration("filesystem", "Local", "PAIMON");
    info.setProperties(Map.of());
    Context ctx = context(info);
    IllegalArgumentException error =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> controller.createCatalog(ctx));
    Assertions.assertTrue(error.getMessage().contains("require property:warehouse"));
    Mockito.verifyNoInteractions(catalogManager, fileManager);
  }

  private CatalogRegisterInfo registration(String type, String storage, String format) {
    CatalogRegisterInfo info = new CatalogRegisterInfo();
    info.setName("local_catalog");
    info.setType(type);
    info.setOptimizerGroup("default");
    info.setTableFormatList(List.of(format));
    info.setStorageConfig(Map.of("storage.type", storage));
    info.setAuthConfig(Map.of("auth.type", "SIMPLE", "auth.simple.hadoop_username", "local"));
    info.setProperties(Map.of("warehouse", tempDir.resolve("warehouse").toUri().toString()));
    info.setTableProperties(new HashMap<>());
    return info;
  }

  private CatalogMeta register(CatalogRegisterInfo info) {
    Context ctx = context(info);
    controller.createCatalog(ctx);
    ArgumentCaptor<CatalogMeta> captor = ArgumentCaptor.forClass(CatalogMeta.class);
    Mockito.verify(catalogManager).createCatalog(captor.capture());
    Mockito.verify(ctx).json(Mockito.any());
    return captor.getValue();
  }

  private Context context(CatalogRegisterInfo info) {
    Context ctx = Mockito.mock(Context.class);
    Mockito.when(ctx.bodyAsClass(CatalogRegisterInfo.class)).thenReturn(info);
    return ctx;
  }
}
