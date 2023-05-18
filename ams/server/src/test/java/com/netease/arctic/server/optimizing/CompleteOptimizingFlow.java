/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.hive.io.reader.AdaptHiveGenericArcticDataReader;
import com.netease.arctic.hive.optimizing.MixFormatRewriteExecutor;
import com.netease.arctic.io.reader.GenericIcebergDataReader;
import com.netease.arctic.optimizing.IcebergRewriteExecutor;
import com.netease.arctic.optimizing.OptimizingExecutor;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.scan.CombinedScanTask;
import com.netease.arctic.scan.KeyedTableScanTask;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ArcticDataFiles;
import com.netease.arctic.utils.TablePropertyUtil;
import com.netease.arctic.utils.map.StructLikeCollections;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.apache.iceberg.util.StructLikeMap;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;

import static com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO;
import static com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO;
import static com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_FILE_CNT;
import static com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT;
import static com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE;

public class CompleteOptimizingFlow {

  private Executor executorPool;

  private int availableCore;

  private ArcticTable table;

  private List<Checker> checkers;

  private CompleteOptimizingFlow(
      ArcticTable table,
      int availableCore,
      Long targetSize,
      Integer fragmentRatio,
      Double duplicateRatio,
      Integer minorTriggerFileCount,
      List<Checker> checkers) {
    this.executorPool = Executors.newFixedThreadPool(availableCore);
    this.table = table;
    this.availableCore = availableCore;
    this.checkers = checkers;

    UpdateProperties updateProperties = table.updateProperties();
    if (targetSize != null) {
      updateProperties.set(SELF_OPTIMIZING_TARGET_SIZE, targetSize + "");
    }
    if (fragmentRatio != null) {
      updateProperties.set(SELF_OPTIMIZING_FRAGMENT_RATIO, fragmentRatio + "");
    }
    if (duplicateRatio != null) {
      updateProperties.set(SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, duplicateRatio + "");
    }
    if (minorTriggerFileCount != null) {
      updateProperties.set(SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, minorTriggerFileCount + "");
    }
    updateProperties.commit();
  }

  public static Builder builder(ArcticTable table, int availableCore) {
    return new Builder(table, availableCore);
  }

  public void optimize() throws Exception {
    OptimizingPlanner planner = planner();
    List<TaskDescriptor> taskDescriptors = planner.planTasks();
    if (CollectionUtils.isEmpty(taskDescriptors)) {
      check(taskDescriptors);
      return;
    }
    List<TaskRuntime> taskRuntimes = mockTaskRuntime(taskDescriptors);

    asyncExecute(taskRuntimes);

    IcebergCommit committer = committer(taskRuntimes, planner.getFromSequence(),
        planner.getToSequence(), planner.getTargetSnapshotId());
    committer.commit();
    check(taskDescriptors);
  }

  private void asyncExecute(List<TaskRuntime> taskRuntimes) throws InterruptedException, ExecutionException {
    CompletableFuture.allOf(
        taskRuntimes.stream()
            .map(taskRuntime -> {
              OptimizingExecutor<RewriteFilesOutput> optimizingExecutor = optimizingExecutor(taskRuntime);
              return CompletableFuture.supplyAsync(optimizingExecutor::execute, executorPool)
                  .thenAccept(taskRuntime::setOutput);
            }).toArray(CompletableFuture[]::new)
    ).get();
  }

  private void check(List<TaskDescriptor> taskDescriptors) throws Exception {
    for (Checker checker: checkers) {
      checker.check(table, taskDescriptors);
    }
  }

  private List<TaskRuntime> mockTaskRuntime(List<TaskDescriptor> taskDescriptors) {
    List<TaskRuntime> list = new ArrayList<>();
    for (TaskDescriptor taskDescriptor: taskDescriptors) {
      TaskRuntime taskRuntime = Mockito.mock(TaskRuntime.class);
      Mockito.when(taskRuntime.getPartition()).thenReturn(taskDescriptor.getPartition());
      Mockito.when(taskRuntime.getInput()).thenReturn(taskDescriptor.getInput());
      Mockito.doCallRealMethod().when(taskRuntime).setOutput(Mockito.any());
      Mockito.doCallRealMethod().when(taskRuntime).getOutput();
      list.add(taskRuntime);
    }
    return list;
  }

