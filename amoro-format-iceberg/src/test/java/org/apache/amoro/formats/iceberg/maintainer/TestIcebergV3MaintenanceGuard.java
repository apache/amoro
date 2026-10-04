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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.config.TagConfiguration;
import org.apache.amoro.formats.iceberg.IcebergMaintenanceCompatibility;
import org.apache.amoro.formats.iceberg.IcebergV3MaintenanceFixture;
import org.apache.amoro.formats.iceberg.IcebergV3TestTables;
import org.apache.amoro.maintainer.TableMaintainerContext;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.SnapshotRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class TestIcebergV3MaintenanceGuard {
  @TempDir private Path temp;

  @ParameterizedTest
  @ValueSource(strings = {"snapshot", "dangling", "orphan"})
  void testDisabledMaintenanceRemainsNoOp(String action) throws Exception {
    IcebergV3MaintenanceFixture fixture =
        new IcebergV3MaintenanceFixture(temp.resolve(action), 3, action);
    fixture.configuration.setExpireSnapshotEnabled(false);
    fixture.configuration.setDeleteDanglingDeleteFilesEnabled(false);
    fixture.configuration.setCleanOrphanEnabled(false);

    IcebergTableMaintainer maintainer = fixture.maintainer();
    assertAll(
        () -> {
          Map<String, String> result;
          switch (action) {
            case "snapshot":
              result = maintainer.expireSnapshots();
              break;
            case "dangling":
              result = maintainer.cleanDanglingDeleteFiles();
              break;
            case "orphan":
              result = maintainer.cleanOrphanFiles();
              break;
            default:
              throw new IllegalArgumentException(action);
          }
          assertTrue(result.isEmpty());
        },
        fixture::assertPreserved);
  }

  @Test
  void testLoadedMetadataCheckDoesNotRefreshButWriterDoes() throws Exception {
    UnkeyedTable table = spy(IcebergV3TestTables.create(temp.resolve("refresh"), 2));

    IcebergMaintenanceCompatibility.checkSupported(table);
    verify(table, never()).refresh();

    IcebergMaintenanceCompatibility.forUpdate(table);
    verify(table).refresh();
  }

  @ParameterizedTest(name = "{0} on V{1}")
  @CsvSource({
    "snapshot,3",
    "dangling,3",
    "data,3",
    "orphan,3",
    "snapshot,2",
    "dangling,2",
    "data,2",
    "orphan,2",
    "snapshot,1",
    "data,1",
    "orphan,1",
    "tag,1"
  })
  void testMaintenancePreservesUnsupportedTables(String action, int version) throws Exception {
    IcebergV3MaintenanceFixture fixture =
        new IcebergV3MaintenanceFixture(temp.resolve(action), version, action);
    IcebergTableMaintainer maintainer = fixture.maintainer();
    if (version == 3) {
      assertAll(
          () -> {
            IllegalArgumentException failure =
                assertThrows(IllegalArgumentException.class, () -> fixture.run(maintainer));
            assertTrue(failure.getMessage().contains("Unsupported Iceberg format version: 3"));
          },
          fixture::assertPreserved);
    } else {
      fixture.run(maintainer);
      fixture.assertChanged();
      if (action.equals("orphan")) {
        assertSame(fixture.table, maintainer.table, "File cleanup does not need a commit wrapper");
      }
    }
  }

  @Test
  void testV3AutoTagDoesNotChangeMetadata() throws Exception {
    UnkeyedTable table = IcebergV3TestTables.create(temp.resolve("v3"), 3);
    assertAutoTagRejected(table, maintainer(table));
  }

  private void assertAutoTagRejected(UnkeyedTable table, IcebergTableMaintainer maintainer)
      throws Exception {
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    byte[] metadataBytes = IcebergV3TestTables.metadataBytes(table);
    Map<String, SnapshotRef> refs = new HashMap<>(table.refs());
    long snapshotId = table.currentSnapshot().snapshotId();

    // assertAll also checks persisted metadata when the missing rejection assertion fails.
    assertAll(
        () -> {
          IllegalArgumentException failure =
              assertThrows(IllegalArgumentException.class, maintainer::autoCreateTags);
          assertTrue(failure.getMessage().contains("Unsupported Iceberg format version: 3"));
        },
        () -> IcebergV3TestTables.assertV3MetadataUnchanged(table, metadataLocation, metadataBytes),
        () -> assertEquals(refs, table.refs(), "Auto tag changed snapshot references"),
        () -> assertEquals(snapshotId, table.currentSnapshot().snapshotId()),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  @Test
  void testV2AutoTagCommits() throws Exception {
    UnkeyedTable table = IcebergV3TestTables.create(temp.resolve("v2"), 2);
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    long snapshotId = table.currentSnapshot().snapshotId();
    assertEquals(0, table.refs().values().stream().filter(SnapshotRef::isTag).count());

    maintainer(table).autoCreateTags();
    table.refresh();

    assertAll(
        () -> assertNotEquals(metadataLocation, IcebergV3TestTables.metadataLocation(table)),
        () -> assertEquals(1, table.refs().values().stream().filter(SnapshotRef::isTag).count()),
        () -> assertEquals(snapshotId, table.currentSnapshot().snapshotId()),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  private IcebergTableMaintainer maintainer(UnkeyedTable table) {
    TagConfiguration tags = new TagConfiguration();
    tags.setAutoCreateTag(true);
    // The snapshot was just created, so it is eligible for the current monthly boundary.
    tags.setTriggerPeriod(TagConfiguration.Period.MONTHLY);
    tags.setTagFormat("'tag-'yyyy-MM");
    tags.setMaxDelayMinutes(0);
    TableConfiguration configuration = new TableConfiguration().setTagConfiguration(tags);
    TableMaintainerContext context = mock(TableMaintainerContext.class);
    when(context.getTableConfiguration()).thenReturn(configuration);
    return new IcebergTableMaintainer(table, table.id(), context);
  }
}
