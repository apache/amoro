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

package com.netease.arctic.ams.server.model;

import com.netease.arctic.ams.api.DataFile;
import com.netease.arctic.ams.api.PartitionFieldData;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.utils.TableMetadataUtil;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.trace.SnapshotSummary;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.relocated.com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class CacheFileInfo {

  private TableIdentifier tableIdentifier;
  private Long addSnapshotId;
  private Long parentSnapshotId;
  private Long deleteSnapshotId;
  private String innerTable;
  private String filePath;
  private String primaryKeyMd5;
  private String fileType;
  private String producer;
  private Long fileSize;
  private Long fileMask;
  private Long fileIndex;
  private Long specId;
  private String partitionName;
  private Long commitTime;
  private Long recordCount;
  private String action;
  private Long watermark;

  public CacheFileInfo() {

  }

  public CacheFileInfo(
      String primaryKeyMd5, TableIdentifier tableIdentifier, Long addSnapshotId,
      Long parentSnapshotId, Long deleteSnapshotId, String innerTable,
      String filePath, String fileType, Long fileSize, Long fileMask, Long fileIndex, Long specId,
      String partitionName, Long commitTime, Long recordCount, String action, Long watermark, String producer) {
    this.primaryKeyMd5 = primaryKeyMd5;
    this.tableIdentifier = tableIdentifier;
    this.addSnapshotId = addSnapshotId;
    this.parentSnapshotId = parentSnapshotId;
    this.deleteSnapshotId = deleteSnapshotId;
    this.innerTable = innerTable;
    this.filePath = filePath;
    this.fileType = fileType;
    this.fileSize = fileSize;
    this.fileMask = fileMask;
    this.fileIndex = fileIndex;
    this.specId = specId;
    this.partitionName = partitionName;
    this.commitTime = commitTime;
    this.recordCount = recordCount;
    this.action = action;
    this.watermark = watermark;
    this.producer = producer;
  }

  public static CacheFileInfo convert(
      Table table, DataFile amsFile, TableIdentifier identifier, String tableType, Snapshot snapshot) {
    long watermark = 0L;
    boolean isDataFile = Objects.equals(amsFile.fileType, DataFileType.INSERT_FILE.name()) ||
        Objects.equals(amsFile.fileType, DataFileType.BASE_FILE.name());
    if (isDataFile &&
        table.properties() != null && table.properties().containsKey(TableProperties.TABLE_EVENT_TIME_FIELD)) {
      watermark =
          amsFile.getUpperBounds()
              .get(table.properties().get(TableProperties.TABLE_EVENT_TIME_FIELD))
              .getLong();
    }
    String primaryKey = TableMetadataUtil.getTableAllIdentifyName(identifier) + tableType + amsFile.getPath();
    String primaryKeyMd5 = Hashing.md5()
        .hashBytes(primaryKey.getBytes(StandardCharsets.UTF_8))
        .toString();
    Long parentId = snapshot.parentId() == null ? -1 : snapshot.parentId();
    String producer =
        snapshot.summary().getOrDefault(SnapshotSummary.SNAPSHOT_PRODUCER, SnapshotSummary.SNAPSHOT_PRODUCER_DEFAULT);
    String partitionName = StringUtils.isEmpty(partitionToPath(amsFile.getPartition())) ?
        null :
        partitionToPath(amsFile.getPartition());
    return new CacheFileInfo(primaryKeyMd5, identifier, snapshot.snapshotId(),
        parentId, null,
        tableType, amsFile.getPath(), amsFile.getFileType(), amsFile.getFileSize(), amsFile.getMask(),
        amsFile.getIndex(), amsFile.getSpecId(), partitionName, snapshot.timestampMillis(),
        amsFile.getRecordCount(), snapshot.operation(), watermark, producer);
  }

  private static String partitionToPath(List<PartitionFieldData> partitionFieldDataList) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < partitionFieldDataList.size(); i++) {
      if (i > 0) {
        sb.append("/");
      }
      sb.append(partitionFieldDataList.get(i).getName()).append("=")
          .append(partitionFieldDataList.get(i).getValue());
    }
    return sb.toString();
  }

  public String getPrimaryKeyMd5() {
    return primaryKeyMd5;
  }

  public void setPrimaryKeyMd5(String primaryKeyMd5) {
    this.primaryKeyMd5 = primaryKeyMd5;
  }

  public Long getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(Long commitTime) {
    this.commitTime = commitTime;
  }

  public String getPartitionName() {
    return partitionName;
  }

  public void setPartitionName(String partitionName) {
    this.partitionName = partitionName;
  }

  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public Long getAddSnapshotId() {
    return addSnapshotId;
  }

  public void setAddSnapshotId(Long addSnapshotId) {
    this.addSnapshotId = addSnapshotId;
  }

  public Long getParentSnapshotId() {
    return parentSnapshotId;
  }

  public void setParentSnapshotId(Long parentSnapshotId) {
    this.parentSnapshotId = parentSnapshotId;
  }

  public Long getDeleteSnapshotId() {
    return deleteSnapshotId;
  }

  public void setDeleteSnapshotId(Long deleteSnapshotId) {
    this.deleteSnapshotId = deleteSnapshotId;
  }

  public String getInnerTable() {
    return innerTable;
  }

  public void setInnerTable(String innerTable) {
    this.innerTable = innerTable;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public String getProducer() {
    return producer;
  }

  public void setProducer(String producer) {
    this.producer = producer;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public Long getFileMask() {
    return fileMask;
  }

  public void setFileMask(Long fileMask) {
    this.fileMask = fileMask;
  }

  public Long getFileIndex() {
    return fileIndex;
  }

  public void setFileIndex(Long fileIndex) {
    this.fileIndex = fileIndex;
  }

  public Long getSpecId() {
    return specId;
  }

  public void setSpecId(Long specId) {
    this.specId = specId;
  }

  public Long getRecordCount() {
    return recordCount;
  }

  public void setRecordCount(Long recordCount) {
    this.recordCount = recordCount;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Long getWatermark() {
    return watermark;
  }

  public void setWatermark(Long watermark) {
    this.watermark = watermark;
  }

  @Override
  public String toString() {
    return "CacheFileInfo{" +
        "tableIdentifier=" + tableIdentifier +
        ", addSnapshotId=" + addSnapshotId +
        ", deleteSnapshotId=" + deleteSnapshotId +
        ", innerTable='" + innerTable + '\'' +
        ", filePath='" + filePath + '\'' +
        ", fileType='" + fileType + '\'' +
        ", fileSize=" + fileSize +
        ", fileMask=" + fileMask +
        ", fileIndex=" + fileIndex +
        ", specId=" + specId +
        ", partitionName='" + partitionName + '\'' +
        ", commitTime=" + commitTime +
        ", recordCount=" + recordCount +
        ", action='" + action + '\'' +
        ", watermark=" + watermark +
        '}';
  }
}
