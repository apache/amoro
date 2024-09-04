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

package org.apache.amoro.formats.hudi;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hudi.common.model.FileSlice;
import org.apache.hudi.common.model.HoodieCommitMetadata;
import org.apache.hudi.common.model.HoodieWriteStat;
import org.apache.hudi.common.model.WriteOperationType;
import org.apache.hudi.common.table.timeline.HoodieActiveTimeline;
import org.apache.hudi.common.table.timeline.HoodieInstant;
import org.apache.hudi.common.table.timeline.HoodieInstantTimeGenerator;
import org.apache.hudi.common.table.timeline.HoodieTimeline;
import org.apache.hudi.common.table.view.SyncableFileSystemView;
import org.apache.hudi.common.util.Option;
import org.apache.hudi.metadata.HoodieTableMetadata;
import org.apache.hudi.table.HoodieJavaTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HudiTable implements AmoroTable<HoodieJavaTable> {
  private static final Logger LOG = LoggerFactory.getLogger(HudiTable.class);
  private final TableIdentifier identifier;
  private final HoodieJavaTable hoodieTable;
  private final Map<String, String> tableProperties;
  private transient List<String> partitions;

  private transient Map<String, HudiSnapshot> snapshots;

  public HudiTable(
      TableIdentifier identifier,
      HoodieJavaTable hoodieTable,
      Map<String, String> tableProperties) {
    this.identifier = identifier;
    this.hoodieTable = hoodieTable;
    this.tableProperties =
        tableProperties == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(tableProperties);
  }

  @Override
  public TableIdentifier id() {
    return identifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.HUDI;
  }

  @Override
  public Map<String, String> properties() {
    return tableProperties;
  }

  @Override
  public HoodieJavaTable originalTable() {
    return hoodieTable;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    throw new IllegalStateException("The method is not implement.");
  }

  public List<HudiSnapshot> getSnapshotList(Executor ioExecutors) {
    return new ArrayList<>(getSnapshots(ioExecutors).values());
  }

  public synchronized List<String> getPartitions() {
    ensurePartitionLoaded();
    return new ArrayList<>(this.partitions);
  }

  private synchronized Map<String, HudiSnapshot> getSnapshots(Executor ioExecutors) {
    if (snapshots == null) {
      ensurePartitionLoaded();
      this.snapshots = constructSnapshots(ioExecutors);
    }
    return snapshots;
  }

  private static final Set<String> OPTIMIZING_INSTANT_TYPES =
      Sets.newHashSet(
          HoodieTimeline.CLEAN_ACTION,
          HoodieTimeline.COMPACTION_ACTION,
          HoodieTimeline.REPLACE_COMMIT_ACTION,
          HoodieTimeline.INDEXING_ACTION,
          HoodieTimeline.LOG_COMPACTION_ACTION);
  private static final Set<String> WRITE_INSTANT_TYPES =
      Sets.newHashSet(
          HoodieTimeline.COMMIT_ACTION,
          HoodieTimeline.DELTA_COMMIT_ACTION,
          HoodieTimeline.REPLACE_COMMIT_ACTION,
          HoodieTimeline.SAVEPOINT_ACTION,
          HoodieTimeline.ROLLBACK_ACTION);

  private static final Set<String> OPTIMIZING_HOODIE_OPERATION =
      Sets.newHashSet(
          WriteOperationType.CLUSTER.value(),
          WriteOperationType.COMPACT.value(),
          WriteOperationType.LOG_COMPACT.value());

  private Map<String, HudiSnapshot> constructSnapshots(Executor ioExecutors) {
    HoodieActiveTimeline timeline = hoodieTable.getActiveTimeline();
    List<HoodieInstant> instants = timeline.filterCompletedInstants().getInstants();

    return instants.stream()
        .map(i -> CompletableFuture.supplyAsync(() -> constructSnapshot(i, timeline), ioExecutors))
        .map(CompletableFuture::join)
        .collect(Collectors.toMap(HudiSnapshot::getSnapshotId, Function.identity()));
  }

  private HudiSnapshot constructSnapshot(HoodieInstant instant, HoodieTimeline timeline) {
    String snapshotId = instant.getTimestamp();
    long timestamp = parseHoodieCommitTime(instant.getTimestamp());
    String hoodieOperationType = null;
    Map<String, String> summary = Collections.emptyMap();
    Option<byte[]> optDetail = timeline.getInstantDetails(instant);
    if (optDetail.isPresent()) {
      byte[] detail = optDetail.get();
      try {
        HoodieCommitMetadata metadata =
            HoodieCommitMetadata.fromBytes(detail, HoodieCommitMetadata.class);
        hoodieOperationType = metadata.getOperationType().value();
        summary = getSnapshotSummary(metadata);
      } catch (IOException e) {
        LOG.error("Error when fetch hoodie instant metadata", e);
      }
    }
    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    AtomicInteger totalFileCount = new AtomicInteger(0);
    AtomicInteger baseFileCount = new AtomicInteger(0);
    AtomicInteger logFileCount = new AtomicInteger(0);
    AtomicLong totalFileSize = new AtomicLong(0);
    for (String partition : this.partitions) {
      Stream<FileSlice> fsStream =
          fileSystemView.getLatestMergedFileSlicesBeforeOrOn(partition, instant.getTimestamp());
      fsStream.forEach(
          fs -> {
            int fsBaseCount = fs.getBaseFile().map(f -> 1).orElse(0);
            int fsLogFileCount = (int) fs.getLogFiles().count();

            baseFileCount.addAndGet(fsBaseCount);
            logFileCount.addAndGet(fsLogFileCount);
            totalFileCount.addAndGet(fsBaseCount);
            totalFileCount.addAndGet(fsLogFileCount);
            totalFileSize.addAndGet(fs.getTotalFileSize());
          });
    }

    String operation = instant.getAction().toUpperCase();
    if (hoodieOperationType != null) {
      operation = hoodieOperationType;
    }
    String operationType = null;
    if (WRITE_INSTANT_TYPES.contains(instant.getAction())) {
      operationType = "NON_OPTIMIZING";
    } else if (OPTIMIZING_INSTANT_TYPES.contains(instant.getAction())) {
      operationType = "OPTIMIZING";
    }

    if (OPTIMIZING_HOODIE_OPERATION.contains(hoodieOperationType)) {
      operationType = "OPTIMIZING";
    }

    return new HudiSnapshot(
        snapshotId,
        timestamp,
        operationType,
        operation,
        totalFileCount.get(),
        baseFileCount.get(),
        logFileCount.get(),
        totalFileSize.get(),
        0L,
        summary);
  }

  private Map<String, String> getSnapshotSummary(HoodieCommitMetadata metadata) {
    Map<String, String> summary = new HashMap<>();
    long totalWriteBytes = 0;
    long recordWrites = 0;
    long recordDeletes = 0;
    long recordInserts = 0;
    long recordUpdates = 0;
    Set<String> partitions = new HashSet<>();
    Set<String> files = new HashSet<>();
    Map<String, List<HoodieWriteStat>> hoodieWriteStats = metadata.getPartitionToWriteStats();
    for (String partition : hoodieWriteStats.keySet()) {
      List<HoodieWriteStat> ptWriteStat = hoodieWriteStats.get(partition);
      partitions.add(partition);
      for (HoodieWriteStat writeStat : ptWriteStat) {
        totalWriteBytes += writeStat.getTotalWriteBytes();
        recordWrites += writeStat.getNumWrites();
        recordDeletes += writeStat.getNumDeletes();
        recordInserts += writeStat.getNumInserts();
        recordUpdates += writeStat.getNumUpdateWrites();
        files.add(writeStat.getPath());
      }
    }
    summary.put("write-bytes", String.valueOf(totalWriteBytes));
    summary.put("write-records", String.valueOf(recordWrites));
    summary.put("delete-records", String.valueOf(recordDeletes));
    summary.put("insert-records", String.valueOf(recordInserts));
    summary.put("update-records", String.valueOf(recordUpdates));
    summary.put("write-partitions", String.valueOf(partitions.size()));
    summary.put("write-files", String.valueOf(files.size()));
    return summary;
  }

  private long parseHoodieCommitTime(String commitTime) {
    try {
      Date date = HoodieInstantTimeGenerator.parseDateFromInstantTime(commitTime);
      return date.getTime();
    } catch (ParseException e) {
      throw new RuntimeException("Error when parse timestamp:" + commitTime, e);
    }
  }

  private List<String> ensurePartitionLoaded() {
    if (partitions == null) {
      HoodieTableMetadata hoodieTableMetadata = hoodieTable.getMetadata();
      try {
        this.partitions = hoodieTableMetadata.getAllPartitionPaths();
      } catch (IOException e) {
        throw new RuntimeException("Error when load partitions for table: " + id(), e);
      }
    }
    return partitions;
  }
}
