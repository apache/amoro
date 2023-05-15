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
import com.netease.arctic.optimizing.IcebergRewriteExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;

public class IcebergPartitionPlan extends AbstractPartitionPlan {

  protected IcebergPartitionPlan(TableRuntime tableRuntime, String partition, ArcticTable table, long planTime) {
    super(tableRuntime, table, partition, planTime, new DefaultPartitionEvaluator(tableRuntime, partition));
  }

  @Override
  public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    evaluator.addFile(dataFile, deletes);
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

  @Override
  protected boolean partitionShouldFullOptimizing() {
    // TODO
    return false;
  }

  @Override
  protected OptimizingInputProperties buildTaskProperties() {
    OptimizingInputProperties properties = new OptimizingInputProperties();
    properties.setExecutorFactoryImpl(IcebergRewriteExecutorFactory.class.getName());
    return properties;
  }

  @Override
  protected boolean shouldFullOptimizing(IcebergDataFile icebergFile, List<IcebergContentFile<?>> deleteFiles) {
    return false;
  }

  private class TaskSplitter extends AbstractPartitionPlan.TaskSplitter {
    @Override
    public List<SplitTask> splitTasks(int targetTaskCount) {
      return Collections.singletonList(new SplitTask(fragmentFiles, segmentFiles));
    }
  }
}
