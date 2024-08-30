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

public class PartitionFileBaseInfo {
  private String commitId;
  private String fileType;
  private Long commitTime;
  private String size;
  private String partition;
  private int specId;
  private String path;
  private String file;
  private long fileSize;
  private String operation;

  public PartitionFileBaseInfo(
      String commitId,
      String fileType,
      Long commitTime,
      String partition,
      int specId,
      String path,
      long fileSize) {
    this.commitId = commitId;
    this.fileType = fileType;
    this.commitTime = commitTime;
    this.partition = partition;
    this.specId = specId;
    setPath(path);
    setFileSize(fileSize);
  }

  public PartitionFileBaseInfo(
      String commitId,
      String fileType,
      Long commitTime,
      String partition,
      String path,
      long fileSize,
      String operation) {
    this.commitId = commitId;
    this.fileType = fileType;
    this.commitTime = commitTime;
    this.partition = partition;
    this.operation = operation;
    setPath(path);
    setFileSize(fileSize);
  }

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public Long getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(Long commitTime) {
    this.commitTime = commitTime;
  }

  public String getSize() {
    return size;
  }

  public String getPartition() {
    return partition;
  }

  public int getSpecId() {
    return specId;
  }

  public void setPartition(String partition) {
    this.partition = partition;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
    this.file = CommonUtil.getFileName(path);
  }

  public String getFile() {
    return file;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
    this.size = CommonUtil.byteToXB(fileSize);
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }
}
