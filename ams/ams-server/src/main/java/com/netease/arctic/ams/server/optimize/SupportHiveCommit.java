package com.netease.arctic.ams.server.optimize;

import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HivePartitionUtil;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hive.metastore.api.Table;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SupportHiveCommit extends BaseOptimizeCommit {
  private static final Logger LOG = LoggerFactory.getLogger(SupportHiveCommit.class);

  protected Consumer<OptimizeTaskItem> updateTargetFiles;

  public SupportHiveCommit(ArcticTable arcticTable,
                           Map<String, List<OptimizeTaskItem>> optimizeTasksToCommit,
                           Consumer<OptimizeTaskItem> updateTargetFiles) {
    super(arcticTable, optimizeTasksToCommit);
    Preconditions.checkArgument(TableTypeUtil.isHive(arcticTable), "The table not support hive");
    this.updateTargetFiles = updateTargetFiles;
  }

  @Override
  public boolean commit(long baseSnapshotId) throws Exception {
    LOG.info("{} get tasks to support hive commit for partitions {}", arcticTable.id(),
        optimizeTasksToCommit.keySet());
    HMSClient hiveClient = ((SupportHive) arcticTable).getHMSClient();
    Map<String, String> partitionPathMap = new HashMap<>();
    Types.StructType partitionSchema = arcticTable.isUnkeyedTable() ?
        arcticTable.asUnkeyedTable().spec().partitionType() :
        arcticTable.asKeyedTable().baseTable().spec().partitionType();

    optimizeTasksToCommit.forEach((partition, optimizeTaskItems) -> {
      // if major optimize task don't contain pos-delete files in a partition, can rewrite or move data files
      // to hive location
      if (isPartitionMajorOptimizeSupportHive(partition, optimizeTaskItems)) {
        for (OptimizeTaskItem optimizeTaskItem : optimizeTaskItems) {
          BaseOptimizeTaskRuntime optimizeRuntime = optimizeTaskItem.getOptimizeRuntime();
          List<DataFile> targetFiles = optimizeRuntime.getTargetFiles().stream()
              .map(fileByte -> (DataFile) SerializationUtil.toInternalTableFile(fileByte))
              .collect(Collectors.toList());
          long maxTransactionId = targetFiles.stream()
              .mapToLong(dataFile -> {
                DefaultKeyedFile.FileMeta fileMeta = DefaultKeyedFile.parseMetaFromFileName(dataFile.path().toString());
                return fileMeta.transactionId();
              })
              .max()
              .orElse(0L);

          List<ByteBuffer> newTargetFiles = new ArrayList<>(targetFiles.size());
          for (DataFile targetFile : targetFiles) {
            if (partitionPathMap.get(partition) == null) {
              List<String> partitionValues =
                  HivePartitionUtil.partitionValuesAsList(targetFile.partition(), partitionSchema);
              String partitionPath;
              if (arcticTable.spec().isUnpartitioned()) {
                try {
                  Table hiveTable = ((SupportHive) arcticTable).getHMSClient().run(client ->
                      client.getTable(arcticTable.id().getDatabase(), arcticTable.id().getTableName()));
                  partitionPath = hiveTable.getSd().getLocation();
                } catch (Exception e) {
                  LOG.error("Get hive table failed", e);
                  break;
                }
              } else {
                partitionPath = arcticTable.isKeyedTable() ?
                    HiveTableUtil.newKeyedHiveDataLocation(
                        ((SupportHive) arcticTable).hiveLocation(), arcticTable.asKeyedTable().baseTable().spec(),
                        targetFile.partition(), maxTransactionId) :
                    HiveTableUtil.newUnKeyedHiveDataLocation(((SupportHive) arcticTable).hiveLocation(),
                        arcticTable.asUnkeyedTable().spec(), targetFile.partition(), HiveTableUtil.getRandomSubDir());
                HivePartitionUtil
                    .createPartitionIfAbsent(hiveClient, arcticTable, partitionValues, partitionPath,
                        Collections.emptyList(), (int) (System.currentTimeMillis() / 1000));

                partitionPath = HivePartitionUtil
                    .getPartition(hiveClient, arcticTable, partitionValues).getSd().getLocation();
              }
              partitionPathMap.put(partition, partitionPath);
            }

            DataFile finalDataFile = moveTargetFiles(targetFile, partitionPathMap.get(partition));
            newTargetFiles.add(SerializationUtil.toByteBuffer(finalDataFile));
          }

          optimizeRuntime.setTargetFiles(newTargetFiles);
          updateTargetFiles.accept(optimizeTaskItem);
        }
      }
    });

    return super.commit(baseSnapshotId);
  }

  protected boolean isPartitionMajorOptimizeSupportHive(String partition, List<OptimizeTaskItem> optimizeTaskItems) {
    for (OptimizeTaskItem optimizeTaskItem : optimizeTaskItems) {
      BaseOptimizeTask optimizeTask = optimizeTaskItem.getOptimizeTask();
      boolean isMajorTaskSupportHive = optimizeTask.getTaskId().getType() == OptimizeType.Major &&
          CollectionUtils.isEmpty(optimizeTask.getPosDeleteFiles());
      if (!isMajorTaskSupportHive) {
        LOG.info("{} is not major task support hive for partitions {}", arcticTable.id(), partition);
        return false;
      }
    }

    LOG.info("{} is major task support hive for partitions {}", arcticTable.id(), partition);
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