  private OptimizingPlanner planner() {
    table.refresh();
    TableConfiguration tableConfiguration = TableConfiguration.parseConfig(table.properties());
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getCurrentSnapshotId()).thenReturn(currentSnapshot());
    Mockito.when(tableRuntime.getNewestProcessId()).thenReturn(1L);
    Mockito.when(tableRuntime.getPendingInput()).thenReturn(null);
    Mockito.when(tableRuntime.loadTable()).thenReturn(table);
    Mockito.when(tableRuntime.getLastMinorOptimizingTime()).thenReturn(Long.MAX_VALUE);
    Mockito.when(tableRuntime.getLastMajorOptimizingTime()).thenReturn(Long.MAX_VALUE);
    Mockito.when(tableRuntime.getLastFullOptimizingTime()).thenReturn(Long.MAX_VALUE);
    Mockito.when(tableRuntime.getOptimizingConfig()).thenReturn(tableConfiguration.getOptimizingConfig());
    Mockito.doCallRealMethod().when(tableRuntime).getCurrentSnapshot(Mockito.any(), Mockito.anyBoolean());
    return new OptimizingPlanner(tableRuntime, table,
        tableRuntime.getCurrentSnapshot(table, false), availableCore);
  }

  private OptimizingExecutor<RewriteFilesOutput> optimizingExecutor(TaskRuntime taskRuntime) {
    if (table.format() == TableFormat.ICEBERG) {
      return new IcebergRewriteExecutor(taskRuntime.getInput(), table, StructLikeCollections.DEFAULT);
    } else {
      return new MixFormatRewriteExecutor(taskRuntime.getInput(), table, StructLikeCollections.DEFAULT);
    }
  }

  private IcebergCommit committer(
      List<TaskRuntime> taskRuntimes,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence,
      Long formSnapshotId) {

    if (table.format() == TableFormat.ICEBERG) {
      return new IcebergCommit(formSnapshotId, table, taskRuntimes);
    } else {
      return new MixedIcebergCommit(
          table,
          taskRuntimes,
          formSnapshotId,
          getStructLike(fromSequence),
          getStructLike(toSequence));
    }
  }

  @Nullable
  private Long currentSnapshot() {
    Long formSnapshotId = null;
    Snapshot snapshot = null;
    if (table.format() == TableFormat.ICEBERG) {
      table.asUnkeyedTable().refresh();
      snapshot = table.asUnkeyedTable().currentSnapshot();
    } else {
      table.asKeyedTable().baseTable().refresh();
      snapshot = table.asKeyedTable().baseTable().currentSnapshot();
    }
    if (snapshot == null) {
      formSnapshotId = ArcticServiceConstants.INVALID_SNAPSHOT_ID;
    } else {
      formSnapshotId = snapshot.snapshotId();
    }
    return formSnapshotId;
  }

  private StructLikeMap<Long> getStructLike(Map<String, Long> partitionSequence) {
    PartitionSpec spec = table.spec();
    StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
    partitionSequence.forEach((partition, sequence) -> {
      if (spec.isUnpartitioned()) {
        results.put(TablePropertyUtil.EMPTY_STRUCT, sequence);
      } else {
        StructLike partitionData = ArcticDataFiles.data(spec, partition);
        results.put(partitionData, sequence);
      }
    });
    return results;
  }

  public interface Checker {
    void check(ArcticTable table, List<TaskDescriptor> taskDescriptors) throws Exception;
  }

  public static final class Builder {
    private ArcticTable table;
    private int availableCore;
    private Long targetSize;
    private Integer fragmentRatio;
    private Double duplicateRatio;
    private Integer minorTriggerFileCount;

    private List<Checker> checkers = new ArrayList<>();

    public Builder(
        ArcticTable table,
        int availableCore) {
      this.table = table;
      this.availableCore = availableCore;
    }

    public Builder setTargetSize(Long targetSize) {
      this.targetSize = targetSize;
      return this;
    }

    public Builder setFragmentRatio(Integer fragmentRatio) {
      this.fragmentRatio = fragmentRatio;
      return this;
    }

    public Builder setDuplicateRatio(Double duplicateRatio) {
      this.duplicateRatio = duplicateRatio;
      return this;
    }

    public Builder setMinorTriggerFileCount(Integer minorTriggerFileCount) {
      this.minorTriggerFileCount = minorTriggerFileCount;
      return this;
    }

    public Builder addChecker(Checker checker) {
      checkers.add(checker);
      return this;
    }

    public CompleteOptimizingFlow build() {
      return new CompleteOptimizingFlow(
          table,
          availableCore,
          targetSize,
          fragmentRatio,
          duplicateRatio,
          minorTriggerFileCount,
          checkers
      );
    }
  }
}
