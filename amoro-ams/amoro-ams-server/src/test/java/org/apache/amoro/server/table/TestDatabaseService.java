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

import static org.apache.amoro.TableTestHelper.TEST_DB_NAME;
import static org.apache.amoro.catalog.CatalogTestHelper.TEST_CATALOG_NAME;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.server.exception.AlreadyExistsException;
import org.apache.amoro.server.exception.IllegalMetadataException;
import org.apache.amoro.server.exception.ObjectNotExistsException;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestDatabaseService extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        TestedCatalogs.internalCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true)
      }
    };
  }

  public TestDatabaseService(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testCreateAndDropDatabase() {
    // test create database
    tableService().createDatabase(TEST_CATALOG_NAME, TEST_DB_NAME);

    // test create duplicate database
    Assert.assertThrows(
        AlreadyExistsException.class,
        () -> tableService().createDatabase(TEST_CATALOG_NAME, TEST_DB_NAME));

    // test list database
    Assert.assertEquals(
        Lists.newArrayList(TEST_DB_NAME), tableService().listDatabases(TEST_CATALOG_NAME));

    // test drop database
    tableService().dropDatabase(TEST_CATALOG_NAME, TEST_DB_NAME);
    Assert.assertEquals(Lists.newArrayList(), tableService().listDatabases(TEST_CATALOG_NAME));

    // test drop unknown database
    Assert.assertThrows(
        ObjectNotExistsException.class,
        () -> tableService().dropDatabase(TEST_CATALOG_NAME, TEST_DB_NAME));

    // test create database in not existed catalog
    Assert.assertThrows(
        ObjectNotExistsException.class,
        () -> tableService().createDatabase("unknown", TEST_DB_NAME));

    // test drop database in not existed catalog
    Assert.assertThrows(
        ObjectNotExistsException.class, () -> tableService().dropDatabase("unknown", TEST_DB_NAME));
  }

  @Test
  public void testDropDatabaseWithTable() {
    Assume.assumeTrue(catalogTestHelper().tableFormat().equals(TableFormat.MIXED_ICEBERG));
    tableService().createDatabase(TEST_CATALOG_NAME, TEST_DB_NAME);
    createTable();
    Assert.assertThrows(
        IllegalMetadataException.class,
        () -> tableService().dropDatabase(TEST_CATALOG_NAME, TEST_DB_NAME));
    dropTable();
    tableService().dropDatabase(TEST_CATALOG_NAME, TEST_DB_NAME);
  }
}
