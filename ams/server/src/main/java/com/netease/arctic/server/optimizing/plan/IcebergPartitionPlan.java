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

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.ams.api.properties.OptimizingTaskProperties;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.IcebergDeleteFile;
import com.netease.arctic.optimizing.IcebergRewriteExecutorFactory;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IcebergPartitionPlan extends AbstractPartitionPlan {

  //  private static final int MAJAR_FRAGEMENT_FILES_COUNT = 1000;

  private int smallFileCount = 0;

  protected IcebergPartitionPlan(TableRuntime tableRuntime, String partition, ArcticTable table, long planTime) {
    super(tableRuntime, table, partition, planTime);
  }

  @Override
  public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    if (dataFile.fileSizeInBytes() <= fragmentSize) {
      fragmentFiles.put(dataFile, deletes);
      fragmentFileSize += dataFile.fileSizeInBytes();
      smallFileCount += 1;
    } else {
      segmentFiles.put(dataFile, deletes);
      segmentFileSize += dataFile.fileSizeInBytes();
    }
    for (IcebergContentFile<?> deleteFile : deletes) {
      if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
        equalityRelatedFiles.add(dataFile);
        equalityDeleteFileMap
            .computeIfAbsent(deleteFile, delete -> Sets.newHashSet())
            .add(dataFile);
        equalityDeleteBytes += deleteFile.fileSizeInBytes();
        smallFileCount += 1;
      }
    }
  }

  @Override
  protected AbstractPartitionPlan.TaskSplitter buildTaskSplitter() {
    return new TaskSplitter();
  }

  private class TaskSplitter implements AbstractPartitionPlan.TaskSplitter {

    Set<IcebergDataFile> rewriteDataFiles = Sets.newHashSet();
    Set<IcebergContentFile<?>> deleteFiles = Sets.newHashSet();
    Set<IcebergDataFile> rewritePosDataFiles = Sets.newHashSet();

    long cost = -1;

    public TaskSplitter() {
      segmentFiles.forEach((icebergFile, deleteFileSet) -> {
        long deleteCount = deleteFileSet.stream().mapToLong(file -> file.recordCount()).sum();
        if (deleteCount >= icebergFile.recordCount() * config.getMajorDuplicateRatio()) {
          rewriteDataFiles.add(icebergFile);
          deleteFiles.addAll(deleteFileSet);
        } else if (equalityRelatedFiles.contains(icebergFile)) {
          rewritePosDataFiles.add(icebergFile);
          deleteFiles.addAll(deleteFileSet);
        } else {
          long posDeleteCount = deleteFileSet.stream()
              .filter(file -> file.content() == FileContent.POSITION_DELETES)
              .count();
          if (posDeleteCount > 1) {
            rewritePosDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        }
      });
      fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
        rewriteDataFiles.add(icebergFile);
        deleteFiles.addAll(deleteFileSet);
      });
    }

    public boolean isNecessary() {
      return smallFileCount >= config.getMinorLeastFileCount() ||
          rewritePosDataFiles.size() > 0 && deleteFiles.size() > 0 &&
              System.currentTimeMillis() - tableRuntime.getLastMinorOptimizingTime() > config.getMinorLeastInterval();
    }

    public long getCost() {
      if (cost < 0) {
        cost = rewriteDataFiles.stream().mapToLong(file -> file.fileSizeInBytes()).sum() * 4 +
            rewritePosDataFiles.stream().mapToLong(file -> file.fileSizeInBytes()).sum() / 10 +
            deleteFiles.stream().mapToLong(file -> file.fileSizeInBytes()).sum();
      }
      return cost;
    }

    public List<TaskDescriptor> splitTasks(int targetTaskCount) {
      RewriteFilesInput input = new RewriteFilesInput(
          rewriteDataFiles.toArray(new IcebergDataFile[rewriteDataFiles.size()]),
          rewritePosDataFiles.toArray(new IcebergDataFile[rewritePosDataFiles.size()]),
          deleteFiles.toArray(new IcebergDeleteFile[deleteFiles.size()]),
          tableObject.asUnkeyedTable());
      List<TaskDescriptor> tasks = Lists.newArrayList();
      Map<String, String> taskProperties = Maps.newHashMap();
      taskProperties.put(
          OptimizingTaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
          IcebergRewriteExecutorFactory.class.getName());
      tasks.add(new TaskDescriptor(partition, input, taskProperties));
      return tasks;
    }

    //TODO
    public OptimizingType getOptimizingType() {
      return OptimizingType.MAJOR;
    }
  }
}
