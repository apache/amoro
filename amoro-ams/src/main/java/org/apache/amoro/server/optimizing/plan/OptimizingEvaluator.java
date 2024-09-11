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

package org.apache.amoro.server.optimizing.plan;

import org.apache.amoro.TableFormat;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.server.optimizing.scan.IcebergTableFileScanHelper;
import org.apache.amoro.server.optimizing.scan.KeyedTableFileScanHelper;
import org.apache.amoro.server.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.server.optimizing.scan.UnkeyedTableFileScanHelper;
import org.apache.amoro.server.table.KeyedTableSnapshot;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableSnapshot;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OptimizingEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingEvaluator.class);

  protected final MixedTable mixedTable;
  protected final TableRuntime tableRuntime;
  protected final TableSnapshot currentSnapshot;
  protected boolean isInitialized = false;

  protected Map<String, PartitionEvaluator> needOptimizingPlanMap = Maps.newHashMap();
  protected Map<String, PartitionEvaluator> partitionPlanMap = Maps.newHashMap();

  public OptimizingEvaluator(TableRuntime tableRuntime, MixedTable table) {
    this.tableRuntime = tableRuntime;
    this.mixedTable = table;
    this.currentSnapshot = IcebergTableUtil.getSnapshot(table, tableRuntime);
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  protected void initEvaluator() {
    long startTime = System.currentTimeMillis();
    TableFileScanHelper tableFileScanHelper;
    if (TableFormat.ICEBERG == mixedTable.format()) {
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
    return Expressions.alwaysTrue();
  }

  private void initPartitionPlans(TableFileScanHelper tableFileScanHelper) {
    long startTime = System.currentTimeMillis();
    long count = 0;
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      for (TableFileScanHelper.FileScanResult fileScanResult : results) {
        PartitionSpec partitionSpec =
            MixedTableUtil.getMixedTablePartitionSpecById(
                mixedTable, fileScanResult.file().specId());
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
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
  }

  private Map<String, String> partitionProperties(Pair<Integer, StructLike> partition) {
    return TablePropertyUtil.getPartitionProperties(mixedTable, partition.second());
  }

  protected PartitionEvaluator buildEvaluator(Pair<Integer, StructLike> partition) {
    if (TableFormat.ICEBERG == mixedTable.format()) {
      return new CommonPartitionEvaluator(tableRuntime, partition, System.currentTimeMillis());
    } else {
      Map<String, String> partitionProperties = partitionProperties(partition);
      if (TableTypeUtil.isHive(mixedTable)) {
        String hiveLocation = (((SupportHive) mixedTable).hiveLocation());
        return new MixedHivePartitionPlan.MixedHivePartitionEvaluator(
            tableRuntime,
            partition,
            partitionProperties,
            hiveLocation,
            System.currentTimeMillis(),
            mixedTable.isKeyedTable());
      } else {
        return new MixedIcebergPartitionPlan.MixedIcebergPartitionEvaluator(
            tableRuntime,
            partition,
            partitionProperties,
            System.currentTimeMillis(),
            mixedTable.isKeyedTable());
      }
    }
  }

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
    return new PendingInput(partitionPlanMap.values());
  }

  public PendingInput getOptimizingPendingInput() {
    if (!isInitialized) {
      initEvaluator();
    }
    return new PendingInput(needOptimizingPlanMap.values());
  }

  public static class PendingInput {

    @JsonIgnore private final Map<Integer, Set<StructLike>> partitions = Maps.newHashMap();

    private int dataFileCount = 0;
    private long dataFileSize = 0;
    private long dataFileRecords = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;
    private long equalityDeleteFileRecords = 0L;
    private long positionalDeleteFileRecords = 0L;
    private int healthScore = -1; // -1 means not calculated

    public PendingInput() {}

    public PendingInput(Collection<PartitionEvaluator> evaluators) {
      double totalHealthScore = 0;
      for (PartitionEvaluator evaluator : evaluators) {
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
        totalHealthScore += evaluator.getHealthScore();
      }
      healthScore = (int) Math.ceil(totalHealthScore / evaluators.size());
    }

    public Map<Integer, Set<StructLike>> getPartitions() {
      return partitions;
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

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
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
          .toString();
    }
  }
}
