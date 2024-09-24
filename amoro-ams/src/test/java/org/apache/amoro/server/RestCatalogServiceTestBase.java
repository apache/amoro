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

package org.apache.amoro.server;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.rest.RESTCatalog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public abstract class RestCatalogServiceTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(RestCatalogServiceTestBase.class);

  static AmsEnvironment ams = AmsEnvironment.getIntegrationInstances();
  static String restCatalogUri = RestCatalogService.ICEBERG_REST_API_PREFIX;

  protected final String database = "test_ns";
  protected final String table = "test_iceberg_tbl";
  protected final TableIdentifier tableIdentifier =
      TableIdentifier.of(catalogName(), database, table);

  protected final Schema schema = BasicTableTestHelper.TABLE_SCHEMA;
  protected final PartitionSpec spec = BasicTableTestHelper.SPEC;
  protected final PrimaryKeySpec keySpec =
      PrimaryKeySpec.builderFor(schema).addColumn("id").build();

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

  protected TableService tableService;
  protected InternalCatalog serverCatalog;

  protected RESTCatalog nsCatalog;

  @BeforeEach
  public void before() {
    tableService = ams.serviceContainer().getTableService();
    serverCatalog = (InternalCatalog) tableService.getServerCatalog(catalogName());
    location =
        serverCatalog.getMetadata().getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE)
            + "/"
            + database
            + "/"
            + table;
    nsCatalog = loadIcebergCatalog(Maps.newHashMap());
  }

  protected RESTCatalog loadIcebergCatalog(Map<String, String> clientProperties) {
    clientProperties.put("uri", ams.getHttpUrl() + restCatalogUri);
    clientProperties.putIfAbsent("warehouse", catalogName());

    CatalogMeta catalogMeta = serverCatalog.getMetadata();
    TableMetaStore store = CatalogUtil.buildMetaStore(catalogMeta);

    return (RESTCatalog)
        org.apache.iceberg.CatalogUtil.loadCatalog(
            "org.apache.iceberg.rest.RESTCatalog",
            "test",
            clientProperties,
            store.getConfiguration());
  }

  protected ServerTableIdentifier getServerTableIdentifier(TableIdentifier identifier) {
    TableMetadata metadata = getTableMetadata(identifier);
    return metadata.getTableIdentifier();
  }

  protected TableMetadata getTableMetadata(TableIdentifier identifier) {
    InternalCatalog internalCatalog = tableService.getInternalCatalog(identifier.getCatalog());
    return internalCatalog.loadTableMetadata(identifier.getDatabase(), identifier.getTableName());
  }

  protected TableRuntime getTableRuntime(TableIdentifier identifier) {
    ServerTableIdentifier serverTableIdentifier = getServerTableIdentifier(identifier);
    return tableService.getRuntime(serverTableIdentifier.getId());
  }

  protected void assertTableRuntime(TableIdentifier identifier, TableFormat format) {
    TableRuntime runtime = getTableRuntime(identifier);
    Assertions.assertNotNull(runtime, "table runtime is not exists after created");
    Assertions.assertEquals(format, runtime.getFormat());
  }
}
