package com.netease.arctic.server;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.table.TableMetaStore;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.apache.iceberg.rest.RESTCatalog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class IcebergRestCatalogServiceTest {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergRestCatalogServiceTest.class);

  @TempDir(cleanup = CleanupMode.ALWAYS)
  public static File TEMP_DIR;

  static AmsEnvironment ams;
  static String restCatalogUri = "/api/iceberg/rest/catalog/" + AmsEnvironment.INTERNAL_ICEBERG_CATALOG;


  private final String database = "test_ns";
  private final String table = "test_iceberg_tbl";

  private final Namespace ns = Namespace.of(database);
  private final TableIdentifier identifier = TableIdentifier.of(ns, table);

  private final Schema schema = BasicTableTestHelper.TABLE_SCHEMA;
  private final PartitionSpec spec = BasicTableTestHelper.SPEC;

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
  }


  @Nested
  public class NamespaceTests {
    RESTCatalog nsCatalog;

    @BeforeEach
    public void setup() {
      nsCatalog = loadCatalog(Maps.newHashMap());
    }

    @Test
    public void testNamespaceOperations() throws IOException {
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
      nsCatalog.createNamespace(Namespace.of(database));
      Assertions.assertEquals(1, nsCatalog.listNamespaces().size());
      nsCatalog.dropNamespace(Namespace.of(database));
      Assertions.assertTrue(nsCatalog.listNamespaces().isEmpty());
    }
  }


  @Nested
  public class TableTests {
    RESTCatalog nsCatalog;

    @BeforeEach
    public void setup() {
      nsCatalog = loadCatalog(Maps.newHashMap());
      serverCatalog.createDatabase(database);
    }

    @AfterEach
    public void clean() {
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

      LOG.info("Assert table exists");
      Assertions.assertTrue(nsCatalog.tableExists(identifier));
      nsCatalog.dropTable(identifier);
    }

    @Test
    public void testTableWriteAndCommit() throws IOException {
      Table tbl = nsCatalog.createTable(identifier, schema);



      try (FileIO io = tbl.io()) {
        String dataLocation = tbl.locationProvider().newDataLocation("test.parquet");
        OutputFile file = io.newOutputFile(dataLocation);
        file.createOrOverwrite().close();

        tbl.newAppend().appendFile(
            DataFiles.builder(PartitionSpec.unpartitioned())
                .withInputFile(file.toInputFile())
                .withFileSizeInBytes(0)
                .withFormat(FileFormat.PARQUET)
                .withRecordCount(0)
                .withPath(dataLocation)
                .build()
        ).commit();

        tbl = nsCatalog.loadTable(identifier);
        List<FileScanTask> files = Streams.stream(tbl.newScan().planFiles()).collect(Collectors.toList());
        Assertions.assertEquals(1, files.size());
      }


    }

    public void testTableTransaction() throws IOException {

    }

  }


  private RESTCatalog loadCatalog(Map<String, String> clientProperties) {
    clientProperties.put("uri", ams.getHttpUrl() + restCatalogUri);
    CatalogMeta catalogMeta = serverCatalog.getMetadata();
    TableMetaStore store = com.netease.arctic.utils.CatalogUtil.buildMetaStore(catalogMeta);

    return (RESTCatalog) CatalogUtil.loadCatalog(
        "org.apache.iceberg.rest.RESTCatalog", "test",
        clientProperties, store.getConfiguration()
    );
  }


  protected static OffsetDateTime ofDateWithZone(int year, int mon, int day, int hour) {
    LocalDateTime dateTime = LocalDateTime.of(year, mon, day, hour, 0);
    return OffsetDateTime.of(dateTime, ZoneOffset.ofHours(0));
  }

  protected static OffsetDateTime quickDateWithZone(int day) {
    return ofDateWithZone(2022, 1, day, 0);
  }

  protected static Record newRecord(Schema schema, Object... val) {
    GenericRecord record = GenericRecord.create(schema);
    for (int i = 0; i < schema.columns().size(); i++) {
      record.set(i, val[i]);
    }
    return record;
  }
}
