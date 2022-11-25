/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.scan;

import com.netease.arctic.data.IcebergContentFile;
import java.util.List;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

public class CombinedIcebergScanTask {

  private IcebergContentFile[] dataFiles;

  private IcebergContentFile[] deleteFiles;

  private PartitionSpec partitionSpec;

  private StructLike partitionData;

  public CombinedIcebergScanTask(
      IcebergContentFile[] dataFiles,
      IcebergContentFile[] deleteFiles,
      PartitionSpec partitionSpec, StructLike partitionData) {
    this.dataFiles = dataFiles;
    this.deleteFiles = deleteFiles;
    this.partitionSpec = partitionSpec;
    this.partitionData = partitionData;
  }

  public List<IcebergContentFile> getDataFiles() {
    return ImmutableList.copyOf(dataFiles);
  }

  public List<IcebergContentFile> getDeleteFiles() {
    return ImmutableList.copyOf(deleteFiles);
  }

  public PartitionSpec getPartitionSpec() {
    return partitionSpec;
  }

  public StructLike getPartitionData() {
    return partitionData;
  }
}
