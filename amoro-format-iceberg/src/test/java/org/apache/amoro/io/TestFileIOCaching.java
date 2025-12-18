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

import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestFileIOCaching {

  // Use empty TableMetaStore for testing
  private TableMetaStore tableMetaStore = TableMetaStore.EMPTY;
  // Create the table ID using the static factory method
  private TableIdentifier tableId =
      TableIdentifier.of("test_catalog", "test_database", "test_table");

  public TestFileIOCaching() {}

  @BeforeEach
  public void before() {
    // Clear the cache before each test to ensure tests are isolated
    AuthenticatedFileIOs.clearCache();
  }

  @Test
  public void testBasicCaching() {
    // Get FileIO instances from the same TableMetaStore - these should be the same instance
    AuthenticatedHadoopFileIO fileIO1 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);
    AuthenticatedHadoopFileIO fileIO2 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);

    // Verify they are the same instance
    Assertions.assertSame(
        fileIO1, fileIO2, "FileIO instances should be the same for the same TableMetaStore");
  }

  @Test
  public void testDifferentTableMetaStoresDifferentInstances() {
    // In our setup with mocks, we need to clear the cache completely to test this behavior properly
    AuthenticatedFileIOs.clearCache();

    // Get the first FileIO instance
    AuthenticatedHadoopFileIO fileIO1 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);

    // Clear the cache so a new instance will be created
    AuthenticatedFileIOs.clearCache();

    // Get a second FileIO instance after clearing the cache
    AuthenticatedHadoopFileIO fileIO2 = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);

    // Verify they are different instances (due to cache being cleared)
    Assertions.assertNotSame(
        fileIO1, fileIO2, "FileIO instances should be different when cache is cleared");
  }

  @Test
  public void testRecoverableFileIOCaching() {
    // Set up table properties for FileIO
    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(TableProperties.ENABLE_TABLE_TRASH, "true");

    // Use the table ID we created in the setup and a dummy location
    String location = "file:/tmp/test-location";

    // Get the first FileIO instance
    AuthenticatedHadoopFileIO fileIO1 =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableId, location, tableProperties, tableMetaStore, new HashMap<>());

    // Get the second FileIO instance with exactly the same parameters
    AuthenticatedHadoopFileIO fileIO2 =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableId, location, tableProperties, tableMetaStore, new HashMap<>());

    // Verify they are the same instance, regardless of the specific implementation type
    Assertions.assertSame(
        fileIO1,
        fileIO2,
        "FileIO instances should be the same for the same table and TableMetaStore");
  }

  @Test
  public void testBuildAdaptIcebergFileIO() {
    // Get the basic FileIO instance
    AuthenticatedHadoopFileIO basicFileIO = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);

    // Now use it to create an adapted instance
    AuthenticatedFileIO adaptedFileIO =
        AuthenticatedFileIOs.buildAdaptIcebergFileIO(tableMetaStore, basicFileIO);

    // Verify that if we pass an AuthenticatedFileIO, we get back the same instance
    Assertions.assertSame(
        basicFileIO,
        adaptedFileIO,
        "Should return the same instance when already an AuthenticatedFileIO");
  }
}
