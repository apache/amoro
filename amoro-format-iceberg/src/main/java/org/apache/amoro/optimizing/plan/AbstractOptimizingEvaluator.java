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

package org.apache.amoro.optimizing.plan;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.scan.IcebergTableFileScanHelper;
import org.apache.amoro.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.optimizing.scan.UnkeyedTableFileScanHelper;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.amoro.table.KeyedTableSnapshot;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableSnapshot;
import org.apache.amoro.utils.ExpressionUtil;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.Pair;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractOptimizingEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractOptimizingEvaluator.class);

  protected final ServerTableIdentifier identifier;
  protected final OptimizingConfig config;
  protected final MixedTable mixedTable;
  protected final TableSnapshot currentSnapshot;
  protected final long lastFullOptimizingTime;
  protected final long lastMinorOptimizingTime;
  protected final long lastMajorOptimizingTime;
  protected final int maxPendingPartitions;
  protected boolean isInitialized = false;
  protected Map<String, PartitionEvaluator> needOptimizingPlanMap = Maps.newHashMap();
  protected Map<String, PartitionEvaluator> partitionPlanMap = Maps.newHashMap();

  public AbstractOptimizingEvaluator(
      ServerTableIdentifier identifier,
      OptimizingConfig config,
      MixedTable table,
      TableSnapshot currentSnapshot,
      int maxPendingPartitions,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime,
      long lastMajorOptimizingTime) {
    this.identifier = identifier;
    this.config = config;
    this.mixedTable = table;
    this.currentSnapshot = currentSnapshot;
    this.maxPendingPartitions = maxPendingPartitions;
    this.lastFullOptimizingTime = lastFullOptimizingTime;
    this.lastMinorOptimizingTime = lastMinorOptimizingTime;
    this.lastMajorOptimizingTime = lastMajorOptimizingTime;
  }

  protected void initEvaluator() {
    long startTime = System.currentTimeMillis();
    TableFileScanHelper tableFileScanHelper;
    if (TableFormat.ICEBERG.equals(mixedTable.format())) {
      tableFileScanHelper =
          new IcebergTableFileScanHelper(mixedTable.asUnkeyedTable(), currentSnapshot.snapshotId());
    } else {
      if (mixedTable.isUnkeyedTable()) {
        tableFileScanHelper =
            new UnkeyedTableFileScanHelper(
                mixedTable.asUnkeyedTable(), currentSnapshot.snapshotId());
      } else {
        tableFileScanHelper =
            new KeyedTableFileScanHelper(
                mixedTable.asKeyedTable(), ((KeyedTableSnapshot) currentSnapshot));
      }
    }
    tableFileScanHelper.withPartitionFilter(getPartitionFilter());
    initPartitionPlans(tableFileScanHelper);
    isInitialized = true;
    LOG.info(
        "{} finished evaluating, found {} partitions that need optimizing in {} ms",
        mixedTable.id(),
        needOptimizingPlanMap.size(),
        System.currentTimeMillis() - startTime);
  }

  protected Expression getPartitionFilter() {
    return ExpressionUtil.convertSqlFilterToIcebergExpression(
        config.getFilter(), mixedTable.schema().columns());
  }

  private void initPartitionPlans(TableFileScanHelper tableFileScanHelper) {
    long startTime = System.currentTimeMillis();
    long count = 0;
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      for (TableFileScanHelper.FileScanResult fileScanResult : results) {
        PartitionSpec partitionSpec = tableFileScanHelper.getSpec(fileScanResult.file().specId());
        StructLike partition = fileScanResult.file().partition();
        String partitionPath = partitionSpec.partitionToPath(partition);
        PartitionEvaluator evaluator =
            partitionPlanMap.computeIfAbsent(
                partitionPath,
                ignore -> buildEvaluator(Pair.of(partitionSpec.specId(), partition)));
        evaluator.addFile(fileScanResult.file(), fileScanResult.deleteFiles());
        count++;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info(
        "{} finished file scanning, scanning {} files in {} ms",
        mixedTable.id(),
        count,
        System.currentTimeMillis() - startTime);
    needOptimizingPlanMap.putAll(
        partitionPlanMap.entrySet().stream()
            .filter(entry -> entry.getValue().isNecessary())
            .limit(maxPendingPartitions)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  protected abstract PartitionEvaluator buildEvaluator(Pair<Integer, StructLike> partition);

  public boolean isNecessary() {
    if (!isInitialized) {
      initEvaluator();
    }
    return !needOptimizingPlanMap.isEmpty();
  }

  public PendingInput getPendingInput() {
    if (!isInitialized) {
      initEvaluator();
    }
    // Dangling delete files will cause the data scanned by TableScan
    // to be inconsistent with the snapshot summary of iceberg
    if (TableFormat.ICEBERG == mixedTable.format()) {
      Snapshot snapshot = mixedTable.asUnkeyedTable().snapshot(currentSnapshot.snapshotId());
      PendingInput pendingInput = new PendingInput(partitionPlanMap.values(), snapshot);
      pendingInput.buildPartitionPaths(mixedTable);
      return pendingInput;
    }

    PendingInput pendingInput = new PendingInput(partitionPlanMap.values());
    pendingInput.buildPartitionPaths(mixedTable);
    return pendingInput;
  }

  public PendingInput getOptimizingPendingInput() {
    if (!isInitialized) {
      initEvaluator();
    }
    PendingInput pendingInput = new PendingInput(needOptimizingPlanMap.values());
    pendingInput.buildPartitionPaths(mixedTable);
    return pendingInput;
  }

  public static class PendingInput {

    @JsonIgnore private final Map<Integer, Set<StructLike>> partitions = Maps.newHashMap();
    private Set<String> partitionPaths = Sets.newHashSet();

    private int totalFileCount = 0;
    private long totalFileSize = 0L;
    private long totalFileRecords = 0L;
    private int dataFileCount = 0;
    private long dataFileSize = 0L;
    private long dataFileRecords = 0L;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;
    private long equalityDeleteFileRecords = 0L;
    private long positionalDeleteFileRecords = 0L;
    private int danglingDeleteFileCount = 0;
    private int healthScore = -1; // -1 means not calculated
    private int smallFileScore = 0; // 0 means not calculated
    private int equalityDeleteScore = 0; // 0 means not calculated
    private int positionalDeleteScore = 0; // 0 means not calculated

    public PendingInput() {}

    public PendingInput(Collection<PartitionEvaluator> evaluators) {
      initialize(evaluators);
      totalFileCount = dataFileCount + positionalDeleteFileCount + equalityDeleteFileCount;
      totalFileSize = dataFileSize + positionalDeleteBytes + equalityDeleteBytes;
      totalFileRecords = dataFileRecords + positionalDeleteFileRecords + equalityDeleteFileRecords;
    }

    public PendingInput(Collection<PartitionEvaluator> evaluators, Snapshot snapshot) {
      initialize(evaluators);
      Map<String, String> summary = snapshot.summary();
      int totalDeleteFiles =
          PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DELETE_FILES_PROP, 0);
      int totalDataFiles =
          PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
      totalFileRecords =
          PropertyUtil.propertyAsLong(summary, SnapshotSummary.TOTAL_RECORDS_PROP, 0);
      totalFileSize = PropertyUtil.propertyAsLong(summary, SnapshotSummary.TOTAL_FILE_SIZE_PROP, 0);
      totalFileCount = totalDeleteFiles + totalDataFiles;
      danglingDeleteFileCount =
          totalDeleteFiles - equalityDeleteFileCount - positionalDeleteFileCount;
    }

    private void initialize(Collection<PartitionEvaluator> evaluators) {
      double totalSmallFileScore = 0;
      double totalEqualityDeleteScore = 0;
      double totalPositionalDeleteScore = 0;
      int defaultSmallFilesWithEqDeleteScore = 40;
      int defaultPosDeleteScore = 20;

      for (PartitionEvaluator evaluator : evaluators) {
        addPartitionData(evaluator);
        totalSmallFileScore += evaluator.getHealthScore().getSmallFileScore();
        totalEqualityDeleteScore += evaluator.getHealthScore().getEqualityDeleteScore();
        totalPositionalDeleteScore += evaluator.getHealthScore().getPositionalDeleteScore();
      }
      smallFileScore =
          avgHealthScore(
              totalSmallFileScore, evaluators.size(), defaultSmallFilesWithEqDeleteScore);
      equalityDeleteScore =
          avgHealthScore(
              totalEqualityDeleteScore, evaluators.size(), defaultSmallFilesWithEqDeleteScore);
      positionalDeleteScore =
          avgHealthScore(totalPositionalDeleteScore, evaluators.size(), defaultPosDeleteScore);
      healthScore = smallFileScore + equalityDeleteScore + positionalDeleteScore;
    }

    private void addPartitionData(PartitionEvaluator evaluator) {
      partitions
          .computeIfAbsent(evaluator.getPartition().first(), ignore -> Sets.newHashSet())
          .add(evaluator.getPartition().second());
      dataFileCount += evaluator.getFragmentFileCount() + evaluator.getSegmentFileCount();
      dataFileSize += evaluator.getFragmentFileSize() + evaluator.getSegmentFileSize();
      dataFileRecords += evaluator.getFragmentFileRecords() + evaluator.getSegmentFileRecords();
      positionalDeleteBytes += evaluator.getPosDeleteFileSize();
      positionalDeleteFileRecords += evaluator.getPosDeleteFileRecords();
      positionalDeleteFileCount += evaluator.getPosDeleteFileCount();
      equalityDeleteBytes += evaluator.getEqualityDeleteFileSize();
      equalityDeleteFileRecords += evaluator.getEqualityDeleteFileRecords();
      equalityDeleteFileCount += evaluator.getEqualityDeleteFileCount();
    }

    private int avgHealthScore(double totalHealthScore, int partitionCount, int defaultScore) {
      if (partitionCount == 0) {
        return defaultScore;
      }
      return (int) Math.ceil(totalHealthScore / partitionCount);
    }

    public Map<Integer, Set<StructLike>> getPartitions() {
      return partitions;
    }

    /**
     * Get serializable partition paths for JSON persistence. Each entry is in the format
     * "specId:partitionPath", e.g. "1:dt=2024-01-01/hour=00".
     */
    @JsonProperty("partitionPaths")
    public Set<String> getPartitionPaths() {
      return partitionPaths;
    }

    @JsonProperty("partitionPaths")
    public void setPartitionPaths(Set<String> partitionPaths) {
      this.partitionPaths = partitionPaths != null ? partitionPaths : Sets.newHashSet();
    }

    /**
     * Rebuild the in-memory partitions map from serializable partition paths. Must be called before
     * {@link #getPartitions()} when PendingInput is deserialized from JSON.
     *
     * @param table the table to resolve partition specs
     */
    public void rebuildPartitions(MixedTable table) {
      if (!partitions.isEmpty() || partitionPaths.isEmpty()) {
        return;
      }
      for (String path : partitionPaths) {
        try {
          int colonIdx = path.indexOf(':');
          if (colonIdx <= 0) {
            continue;
          }
          int specId = Integer.parseInt(path.substring(0, colonIdx));
          String partitionPath = path.substring(colonIdx + 1);
          PartitionSpec spec = MixedTableUtil.getMixedTablePartitionSpecById(table, specId);
          if (spec != null) {
            StructLike struct;
            if (spec.isUnpartitioned() || partitionPath.isEmpty()) {
              // For unpartitioned tables, use an empty record since there's no path to parse
              struct = TablePropertyUtil.EMPTY_STRUCT;
            } else {
              struct = MixedDataFiles.data(spec, partitionPath);
            }
            partitions.computeIfAbsent(specId, k -> Sets.newHashSet()).add(struct);
          }
        } catch (Exception e) {
          LOG.warn("Failed to rebuild partition from path: {}", path, e);
        }
      }
    }

    /** Build serializable partition paths from in-memory partitions. Called before persisting. */
    public void buildPartitionPaths(MixedTable table) {
      if (!partitionPaths.isEmpty()) {
        return;
      }
      for (Map.Entry<Integer, Set<StructLike>> entry : partitions.entrySet()) {
        int specId = entry.getKey();
        PartitionSpec spec = MixedTableUtil.getMixedTablePartitionSpecById(table, specId);
        if (spec == null) {
          continue;
        }
        for (StructLike struct : entry.getValue()) {
          partitionPaths.add(specId + ":" + spec.partitionToPath(struct));
        }
      }
    }

    public int getDataFileCount() {
      return dataFileCount;
    }

    public long getDataFileSize() {
      return dataFileSize;
    }

    public long getDataFileRecords() {
      return dataFileRecords;
    }

    public int getEqualityDeleteFileCount() {
      return equalityDeleteFileCount;
    }

    public int getPositionalDeleteFileCount() {
      return positionalDeleteFileCount;
    }

    public long getPositionalDeleteBytes() {
      return positionalDeleteBytes;
    }

    public long getEqualityDeleteBytes() {
      return equalityDeleteBytes;
    }

    public long getEqualityDeleteFileRecords() {
      return equalityDeleteFileRecords;
    }

    public long getPositionalDeleteFileRecords() {
      return positionalDeleteFileRecords;
    }

    public int getHealthScore() {
      return healthScore;
    }

    public int getSmallFileScore() {
      return smallFileScore;
    }

    public int getEqualityDeleteScore() {
      return equalityDeleteScore;
    }

    public int getPositionalDeleteScore() {
      return positionalDeleteScore;
    }

    public int getTotalFileCount() {
      return totalFileCount;
    }

    public long getTotalFileSize() {
      return totalFileSize;
    }

    public long getTotalFileRecords() {
      return totalFileRecords;
    }

    public int getDanglingDeleteFileCount() {
      return danglingDeleteFileCount;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("totalFileCount", totalFileCount)
          .add("totalFileSize", totalFileSize)
          .add("totalFileRecords", totalFileRecords)
          .add("partitions", partitions)
          .add("dataFileCount", dataFileCount)
          .add("dataFileSize", dataFileSize)
          .add("dataFileRecords", dataFileRecords)
          .add("equalityDeleteFileCount", equalityDeleteFileCount)
          .add("positionalDeleteFileCount", positionalDeleteFileCount)
          .add("positionalDeleteBytes", positionalDeleteBytes)
          .add("equalityDeleteBytes", equalityDeleteBytes)
          .add("equalityDeleteFileRecords", equalityDeleteFileRecords)
          .add("positionalDeleteFileRecords", positionalDeleteFileRecords)
          .add("healthScore", healthScore)
          .add("smallFileScore", smallFileScore)
          .add("equalityDeleteScore", equalityDeleteScore)
          .add("positionalDeleteScore", positionalDeleteScore)
          .add("danglingDeleteFileCount", danglingDeleteFileCount)
          .toString();
    }
  }
}
