package com.netease.arctic.ams.server.optimize;

import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HivePartitionUtil;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SupportHiveCommit extends BaseOptimizeCommit {
  private static final Logger LOG = LoggerFactory.getLogger(SupportHiveCommit.class);

  protected Function<Void, Void> updateTargetFiles;

  public SupportHiveCommit(ArcticTable arcticTable,
                           Map<String, List<OptimizeTaskItem>> optimizeTasksToCommit,
                           Function<Void, Void> updateTargetFiles) {
    super(arcticTable, optimizeTasksToCommit);
    Preconditions.checkArgument(HiveTableUtil.isHive(arcticTable), "The table not support hive");
    this.updateTargetFiles = updateTargetFiles;
  }

  @Override
  public long commit(TableOptimizeRuntime tableOptimizeRuntime) throws Exception {
    HMSClient hiveClient = ((SupportHive) arcticTable).getHMSClient();
    Map<String, String> partitionPathMap = new HashMap<>();
    Types.StructType partitionSchema = arcticTable.isUnkeyedTable() ?
        arcticTable.asUnkeyedTable().spec().partitionType() :
        arcticTable.asKeyedTable().baseTable().spec().partitionType();

    optimizeTasksToCommit.forEach((partition, optimizeTaskItems) -> {
      // if major optimize task don't contain pos-delete files in a partition, can rewrite or move data files
      // to hive location
      if (isPartitionMajorOptimizeSupportHive(optimizeTaskItems)) {
        for (OptimizeTaskItem optimizeTaskItem : optimizeTaskItems) {
          BaseOptimizeTaskRuntime optimizeRuntime = optimizeTaskItem.getOptimizeRuntime();
          List<DataFile> targetFiles = optimizeRuntime.getTargetFiles().stream()
              .map(fileByte -> (DataFile) SerializationUtil.toInternalTableFile(fileByte))
              .collect(Collectors.toList());

          List<ByteBuffer> newTargetFiles = new ArrayList<>(targetFiles.size());
          for (DataFile targetFile : targetFiles) {
            if (partitionPathMap.get(partition) == null) {
              List<String> partitionValues =
                  HivePartitionUtil.partitionValuesAsList(targetFile.partition(), partitionSchema);
              String partitionPath;
              if (arcticTable.spec().isUnpartitioned()) {
                partitionPath = ((SupportHive) arcticTable).hiveLocation();
              } else {
                partitionPath =
                    ((SupportHive) arcticTable).hiveLocation() +
                        FileUtil.getPartitionPathFromFilePath(targetFile.path().toString(), arcticTable.isKeyedTable() ?
                        arcticTable.asKeyedTable().baseLocation() : arcticTable.asUnkeyedTable().location(),
                            targetFile.path().toString().substring(targetFile.path().toString().lastIndexOf("/") + 1));
                HivePartitionUtil
                    .createPartitionIfAbsent(hiveClient, arcticTable, partitionValues, partitionPath,
                        Collections.emptyList(), (int) (System.currentTimeMillis() / 1000));
              }
              partitionPathMap.put(partition, partitionPath);
            }

            DataFile finalDataFile = moveTargetFiles(targetFile, partitionPathMap.get(partition));
            newTargetFiles.add(SerializationUtil.toByteBuffer(finalDataFile));
          }

          optimizeRuntime.setTargetFiles(newTargetFiles);
          updateTargetFiles.apply(null);
        }
      }
    });

    return super.commit(tableOptimizeRuntime);
  }

  protected boolean isPartitionMajorOptimizeSupportHive(List<OptimizeTaskItem> optimizeTaskItems) {
    for (OptimizeTaskItem optimizeTaskItem : optimizeTaskItems) {
      BaseOptimizeTask optimizeTask = optimizeTaskItem.getOptimizeTask();
      boolean isMajorTaskSupportHive = optimizeTask.getTaskId().getType() == OptimizeType.Major &&
          CollectionUtils.isEmpty(optimizeTask.getPosDeleteFiles());
      if (!isMajorTaskSupportHive) {
        return false;
      }
    }

    return true;
  }

  private DataFile moveTargetFiles(DataFile targetFile, String hiveLocation) {
    String oldFilePath = targetFile.path().toString();
    String newFilePath = FileUtil.getNewFilePath(hiveLocation, oldFilePath);

    if (!arcticTable.io().exists(newFilePath)) {
      arcticTable.io().rename(oldFilePath, newFilePath);
      LOG.debug("{} move file from {} to {}", arcticTable.id(), oldFilePath, newFilePath);
    }

    // org.apache.iceberg.BaseFile.set
    ((StructLike) targetFile).set(1, newFilePath);
    return targetFile;
  }
}
