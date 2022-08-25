package com.netease.arctic.ams.server.optimize;

import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SupportHiveMajorOptimizePlan extends MajorOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(SupportHiveMajorOptimizePlan.class);

  // partitions are support hive
  protected final Set<String> supportHivePartitions = new HashSet<>();
  // hive location.
  protected final String hiveLocation;

  public SupportHiveMajorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                      List<DataFileInfo> baseTableFileList, List<DataFileInfo> posDeleteFileList,
                                      Map<String, Boolean> partitionTaskRunning, int queueId, long currentTime,
                                      Predicate<Long> snapshotIsCached) {
    super(arcticTable, tableOptimizeRuntime, baseTableFileList, posDeleteFileList,
        partitionTaskRunning, queueId, currentTime, snapshotIsCached);

    Preconditions.checkArgument(HiveTableUtil.isHive(arcticTable), "The table not support hive");
    hiveLocation = ((SupportHive) arcticTable).hiveLocation();
    excludeLocations.add(hiveLocation);
  }

  @Override
  public boolean partitionNeedPlan(String partitionToPath) {
    long current = System.currentTimeMillis();

    // check position delete file total size
    if (checkPosDeleteTotalSize(partitionToPath)) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.FullMajor);
      supportHivePartitions.add(partitionToPath);
      return true;
    }

    // check full major optimize interval
    if (checkFullMajorOptimizeInterval(current, partitionToPath)) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.FullMajor);
      supportHivePartitions.add(partitionToPath);
      return true;
    }

    // check small data file count
    if (checkSmallFileCount(partitionToPath, partitionNeedOptimizeFiles.get(partitionToPath))) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.Major);
      if (CollectionUtils.isEmpty(partitionPosDeleteFiles.get(partitionToPath))) {
        supportHivePartitions.add(partitionToPath);
      }
      return true;
    }

    // check major optimize interval
    if (checkMajorOptimizeInterval(current, partitionToPath)) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.Major);
      if (CollectionUtils.isEmpty(partitionPosDeleteFiles.get(partitionToPath))) {
        supportHivePartitions.add(partitionToPath);
      }
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}", tableId(), getOptimizeType(), partitionToPath);
    return false;
  }

  @Override
  protected boolean checkMajorOptimizeInterval(long current, String partitionToPath) {
    if (current - tableOptimizeRuntime.getLatestMajorOptimizeTime(partitionToPath) >=
        PropertyUtil.propertyAsLong(arcticTable.properties(), TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL,
            TableProperties.MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL_DEFAULT)) {
      // need to rewrite or move all files that not in hive location to hive location.
      long fileCount = partitionNeedOptimizeFiles.get(partitionToPath) == null ?
          0 : partitionNeedOptimizeFiles.get(partitionToPath).size();
      return fileCount >= 1;
    }

    return false;
  }

  @Override
  protected boolean checkSmallFileCount(String partition, List<DataFile> dataFileList) {
    // for support hive table, filter small files
    List<DataFile> smallFileList = dataFileList.stream().filter(file -> file.fileSizeInBytes() <=
        PropertyUtil.propertyAsLong(arcticTable.properties(), TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
            TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT)).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(partitionPosDeleteFiles.get(partition))) {
      partitionNeedOptimizeFiles.put(partition, smallFileList);
    }

    return super.checkSmallFileCount(partition, smallFileList);
  }

  @Override
  protected void fillPartitionNeedOptimizeFiles(String partition, ContentFile<?> contentFile) {
    // for support hive table, add all files in iceberg base store and not in hive store
    if (canInclude(contentFile.path().toString())) {
      List<DataFile> files = partitionNeedOptimizeFiles.computeIfAbsent(partition, e -> new ArrayList<>());
      files.add((DataFile) contentFile);
      partitionNeedOptimizeFiles.put(partition, files);
    }
  }

  @Override
  protected boolean canSkip(List<DeleteFile> posDeleteFiles, List<DataFile> baseFiles) {
    return CollectionUtils.isEmpty(posDeleteFiles) && baseFiles.isEmpty();
  }
}
