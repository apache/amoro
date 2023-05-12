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

public class HiveUnkeyedTablePartitionPlan extends UnkeyedTablePartitionPlan {

  private final String hiveLocation;

  public HiveUnkeyedTablePartitionPlan(TableRuntime tableRuntime,
                                       ArcticTable table, String partition, String hiveLocation, long planTime) {
    super(tableRuntime, table, partition, planTime);
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
  protected void fillTaskProperties(OptimizingInputProperties properties) {
    super.fillTaskProperties(properties);
    if (partitionShouldFullOptimizing()) {
      if (moveFilesToHiveLocation()) {
        properties.needMoveFile2HiveLocation();
      } else {
        properties.setOutputDir(constructCustomHiveSubdirectory());
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
