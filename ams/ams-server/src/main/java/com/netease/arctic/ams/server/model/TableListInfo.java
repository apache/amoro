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

public class TableListInfo {
  private String tableName;
  private String creator;
  private String userId;
  private Long createTime;
  private Long storage;
  private Integer snapshotCount;
  private Long visibleTime;
  private String viewUrl;
  private String dataSyncStatus;
  private String ingestionStatus;

  public TableListInfo() {
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Long getStorage() {
    return storage;
  }

  public void setStorage(Long storage) {
    this.storage = storage;
  }

  public Integer getSnapshotCount() {
    return snapshotCount;
  }

  public void setSnapshotCount(Integer snapshotCount) {
    this.snapshotCount = snapshotCount;
  }

  public Long getVisibleTime() {
    return visibleTime;
  }

  public void setVisibleTime(Long visibleTime) {
    this.visibleTime = visibleTime;
  }

  public String getViewUrl() {
    return viewUrl;
  }

  public void setViewUrl(String viewUrl) {
    this.viewUrl = viewUrl;
  }

  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  public String getDataSyncStatus() {
    return dataSyncStatus;
  }

  public void setDataSyncStatus(String dataSyncStatus) {
    this.dataSyncStatus = dataSyncStatus;
  }

  public String getIngestionStatus() {
    return ingestionStatus;
  }

  public void setIngestionStatus(String ingestionStatus) {
    this.ingestionStatus = ingestionStatus;
  }

  @Override
  public String toString() {
    return "TableListInfo{" +
        "tableName='" + tableName + '\'' +
        ", creator='" + creator + '\'' +
        ", userId='" + userId + '\'' +
        ", createTime=" + createTime +
        ", storage=" + storage +
        ", snapshotCount=" + snapshotCount +
        ", visibleTime=" + visibleTime +
        ", viewUrl='" + viewUrl + '\'' +
        ", dataSyncStatus='" + dataSyncStatus + '\'' +
        ", ingestionStatus='" + ingestionStatus + '\'' +
        '}';
  }
}
