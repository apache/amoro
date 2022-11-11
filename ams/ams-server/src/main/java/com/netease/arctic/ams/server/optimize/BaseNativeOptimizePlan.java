package com.netease.arctic.ams.server.optimize;

import com.alibaba.fastjson.JSON;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.api.properties.OptimizeTaskProperties;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.FilesStatistics;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.ams.server.utils.FilesStatisticsBuilder;
import com.netease.arctic.ams.server.utils.UnKeyedTableUtil;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * only used for native iceberg
 */
public abstract class BaseNativeOptimizePlan implements OptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(BaseNativeOptimizePlan.class);

  protected final ArcticTable arcticTable;
  // DataFiles and relate DeleteFiles
  protected final Map<DataFile, List<DeleteFile>> dataDeleteFileMap;
  protected final TableOptimizeRuntime tableOptimizeRuntime;
  protected final int queueId;
  protected final long currentTime;
  protected final Map<String, Boolean> partitionTaskRunning;
  protected final String planGroup;

  // partition -> fileTree
  protected final Map<String, FileTree> partitionFileTree = new LinkedHashMap<>();
  // partition -> optimize type(Major or Minor)
  protected final Map<String, OptimizeType> partitionOptimizeType = new HashMap<>();

  // We store current partitions, for the next plans to decide if any partition reach the max plan interval,
  // if not, the new added partitions will be ignored by mistake.
  // After plan files, current partitions of table will be set.
  protected final Set<String> currentPartitions = new HashSet<>();
  protected long currentSnapshotId = TableOptimizeRuntime.INVALID_SNAPSHOT_ID;

  public BaseNativeOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                Map<DataFile, List<DeleteFile>> dataDeleteFileMap,
                                Map<String, Boolean> partitionTaskRunning,
                                int queueId, long currentTime) {
    this.arcticTable = arcticTable;
    this.tableOptimizeRuntime = tableOptimizeRuntime;
    this.queueId = queueId;
    this.currentTime = currentTime;
    this.partitionTaskRunning = partitionTaskRunning;
    this.planGroup = UUID.randomUUID().toString();
    this.dataDeleteFileMap = dataDeleteFileMap;
  }

  /**
   * check whether partition need to plan
   * @param partitionToPath target partition
   * @return whether partition need to plan. if true, partition try to plan, otherwise skip.
   */
  protected abstract boolean partitionNeedPlan(String partitionToPath);

  protected abstract OptimizeType getOptimizeType();

  protected abstract List<BaseOptimizeTask> collectTask(String partition);

  public List<BaseOptimizeTask> plan() {
    long startTime = System.nanoTime();

    if (!tableNeedPlan()) {
      LOG.debug("{} === skip {} plan", tableId(), getOptimizeType());
      return Collections.emptyList();
    }

    addOptimizeFilesTree();

    if (!hasFileToOptimize()) {
      return Collections.emptyList();
    }

    List<BaseOptimizeTask> results = collectTasks();

    long endTime = System.nanoTime();
    LOG.debug("{} ==== {} plan tasks cost {} ns, {} ms", tableId(), getOptimizeType(), endTime - startTime,
        (endTime - startTime) / 1_000_000);
    LOG.debug("{} {} plan get {} tasks", tableId(), getOptimizeType(), results.size());
    return results;
  }

  public List<BaseOptimizeTask> collectTasks() {
    List<BaseOptimizeTask> results = new ArrayList<>();

    List<String> skippedPartitions = new ArrayList<>();
    for (Map.Entry<String, FileTree> fileTreeEntry : partitionFileTree.entrySet()) {
      String partition = fileTreeEntry.getKey();

      if (anyTaskRunning(partition)) {
        LOG.warn("{} {} any task running while collect tasks? should not arrive here, partitionPath={}",
            tableId(), getOptimizeType(), partition);
        skippedPartitions.add(partition);
        continue;
      }

      // partition don't need to plan
      if (!partitionNeedPlan(partition)) {
        skippedPartitions.add(partition);
        continue;
      }

      List<BaseOptimizeTask> optimizeTasks = collectTask(partition);
      LOG.debug("{} partition {} ==== collect {} {} tasks", tableId(), partition, optimizeTasks.size(),
          getOptimizeType());
      results.addAll(optimizeTasks);
    }

    LOG.debug("{} ==== after collect {} task, skip partitions {}/{}", tableId(), getOptimizeType(),
        skippedPartitions.size(), partitionFileTree.entrySet().size());
    return results;
  }

  protected boolean tableChanged() {
    long lastSnapshotId = tableOptimizeRuntime.getCurrentSnapshotId();
    LOG.debug("{} ==== {} currentSnapshotId={}, lastSnapshotId={}", tableId(), getOptimizeType(),
        currentSnapshotId, lastSnapshotId);
    return currentSnapshotId != lastSnapshotId;
  }

  protected void addOptimizeFilesTree() {

    LOG.debug("{} start plan native table files", tableId());
    AtomicInteger addCnt = new AtomicInteger();

    // add DataFile into partition file tree (for native table, target node is (0, 0))
    dataDeleteFileMap.forEach((dataFile, deleteFilePair) -> {
      String partitionPath = arcticTable.spec().partitionToPath(dataFile.partition());
      currentPartitions.add(partitionPath);

      if (!anyTaskRunning(partitionPath)) {
        FileTree treeRoot =
            partitionFileTree.computeIfAbsent(partitionPath, p -> FileTree.newTreeRoot());
        treeRoot.putNodeIfAbsent(FileUtil.parseFileNodeFromFileName(dataFile.path().toString()))
            .addFile(dataFile, DataFileType.BASE_FILE);
        addCnt.getAndIncrement();
      }
    });

    LOG.debug("{} ==== {} add {} data files into tree, total files: {}." + " After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), addCnt, dataDeleteFileMap.size(), partitionFileTree.size());
  }

  protected BaseOptimizeTask buildOptimizeTask(List<DataFile> dataFiles,
                                               List<DeleteFile> eqDeleteFiles,
                                               Map<Integer, List<Integer>> eqDeleteFileIndexMap,
                                               List<DeleteFile> posDeleteFiles,
                                               Map<Integer, List<Integer>> posDeleteFileIndexMap,
                                               TaskConfig taskConfig) {
    // build task
    BaseOptimizeTask optimizeTask = new BaseOptimizeTask();
    optimizeTask.setTaskCommitGroup(taskConfig.getCommitGroup());
    optimizeTask.setTaskPlanGroup(taskConfig.getPlanGroup());
    optimizeTask.setCreateTime(taskConfig.getCreateTime());

    List<ByteBuffer> dataFileBytesList =
        dataFiles.stream()
            .map(SerializationUtil::toByteBuffer)
            .collect(Collectors.toList());
    List<ByteBuffer> eqDeleteFileBytesList =
        eqDeleteFiles.stream()
            .map(SerializationUtil::toByteBuffer)
            .collect(Collectors.toList());
    List<ByteBuffer> posDeleteFileBytesList =
        posDeleteFiles.stream()
            .map(SerializationUtil::toByteBuffer)
            .collect(Collectors.toList());
    optimizeTask.setBaseFiles(dataFileBytesList);
    optimizeTask.setEqDeleteFiles(eqDeleteFileBytesList);
    optimizeTask.setPosDeleteFiles(posDeleteFileBytesList);
    optimizeTask.setInsertFiles(Collections.emptyList());
    optimizeTask.setDeleteFiles(Collections.emptyList());

    FilesStatisticsBuilder baseFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder eqDeleteFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder posDeleteFb = new FilesStatisticsBuilder();
    dataFiles.stream().map(DataFile::fileSizeInBytes)
        .forEach(baseFb::addFile);
    eqDeleteFiles.stream().distinct().map(DeleteFile::fileSizeInBytes)
        .forEach(eqDeleteFb::addFile);
    posDeleteFiles.stream().distinct().map(DeleteFile::fileSizeInBytes)
        .forEach(posDeleteFb::addFile);

    FilesStatistics baseFs = baseFb.build();
    FilesStatistics eqDeleteFs = eqDeleteFb.build();
    FilesStatistics posDeleteFs = posDeleteFb.build();

    // file size
    optimizeTask.setBaseFileSize(baseFs.getTotalSize());
    optimizeTask.setEqDeleteFileSize(eqDeleteFs.getTotalSize());
    optimizeTask.setPosDeleteFileSize(posDeleteFs.getTotalSize());

    // file count
    optimizeTask.setBaseFileCnt(baseFs.getFileCnt());
    optimizeTask.setEqDeleteFileCnt(eqDeleteFs.getFileCnt());
    optimizeTask.setPosDeleteFileCnt(posDeleteFs.getFileCnt());

    optimizeTask.setPartition(taskConfig.getPartition());
    optimizeTask.setQueueId(queueId);
    optimizeTask.setTaskId(new OptimizeTaskId(taskConfig.getOptimizeType(), UUID.randomUUID().toString()));
    optimizeTask.setTableIdentifier(arcticTable.id().buildTableIdentifier());

    Map<String, String> properties = new HashMap<>();
    // for native table, fill eqDeleteFileIndexMap and posDeleteFileIndexMap
    if (MapUtils.isNotEmpty(eqDeleteFileIndexMap)) {
      properties.put(OptimizeTaskProperties.EQ_DELETE_FILES_INDEX, JSON.toJSONString(eqDeleteFileIndexMap));
    }
    if (MapUtils.isNotEmpty(posDeleteFileIndexMap)) {
      properties.put(OptimizeTaskProperties.POS_DELETE_FILES_INDEX, JSON.toJSONString(posDeleteFileIndexMap));
    }

    // fill task summary to check
    properties.put(OptimizeTaskProperties.ALL_FILE_COUNT, (optimizeTask.getBaseFiles().size() +
        optimizeTask.getInsertFiles().size() + optimizeTask.getDeleteFiles().size()) +
        optimizeTask.getEqDeleteFiles().size() + optimizeTask.getPosDeleteFiles().size() + "");
    properties.put(OptimizeTaskProperties.CUSTOM_HIVE_SUB_DIRECTORY, taskConfig.getCustomHiveSubdirectory());
    optimizeTask.setProperties(properties);
    return optimizeTask;
  }

  public Map<String, OptimizeType> getPartitionOptimizeType() {
    return partitionOptimizeType;
  }


  public boolean tableNeedPlan() {
    this.currentSnapshotId = UnKeyedTableUtil.getSnapshotId(arcticTable.asUnkeyedTable());
    return tableChanged();
  }

  public boolean hasFileToOptimize() {
    return !partitionFileTree.isEmpty();
  }

  public TableIdentifier tableId() {
    return arcticTable.id();
  }

  public Set<String> getCurrentPartitions() {
    return currentPartitions;
  }

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public long getCurrentChangeSnapshotId() {
    throw new IllegalArgumentException("Native iceberg don't have change snapshot");
  }

  protected boolean anyTaskRunning(String partition) {
    return partitionTaskRunning.get(partition) != null && partitionTaskRunning.get(partition);
  }
}
