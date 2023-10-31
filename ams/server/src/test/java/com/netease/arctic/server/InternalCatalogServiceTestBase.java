package com.netease.arctic.server;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableMetaStore;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.rest.RESTCatalog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public abstract class InternalCatalogServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(InternalCatalogServiceTestBase.class);

  static AmsEnvironment ams = AmsEnvironment.getIntegrationInstances();
  static String restCatalogUri = IcebergRestCatalogService.ICEBERG_REST_API_PREFIX;

  protected final String database = "test_ns";
  protected final String table = "test_iceberg_tbl";

  protected final Schema schema = BasicTableTestHelper.TABLE_SCHEMA;
  protected final PartitionSpec spec = BasicTableTestHelper.SPEC;
  protected final PrimaryKeySpec keySpec = PrimaryKeySpec.builderFor(schema)
      .addColumn("id").build();

  protected String location;

  @BeforeAll
  public static void beforeAll() throws Exception {
    ams.start();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    ams.stop();
  }

  protected abstract String catalogName();

  protected TableService service;
  protected InternalCatalog serverCatalog;

  protected RESTCatalog nsCatalog;

  @BeforeEach
  public void before() {
    service = ams.serviceContainer().getTableService();
    serverCatalog = (InternalCatalog) service.getServerCatalog(catalogName());
    location = serverCatalog.getMetadata().getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE) +
        "/" + database + "/" + table;
    nsCatalog = loadIcebergCatalog(Maps.newHashMap());
  }


  protected RESTCatalog loadIcebergCatalog(Map<String, String> clientProperties) {
    clientProperties.put("uri", ams.getHttpUrl() + restCatalogUri);
    clientProperties.putIfAbsent("warehouse", catalogName());

    CatalogMeta catalogMeta = serverCatalog.getMetadata();
    TableMetaStore store = com.netease.arctic.utils.CatalogUtil.buildMetaStore(catalogMeta);

    return (RESTCatalog)
        CatalogUtil.loadCatalog(
            "org.apache.iceberg.rest.RESTCatalog",
            "test",
            clientProperties,
            store.getConfiguration());
  }
}
