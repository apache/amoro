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

package org.apache.amoro.io;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestFileIOCaching extends TableTestBase {

  private TableMetaStore tableMetaStore;
  private MixedTable mixedTable;

  public TestFileIOCaching() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true));
  }

  @Before
  public void before() {
    // Clear the cache before each test to ensure tests are isolated
    AuthenticatedFileIOs.clearCache();

    tableMetaStore = getTableMetaStore();
    mixedTable = getMixedTable();
  }

  @Test
  public void testBasicCaching() {
    // Get FileIO instances from the same TableMetaStore - these should be the same instance
    AuthenticatedHadoopFileIO fileIO1 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);
    AuthenticatedHadoopFileIO fileIO2 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);

    // Verify they are the same instance
    Assert.assertSame(
        "FileIO instances should be the same for the same TableMetaStore", fileIO1, fileIO2);
  }

  @Test
  public void testDifferentTableMetaStoresDifferentInstances() {
    // Create another empty TableMetaStore instance for testing different TableMetaStore instances
    TableMetaStore otherMetaStore = TableMetaStore.EMPTY;

    // Get FileIO instances from different TableMetaStores
    AuthenticatedHadoopFileIO fileIO1 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);
    AuthenticatedHadoopFileIO fileIO2 = AuthenticatedFileIOs.buildHadoopFileIO(otherMetaStore);

    // Verify they are different instances
    Assert.assertNotSame(
        "FileIO instances should be different for different TableMetaStores", fileIO1, fileIO2);
  }

  @Test
  public void testRecoverableFileIOCaching() {
    // Set up table properties for FileIO
    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(TableProperties.ENABLE_TABLE_TRASH, "true");

    // Create two FileIO instances for the same table
    TableIdentifier tableId = mixedTable.id();
    String location = mixedTable.location();

    // Get the first FileIO instance
    AuthenticatedHadoopFileIO fileIO1 =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableId, location, tableProperties, tableMetaStore, new HashMap<>());

    // Get the second FileIO instance with exactly the same parameters
    AuthenticatedHadoopFileIO fileIO2 =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableId, location, tableProperties, tableMetaStore, new HashMap<>());

    // Verify they are the same instance, regardless of the specific implementation type
    Assert.assertSame(
        "FileIO instances should be the same for the same table and TableMetaStore",
        fileIO1,
        fileIO2);

    // Test with different table ID but same meta store
    TableIdentifier otherTableId = TableIdentifier.of("test_catalog", "different", "table");

    // Get a FileIO instance for a different table
    AuthenticatedHadoopFileIO fileIO3 =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            otherTableId, location, tableProperties, tableMetaStore, new HashMap<>());

    // For cache key testing, get another instance with the same different table
    AuthenticatedHadoopFileIO fileIO4 =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            otherTableId, location, tableProperties, tableMetaStore, new HashMap<>());

    // Verify different table with same parameters also returns cached instances
    Assert.assertSame(
        "FileIO instances should be the same for the same table ID", fileIO3, fileIO4);
  }

  @Test
  public void testBuildAdaptIcebergFileIO() {
    // Get the basic FileIO instance
    AuthenticatedHadoopFileIO basicFileIO = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);

    // Now use it to create an adapted instance
    AuthenticatedFileIO adaptedFileIO =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(tableMetaStore, basicFileIO);

    // Verify that if we pass an AuthenticatedFileIO, we get back the same instance
    Assert.assertSame(
        "Should return the same instance when already an AuthenticatedFileIO",
        basicFileIO,
        adaptedFileIO);
  }
}
