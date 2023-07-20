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

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BasicOptimizeTask;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.file.FileNameGenerator;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.util.BinPacking;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Collectors;

public class FullOptimizePlan extends AbstractArcticOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(FullOptimizePlan.class);
  // cache partition delete file size
  private final Map<String, Long> partitionDeleteFileSize = new HashMap<>();

  public FullOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                          List<FileScanTask> baseFileScanTasks, int queueId, long currentTime,
                          long baseSnapshotId) {
    super(arcticTable, tableOptimizeRuntime, Collections.emptyList(), baseFileScanTasks, queueId, currentTime,
        TableOptimizeRuntime.INVALID_SNAPSHOT_ID, baseSnapshotId);
  }

  @Override
  protected boolean partitionNeedPlan(String partitionToPath) {
    // check should split root node
    if (needSplitRootNode(partitionToPath)) {
      return true;
    }

    // check position delete file total size
    if (checkPosDeleteTotalSize(partitionToPath)) {
      return true;
    }

    // check full optimize interval
    if (checkOptimizeInterval(partitionToPath)) {
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}", tableId(), getOptimizeType(), partitionToPath);
    return false;
  }

  @Override
  protected PartitionWeight getPartitionWeight(String partition) {
    return new FullPartitionWeight(checkOptimizeInterval(partition), getPosDeleteFileSize(partition));
  }

  protected static class FullPartitionWeight implements PartitionWeight {
    private final boolean reachInterval;

    private final long deleteFileSize;

    public FullPartitionWeight(boolean reachInterval, long deleteFileSize) {
      this.reachInterval = reachInterval;
      this.deleteFileSize = deleteFileSize;
    }

    @Override
    public int compareTo(PartitionWeight o) {
      FullPartitionWeight that = (FullPartitionWeight) o;
      int compare = Boolean.compare(that.reachInterval, this.reachInterval);
      if (compare != 0) {
        return compare;
      }
      return Long.compare(that.deleteFileSize, this.deleteFileSize);
    }
  }


  @Override
  protected OptimizeType getOptimizeType() {
    return OptimizeType.FullMajor;
  }

  @Override
  protected List<BasicOptimizeTask> collectTask(String partition) {
    List<BasicOptimizeTask> result;
    if (arcticTable.isUnkeyedTable()) {
      result = collectTasksWithBinPack(partition, getOptimizingTargetSize());
    } else {
      if (canBinPackKeyedTableTasks(partition)) {
        // TO avoid too big task size leading to optimizer OOM, we limit the max task size to 4 * optimizing target size
        result = collectTasksWithBinPack(partition, getOptimizingTargetSize() * Math.min(4, getBaseBucketSize()));
      } else {
        result = collectTasksWithNodes(partition);
      }
    }

    return result;
  }

  @Override
  protected boolean baseFileShouldOptimize(DataFile baseFile, String partition) {
    return true;
  }

  protected boolean checkPosDeleteTotalSize(String partitionToPath) {
    long posDeleteSize = getPosDeleteFileSize(partitionToPath);
    if (posDeleteSize <= 0) {
      return false;
    }
    Map<String, String> properties = arcticTable.properties();
    if (!properties.containsKey(TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO) &&
        properties.containsKey(TableProperties.FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES)) {
      return posDeleteSize >=
          Long.parseLong(properties.get(TableProperties.FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES));
    } else {
      long targetSize = PropertyUtil.propertyAsLong(properties,
          TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
          TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
      double duplicateRatio = PropertyUtil.propertyAsDouble(properties,
          TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO,
          TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO_DEFAULT);
      return posDeleteSize >= targetSize * duplicateRatio;
    }
  }

  private long getPosDeleteFileSize(String partition) {
    Long cache = partitionDeleteFileSize.get(partition);
    if (cache != null) {
      return cache;
    }
    List<DeleteFile> posDeleteFiles = getPosDeleteFilesFromFileTree(partition);
    long posDeleteSize = posDeleteFiles.stream().mapToLong(DeleteFile::fileSizeInBytes).sum();
    partitionDeleteFileSize.put(partition, posDeleteSize);
    return posDeleteSize;
  }

  @Override
  protected long getMaxOptimizeInterval() {
    return CompatiblePropertyUtil.propertyAsLong(arcticTable.properties(),
        TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL,
        TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL_DEFAULT);
  }

  @Override
  protected long getLatestOptimizeTime(String partition) {
    return tableOptimizeRuntime.getLatestFullOptimizeTime(partition);
  }

  /**
   * check whether node task need to build
   *
   * @param partition     partition
   * @param posDeleteFiles pos-delete files in node
   * @param baseFiles      base files in node
   * @return whether the node task need to build. If true, build task, otherwise skip.
   */
  protected boolean nodeTaskNeedBuild(String partition, List<DeleteFile> posDeleteFiles, List<DataFile> baseFiles) {
    List<DataFile> smallFiles = baseFiles.stream().filter(file -> file.fileSizeInBytes() <=
        getSmallFileSize(arcticTable.properties())).collect(Collectors.toList());
    return CollectionUtils.isNotEmpty(posDeleteFiles) || smallFiles.size() >= 2 || needSplitRootNode(partition);
  }

  private List<BasicOptimizeTask> collectTasksWithBinPack(String partition, long taskSize) {
    List<BasicOptimizeTask> collector = new ArrayList<>();

    List<DataFile> baseFiles = getBaseFilesFromFileTree(partition);
    List<DeleteFile> posDeleteFiles = getPosDeleteFilesFromFileTree(partition);
    if (nodeTaskNeedBuild(partition, posDeleteFiles, baseFiles)) {
      String commitGroup = UUID.randomUUID().toString();
      long createTime = System.currentTimeMillis();
      TaskConfig taskPartitionConfig = new TaskConfig(getOptimizeType(), partition, commitGroup, planGroup, createTime,
          false, constructCustomHiveSubdirectory(baseFiles)
      );

      Long sum = baseFiles.stream().map(DataFile::fileSizeInBytes).reduce(0L, Long::sum);
      int taskCnt = (int) (sum / taskSize) + 1;
      List<List<DataFile>> packed = new BinPacking.ListPacker<DataFile>(taskSize, taskCnt, true)
          .pack(baseFiles, DataFile::fileSizeInBytes);
      for (List<DataFile> files : packed) {
        if (CollectionUtils.isNotEmpty(files)) {
          collector.add(buildOptimizeTask(null,
              Collections.emptyList(), Collections.emptyList(), files, posDeleteFiles, taskPartitionConfig));
        }
      }
    }

    return collector;
  }

  private long getOptimizingTargetSize() {
    return CompatiblePropertyUtil.propertyAsLong(arcticTable.properties(),
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
  }

  private int getBaseBucketSize() {
    return CompatiblePropertyUtil.propertyAsInt(arcticTable.properties(),
            TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
            TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT);
  }

  private List<BasicOptimizeTask> collectTasksWithNodes(String partition) {
    FileTree treeRoot = partitionFileTree.get(partition);
    if (treeRoot == null) {
      return Collections.emptyList();
    }
    List<BasicOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    List<DataFile> allBaseFiles = new ArrayList<>();
    treeRoot.collectBaseFiles(allBaseFiles);
    TaskConfig taskPartitionConfig = new TaskConfig(getOptimizeType(), partition, commitGroup, planGroup, createTime,
        false, constructCustomHiveSubdirectory(allBaseFiles));
    List<FileTree> subTrees = new ArrayList<>();
    // split tasks
    treeRoot.splitFileTree(subTrees, new SplitIfNoFileExists());
    for (FileTree subTree : subTrees) {
      List<DataFile> baseFiles = new ArrayList<>();
      subTree.collectBaseFiles(baseFiles);
      if (!baseFiles.isEmpty()) {
        List<DeleteFile> posDeleteFiles = new ArrayList<>();
        subTree.collectPosDeleteFiles(posDeleteFiles);
        List<DataTreeNode> sourceNodes = Collections.singletonList(subTree.getNode());

        if (nodeTaskNeedBuild(partition, posDeleteFiles, baseFiles)) {
          collector.add(buildOptimizeTask(sourceNodes,
              Collections.emptyList(), Collections.emptyList(), baseFiles, posDeleteFiles, taskPartitionConfig));
        }
      }
    }

    return collector;
  }

  /**
   * If all files in root node, but target base hash bucket > 1, we should split the root node.
   *
   * @param partitionToPath - partition
   * @return true - if it needs to split
   */
  protected boolean needSplitRootNode(String partitionToPath) {
    if (arcticTable.spec().isPartitioned()) {
      // To limit the scope of this feature and avoid optimizing a large number of historical partitions, 
      // it only applies to unpartitioned tables.
      return false;
    }
    int baseBucket = PropertyUtil.propertyAsInt(arcticTable.properties(), TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
        TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT);
    if (baseBucket <= 1) {
      return false;
    }
    FileTree fileTree = partitionFileTree.get(partitionToPath);
    if (fileTree == null) {
      return false;
    }
    return !fileTree.isRootEmpty() && fileTree.isLeaf();
  }

  /**
   * If all files in the Node(0,0) and only base files exist, binPack is supported.
   *
   * @param partition -
   * @return true if support binPack
   */
  private boolean canBinPackKeyedTableTasks(String partition) {
    FileTree treeRoot = partitionFileTree.get(partition);
    if (treeRoot == null) {
      return false;
    }
    if (!treeRoot.isLeaf()) {
      return false;
    }
    return !treeRoot.getBaseFiles().isEmpty() && treeRoot.getInsertFiles().isEmpty() &&
        treeRoot.getDeleteFiles().isEmpty() && treeRoot.getPosDeleteFiles().isEmpty();
  }

  private long getMaxTransactionId(List<DataFile> dataFiles) {
    OptionalLong maxTransactionId = dataFiles.stream()
        .mapToLong(file -> FileNameGenerator.parseTransactionId(file.path().toString())).max();
    if (maxTransactionId.isPresent()) {
      return maxTransactionId.getAsLong();
    }

    return 0;
  }

  private String constructCustomHiveSubdirectory(List<DataFile> fileList) {
    if (isCustomizeDir) {
      if (arcticTable.isKeyedTable()) {
        return HiveTableUtil.newHiveSubdirectory(getMaxTransactionId(fileList));
      } else {
        return HiveTableUtil.newHiveSubdirectory();
      }
    } else {
      return null;
    }
  }
}
