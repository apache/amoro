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
 * Entity representing an HA lease stored in the database.
 *
 * <p>Fields: - cluster_name: AMS cluster name - service_name: service name (AMS, TABLE_SERVICE,
 * OPTIMIZING_SERVICE) - node_id: unique node identifier - node_ip: node IP address -
 * server_info_json: serialized AmsServerInfo - lease_expire_ts: lease expiration timestamp
 * (milliseconds) - version: row version used for optimistic locking - updated_at: last update
 * timestamp (milliseconds)
 */
public class HaLeaseMeta {
  private String clusterName;
  private String serviceName;
  private String nodeId;
  private String nodeIp;
  private String serverInfoJson;
  private Long leaseExpireTs;
  private Integer version;
  private Long updatedAt;

  public HaLeaseMeta() {}

  public HaLeaseMeta(
      String clusterName,
      String serviceName,
      String nodeId,
      String nodeIp,
      String serverInfoJson,
      Long leaseExpireTs,
      Integer version,
      Long updatedAt) {
    this.clusterName = clusterName;
    this.serviceName = serviceName;
    this.nodeId = nodeId;
    this.nodeIp = nodeIp;
    this.serverInfoJson = serverInfoJson;
    this.leaseExpireTs = leaseExpireTs;
    this.version = version;
    this.updatedAt = updatedAt;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getNodeIp() {
    return nodeIp;
  }

  public void setNodeIp(String nodeIp) {
    this.nodeIp = nodeIp;
  }

  public String getServerInfoJson() {
    return serverInfoJson;
  }

  public void setServerInfoJson(String serverInfoJson) {
    this.serverInfoJson = serverInfoJson;
  }

  public Long getLeaseExpireTs() {
    return leaseExpireTs;
  }

  public void setLeaseExpireTs(Long leaseExpireTs) {
    this.leaseExpireTs = leaseExpireTs;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    HaLeaseMeta haLeaseMeta = (HaLeaseMeta) other;
    return Objects.equals(clusterName, haLeaseMeta.clusterName)
        && Objects.equals(serviceName, haLeaseMeta.serviceName)
        && Objects.equals(nodeId, haLeaseMeta.nodeId)
        && Objects.equals(nodeIp, haLeaseMeta.nodeIp)
        && Objects.equals(serverInfoJson, haLeaseMeta.serverInfoJson)
        && Objects.equals(leaseExpireTs, haLeaseMeta.leaseExpireTs)
        && Objects.equals(version, haLeaseMeta.version)
        && Objects.equals(updatedAt, haLeaseMeta.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        clusterName,
        serviceName,
        nodeId,
        nodeIp,
        serverInfoJson,
        leaseExpireTs,
        version,
        updatedAt);
  }
}
