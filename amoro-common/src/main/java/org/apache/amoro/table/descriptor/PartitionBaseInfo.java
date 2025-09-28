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

package org.apache.amoro.table.descriptor;

import org.apache.amoro.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

public class PartitionBaseInfo {
  String partition;
  int specId;
  long fileCount = 0;
  long fileSize = 0;
  long lastCommitTime = 0;

  /** detailed properties used for evaluating */
  Map<String, Object> detailProperties = new HashMap<>();

  // parameters needed for front-end only
  String size;

  public PartitionBaseInfo() {}

  public PartitionBaseInfo(
      String partition, int specId, long fileCount, long fileSize, long lastCommitTime) {
    this.partition = partition;
    this.specId = specId;
    this.fileCount = fileCount;
    setFileSize(fileSize);
    this.lastCommitTime = lastCommitTime;
  }

  public String getPartition() {
    return partition;
  }

  public void setPartition(String partition) {
    this.partition = partition;
  }

  public int getSpecId() {
    return specId;
  }

  public void setSpecId(int specId) {
    this.specId = specId;
  }

  public long getFileCount() {
    return fileCount;
  }

  public void setFileCount(long fileCount) {
    this.fileCount = fileCount;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
    this.size = CommonUtil.byteToXB(fileSize);
  }

  public long getLastCommitTime() {
    return lastCommitTime;
  }

  public void setLastCommitTime(long lastCommitTime) {
    this.lastCommitTime = lastCommitTime;
  }

  public String getSize() {
    return this.size;
  }

  public Object getPropertyOrDefault(String item, Object defaultValue) {
    return detailProperties.getOrDefault(item, defaultValue);
  }

  public void setProperty(String item, Object value) {
    detailProperties.put(item, value);
  }

  public boolean containsProperty(String item) {
    return detailProperties.containsKey(item);
  }

  @Override
  public String toString() {
    return String.format(
        "PartitionBaseInfo(partition=%s, specId=%d, fileCount=%d, fileSize=%d, lastCommitTime=%d, detailProperties=%s)",
        partition, specId, fileCount, fileSize, lastCommitTime, detailProperties);
  }
}
