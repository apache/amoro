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

package org.apache.amoro.server.table;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.formats.paimon.PaimonHadoopCatalogTestHelper;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.TableCatalogTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestSyncUnsupportedTableFormat extends TableCatalogTestBase {

  private static final String TEST_DATABASE = "test_database";
  private static final String TEST_TABLE = "test_table";

  public TestSyncUnsupportedTableFormat() {
    super(PaimonHadoopCatalogTestHelper.defaultHelper());
  }

  @Before
  public void createPaimonTable() throws Exception {
    getAmoroCatalog().createDatabase(TEST_DATABASE);
    getAmoroCatalogTestHelper().createTable(TEST_DATABASE, TEST_TABLE);
  }

  @Test
  public void unsupportedTableFormatDoesNotCreateRuntimeDuringRepeatedSynchronization() {
    ExternalCatalog externalCatalog =
        (ExternalCatalog)
            CATALOG_MANAGER.getServerCatalog(getAmoroCatalogTestHelper().catalogName());

    tableService().exploreExternalCatalog(externalCatalog);
    tableService().exploreExternalCatalog(externalCatalog);

    List<ServerTableIdentifier> managedTables = tableManager().listManagedTables();
    Assert.assertEquals(1, managedTables.size());
    Assert.assertNull(tableManager().getTableRuntimeMata(managedTables.get(0)));
  }
}
