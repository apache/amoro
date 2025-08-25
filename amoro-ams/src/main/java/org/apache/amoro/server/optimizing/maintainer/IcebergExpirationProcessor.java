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
import static org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer.AMORO_MAINTAIN_COMMITS;

import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.optimizing.scan.IcebergTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.*;
import java.util.*;

/**
 * Data expiration processor for Iceberg tables.
 *
 * <p>It supports expiring data files and delete files based on a specified expiration field and
 * level.
 *
 * <p>The expiration field requires to be a partition field if the expiration level is PARTITION.
 *
 * <p>The expiration field can be a non-partition field if the expiration level is FILE.
 *
 * <p>The expiration field can be of type Timestamp, Long (epoch millis/seconds) or String
 * (formatted date time).
 */
public class IcebergExpirationProcessor extends DataExpirationProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergExpirationProcessor.class);

  private final Table table;
  private final Types.NestedField expirationField;

  public IcebergExpirationProcessor(Table table, DataExpirationConfig expirationConfig) {
    super(
        expirationConfig,
        dataFile -> {
          if (table instanceof IcebergTable) {
            return dataFile.path();
          } else {
            return dataFile.copyWithoutStats();
          }
        });
    this.table = table;
    this.expirationField = table.schema().findField(expirationConfig.getExpirationField());
    TableConfigurations.validateExpirationField(expirationField, table.name());
  }

  @Override
  public void process(Instant expireInstant) {
    Snapshot currentSnapshot = table.currentSnapshot();
    if (currentSnapshot == null || currentSnapshot.snapshotId() == INVALID_SNAPSHOT_ID) {
      LOG.debug("Table {} has no valid snapshot, skip data expiration", table.name());
      return;
    }

    Map<String, String> snapshotSummary = currentSnapshot.summary();
    if (snapshotSummary.values().stream().anyMatch(AMORO_MAINTAIN_COMMITS::contains)) {
      LOG.debug(
          "{}'s last snapshot {} was maintained, there are no incremental changes, skip data expiration",
          table.name(),
          currentSnapshot.snapshotId());
      return;
    }

    long expireTimestamp = expireInstant.toEpochMilli();
    DataExpirationConfig.ExpireLevel level = config.getExpirationLevel();
    if (level == DataExpirationConfig.ExpireLevel.PARTITION && !table.spec().isPartitioned()) {
      throw new UnsupportedOperationException(
          "Partition-level expiration is not supported for unpartitioned table " + table.name());
    }

    try {
      if (level == DataExpirationConfig.ExpireLevel.FILE) {
        expireTableFiles(expireTimestamp, currentSnapshot);
      } else if (level == DataExpirationConfig.ExpireLevel.PARTITION) {
        expireTablePartitions(expireTimestamp, currentSnapshot);
      }
    } catch (Exception e) {
      LOG.error(
          "Failed to expire data files in table {} with level {}, before {}",
          table.name(),
          level,
          expireTimestamp,
          e);
      throw new RuntimeException(
          "Failed to expire data files in table " + table.name() + "(level=" + level + ")", e);
    }
  }

  private void expireTableFiles(long expireTimestamp, Snapshot currentSnapshot) throws IOException {
    Expression filter = buildExpirationFilter(expireTimestamp);
    IcebergTableFileScanHelper scanHelper =
        new IcebergTableFileScanHelper(table, currentSnapshot.snapshotId());
    long expiredFileCount = 0L;

    List<Object> dataFilesToExpire = Lists.newArrayList();
    List<DeleteFile> deleteFilesToExpire = Lists.newArrayList();
    try (CloseableIterable<TableFileScanHelper.FileScanResult> iterable =
        scanHelper.withPartitionFilter(filter).scan()) {
      try (CloseableIterator<TableFileScanHelper.FileScanResult> iterator = iterable.iterator()) {
        while (iterator.hasNext()) {
          TableFileScanHelper.FileScanResult result = iterator.next();
          dataFilesToExpire.add(dataFileDeleteFunc.apply(result.file()));
          List<ContentFile<?>> deleteFiles = result.deleteFiles();
          deleteFiles.forEach(d -> deleteFilesToExpire.add(((DeleteFile) d).copyWithoutStats()));
          expiredFileCount += 1 + deleteFiles.size();

          if (exceedMaxSize(expiredFileCount)) {
            break;
          }
        }
      }
    }

    commitExpiration(dataFilesToExpire, deleteFilesToExpire, "null", currentSnapshot.snapshotId());
  }

  private void expireTablePartitions(long expireTimestamp, Snapshot currentSnapshot)
      throws IOException {
    List<ManifestFile> manifestFiles = currentSnapshot.allManifests(table.io());
    if (manifestFiles.isEmpty()) {
      LOG.debug("No manifests found in table {}, skip data expiration", table.name());
      return;
    }

    ManifestsCollection manifestsCollection = buildManifestsIndex(manifestFiles, expireTimestamp);
    if (manifestsCollection.totalSize() == 0) {
      LOG.info(
          "No candidate manifests found <= {} in the table {}, skip data expiration",
          expireTimestamp,
          table.name());
      return;
    }

    Map<StructLike, List<Object>> dataFilesToExpire = Maps.newHashMap();
    Map<StructLike, List<DeleteFile>> deleteFilesToExpire = Maps.newHashMap();
    long expiredFileCount = 0L;
    collectExpiredFiles(
        table,
        manifestsCollection,
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
      String partitionString = table.spec().partitionToPath(partition);
      commitExpiration(dataFiles, deleteFiles, partitionString, currentSnapshot.snapshotId());
    }
  }

  ManifestsCollection buildManifestsIndex(List<ManifestFile> manifestFiles, long expireTimestamp) {
    ManifestsCollection manifestsCollection = new ManifestsCollection(manifestFiles.size(), config);
    // Find the partition field that matches the expiration field
    Optional<PartitionFieldInfo> partitionFieldOp =
        findFieldInSpec(table.spec(), expirationField.fieldId());

    if (partitionFieldOp.isEmpty()) {
      LOG.warn(
          "Expiration field: {} is not used for partitioning, cannot extract from manifest",
          expirationField.name());
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
        if (manifestFieldOp.isEmpty()) {
          // manifest's spec does not have the expiration field, skip it
          continue;
        }

        partitionFieldInfo = manifestFieldOp.get();
      }

      List<ManifestFile.PartitionFieldSummary> partitionSummaries = manifestFile.partitions();
      ManifestFile.PartitionFieldSummary summary = partitionSummaries.get(partitionFieldInfo.index);
      PartitionRange partitionRange =
          new PartitionRange(summary, partitionFieldInfo.field, expirationField.type(), config);
      if (partitionRange.lowerBoundGt(expireTimestamp)) {
        // this manifest's partition range is all greater than expire timestamp, skip it
        continue;
      }

      ManifestFileWrapper wrapper = new ManifestFileWrapper(manifestFile, partitionRange);
      manifestsCollection.add(wrapper, expireTimestamp);
    }

    LOG.info(
        "Found {} candidate manifests that less than or equal to expire timestamp {} in table {}",
        manifestsCollection.totalSize(),
        expireTimestamp,
        table.name());

    return manifestsCollection;
  }

  private void commitExpiration(
      List<Object> dataFiles, List<DeleteFile> deleteFiles, String partition, long snapshotId) {
    if (dataFiles.isEmpty() && deleteFiles.isEmpty()) {
      LOG.info("No files to expire in table {}", table.name());
      return;
    }

    // expire data files
    DeleteFiles delete = table.newDelete();
    dataFiles.forEach(
        d -> {
          if (d instanceof String) {
            delete.deleteFile((String) d);
          } else if (d instanceof DataFile) {
            delete.deleteFile((DataFile) d);
          }
        });
    delete.set(
        org.apache.amoro.op.SnapshotSummary.SNAPSHOT_PRODUCER,
        CommitMetaProducer.DATA_EXPIRATION.name());
    delete.commit();
    // expire delete files
    if (!deleteFiles.isEmpty()) {
      RewriteFiles rewriteFiles = table.newRewrite().validateFromSnapshot(snapshotId);
      deleteFiles.forEach(rewriteFiles::deleteFile);
      rewriteFiles.set(
          org.apache.amoro.op.SnapshotSummary.SNAPSHOT_PRODUCER,
          CommitMetaProducer.DATA_EXPIRATION.name());
      rewriteFiles.commit();
    }

    LOG.info(
        "Expired {} {} data files and {} delete files{}",
        table.name(),
        dataFiles.size(),
        deleteFiles.size(),
        partition.equals("null") ? "" : ", in the partition: " + partition);
  }

  @Override
  public Types.NestedField expirationField() {
    return expirationField;
  }
}
