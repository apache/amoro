package com.netease.arctic.server;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.table.TableService;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class InternalCatalogServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(InternalCatalogServiceTestBase.class);

  static AmsEnvironment ams = AmsEnvironment.getIntegrationInstances();
  static String restCatalogUri = IcebergRestCatalogService.ICEBERG_REST_API_PREFIX;

  protected final String database = "test_ns";
  protected final String table = "test_iceberg_tbl";

  protected final Schema schema = BasicTableTestHelper.TABLE_SCHEMA;
  protected final PartitionSpec spec = BasicTableTestHelper.SPEC;

  protected String location;

  @BeforeAll
  public static void beforeAll() throws Exception {
    ams.start();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    ams.stop();
  }

  protected TableService service;
  protected InternalCatalog serverCatalog;

  @BeforeEach
  public void before() {
    service = ams.serviceContainer().getTableService();
    serverCatalog = (InternalCatalog) service.getServerCatalog(AmsEnvironment.INTERNAL_ICEBERG_CATALOG);
    location = serverCatalog.getMetadata().getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE) +
        "/" + database + "/" + table;
  }


}
