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

package org.apache.amoro.formats.paimon.optimizing.primary;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Arrays;

public class PaimonBucketCompactionUnit implements Serializable {

  private static final long serialVersionUID = 1L;

  private byte[] partitionBytes;
  private int bucket;
  private long fileCount;
  private long fileSizeInBytes;
  private long recordCount;
  private long lastFileCreationTime;

  public PaimonBucketCompactionUnit() {}

  public PaimonBucketCompactionUnit(
      byte[] partitionBytes,
      int bucket,
      long fileCount,
      long fileSizeInBytes,
      long recordCount,
      long lastFileCreationTime) {
    this.partitionBytes = partitionBytes;
    this.bucket = bucket;
    this.fileCount = fileCount;
    this.fileSizeInBytes = fileSizeInBytes;
    this.recordCount = recordCount;
    this.lastFileCreationTime = lastFileCreationTime;
  }

  public byte[] getPartitionBytes() {
    return partitionBytes;
  }

  public int getBucket() {
    return bucket;
  }

  public long getFileCount() {
    return fileCount;
  }

  public long getFileSizeInBytes() {
    return fileSizeInBytes;
  }

  public long getRecordCount() {
    return recordCount;
  }

  public long getLastFileCreationTime() {
    return lastFileCreationTime;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("partitionBytes", Arrays.toString(partitionBytes))
        .add("bucket", bucket)
        .add("fileCount", fileCount)
        .add("fileSizeInBytes", fileSizeInBytes)
        .add("recordCount", recordCount)
        .add("lastFileCreationTime", lastFileCreationTime)
        .toString();
  }
}
