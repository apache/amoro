package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;

import java.util.List;
import java.util.Map;

public class MixedHivePartitionPlan extends MixedIcebergPartitionPlan {

  private final String hiveLocation;

  public MixedHivePartitionPlan(TableRuntime tableRuntime,
                                ArcticTable table, String partition, String hiveLocation) {
    super(tableRuntime, table, partition);
    this.hiveLocation = hiveLocation;
  }

  @Override
  protected boolean isFragmentFile(IcebergDataFile dataFile) {
    PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile;
    if (file.type() == DataFileType.BASE_FILE) {
      return dataFile.fileSizeInBytes() <= fragmentSize && notInHiveLocation(dataFile.path().toString());
    } else if (file.type() == DataFileType.INSERT_FILE) {
      return true;
    } else {
      throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
    }
  }


  private boolean notInHiveLocation(String filePath) {
    return !filePath.contains(hiveLocation);
  }

  @Override
  protected boolean canRewriteFile(IcebergDataFile dataFile) {
    return notInHiveLocation(dataFile.path().toString());
  }

  @Override
  protected boolean shouldFullOptimizing(IcebergDataFile dataFile, List<IcebergContentFile<?>> deleteFiles) {
    if (moveFilesToHiveLocation()) {
      return notInHiveLocation(dataFile.path().toString());
    } else {
      return true;
    }
  }

  private boolean moveFilesToHiveLocation() {
    return partitionShouldFullOptimizing() && !config.isFullRewriteAllFiles() && !findAnyDelete();
  }

  @Override
  protected void fillTaskProperties(Map<String, String> properties) {
    super.fillTaskProperties(properties);
    if (partitionShouldFullOptimizing()) {
      if (moveFilesToHiveLocation()) {
        properties.put(OptimizingInputProperties.MOVE_FILE_TO_HIVE_LOCATION, "true");
      } else {
        properties.put(OptimizingInputProperties.OUTPUT_DIR, constructCustomHiveSubdirectory());
      }
    }
  }

  private String constructCustomHiveSubdirectory() {
    if (tableObject.isKeyedTable()) {
      return HiveTableUtil.newHiveSubdirectory(getToSequence());
    } else {
      return HiveTableUtil.newHiveSubdirectory();
    }
  }

}
