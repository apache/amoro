/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class MixedHivePartitionPlan extends MixedIcebergPartitionPlan {
  private final String hiveLocation;
  private long maxSequence = 0;
  private String customHiveSubdirectory;

  public MixedHivePartitionPlan(TableRuntime tableRuntime,
                                ArcticTable table, String partition, String hiveLocation, long planTime) {
    super(tableRuntime, table, partition, planTime);
    this.hiveLocation = hiveLocation;
  }

  @Override
  public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    super.addFile(dataFile, deletes);
    long sequenceNumber = dataFile.getSequenceNumber();
    if (sequenceNumber > maxSequence) {
      maxSequence = sequenceNumber;
    }
  }

  @Override
  protected boolean isFragmentFile(IcebergDataFile dataFile) {
    PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile.internalFile();
    if (file.type() == DataFileType.BASE_FILE) {
      // we treat all files in hive location as segment files
      return dataFile.fileSizeInBytes() <= maxFragmentSize && notInHiveLocation(dataFile.path().toString());
    } else if (file.type() == DataFileType.INSERT_FILE) {
      // we treat all insert files as fragment files
      return true;
    } else {
      throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
    }
  }

  private boolean notInHiveLocation(String filePath) {
    return !filePath.contains(hiveLocation);
  }

  @Override
  protected boolean canSegmentFileRewrite(IcebergDataFile dataFile) {
    // files in hive location should not be rewritten
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
    return evaluator().isFullNecessary() && !config.isFullRewriteAllFiles() && !findAnyDelete();
  }

  @Override
  protected OptimizingInputProperties buildTaskProperties() {
    OptimizingInputProperties properties = super.buildTaskProperties();
    if (moveFiles2CurrentHiveLocation()) {
      properties.needMoveFile2HiveLocation();
    } else if (evaluator().isFullNecessary()){
      properties.setOutputDir(constructCustomHiveSubdirectory());
    }
    return properties;
  }

  private String constructCustomHiveSubdirectory() {
    if (customHiveSubdirectory == null) {
      if (isKeyedTable()) {
        customHiveSubdirectory = HiveTableUtil.newHiveSubdirectory(maxSequence);
      } else {
        customHiveSubdirectory = HiveTableUtil.newHiveSubdirectory();
      }
    }
    return customHiveSubdirectory;
  }

}
