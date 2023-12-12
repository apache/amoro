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

package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.server.optimizing.scan.IcebergTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.KeyedTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.UnkeyedTableFileScanHelper;
import com.netease.arctic.server.table.KeyedTableSnapshot;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableSnapshot;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class OptimizingEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingEvaluator.class);

  protected final ArcticTable arcticTable;
  protected final TableRuntime tableRuntime;
  protected final TableSnapshot currentSnapshot;
  protected boolean isInitialized = false;

  protected Map<String, PartitionEvaluator> partitionPlanMap = Maps.newHashMap();

  public OptimizingEvaluator(TableRuntime tableRuntime, ArcticTable table) {
    this.tableRuntime = tableRuntime;
    this.arcticTable = table;
    this.currentSnapshot = IcebergTableUtil.getSnapshot(table, tableRuntime);
  }

  public ArcticTable getArcticTable() {
    return arcticTable;
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  protected void initEvaluator() {
    long startTime = System.currentTimeMillis();
    TableFileScanHelper tableFileScanHelper;
    if (TableFormat.ICEBERG == arcticTable.format()) {
      tableFileScanHelper =
          new IcebergTableFileScanHelper(
              arcticTable.asUnkeyedTable(), currentSnapshot.snapshotId());
    } else {
      if (arcticTable.isUnkeyedTable()) {
        tableFileScanHelper =
            new UnkeyedTableFileScanHelper(
                arcticTable.asUnkeyedTable(), currentSnapshot.snapshotId());
      } else {
        tableFileScanHelper =
            new KeyedTableFileScanHelper(
                arcticTable.asKeyedTable(), ((KeyedTableSnapshot) currentSnapshot));
      }
    }
    tableFileScanHelper.withPartitionFilter(getPartitionFilter());
    initPartitionPlans(tableFileScanHelper);
    isInitialized = true;
    LOG.info(
        "{} finished evaluating, found {} partitions that need optimizing in {} ms",
        arcticTable.id(),
        partitionPlanMap.size(),
        System.currentTimeMillis() - startTime);
  }

  protected TableFileScanHelper.PartitionFilter getPartitionFilter() {
    return null;
  }

  private void initPartitionPlans(TableFileScanHelper tableFileScanHelper) {
    long startTime = System.currentTimeMillis();
    long count = 0;
    try (CloseableIterable<TableFileScanHelper.FileScanResult> results =
        tableFileScanHelper.scan()) {
      for (TableFileScanHelper.FileScanResult fileScanResult : results) {
        PartitionSpec partitionSpec;
        if (arcticTable.format() == TableFormat.ICEBERG) {
          partitionSpec = arcticTable.asUnkeyedTable().specs().get(fileScanResult.file().specId());
        } else {
          partitionSpec = arcticTable.spec();
        }

        StructLike partition = fileScanResult.file().partition();
        String partitionPath = partitionSpec.partitionToPath(partition);
        PartitionEvaluator evaluator =
            partitionPlanMap.computeIfAbsent(partitionPath, this::buildEvaluator);
        evaluator.addFile(fileScanResult.file(), fileScanResult.deleteFiles());
        count++;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info(
        "{} finished file scanning, scanning {} files in {} ms",
        arcticTable.id(),
        count,
        System.currentTimeMillis() - startTime);
    partitionPlanMap.values().removeIf(plan -> !plan.isNecessary());
  }

  private Map<String, String> partitionProperties(String partitionPath) {
    return TablePropertyUtil.getPartitionProperties(arcticTable, partitionPath);
  }

  protected PartitionEvaluator buildEvaluator(String partitionPath) {
    if (TableFormat.ICEBERG == arcticTable.format()) {
      return new CommonPartitionEvaluator(tableRuntime, partitionPath, System.currentTimeMillis());
    } else {
      Map<String, String> partitionProperties = partitionProperties(partitionPath);
      if (com.netease.arctic.hive.utils.TableTypeUtil.isHive(arcticTable)) {
        String hiveLocation = (((SupportHive) arcticTable).hiveLocation());
        return new MixedHivePartitionPlan.MixedHivePartitionEvaluator(
            tableRuntime,
            partitionPath,
            partitionProperties,
            hiveLocation,
            System.currentTimeMillis(),
            arcticTable.isKeyedTable());
      } else {
        return new MixedIcebergPartitionPlan.MixedIcebergPartitionEvaluator(
            tableRuntime,
            partitionPath,
            partitionProperties,
            System.currentTimeMillis(),
            arcticTable.isKeyedTable());
      }
    }
  }

  public boolean isNecessary() {
    if (!isInitialized) {
      initEvaluator();
    }
    return !partitionPlanMap.isEmpty();
  }

  public PendingInput getPendingInput() {
    if (!isInitialized) {
      initEvaluator();
    }
    return new PendingInput(partitionPlanMap.values());
  }

  public static class PendingInput {

    private final Set<String> partitions = Sets.newHashSet();

    private int dataFileCount = 0;
    private long dataFileSize = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;

    public PendingInput() {}

    public PendingInput(Collection<PartitionEvaluator> evaluators) {
      for (PartitionEvaluator evaluator : evaluators) {
        partitions.add(evaluator.getPartition());
        dataFileCount += evaluator.getFragmentFileCount() + evaluator.getSegmentFileCount();
        dataFileSize += evaluator.getFragmentFileSize() + evaluator.getSegmentFileSize();
        positionalDeleteBytes += evaluator.getPosDeleteFileSize();
        positionalDeleteFileCount += evaluator.getPosDeleteFileCount();
        equalityDeleteBytes += evaluator.getEqualityDeleteFileSize();
        equalityDeleteFileCount += evaluator.getEqualityDeleteFileCount();
      }
    }

    public Set<String> getPartitions() {
      return partitions;
    }

    public int getDataFileCount() {
      return dataFileCount;
    }

    public long getDataFileSize() {
      return dataFileSize;
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

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("partitions", partitions)
          .add("dataFileCount", dataFileCount)
          .add("dataFileSize", dataFileSize)
          .add("equalityDeleteFileCount", equalityDeleteFileCount)
          .add("positionalDeleteFileCount", positionalDeleteFileCount)
          .add("positionalDeleteBytes", positionalDeleteBytes)
          .add("equalityDeleteBytes", equalityDeleteBytes)
          .toString();
    }
  }
}
