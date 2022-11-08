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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NativeMinorOptimizePlan extends BaseNativeOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(NativeMinorOptimizePlan.class);

  public NativeMinorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                 Map<DataFile, List<DeleteFile>> dataDeleteFileMap,
                                 Map<String, Boolean> partitionTaskRunning,
                                 int queueId, long currentTime) {
    super(arcticTable, tableOptimizeRuntime, dataDeleteFileMap, partitionTaskRunning, queueId, currentTime);
  }

  @Override
  protected boolean partitionNeedPlan(String partitionToPath) {
    FileTree partitionTree = partitionFileTree.get(partitionToPath);
    List<DataFile> partitionFiles = new ArrayList<>();
    partitionTree.collectBaseFiles(partitionFiles);

    long smallFileSize = PropertyUtil.propertyAsLong(arcticTable.properties(),
        TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
        TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT);
    // partition has greater than 1 small files to optimize
    long fileCount = partitionFiles.stream().filter(dataFile -> dataFile.fileSizeInBytes() <= smallFileSize).count();
    if (fileCount > 1) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.Minor);
      LOG.debug("{} ==== need native minor optimize plan, partition is {}, small file count is {}",
          tableId(), partitionToPath, fileCount);
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}", tableId(), getOptimizeType(), partitionToPath);
    return false;
  }

  @Override
  protected OptimizeType getOptimizeType() {
    return OptimizeType.Minor;
  }

  @Override
  protected List<BaseOptimizeTask> collectTask(String partition) {
    List<BaseOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    TaskConfig taskPartitionConfig = new TaskConfig(partition, null,
        commitGroup, planGroup, OptimizeType.Minor, createTime, "");
    long smallFileSize = PropertyUtil.propertyAsLong(arcticTable.properties(),
        TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
        TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT);
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
      List<DataFile> allDataFiles = new ArrayList<>();
      List<DataFile> smallDataFiles = new ArrayList<>();
      List<DeleteFile> eqDeleteFiles = new ArrayList<>();
      List<DeleteFile> posDeleteFiles = new ArrayList<>();
      subTree.collectBaseFiles(allDataFiles);
      for (DataFile dataFile : allDataFiles) {
        if (dataFile.fileSizeInBytes() > smallFileSize) {
          continue;
        }
        smallDataFiles.add(dataFile);
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
          eqDeleteFileIndexMap.put(smallDataFiles.size() - 1, eqDeleteIndex);
        }
        if (CollectionUtils.isNotEmpty(posDeleteIndex)) {
          posDeleteFileIndexMap.put(smallDataFiles.size() - 1, posDeleteIndex);
        }
      }
      collector.add(buildOptimizeTask(smallDataFiles,
          eqDeleteFiles, eqDeleteFileIndexMap, posDeleteFiles, posDeleteFileIndexMap, taskPartitionConfig));
    }

    return collector;
  }
}
