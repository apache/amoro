package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NativeMajorOptimizePlan extends BaseNativeOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(NativeMajorOptimizePlan.class);

  public NativeMajorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                 Map<DataFile, List<DeleteFile>> dataDeleteFileMap,
                                 Map<String, Boolean> partitionTaskRunning,
                                 int queueId, long currentTime) {
    super(arcticTable, tableOptimizeRuntime, dataDeleteFileMap, partitionTaskRunning, queueId, currentTime);
  }

  @Override
  protected boolean partitionNeedPlan(String partitionToPath) {
    FileTree partitionTree = partitionFileTree.get(partitionToPath);
    List<DataFile> partitionDataFiles = new ArrayList<>();
    partitionTree.collectBaseFiles(partitionDataFiles);
    if (CollectionUtils.isEmpty(partitionDataFiles)) {
      LOG.debug("{} ==== don't need {} optimize plan, skip partition {}, there are no data files",
          tableId(), getOptimizeType(), partitionToPath);
      return false;
    }

    Set<DeleteFile> partitionDeleteFiles = new HashSet<>();
    for (List<DeleteFile> deleteFiles : dataDeleteFileMap.values()) {
      partitionDeleteFiles.addAll(deleteFiles);
    }

    double deleteFilesTotalSize = partitionDeleteFiles.stream().mapToLong(DeleteFile::fileSizeInBytes).sum();
    double dataFilesTotalSize = partitionDataFiles.stream().mapToLong(DataFile::fileSizeInBytes).sum();

    long duplicateSize = PropertyUtil.propertyAsLong(arcticTable.properties(),
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD,
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD_DEFAULT);
    double duplicateRatio = PropertyUtil.propertyAsDouble(arcticTable.properties(),
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_RATIO_THRESHOLD,
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_RATIO_THRESHOLD_DEFAULT);
    // delete files total size reach target or delete files rate reach target
    if (deleteFilesTotalSize > duplicateSize || deleteFilesTotalSize / dataFilesTotalSize >= duplicateRatio) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.Major);
      LOG.debug("{} ==== need native Major optimize plan, partition is {}, " +
              "delete files totalSize is {}, data files totalSize is {}",
          tableId(), partitionToPath, deleteFilesTotalSize, dataFilesTotalSize);
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}, " +
            "delete files totalSize is {}, data files totalSize is {}",
        tableId(), getOptimizeType(), partitionToPath, deleteFilesTotalSize, dataFilesTotalSize);
    return false;
  }

  @Override
  protected OptimizeType getOptimizeType() {
    return OptimizeType.Major;
  }

  @Override
  protected List<BaseOptimizeTask> collectTask(String partition) {
    List<BaseOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    TaskConfig taskPartitionConfig = new TaskConfig(partition, null,
        commitGroup, planGroup, OptimizeType.Major, createTime, "");
    FileTree treeRoot = partitionFileTree.get(partition);
    treeRoot.completeTree(false);
    List<FileTree> subTrees = new ArrayList<>();
    // split tasks
    treeRoot.splitSubTree(subTrees, new MinorOptimizePlan.CanSplitFileTree());
    for (FileTree subTree : subTrees) {
      // key -> dataFile index in dataFileList in task
      // value -> relate eq-deleteFile index in eq-deleteFileList in task
      Map<Integer, List<Integer>> eqDeleteFileIndexMap = new HashMap<>();
      // value -> relate pos-deleteFile index in pos-deleteFileList in task
      Map<Integer, List<Integer>> posDeleteFileIndexMap = new HashMap<>();
      List<DataFile> dataFiles = new ArrayList<>();
      List<DeleteFile> eqDeleteFiles = new ArrayList<>();
      List<DeleteFile> posDeleteFiles = new ArrayList<>();
      subTree.collectBaseFiles(dataFiles);
      for (int i = 0; i < dataFiles.size(); i++) {
        DataFile dataFile = dataFiles.get(i);
        List<Integer> eqDeleteIndex = new ArrayList<>();
        List<Integer> posDeleteIndex = new ArrayList<>();
        for (DeleteFile deleteFile : dataDeleteFileMap.get(dataFile)) {
          if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
            eqDeleteFiles.add(deleteFile);
            eqDeleteIndex.add(eqDeleteFiles.size() - 1);
          } else {
            posDeleteFiles.add(deleteFile);
            posDeleteIndex.add(posDeleteFiles.size() - 1);
          }
        }
        if (CollectionUtils.isNotEmpty(eqDeleteIndex)) {
          eqDeleteFileIndexMap.put(i, eqDeleteIndex);
        }
        if (CollectionUtils.isNotEmpty(posDeleteIndex)) {
          posDeleteFileIndexMap.put(i, posDeleteIndex);
        }
      }
      collector.add(buildOptimizeTask(dataFiles,
          eqDeleteFiles, eqDeleteFileIndexMap, posDeleteFiles, posDeleteFileIndexMap, taskPartitionConfig));
    }

    return collector;
  }
}