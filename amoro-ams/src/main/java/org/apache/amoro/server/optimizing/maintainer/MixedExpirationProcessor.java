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

import static org.apache.amoro.iceberg.Constants.INVALID_SNAPSHOT_ID;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.op.SnapshotSummary;
import org.apache.amoro.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Data expiration processor for mixed tables.
 *
 * <p>This processor handles data expiration for both change table and base table in MixedTable,
 * ensuring proper coordination between the two tables during expiration process.
 */
public class MixedExpirationProcessor extends DataExpirationProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(MixedExpirationProcessor.class);

  private final MixedTable mixedTable;
  private final Types.NestedField expirationField;

  public MixedExpirationProcessor(MixedTable mixedTable, DataExpirationConfig config) {
    super(config, dataFile -> dataFile);
    this.mixedTable = mixedTable;
    this.expirationField = mixedTable.schema().findField(config.getExpirationField());
    TableConfigurations.validateExpirationField(expirationField, mixedTable.name());
  }

  /**
   * Process data expiration for the mixed table. This method coordinates expiration between change
   * table and base table to ensure proper partition and file statistics.
   *
   * @param expireInstant the expiration instant
   */
  @Override
  public void process(Instant expireInstant) {
    DataExpirationConfig.ExpireLevel level = config.getExpirationLevel();
    if (mixedTable.spec().isUnpartitioned()
        && level == DataExpirationConfig.ExpireLevel.PARTITION) {
      throw new UnsupportedOperationException(
          "Partition-level expiration is not supported for unpartitioned table: "
              + mixedTable.name());
    }

    LOG.info(
        "Starting coordinated data expiration for mixed table {} before {}",
        mixedTable.name(),
        expireInstant);

    try {
      if (mixedTable.isKeyedTable()) {
        processKeyedTable(expireInstant);
      } else {
        processUnkeyedTable(expireInstant);
      }
    } catch (Exception e) {
      LOG.error(
          "Failed to complete data expiration for mixed table {} before {}",
          mixedTable.name(),
          expireInstant,
          e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Process data expiration for keyed table (with both change and base tables). This method ensures
   * coordinated expiration across both tables.
   */
  private void processKeyedTable(Instant expireInstant) throws IOException {
    KeyedTable keyedTable = mixedTable.asKeyedTable();

    long expireTimestamp = expireInstant.toEpochMilli();

    if (config.getExpirationLevel() == DataExpirationConfig.ExpireLevel.FILE) {
      expireKeyedTableFiles(keyedTable, expireTimestamp);
    } else if (config.getExpirationLevel() == DataExpirationConfig.ExpireLevel.PARTITION) {
      expireKeyedTablePartitions(keyedTable, expireTimestamp);
    }
  }

  private void expireKeyedTablePartitions(KeyedTable keyedTable, long expireTimestamp)
      throws IOException {
    ChangeTable changeTable = keyedTable.changeTable();
    BaseTable baseTable = keyedTable.baseTable();

    Snapshot changeSnapshot = changeTable.currentSnapshot();
    Snapshot baseSnapshot = baseTable.currentSnapshot();

    Map<StructLike, List<Object>> dataFilesToExpire = Maps.newHashMap();
    Map<StructLike, List<DeleteFile>> deleteFilesToExpire = Maps.newHashMap();
    long expiredFileCount = 0L;

    ManifestsCollection changeManifests = null, baseManifests = null;
    if (changeSnapshot != null && changeSnapshot.snapshotId() != INVALID_SNAPSHOT_ID) {
      changeManifests = buildManifestsIndex(changeTable, expireTimestamp);
    }
    if (baseSnapshot != null && baseSnapshot.snapshotId() != INVALID_SNAPSHOT_ID) {
      baseManifests = buildManifestsIndex(baseTable, expireTimestamp);
    }

    ManifestsCollection unionCollection =
        unionManifests(changeManifests, baseManifests, expireTimestamp);
    collectExpiredFiles(
        changeTable,
        unionCollection,
        dataFilesToExpire,
        deleteFilesToExpire,
        expireTimestamp,
        expiredFileCount);
    collectExpiredFiles(
        baseTable,
        unionCollection,
        dataFilesToExpire,
        deleteFilesToExpire,
        expireTimestamp,
        expiredFileCount);

    // expire data files and delete files partition by partition
    Set<StructLike> dataPartitions = dataFilesToExpire.keySet();
    Set<StructLike> deletePartitions = deleteFilesToExpire.keySet();
    Set<StructLike> allPartitions = Sets.union(dataPartitions, deletePartitions);
    for (StructLike partition : allPartitions) {
      List<Object> dataFiles = dataFilesToExpire.getOrDefault(partition, Collections.emptyList());
      List<DeleteFile> deleteFiles =
          deleteFilesToExpire.getOrDefault(partition, Collections.emptyList());
      String partitionString = keyedTable.spec().partitionToPath(partition);
      expireFilesFromTable(changeTable, dataFiles, deleteFiles);
      expireFilesFromTable(baseTable, dataFiles, deleteFiles);
      LOG.info(
          "Expired {} data files and {} delete files from partition {}",
          dataFiles.size(),
          deleteFiles.size(),
          partitionString);
    }
  }

  private ManifestsCollection unionManifests(
      ManifestsCollection changeManifests,
      ManifestsCollection baseManifests,
      long expireTimestamp) {
    List<ManifestFileWrapper> allManifests = Lists.newArrayList();
    if (changeManifests != null) {
      allManifests.addAll(changeManifests.fullyExpired);
      allManifests.addAll(changeManifests.partiallyExpired);
    }
    if (baseManifests != null) {
      allManifests.addAll(baseManifests.fullyExpired);
      allManifests.addAll(baseManifests.partiallyExpired);
    }

    ManifestsCollection unionCollection = new ManifestsCollection(allManifests.size(), config);
    for (ManifestFileWrapper manifest : allManifests) {
      unionCollection.add(manifest, expireTimestamp);
    }

    return unionCollection;
  }

  /** Process data expiration for unkeyed table (only base table). */
  private void processUnkeyedTable(Instant expireInstant) {
    UnkeyedTable unkeyedTable = mixedTable.asUnkeyedTable();
    Snapshot snapshot = unkeyedTable.currentSnapshot();

    if (snapshot == null || snapshot.snapshotId() == INVALID_SNAPSHOT_ID) {
      LOG.debug("Table {} has no valid snapshot, skip data expiration", unkeyedTable.name());
      return;
    }

    UnkeyedExpirationProcessor processor = new UnkeyedExpirationProcessor(unkeyedTable, config);
    processor.process(expireInstant);
  }

  /** Build coordinated expiration plan for both change and base tables. */
  private void expireKeyedTableFiles(KeyedTable keyedTable, long expireTimestamp)
      throws IOException {
    ChangeTable changeTable = keyedTable.changeTable();
    BaseTable baseTable = keyedTable.baseTable();
    // Check snapshots
    Snapshot changeSnapshot = changeTable.currentSnapshot();
    Snapshot baseSnapshot = baseTable.currentSnapshot();

    KeyedTableSnapshot keyedSnapshot =
        new KeyedTableSnapshot(
            baseSnapshot != null ? baseSnapshot.snapshotId() : INVALID_SNAPSHOT_ID,
            changeSnapshot != null ? changeSnapshot.snapshotId() : INVALID_SNAPSHOT_ID);
    Expression filter = buildExpirationFilter(expireTimestamp);

    List<DataFile> changeDataFilesToExpire = Lists.newArrayList();
    List<DeleteFile> changeDeleteFilesToExpire = Lists.newArrayList();
    List<DataFile> baseDataFilesToExpire = Lists.newArrayList();
    List<DeleteFile> baseDeleteFilesToExpire = Lists.newArrayList();

    scanKeyedTableFiles(
        keyedTable,
        keyedSnapshot,
        filter,
        changeDataFilesToExpire,
        changeDeleteFilesToExpire,
        baseDataFilesToExpire,
        baseDeleteFilesToExpire);

    expireFilesFromTable(changeTable, changeDataFilesToExpire, changeDeleteFilesToExpire);
    expireFilesFromTable(baseTable, baseDataFilesToExpire, baseDeleteFilesToExpire);
    long total =
        changeDataFilesToExpire.size()
            + changeDeleteFilesToExpire.size()
            + baseDataFilesToExpire.size()
            + baseDeleteFilesToExpire.size();
    LOG.info("Expired {} files from the keyed table {}", total, keyedTable.name());
  }

  private void scanKeyedTableFiles(
      KeyedTable table,
      KeyedTableSnapshot snapshot,
      Expression filter,
      List<DataFile> changeDataFilesToExpire,
      List<DeleteFile> changeDeleteFilesToExpire,
      List<DataFile> baseDataFilesToExpire,
      List<DeleteFile> baseDeleteFilesToExpire)
      throws IOException {
    KeyedTableFileScanHelper scanHelper = new KeyedTableFileScanHelper(table, snapshot);
    long expiredFileCount = 0L;

    try (CloseableIterable<TableFileScanHelper.FileScanResult> iterable =
        scanHelper.withPartitionFilter(filter).scan()) {
      try (CloseableIterator<TableFileScanHelper.FileScanResult> iterator = iterable.iterator()) {
        while (iterator.hasNext()) {
          TableFileScanHelper.FileScanResult result = iterator.next();
          if (result.storeType() == TableFileScanHelper.StoreType.CHANGE) {
            changeDataFilesToExpire.add(result.file());
            List<ContentFile<?>> deleteFiles = result.deleteFiles();
            deleteFiles.forEach(
                d -> changeDeleteFilesToExpire.add(((DeleteFile) d).copyWithoutStats()));
          } else {
            baseDataFilesToExpire.add(result.file());
            List<ContentFile<?>> deleteFiles = result.deleteFiles();
            deleteFiles.forEach(
                d -> baseDeleteFilesToExpire.add(((DeleteFile) d).copyWithoutStats()));
          }

          expiredFileCount += 1 + result.deleteFiles().size();
          if (exceedMaxSize(expiredFileCount)) {
            break;
          }
        }
      }
    }
  }

  @Override
  Optional<PartitionFieldInfo> findFieldInSpec(PartitionSpec spec, int sourceId) {
    if (spec == null) {
      return Optional.empty();
    }

    TableFormat format = mixedTable.format();
    if (format == TableFormat.MIXED_HIVE) {
      return findFieldInMixedHiveSpec(spec, sourceId);
    } else {
      for (int i = 0; i < spec.fields().size(); i++) {
        PartitionField f = spec.fields().get(i);
        if (f.sourceId() == sourceId) {
          return Optional.of(new PartitionFieldInfo(f, i, spec.specId()));
        }
      }
    }

    return Optional.empty();
  }

  Optional<PartitionFieldInfo> findFieldInMixedHiveSpec(PartitionSpec spec, int sourceId) {
    if (spec == null) {
      return Optional.empty();
    }

    // In Hive, partition field names may differ from source field names due to naming
    // conventions.
    // We need to match based on sourceId.
    Schema schema = spec.schema();
    for (int i = 0; i < spec.fields().size(); i++) {
      PartitionField field = spec.fields().get(i);
      String partitionFieldName = field.name();
      String sourceName = schema.findColumnName(field.sourceId());
      String originalName = schema.findColumnName(sourceId);
      if (partitionFieldName.equals(sourceName) && partitionFieldName.startsWith(originalName)) {
        return Optional.of(new PartitionFieldInfo(field, i, spec.specId()));
      }
    }

    return Optional.empty();
  }

  @Override
  Types.NestedField findOriginalField(PartitionSpec spec, int sourceId) {
    if (spec == null) {
      return null;
    }

    TableFormat format = mixedTable.format();
    Schema schema = spec.schema();
    if (format == TableFormat.MIXED_HIVE) {
      return findHiveOriginalField(spec, sourceId);
    } else {
      return schema.findField(sourceId);
    }
  }

  Types.NestedField findHiveOriginalField(PartitionSpec spec, int sourceId) {
    if (spec == null) {
      return null;
    }

    Schema schema = spec.schema();
    for (PartitionField field : spec.fields()) {
      String partitionFieldName = field.name();
      String sourceName = spec.schema().findColumnName(field.sourceId());
      String originalName = spec.schema().findColumnName(sourceId);
      if (partitionFieldName.equals(sourceName) && partitionFieldName.startsWith(originalName)) {
        return schema.findField(field.sourceId());
      }
    }

    return null;
  }

  ManifestsCollection buildManifestsIndex(UnkeyedTable table, long expireTimestamp) {
    List<ManifestFile> manifestFiles = table.currentSnapshot().allManifests(table.io());
    ManifestsCollection manifestsCollection = new ManifestsCollection(manifestFiles.size(), config);
    // Find the partition field that matches the expiration field
    Optional<PartitionFieldInfo> partitionFieldOp =
        findFieldInSpec(table.spec(), expirationField.fieldId());

    if (!partitionFieldOp.isPresent()) {
      throw new UnsupportedOperationException(
          "Expiration field " + expirationField.name() + " is not used for partitioning");
    }

    PartitionFieldInfo partitionFieldInfo = partitionFieldOp.get();

    for (ManifestFile manifestFile : manifestFiles) {
      int manifestSpecId = manifestFile.partitionSpecId();
      if (manifestSpecId != partitionFieldInfo.specId) {
        // manifest's spec is different from current table spec, try to find the field in manifest's
        // spec
        Optional<PartitionFieldInfo> manifestFieldOp =
            findFieldInSpec(table.specs().get(manifestSpecId), expirationField.fieldId());
        if (!manifestFieldOp.isPresent()) {
          // manifest's spec does not have the expiration field, skip it
          continue;
        }

        partitionFieldInfo = manifestFieldOp.get();
      }

      List<ManifestFile.PartitionFieldSummary> partitionSummaries = manifestFile.partitions();
      ManifestFile.PartitionFieldSummary summary = partitionSummaries.get(partitionFieldInfo.index);
      Type sourceType = table.schema().findType(partitionFieldInfo.field.sourceId());
      PartitionRange partitionRange =
          new PartitionRange(summary, partitionFieldInfo.field, sourceType, config);
      if (partitionRange.lowerBoundGt(expireTimestamp)) {
        // this manifest's partition range is all greater than expire timestamp, skip it
        continue;
      }

      ManifestFileWrapper wrapper = new ManifestFileWrapper(manifestFile, partitionRange);
      manifestsCollection.add(wrapper, expireTimestamp);
    }

    return manifestsCollection;
  }

  @Override
  public Types.NestedField expirationField() {
    return expirationField;
  }

  /** Expire files from a specific table. */
  private <T> void expireFilesFromTable(
      UnkeyedTable table, List<T> dataFiles, List<DeleteFile> deleteFiles) {

    if (dataFiles.isEmpty() && deleteFiles.isEmpty()) {
      return;
    }

    // Expire data files
    if (!dataFiles.isEmpty()) {
      DeleteFiles delete = table.newDelete();
      dataFiles.forEach(dataFile -> delete.deleteFile((DataFile) dataFile));
      delete.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.DATA_EXPIRATION.name());
      delete.commit();
    }

    // Expire delete files
    if (!deleteFiles.isEmpty()) {
      RewriteFiles rewriteFiles = table.newRewrite();
      deleteFiles.forEach(rewriteFiles::deleteFile);
      rewriteFiles.set(
          SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.DATA_EXPIRATION.name());
      rewriteFiles.commit();
    }
  }

  class UnkeyedExpirationProcessor extends IcebergExpirationProcessor {
    private final TableFormat tableFormat;

    public UnkeyedExpirationProcessor(UnkeyedTable table, DataExpirationConfig config) {
      super(table, config);
      this.tableFormat = table.format();
    }

    @Override
    Optional<PartitionFieldInfo> findFieldInSpec(PartitionSpec spec, int sourceId) {
      return tableFormat == TableFormat.MIXED_HIVE
          ? findFieldInMixedHiveSpec(spec, sourceId)
          : super.findFieldInSpec(spec, sourceId);
    }

    @Override
    Types.NestedField findOriginalField(PartitionSpec spec, int sourceId) {
      return tableFormat == TableFormat.MIXED_HIVE
          ? findHiveOriginalField(spec, sourceId)
          : super.findOriginalField(spec, sourceId);
    }
  }
}
