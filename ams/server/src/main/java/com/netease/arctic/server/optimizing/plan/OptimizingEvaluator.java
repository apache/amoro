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

import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.optimizing.scan.IcebergTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.MixedFormatTableFileScanHelper;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.SequenceNumberFetcher;
import com.netease.arctic.utils.TablePropertyUtil;
import com.netease.arctic.utils.TableTypeUtil;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.StructLikeMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptimizingEvaluator {

  protected final ArcticTable arcticTable;
  protected final TableRuntime tableRuntime;
  protected boolean isInitEvaluator = false;

  protected Map<String, PartitionEvaluator> partitionEvaluatorMap = Maps.newHashMap();

  public OptimizingEvaluator(TableRuntime tableRuntime) {
    this(tableRuntime, tableRuntime.loadTable());
  }

  public OptimizingEvaluator(TableRuntime tableRuntime, ArcticTable table) {
    this.tableRuntime = tableRuntime;
    this.arcticTable = table;
  }

  public ArcticTable getArcticTable() {
    return arcticTable;
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  protected void initEvaluator() {
    TableFileScanHelper tableFileScanHelper;
    if (TableTypeUtil.isIcebergTableFormat(arcticTable)) {
      long currentSnapshotId = tableRuntime.getCurrentSnapshotId();
      tableFileScanHelper = new IcebergTableFileScanHelper(arcticTable.asUnkeyedTable(),
          new SequenceNumberFetcher(arcticTable.asUnkeyedTable(), currentSnapshotId), currentSnapshotId);
    } else {
      // TODO refresh snapshotId
      if (arcticTable.isUnkeyedTable()) {
        long baseSnapshotId = IcebergTableUtil.getSnapshotId(arcticTable.asUnkeyedTable(), true);
        tableFileScanHelper =
            new MixedFormatTableFileScanHelper(arcticTable, baseSnapshotId, ArcticServiceConstants.INVALID_SNAPSHOT_ID,
                null, null);
      } else {
        long baseSnapshotId = IcebergTableUtil.getSnapshotId(arcticTable.asKeyedTable().baseTable(), true);
        StructLikeMap<Long> partitionOptimizedSequence =
            TablePropertyUtil.getPartitionOptimizedSequence(arcticTable.asKeyedTable());
        StructLikeMap<Long> legacyPartitionMaxTransactionId =
            TablePropertyUtil.getLegacyPartitionMaxTransactionId(arcticTable.asKeyedTable());
        long changeSnapshotId = IcebergTableUtil.getSnapshotId(arcticTable.asKeyedTable().changeTable(), true);
        tableFileScanHelper = new MixedFormatTableFileScanHelper(arcticTable, baseSnapshotId, changeSnapshotId,
            partitionOptimizedSequence, legacyPartitionMaxTransactionId);
      }
    }
    tableFileScanHelper.withPartitionFilter(getPartitionFilter());
    initPartitionPlans(tableFileScanHelper);
    isInitEvaluator = true;
  }

  protected TableFileScanHelper.PartitionFilter getPartitionFilter() {
    return null;
  }

  private void initPartitionPlans(TableFileScanHelper tableFileScanHelper) {
    PartitionSpec partitionSpec = arcticTable.spec();
    for (TableFileScanHelper.FileScanResult fileScanResult : tableFileScanHelper.scan()) {
      StructLike partition = fileScanResult.file().partition();
      String partitionPath = partitionSpec.partitionToPath(partition);
      PartitionEvaluator evaluator = partitionEvaluatorMap.computeIfAbsent(partitionPath, this::buildEvaluator);
      evaluator.addFile(fileScanResult.file(), fileScanResult.deleteFiles());
    }
    partitionEvaluatorMap.values().removeIf(plan -> !plan.isNecessary());
  }

  protected PartitionEvaluator buildEvaluator(String partitionPath) {
    return new PartitionEvaluatorImpl(tableRuntime, arcticTable, partitionPath);
  }

  public boolean isNecessary() {
    if (!isInitEvaluator) {
      initEvaluator();
    }
    return !partitionEvaluatorMap.isEmpty();
  }

  public PendingInput getPendingInput() {
    if (!isInitEvaluator) {
      initEvaluator();
    }
    return new PendingInput(partitionEvaluatorMap.values());
  }

  public static class PendingInput {

    private final Set<String> partitions = Sets.newHashSet();

    private int dataFileCount = 0;
    private long dataFileSize = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;

    public PendingInput(Collection<PartitionEvaluator> evaluators) {
      for (PartitionEvaluator e : evaluators) {
        PartitionEvaluatorImpl evaluator = (PartitionEvaluatorImpl) e;
        partitions.add(evaluator.getPartition());
        dataFileCount += evaluator.fragementFileCount + evaluator.segmentFileCount;
        dataFileSize += evaluator.fragementFileSize + evaluator.segmentFileSize;
        positionalDeleteBytes += evaluator.positionalDeleteBytes;
        positionalDeleteFileCount += evaluator.positionalDeleteFileCount;
        equalityDeleteBytes += evaluator.equalityDeleteBytes;
        equalityDeleteFileCount += evaluator.equalityDeleteFileCount;
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
  }

  private class PartitionEvaluatorImpl extends PartitionEvaluator {

    private final OptimizingConfig config;
    private final long fragmentSize;

    private int fragementFileCount = 0;
    private long fragementFileSize = 0;
    private int segmentFileCount = 0;
    private long segmentFileSize = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;
    private long rewriteSegmentFileSize = 0L;

    public PartitionEvaluatorImpl(TableRuntime tableRuntime, ArcticTable arcticTable,
                                  String partition) {
      super(partition);
      this.config = tableRuntime.getOptimizingConfig();
      this.fragmentSize = config.getTargetSize() / config.getFragmentRatio();
    }

    @Override
    public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
      boolean isSegment = false;
      int posDeleteCount = 0;
      if (dataFile.fileSizeInBytes() <= fragmentSize) {
        fragementFileSize += dataFile.fileSizeInBytes();
        fragementFileCount += 1;
      } else {
        segmentFileSize += dataFile.fileSizeInBytes();
        segmentFileCount += 1;
        isSegment = true;
      }

      for (IcebergContentFile<?> delete : deletes) {
        if (delete.content() == FileContent.DATA) {
          equalityDeleteFileCount += 1;
          equalityDeleteBytes += delete.fileSizeInBytes();
        } else if (delete.content() == FileContent.EQUALITY_DELETES) {
          equalityDeleteFileCount += 1;
          equalityDeleteBytes += delete.fileSizeInBytes();
        } else {
          if (++posDeleteCount > 1 || isSegment &&
              delete.recordCount() >= dataFile.recordCount() * config.getMajorDuplicateRatio()) {
            rewriteSegmentFileSize += dataFile.fileSizeInBytes();
          }
          posDeleteCount += 1;
          positionalDeleteBytes += delete.fileSizeInBytes();
        }
      }
    }

    @Override
    public boolean isNecessary() {
      return isMajorNecessary() || isMinorNecessary();
    }

    @Override
    public long getCost() {
      throw new UnsupportedOperationException();
    }

    @Override
    public OptimizingType getOptimizingType() {
      return isMajorNecessary() ? OptimizingType.MAJOR : OptimizingType.MINOR;
    }

    private boolean isMajorNecessary() {
      return rewriteSegmentFileSize > 0;
    }

    private boolean isMinorNecessary() {
      int sourceFileCount = fragementFileCount + equalityDeleteFileCount;
      return sourceFileCount >= config.getMinorLeastFileCount() ||
          (sourceFileCount > 1 &&
              System.currentTimeMillis() - tableRuntime.getLastMinorOptimizingTime() > config.getMinorLeastInterval());
    }
  }

}
