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

package org.apache.amoro.server.optimizing.flow;

import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO;
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO;
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT;
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.hive.optimizing.MixedHiveRewriteExecutor;
import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.optimizing.IcebergRewriteExecutor;
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.server.optimizing.KeyedTableCommit;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.optimizing.UnKeyedTableCommit;
import org.apache.amoro.server.table.DefaultOptimizingState;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.util.StructLikeMap;
import org.mockito.Mockito;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CompleteOptimizingFlow {

  private final Executor executorPool;

  private final int availableCore;

  private final MixedTable table;

  private final List<Checker> checkers;

  private CompleteOptimizingFlow(
      MixedTable table,
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

  public static Builder builder(MixedTable table, int availableCore) {
    return new Builder(table, availableCore);
  }

  public void optimize() throws Exception {
    AbstractOptimizingPlanner planner = planner();
    List<RewriteStageTask> taskDescriptors = planner.planTasks();
    if (CollectionUtils.isEmpty(taskDescriptors)) {
      check(taskDescriptors, planner, null);
      return;
    }
    List<TaskRuntime<RewriteStageTask>> taskRuntimes = mockTaskRuntime(taskDescriptors);

    asyncExecute(taskRuntimes);

    UnKeyedTableCommit committer =
        committer(
            taskRuntimes,
            planner.getFromSequence(),
            planner.getToSequence(),
            planner.getTargetSnapshotId());
    committer.commit();
    check(taskDescriptors, planner, null);
  }

  public List<Checker> unTriggerChecker() {
    return checkers.stream().filter(s -> !s.senseHasChecked()).collect(Collectors.toList());
  }

  private void asyncExecute(List<TaskRuntime<RewriteStageTask>> taskRuntimes)
      throws InterruptedException, ExecutionException {
    CompletableFuture.allOf(
            taskRuntimes.stream()
                .map(
                    taskRuntime -> {
                      OptimizingExecutor<RewriteFilesOutput> optimizingExecutor =
                          optimizingExecutor(taskRuntime);
                      return CompletableFuture.supplyAsync(
                              optimizingExecutor::execute, executorPool)
                          .thenAccept(
                              s ->
                                  Mockito.when(taskRuntime.getTaskDescriptor().getOutput())
                                      .thenReturn(s));
                    })
                .toArray(CompletableFuture[]::new))
        .get();
  }

  private void check(
      List<RewriteStageTask> taskDescriptors,
      AbstractOptimizingPlanner planner,
      UnKeyedTableCommit commit)
      throws Exception {
    for (Checker checker : checkers) {
      if (checker.condition(table, taskDescriptors, planner, commit)) {
        checker.check(table, taskDescriptors, planner, commit);
      }
    }
  }

  private List<TaskRuntime<RewriteStageTask>> mockTaskRuntime(
      List<RewriteStageTask> taskDescriptors) {
    List<TaskRuntime<RewriteStageTask>> list = new ArrayList<>();
    for (RewriteStageTask taskDescriptor : taskDescriptors) {
      TaskRuntime<RewriteStageTask> taskRuntime = Mockito.mock(TaskRuntime.class);
      RewriteStageTask task = Mockito.mock(RewriteStageTask.class);
      Mockito.when(taskRuntime.getTaskDescriptor()).thenReturn(task);
      Mockito.when(taskRuntime.getTaskDescriptor().getPartition())
          .thenReturn(taskDescriptor.getPartition());
      Mockito.when(taskRuntime.getTaskDescriptor().getInput())
          .thenReturn(taskDescriptor.getInput());
      Mockito.when(taskRuntime.getProperties()).thenReturn(taskDescriptor.getProperties());
      list.add(taskRuntime);
    }
    return list;
  }

  private AbstractOptimizingPlanner planner() {
    table.refresh();
    DefaultTableRuntime tableRuntime = Mockito.mock(DefaultTableRuntime.class);
    DefaultOptimizingState optimizingState = Mockito.mock(DefaultOptimizingState.class);
    Mockito.when(tableRuntime.getOptimizingState()).thenReturn(optimizingState);
    Mockito.when(optimizingState.getCurrentSnapshotId()).thenAnswer(f -> getCurrentSnapshotId());
    Mockito.when(optimizingState.getCurrentChangeSnapshotId())
        .thenAnswer(f -> getCurrentChangeSnapshotId());
    Mockito.when(optimizingState.getNewestProcessId()).thenReturn(1L);
    Mockito.when(optimizingState.getPendingInput()).thenReturn(null);
    Mockito.doCallRealMethod().when(optimizingState).getLastMinorOptimizingTime();
    Mockito.doCallRealMethod().when(optimizingState).getLastMajorOptimizingTime();
    Mockito.doCallRealMethod().when(optimizingState).getLastFullOptimizingTime();
    Mockito.when(optimizingState.getOptimizingConfig()).thenAnswer(f -> optimizingConfig());
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of(1L, "a", "b", "c", table.format()));
    Mockito.when(optimizingState.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of(1L, "a", "b", "c", table.format()));
    return IcebergTableUtil.createOptimizingPlanner(
        tableRuntime,
        table,
        availableCore,
        OptimizerProperties.MAX_INPUT_FILE_SIZE_PER_THREAD_DEFAULT);
  }

  private OptimizingConfig optimizingConfig() {
    TableConfiguration tableConfiguration =
        TableConfigurations.parseTableConfig(table.properties());
    return tableConfiguration.getOptimizingConfig();
  }

  private OptimizingExecutor<RewriteFilesOutput> optimizingExecutor(
      TaskRuntime<RewriteStageTask> taskRuntime) {
    if (table.format() == TableFormat.ICEBERG) {
      return new IcebergRewriteExecutor(
          taskRuntime.getTaskDescriptor().getInput(), table, StructLikeCollections.DEFAULT);
    } else {
      return new MixedHiveRewriteExecutor(
          taskRuntime.getTaskDescriptor().getInput(),
          table,
          StructLikeCollections.DEFAULT,
          OptimizingInputProperties.parse(taskRuntime.getProperties()).getOutputDir());
    }
  }

  private UnKeyedTableCommit committer(
      List<TaskRuntime<RewriteStageTask>> taskRuntimes,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence,
      Long formSnapshotId) {

    if (table.isUnkeyedTable()) {
      return new UnKeyedTableCommit(formSnapshotId, table, taskRuntimes);
    } else {
      return new KeyedTableCommit(
          table,
          taskRuntimes,
          formSnapshotId,
          getStructLike(fromSequence),
          getStructLike(toSequence));
    }
  }

  private long getCurrentSnapshotId() {
    if (table.isKeyedTable()) {
      return IcebergTableUtil.getSnapshotId(table.asKeyedTable().baseTable(), false);
    } else {
      return IcebergTableUtil.getSnapshotId(table.asUnkeyedTable(), false);
    }
  }

  private long getCurrentChangeSnapshotId() {
    if (table.isKeyedTable()) {
      return IcebergTableUtil.getSnapshotId(table.asKeyedTable().changeTable(), false);
    } else {
      return Constants.INVALID_SNAPSHOT_ID;
    }
  }

  private StructLikeMap<Long> getStructLike(Map<String, Long> partitionSequence) {
    PartitionSpec spec = table.spec();
    StructLikeMap<Long> results = StructLikeMap.create(spec.partitionType());
    partitionSequence.forEach(
        (partition, sequence) -> {
          if (spec.isUnpartitioned()) {
            results.put(TablePropertyUtil.EMPTY_STRUCT, sequence);
          } else {
            StructLike partitionData = MixedDataFiles.data(spec, partition);
            results.put(partitionData, sequence);
          }
        });
    return results;
  }

  public interface Checker {

    boolean condition(
        MixedTable table,
        @Nullable List<RewriteStageTask> latestTaskDescriptors,
        AbstractOptimizingPlanner latestPlanner,
        @Nullable UnKeyedTableCommit latestCommit);

    boolean senseHasChecked();

    void check(
        MixedTable table,
        @Nullable List<RewriteStageTask> latestTaskDescriptors,
        AbstractOptimizingPlanner latestPlanner,
        @Nullable UnKeyedTableCommit latestCommit)
        throws Exception;
  }

  public static final class Builder {
    private final MixedTable table;
    private final int availableCore;
    private Long targetSize;
    private Integer fragmentRatio;
    private Double duplicateRatio;
    private Integer minorTriggerFileCount;

    private final List<Checker> checkers = new ArrayList<>();

    public Builder(MixedTable table, int availableCore) {
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
          checkers);
    }
  }
}
