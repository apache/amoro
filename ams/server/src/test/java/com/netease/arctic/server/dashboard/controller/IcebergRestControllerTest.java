package com.netease.arctic.server.dashboard.controller;

import com.netease.arctic.server.AmsEnvironment;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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



  @BeforeAll
  public static void beforeAll() throws Exception {
    String rootPath = TEMP_DIR.getAbsolutePath();
    ams = new AmsEnvironment(rootPath);
    ams.start();
  }

  @AfterAll
  public static void after() throws IOException {
    ams.stop();
  }


  @Test
  public void test() {
    Map<String, String> catalogClientProperties = Maps.newHashMap();

    catalogClientProperties.put("uri", ams.getHttpUrl() + restCatalogUri);
    Catalog catalog = CatalogUtil.loadCatalog(
        "org.apache.iceberg.rest.RESTCatalog", "test",
        catalogClientProperties, null
    );
    SupportsNamespaces nsCatalog = (SupportsNamespaces)catalog;

    Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());

    nsCatalog.createNamespace(Namespace.of(
        "test"
    ));
    Assertions.assertEquals(1, nsCatalog.listNamespaces().size());

    System.out.println(((SupportsNamespaces)catalog).listNamespaces());
  }
}
