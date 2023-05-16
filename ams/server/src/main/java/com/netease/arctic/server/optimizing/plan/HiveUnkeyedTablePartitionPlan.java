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
    PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile.internalFile();
    if (file.type() == DataFileType.BASE_FILE) {
      // we treat all files in hive location as segment files
      return dataFile.fileSizeInBytes() <= fragmentSize && notInHiveLocation(dataFile.path().toString());
    } else {
      throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
    }
  }

  private boolean notInHiveLocation(String filePath) {
    // files in hive location should not be rewritten
    return !filePath.contains(hiveLocation);
  }

  @Override
  protected boolean canSegmentFileRewrite(IcebergDataFile dataFile) {
    return notInHiveLocation(dataFile.path().toString());
  }

  @Override
  protected boolean fileShouldFullOptimizing(IcebergDataFile dataFile, List<IcebergContentFile<?>> deleteFiles) {
    if (moveFiles2CurrentHiveLocation()) {
      // if we are going to move files to old hive location, only files not in hive location should full optimizing
      return notInHiveLocation(dataFile.path().toString());
    } else {
      // if we are going to rewrite all files to a new hive location, all files should full optimizing
      return true;
    }
  }

  private boolean moveFiles2CurrentHiveLocation() {
    return isFullNecessary() && !config.isFullRewriteAllFiles() && !findAnyDelete();
  }

  @Override
  protected OptimizingInputProperties buildTaskProperties() {
    OptimizingInputProperties properties = super.buildTaskProperties();
    if (moveFiles2CurrentHiveLocation()) {
      properties.needMoveFile2HiveLocation();
    } else {
      properties.setOutputDir(constructCustomHiveSubdirectory());
    }
    return properties;
  }

  private String constructCustomHiveSubdirectory() {
    return HiveTableUtil.newHiveSubdirectory();
  }

}
