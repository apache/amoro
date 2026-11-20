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

package org.apache.amoro.formats.iceberg.maintainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.formats.iceberg.IcebergV3MaintenanceFixture;
import org.apache.amoro.formats.iceberg.IcebergV3TestTables;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;

class TestSharedLocationOrphanCleanup {
  @TempDir private Path temp;

  @ParameterizedTest
  @ValueSource(strings = {"foreign.metadata.json", "foreign.gz.metadata.json"})
  void testForeignTableMetadataPreventsAllOrphanDeletion(String name) throws Exception {
    IcebergV3MaintenanceFixture fixture = fixture();
    Path metadata = writeForeignMetadata(fixture, name);
    Files.setLastModifiedTime(metadata, FileTime.fromMillis(1));

    assertTrue(fixture.maintainer().cleanOrphanFiles().isEmpty());

    assertPreserved(fixture);
    assertTrue(Files.exists(metadata));
  }

  @ParameterizedTest
  @ValueSource(strings = {"corrupt.metadata.json", "corrupt.metadata.json.gz"})
  void testUnreadableMetadataPreventsDeletion(String name) throws Exception {
    IcebergV3MaintenanceFixture fixture = fixture();
    Path metadata = temp.resolve("table/metadata").resolve(name);
    Files.write(metadata, Collections.singletonList("invalid metadata"));
    Files.setLastModifiedTime(metadata, FileTime.fromMillis(1));

    assertTrue(fixture.maintainer().cleanOrphanFiles().isEmpty());

    assertPreserved(fixture);
    assertTrue(Files.exists(metadata));
  }

  @Test
  void testOldMetadataWithSameUuidAllowsCleanup() throws Exception {
    IcebergV3MaintenanceFixture fixture = fixture();
    TableMetadata current = ((HasTableOperations) fixture.table).operations().current();
    Path previous = temp.resolve("table/metadata/previous.metadata.json");
    TableMetadataParser.write(current, fixture.table.io().newOutputFile(previous.toString()));
    Files.setLastModifiedTime(previous, FileTime.fromMillis(1));

    fixture.maintainer().cleanOrphanFiles();

    fixture.assertChanged();
    assertFalse(Files.exists(previous));
  }

  @Test
  void testExplicitOptOutAllowsCleanup() throws Exception {
    IcebergV3MaintenanceFixture fixture = fixture();
    writeForeignMetadata(fixture, "foreign.metadata.json");
    fixture
        .table
        .updateProperties()
        .set("clean-orphan-file.ignore-location-conflict", "true")
        .commit();

    fixture.maintainer().cleanOrphanFiles();

    assertFalse(Files.exists(temp.resolve("table/data/orphan.parquet")));
  }

  private IcebergV3MaintenanceFixture fixture() throws Exception {
    return new IcebergV3MaintenanceFixture(temp.resolve("table"), 2, "orphan");
  }

  private void assertPreserved(IcebergV3MaintenanceFixture fixture) throws Exception {
    assertTrue(Files.exists(temp.resolve("table/data/orphan.parquet")));
    assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(fixture.table));
  }

  private Path writeForeignMetadata(IcebergV3MaintenanceFixture fixture, String name) {
    TableMetadata foreign =
        TableMetadata.newTableMetadata(
            fixture.table.schema(),
            fixture.table.spec(),
            fixture.table.location(),
            Collections.emptyMap());
    Path metadata = temp.resolve("table/metadata").resolve(name);
    TableMetadataParser.write(foreign, fixture.table.io().newOutputFile(metadata.toString()));
    return metadata;
  }
}
