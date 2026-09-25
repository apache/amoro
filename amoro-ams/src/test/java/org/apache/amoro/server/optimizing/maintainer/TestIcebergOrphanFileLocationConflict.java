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

package org.apache.amoro.server.optimizing.maintainer;

import static org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer.DATA_FOLDER_NAME;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer;
import org.apache.amoro.formats.iceberg.utils.IcebergTableUtil;
import org.apache.amoro.server.scheduler.inline.ExecutorTestBase;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.FileIO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@RunWith(Parameterized.class)
public class TestIcebergOrphanFileLocationConflict extends ExecutorTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)}
    };
  }

  public TestIcebergOrphanFileLocationConflict(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  private UnkeyedTable baseTable() {
    return getMixedTable().asUnkeyedTable();
  }

  /** No conflict: a freshly committed Iceberg table is the only one in its metadata location. */
  @Test
  public void testHasOtherTableInLocationNoConflict() {
    baseTable().newAppend().commit();
    Assert.assertFalse(IcebergTableUtil.hasOtherTableInLocation(baseTable()));
  }

  /**
   * Fail-safe conflict: a corrupt/legacy metadata json (unreadable uuid) in the metadata directory
   * is treated as a conflict and returns {@code true}.
   */
  @Test
  public void testHasOtherTableInLocationWithCorruptMetadata() throws IOException {
    baseTable().newAppend().commit();
    String corruptMeta =
        baseTable().location() + File.separator + "metadata" + File.separator + "v0.metadata.json";
    baseTable().io().newOutputFile(corruptMeta).createOrOverwrite().close();
    Assert.assertTrue(baseTable().io().exists(corruptMeta));
    Assert.assertTrue(IcebergTableUtil.hasOtherTableInLocation(baseTable()));
  }

  /**
   * When the FileIO does not support prefix operations, the conflict detection throws a {@link
   * ValidationException} instead of silently proceeding.
   */
  @Test
  public void testHasOtherTableInLocationThrowsWhenFileIoLacksPrefixSupport() {
    Table table =
        Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(HasTableOperations.class));
    TableOperations ops = Mockito.mock(TableOperations.class);
    TableMetadata current = Mockito.mock(TableMetadata.class);
    FileIO io = Mockito.mock(FileIO.class);

    Mockito.when(((HasTableOperations) table).operations()).thenReturn(ops);
    Mockito.when(ops.current()).thenReturn(current);
    Mockito.when(current.uuid()).thenReturn("my-uuid");
    Mockito.when(current.metadataFileLocation()).thenReturn("/tmp/meta/metadata/v1.metadata.json");
    Mockito.when(current.previousFiles()).thenReturn(Collections.emptyList());
    Mockito.when(table.io()).thenReturn(io);

    try {
      IcebergTableUtil.hasOtherTableInLocation(table);
      Assert.fail("Expected ValidationException because the FileIO lacks prefix support");
    } catch (ValidationException e) {
      // expected
    }
  }

  /**
   * Default behavior: when a location conflict is detected, {@code cleanOrphanFiles} skips cleanup
   * so it does not risk deleting another table's files.
   */
  @Test
  public void testLocationConflictSkipsCleanupByDefault() throws IOException {
    baseTable().newAppend().commit();
    UnkeyedTable baseTable = baseTable();

    String orphanDir =
        baseTable.location() + File.separator + DATA_FOLDER_NAME + File.separator + "testLocation";
    String orphanFile = orphanDir + File.separator + "orphan.parquet";
    baseTable.io().newOutputFile(orphanFile).createOrOverwrite().close();
    Assert.assertTrue(baseTable.io().exists(orphanFile));

    // Simulate a location conflict: a corrupt metadata json in the metadata directory.
    String corruptMeta =
        baseTable.location() + File.separator + "metadata" + File.separator + "v0.metadata.json";
    baseTable.io().newOutputFile(corruptMeta).createOrOverwrite().close();
    Assert.assertTrue(IcebergTableUtil.hasOtherTableInLocation(baseTable));

    baseTable
        .updateProperties()
        .set(TableProperties.ENABLE_ORPHAN_CLEAN, "true")
        .set(TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME, "0")
        .commit();

    new IcebergTableMaintainer(baseTable, baseTable.id(), TestTableMaintainerContext.of(baseTable))
        .cleanOrphanFiles();

    // Conflict detected -> cleanup skipped, orphan file must remain.
    Assert.assertTrue(baseTable.io().exists(orphanFile));
  }

  /**
   * When {@code clean-orphan-file.ignore-location-conflict=true}, the conflict is ignored and
   * orphan files are cleaned up even though another table appears to share the location.
   */
  @Test
  public void testLocationConflictIgnoredWhenPropertyEnabled() throws IOException {
    baseTable().newAppend().commit();
    UnkeyedTable baseTable = baseTable();

    String orphanDir =
        baseTable.location() + File.separator + DATA_FOLDER_NAME + File.separator + "testLocation";
    String orphanFile = orphanDir + File.separator + "orphan.parquet";
    baseTable.io().newOutputFile(orphanFile).createOrOverwrite().close();
    Assert.assertTrue(baseTable.io().exists(orphanFile));

    // Simulate a location conflict: a corrupt metadata json in the metadata directory.
    String corruptMeta =
        baseTable.location() + File.separator + "metadata" + File.separator + "v0.metadata.json";
    baseTable.io().newOutputFile(corruptMeta).createOrOverwrite().close();
    Assert.assertTrue(IcebergTableUtil.hasOtherTableInLocation(baseTable));

    baseTable
        .updateProperties()
        .set(TableProperties.ENABLE_ORPHAN_CLEAN, "true")
        .set(TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME, "0")
        .set(TableProperties.IGNORE_LOCATION_CONFLICT_WHEN_CLEAN_ORPHAN, "true")
        .commit();

    new IcebergTableMaintainer(baseTable, baseTable.id(), TestTableMaintainerContext.of(baseTable))
        .cleanOrphanFiles();

    // Conflict ignored -> cleanup proceeds, orphan file must be deleted.
    Assert.assertFalse(baseTable.io().exists(orphanFile));
  }
}
