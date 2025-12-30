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

package org.apache.amoro.server.persistence;

import java.util.Objects;

/**
 * Entity representing bucket assignment stored in the database.
 *
 * <p>Fields: - cluster_name: AMS cluster name - node_key: node key (host:thriftBindPort) -
 * bucket_ids_json: JSON array of assigned bucket IDs - last_update_time: last update timestamp
 * (milliseconds)
 */
public class BucketAssignMeta {
  private String clusterName;
  private String nodeKey;
  private String bucketIdsJson;
  private Long lastUpdateTime;

  public BucketAssignMeta() {}

  public BucketAssignMeta(
      String clusterName, String nodeKey, String bucketIdsJson, Long lastUpdateTime) {
    this.clusterName = clusterName;
    this.nodeKey = nodeKey;
    this.bucketIdsJson = bucketIdsJson;
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getNodeKey() {
    return nodeKey;
  }

  public void setNodeKey(String nodeKey) {
    this.nodeKey = nodeKey;
  }

  public String getBucketIdsJson() {
    return bucketIdsJson;
  }

  public void setBucketIdsJson(String bucketIdsJson) {
    this.bucketIdsJson = bucketIdsJson;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    BucketAssignMeta that = (BucketAssignMeta) other;
    return Objects.equals(clusterName, that.clusterName)
        && Objects.equals(nodeKey, that.nodeKey)
        && Objects.equals(bucketIdsJson, that.bucketIdsJson)
        && Objects.equals(lastUpdateTime, that.lastUpdateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, nodeKey, bucketIdsJson, lastUpdateTime);
  }
}
