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

package org.apache.amoro.mixed;

import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBasicMixedIcebergCatalog {

  @Test
  public void testListTablesSkipsTableWithMissingMetadata() {
    assertListTablesSkipsUnavailableTable(new NotFoundException("metadata file does not exist"));
  }

  @Test
  public void testListTablesSkipsTableRemovedAfterListing() {
    assertListTablesSkipsUnavailableTable(new NoSuchTableException("table does not exist"));
  }

  private void assertListTablesSkipsUnavailableTable(RuntimeException loadFailure) {
    Catalog icebergCatalog = mock(Catalog.class);
    MixedTables mixedTables = mock(MixedTables.class);
    Table healthyTable = mock(Table.class);
    org.apache.iceberg.catalog.TableIdentifier brokenIdentifier =
        org.apache.iceberg.catalog.TableIdentifier.of("db", "broken_table");
    org.apache.iceberg.catalog.TableIdentifier healthyIdentifier =
        org.apache.iceberg.catalog.TableIdentifier.of("db", "healthy_table");

    when(icebergCatalog.listTables(Namespace.of("db")))
        .thenReturn(Arrays.asList(brokenIdentifier, healthyIdentifier));
    when(icebergCatalog.loadTable(brokenIdentifier)).thenThrow(loadFailure);
    when(icebergCatalog.loadTable(healthyIdentifier)).thenReturn(healthyTable);
    when(mixedTables.isBaseStore(healthyTable)).thenReturn(true);
    when(healthyTable.schema()).thenReturn(new Schema());
    when(healthyTable.properties()).thenReturn(Collections.emptyMap());

    BasicMixedIcebergCatalog catalog = new TestCatalog(icebergCatalog, mixedTables);
    Map<String, String> properties = new HashMap<>();
    properties.put(ICEBERG_CATALOG_TYPE, "hadoop");
    properties.put(CatalogProperties.CACHE_ENABLED, "false");
    catalog.initialize("test_catalog", properties, TableMetaStore.EMPTY);

    List<TableIdentifier> tables = catalog.listTables("db");

    assertEquals(
        Collections.singletonList(TableIdentifier.of("test_catalog", "db", "healthy_table")),
        tables);
    verify(icebergCatalog).loadTable(brokenIdentifier);
    verify(icebergCatalog).loadTable(healthyIdentifier);
  }

  private static class TestCatalog extends BasicMixedIcebergCatalog {
    private final Catalog icebergCatalog;
    private final MixedTables mixedTables;

    private TestCatalog(Catalog icebergCatalog, MixedTables mixedTables) {
      this.icebergCatalog = icebergCatalog;
      this.mixedTables = mixedTables;
    }

    @Override
    protected Catalog buildIcebergCatalog(
        String name, Map<String, String> properties, Configuration hadoopConf) {
      return icebergCatalog;
    }

    @Override
    protected MixedTables newMixedTables(
        TableMetaStore metaStore, Map<String, String> catalogProperties, Catalog icebergCatalog) {
      return mixedTables;
    }
  }
}
