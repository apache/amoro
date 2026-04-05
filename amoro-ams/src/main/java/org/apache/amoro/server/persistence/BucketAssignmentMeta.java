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

/**
 * Entity for a single bucket assignment row: one node (node_key) in a cluster with its assigned
 * bucket IDs and last update time.
 */
public class BucketAssignmentMeta {
  private String clusterName;
  private String nodeKey;
  private String serverInfoJson;
  private String assignmentsJson;
  private Long lastUpdateTime;
  /** Per-node heartbeat timestamp. Updated only by the owning node, never by the leader. */
  private Long nodeHeartbeatTs;

  public BucketAssignmentMeta() {}

  public BucketAssignmentMeta(
      String clusterName,
      String nodeKey,
      String serverInfoJson,
      String assignmentsJson,
      Long lastUpdateTime) {
    this.clusterName = clusterName;
    this.nodeKey = nodeKey;
    this.serverInfoJson = serverInfoJson;
    this.assignmentsJson = assignmentsJson;
    this.lastUpdateTime = lastUpdateTime;
    this.nodeHeartbeatTs = lastUpdateTime;
  }

  public BucketAssignmentMeta(
      String clusterName,
      String nodeKey,
      String serverInfoJson,
      String assignmentsJson,
      Long lastUpdateTime,
      Long nodeHeartbeatTs) {
    this.clusterName = clusterName;
    this.nodeKey = nodeKey;
    this.serverInfoJson = serverInfoJson;
    this.assignmentsJson = assignmentsJson;
    this.lastUpdateTime = lastUpdateTime;
    this.nodeHeartbeatTs = nodeHeartbeatTs;
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

  public String getServerInfoJson() {
    return serverInfoJson;
  }

  public void setServerInfoJson(String serverInfoJson) {
    this.serverInfoJson = serverInfoJson;
  }

  public String getAssignmentsJson() {
    return assignmentsJson;
  }

  public void setAssignmentsJson(String assignmentsJson) {
    this.assignmentsJson = assignmentsJson;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public Long getNodeHeartbeatTs() {
    return nodeHeartbeatTs;
  }

  public void setNodeHeartbeatTs(Long nodeHeartbeatTs) {
    this.nodeHeartbeatTs = nodeHeartbeatTs;
  }
}
