package com.netease.arctic.server.dashboard.controller;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.AmsEnvironment;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.catalog.ServerCatalog;
import com.netease.arctic.server.table.TableService;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.rest.RESTCatalog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class IcebergRestControllerTest {

  @TempDir(cleanup = CleanupMode.ALWAYS)
  public static File TEMP_DIR;

  static AmsEnvironment ams;
  static String restCatalogUri = "/api/iceberg/rest/catalog/" + AmsEnvironment.INTERNAL_ICEBERG_CATALOG;


  private final String database = "test_ns";
  private final String table = "test_iceberg_tbl";

  @BeforeAll
  public static void beforeAll() throws Exception {
    String rootPath = TEMP_DIR.getAbsolutePath();
    ams = new AmsEnvironment(rootPath);
    ams.start();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    ams.stop();
  }

  TableService service;
  InternalCatalog serverCatalog;

  @BeforeEach
  public void before() {
    service = ams.serviceContainer().getTableService();
    serverCatalog = (InternalCatalog) service.getServerCatalog(AmsEnvironment.INTERNAL_ICEBERG_CATALOG);
  }

  @Test
  public void testCatalogProperties() {
    CatalogMeta meta = serverCatalog.getMetadata();
    CatalogMeta oldMeta = meta.deepCopy();
    meta.putToCatalogProperties("cache-enabled", "false");
    meta.putToCatalogProperties("cache.expiration-interval-ms", "10000");
    serverCatalog.updateMetadata(meta);
    String warehouseInAMS = meta.getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);

    Map<String, String> clientSideConfiguration = Maps.newHashMap();
    clientSideConfiguration.put("warehouse", "/tmp");
    clientSideConfiguration.put("cache-enabled", "true");

    try (RESTCatalog catalog = loadCatalog(clientSideConfiguration)) {
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


  @Test
  public void testNamespaceOperations() throws IOException {
    try (RESTCatalog nsCatalog = loadCatalog(Maps.newHashMap())) {
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
      nsCatalog.createNamespace(Namespace.of(database));
      Assertions.assertEquals(1, nsCatalog.listNamespaces().size());
      nsCatalog.dropNamespace(Namespace.of(database));
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
    }
  }





  private RESTCatalog loadCatalog(Map<String, String> clientProperties) {
    clientProperties.put("uri", ams.getHttpUrl() + restCatalogUri);
    return (RESTCatalog) CatalogUtil.loadCatalog(
        "org.apache.iceberg.rest.RESTCatalog", "test",
        clientProperties, null
    );
  }
}
