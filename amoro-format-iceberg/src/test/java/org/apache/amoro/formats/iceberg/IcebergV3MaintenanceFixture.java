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

package org.apache.amoro.formats.iceberg;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.config.TagConfiguration;
import org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.maintainer.MaintainerMetrics;
import org.apache.amoro.maintainer.OptimizingInfo;
import org.apache.amoro.maintainer.TableMaintainerContext;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.FileHelpers;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.Pair;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/** Real maintenance work, shared by direct action and recovered-process tests. */
public class IcebergV3MaintenanceFixture {
  public final UnkeyedTable table;
  public final TableConfiguration configuration;
  private final String action;
  private final String metadataLocation;
  private final byte[] metadataBytes;
  private final long snapshotId;
  private final Map<?, ?> refs;
  private final long snapshots;
  private final Path orphan;

  public IcebergV3MaintenanceFixture(Path location, int version, String action) throws Exception {
    this.action = action;
    Schema schema =
        new Schema(
            Types.NestedField.required(1, "id", Types.IntegerType.get()),
            Types.NestedField.required(2, "event_time", Types.LongType.get()));
    Table raw =
        new HadoopTables(new Configuration())
            .create(
                schema,
                PartitionSpec.unpartitioned(),
                Collections.singletonMap("format-version", version == 1 ? "1" : "2"),
                location.toString());
    GenericRecord record = GenericRecord.create(schema);
    IcebergDataTestHelpers.append(
        raw, Collections.singletonList(record.copy("id", 1, "event_time", 1L)));
    IcebergDataTestHelpers.append(
        raw, Collections.singletonList(record.copy("id", 2, "event_time", 2L)));

    if (action.equals("dangling")) {
      DataFile oldFile = IcebergV3TestTables.dataFiles(raw).get(0);
      DeleteFile delete =
          FileHelpers.writeDeleteFile(
                  raw,
                  OutputFileFactory.builderFor(raw, 1, 1)
                      .build()
                      .newOutputFile(oldFile.partition())
                      .encryptingOutputFile(),
                  oldFile.partition(),
                  Collections.singletonList(Pair.of(oldFile.path(), 0L)))
              .first();
      raw.newRowDelta().addDeletes(delete).commit();
      int id;
      try (org.apache.iceberg.io.CloseableIterable<org.apache.iceberg.data.Record> rows =
          org.apache.iceberg.data.IcebergGenerics.read(raw).build()) {
        id = rows.iterator().next().getField("id").equals(1) ? 2 : 1;
      }
      DataFile replacement =
          IcebergDataTestHelpers.insert(
                  raw, Collections.singletonList(record.copy("id", id, "event_time", (long) id)))
              .dataFiles()[0];
      raw.newRewrite()
          .rewriteFiles(Collections.singleton(oldFile), Collections.singleton(replacement))
          .validateFromSnapshot(raw.currentSnapshot().snapshotId())
          .commit();
      assertEquals(
          1,
          org.apache.amoro.formats.iceberg.utils.IcebergTableUtil.getDanglingDeleteFiles(raw)
              .size());
    }
    table = version == 3 ? IcebergV3TestTables.upgradeToV3(raw) : IcebergV3TestTables.wrap(raw);
    configuration =
        new TableConfiguration()
            .setExpireSnapshotEnabled(true)
            .setSnapshotTTLMinutes(0)
            .setSnapshotMinCount(1)
            .setDeleteDanglingDeleteFilesEnabled(true)
            .setCleanOrphanEnabled(true)
            .setOrphanExistingMinutes(1);
    configuration.setExpiringDataConfig(
        new DataExpirationConfig()
            .setEnabled(true)
            .setExpirationField("event_time")
            .setExpirationLevel(DataExpirationConfig.ExpireLevel.FILE)
            .setRetentionTime(1)
            .setDateTimePattern("yyyy-MM-dd HH:mm:ss")
            .setNumberDateFormat(IcebergTableMaintainer.EXPIRE_TIMESTAMP_MS)
            .setBaseOnRule(DataExpirationConfig.BaseOnRule.CURRENT_TIME));
    TagConfiguration tags = new TagConfiguration();
    tags.setAutoCreateTag(true);
    tags.setTriggerPeriod(TagConfiguration.Period.MONTHLY);
    tags.setTagFormat("'tag-'yyyy-MM");
    tags.setMaxDelayMinutes(0);
    configuration.setTagConfiguration(tags);
    orphan = location.resolve("data").resolve("orphan.parquet");
    if (action.equals("orphan")) {
      try (OutputStream output = table.io().newOutputFile(orphan.toString()).create()) {
        output.write(1);
      }
      Files.setLastModifiedTime(orphan, FileTime.fromMillis(1));
      assertTrue(Files.exists(orphan));
    }
    metadataLocation = IcebergV3TestTables.metadataLocation(table);
    metadataBytes = IcebergV3TestTables.metadataBytes(table);
    snapshotId = table.currentSnapshot().snapshotId();
    refs = new HashMap<>(table.refs());
    snapshots = snapshotCount();
    assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table));
  }

  public IcebergTableMaintainer maintainer() {
    TableMaintainerContext context = mock(TableMaintainerContext.class);
    when(context.getTableConfiguration()).thenReturn(configuration);
    when(context.getMetrics()).thenReturn(mock(MaintainerMetrics.class));
    when(context.getOptimizingInfo()).thenReturn(mock(OptimizingInfo.class));
    return new IcebergTableMaintainer(table, table.id(), context);
  }

  public void run(IcebergTableMaintainer maintainer) {
    switch (action) {
      case "snapshot":
        maintainer.expireSnapshots();
        break;
      case "dangling":
        maintainer.cleanDanglingDeleteFiles();
        break;
      case "data":
        maintainer.expireData();
        break;
      case "orphan":
        maintainer.cleanOrphanFiles();
        break;
      case "tag":
        maintainer.autoCreateTags();
        break;
      default:
        throw new IllegalArgumentException(action);
    }
  }

  public void assertPreserved() throws Exception {
    assertAll(
        () -> IcebergV3TestTables.assertV3MetadataUnchanged(table, metadataLocation, metadataBytes),
        () -> assertEquals(snapshotId, table.currentSnapshot().snapshotId()),
        () -> assertEquals(snapshots, snapshotCount()),
        () -> assertEquals(refs, table.refs()),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)),
        () -> {
          if (action.equals("orphan")) {
            assertTrue(Files.exists(orphan));
          }
        });
  }

  public void assertChanged() throws Exception {
    table.refresh();
    if (action.equals("orphan")) {
      assertFalse(Files.exists(orphan));
      assertEquals(metadataLocation, IcebergV3TestTables.metadataLocation(table));
    } else {
      assertNotEquals(metadataLocation, IcebergV3TestTables.metadataLocation(table));
    }
    switch (action) {
      case "snapshot":
        assertEquals(1, snapshotCount());
        break;
      case "dangling":
        assertEquals(
            0,
            org.apache.amoro.formats.iceberg.utils.IcebergTableUtil.getDanglingDeleteFiles(table)
                .size());
        assertEquals("0", table.currentSnapshot().summary().get("total-delete-files"));
        break;
      case "data":
        assertTrue(IcebergV3TestTables.dataFiles(table).isEmpty());
        break;
      case "tag":
        assertEquals(
            1,
            table.refs().values().stream().filter(org.apache.iceberg.SnapshotRef::isTag).count());
        break;
      default:
        break;
    }
    assertEquals(
        action.equals("data") ? Collections.emptyList() : Arrays.asList(1, 2),
        IcebergV3TestTables.readIds(table));
  }

  private long snapshotCount() {
    return StreamSupport.stream(table.snapshots().spliterator(), false).count();
  }
}
