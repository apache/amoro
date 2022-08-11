package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.FileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class AdaptHiveMajorOptimizePlan extends MajorOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(AdaptHiveMajorOptimizePlan.class);

  // partitions are adapt hive
  protected final Set<String> adaptHivePartitions = new HashSet<>();
  // hive table/partition location.
  protected final Map<String, String> hiveLocations = new HashMap<>();

  public AdaptHiveMajorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                    List<DataFileInfo> baseTableFileList, List<DataFileInfo> posDeleteFileList,
                                    Map<String, Boolean> partitionTaskRunning, int queueId, long currentTime,
                                    Predicate<Long> snapshotIsCached) {
    super(arcticTable, tableOptimizeRuntime, baseTableFileList, posDeleteFileList,
        partitionTaskRunning, queueId, currentTime, snapshotIsCached);

    // todo getHiveLocation fill hiveLocations
    if (arcticTable.isUnkeyedTable()) {
      // todo lt excludeLocations
    }
  }

  @Override
  public boolean partitionNeedPlan(String partitionToPath) {
    long current = System.currentTimeMillis();

    // check major optimize interval（for keyed table is full major, unKeyed table is major）
    if (checkMajorOptimizeInterval(current, partitionToPath)) {
      adaptHivePartitions.add(partitionToPath);
      return true;
    }

    // check position delete file total size
    if (checkPosDeleteTotalSize(partitionToPath)) {
      adaptHivePartitions.add(partitionToPath);
      return true;
    }

    // check small data file count
    if (checkSmallFileCount(partitionToPath)) {
      if (arcticTable.isUnkeyedTable()) {
        adaptHivePartitions.add(partitionToPath);
      }
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}", tableId(), getOptimizeType(), partitionToPath);
    return false;
  }

  @Override
  public boolean isAdaptHive(String partition) {
    return adaptHivePartitions.contains(partition);
  }

  @Override
  protected void generatePartitionLocation(String partition) {
    String baseLocation = arcticTable.isKeyedTable() ?
        arcticTable.asKeyedTable().baseLocation() : arcticTable.asUnkeyedTable().location();
    String optimizeLocation = adaptHivePartitions.contains(partition) ?
        FileUtil.getNewBaseLocation(baseLocation) : null;
    partitionOptimizeLocation.put(partition, optimizeLocation);
  }

  @Override
  protected boolean checkMajorOptimizeInterval(long current, String partitionToPath) {
    if (current - tableOptimizeRuntime.getLatestMajorOptimizeTime(partitionToPath) >=
        PropertyUtil.propertyAsLong(arcticTable.properties(), TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL,
            TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL_DEFAULT)) {
      if (arcticTable.isKeyedTable()) {
        FileTree fileTree = partitionFileTree.get(partitionToPath);
        List<DataFile> baseFiles = new ArrayList<>();
        fileTree.collectBaseFiles(baseFiles);
        boolean adaptHiveAndNoFileToOptimize =
            isAllInHiveLocation(baseFiles, hiveLocations.get(partitionToPath)) &&
            CollectionUtils.isEmpty(partitionPosDeleteFiles.get(partitionToPath));
        if (adaptHiveAndNoFileToOptimize) {
          return false;
        }
        mergePosDeletePartition.add(partitionToPath);
      } else {
        long fileCount = partitionSmallFiles.get(partitionToPath) == null ?
            0 : partitionSmallFiles.get(partitionToPath).size();
        // for unKeyed table, need to rewrite all files that not in hive location
        return fileCount >= 1;
      }
      return true;
    }

    return false;
  }

  private boolean isAllInHiveLocation(List<DataFile> files, String hiveLocation) {
    for (DataFile file : files) {
      if (!file.path().toString().contains(hiveLocation)) {
        return false;
      }
    }
    return true;
  }
}
