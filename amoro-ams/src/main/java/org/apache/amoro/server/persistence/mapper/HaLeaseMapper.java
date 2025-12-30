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

package org.apache.amoro.server.persistence.mapper;

import org.apache.amoro.server.persistence.HaLeaseMeta;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MyBatis mapper for HA lease table. Provides concrete SQL in annotations.
 *
 * <p>Notes: - insertIfAbsent: plain INSERT, duplicate key errors are ignored by caller via
 * doAsIgnoreError. - acquireLease: try to acquire leadership when expired or empty. - renewLease:
 * optimistic lock renewal by version. - selectLease: map a single row to {@link HaLeaseMeta}. -
 * releaseLease: mark lease as expired. - upsertServerInfo: MySQL-style INSERT ... ON DUPLICATE KEY
 * UPDATE for server info.
 */
public interface HaLeaseMapper {

  /**
   * Insert a lease row. If the primary key already exists, the caller may ignore the exception.
   *
   * @param lease lease entity to insert
   * @return affected rows
   */
  @Insert(
      "INSERT INTO ha_lease "
          + "(cluster_name, service_name, node_id, node_ip, server_info_json, lease_expire_ts, version, updated_at) "
          + "VALUES (#{lease.clusterName}, #{lease.serviceName}, #{lease.nodeId}, #{lease.nodeIp}, "
          + "#{lease.serverInfoJson}, #{lease.leaseExpireTs}, #{lease.version}, #{lease.updatedAt})")
  int insertIfAbsent(@Param("lease") HaLeaseMeta lease);

  /**
   * Try to acquire leadership by updating the lease only when it is expired or empty. Increments
   * version and sets new lease_expire_ts.
   *
   * @return affected rows (1 means acquired)
   */
  @Update(
      "UPDATE ha_lease "
          + "SET node_id = #{nodeId}, node_ip = #{nodeIp}, server_info_json = #{serverInfoJson}, "
          + "lease_expire_ts = #{leaseExpireTs}, version = version + 1, updated_at = #{updatedAt} "
          + "WHERE cluster_name = #{clusterName} AND service_name = #{serviceName} "
          + "AND (lease_expire_ts IS NULL OR lease_expire_ts < #{updatedAt})")
  int acquireLease(
      @Param("clusterName") String clusterName,
      @Param("serviceName") String serviceName,
      @Param("nodeId") String nodeId,
      @Param("nodeIp") String nodeIp,
      @Param("serverInfoJson") String serverInfoJson,
      @Param("leaseExpireTs") Long leaseExpireTs,
      @Param("updatedAt") Long updatedAt);

  /**
   * Renew the lease using optimistic concurrency control. Only succeeds if the expected version
   * matches and the lease is not expired.
   *
   * @return affected rows (1 means renewed)
   */
  @Update(
      "UPDATE ha_lease "
          + "SET lease_expire_ts = #{newLeaseExpireTs}, version = version + 1, updated_at = #{updatedAt} "
          + "WHERE cluster_name = #{clusterName} AND service_name = #{serviceName} "
          + "AND node_id = #{nodeId} AND version = #{expectedVersion} AND lease_expire_ts > #{updatedAt}")
  int renewLease(
      @Param("clusterName") String clusterName,
      @Param("serviceName") String serviceName,
      @Param("nodeId") String nodeId,
      @Param("expectedVersion") Integer expectedVersion,
      @Param("newLeaseExpireTs") Long newLeaseExpireTs,
      @Param("updatedAt") Long updatedAt);

  /**
   * Select current lease for cluster and service.
   *
   * @param clusterName cluster name
   * @param serviceName service name
   * @return lease row or null
   */
  @Select(
      "SELECT cluster_name, service_name, node_id, node_ip, server_info_json, lease_expire_ts, version, updated_at "
          + "FROM ha_lease WHERE cluster_name = #{clusterName} AND service_name = #{serviceName}")
  @ResultMap("HaLeaseMetaMap")
  HaLeaseMeta selectLease(
      @Param("clusterName") String clusterName, @Param("serviceName") String serviceName);

  /**
   * Select all leases for cluster and service.
   *
   * @param clusterName cluster name
   * @param serviceName service name
   * @return list of lease rows
   */
  @Select(
      "SELECT cluster_name, service_name, node_id, node_ip, server_info_json, lease_expire_ts, version, updated_at "
          + "FROM ha_lease WHERE cluster_name = #{clusterName} AND service_name = #{serviceName}")
  @ResultMap("HaLeaseMetaMap")
  List<HaLeaseMeta> selectLeasesByService(
      @Param("clusterName") String clusterName, @Param("serviceName") String serviceName);

  /**
   * Select current lease for cluster and service.
   *
   * @param clusterName cluster name
   * @param serviceName service name
   * @return lease row or null
   */
  @Select(
      "SELECT cluster_name, service_name, node_id, node_ip, server_info_json, lease_expire_ts, version, updated_at "
          + "FROM ha_lease")
  @Results(
      id = "HaLeaseMetaMap",
      value = {
        @Result(column = "cluster_name", property = "clusterName"),
        @Result(column = "service_name", property = "serviceName"),
        @Result(column = "node_id", property = "nodeId"),
        @Result(column = "node_ip", property = "nodeIp"),
        @Result(column = "server_info_json", property = "serverInfoJson"),
        @Result(column = "lease_expire_ts", property = "leaseExpireTs"),
        @Result(column = "version", property = "version"),
        @Result(column = "updated_at", property = "updatedAt")
      })
  List<HaLeaseMeta> selectAllLease();

  /**
   * Release the lease by marking it expired at updatedAt.
   *
   * @return affected rows
   */
  @Update(
      "UPDATE ha_lease SET lease_expire_ts = #{updatedAt}, updated_at = #{updatedAt} "
          + "WHERE cluster_name = #{clusterName} AND service_name = #{serviceName} AND node_id = #{nodeId}")
  int releaseLease(
      @Param("clusterName") String clusterName,
      @Param("serviceName") String serviceName,
      @Param("nodeId") String nodeId,
      @Param("updatedAt") Long updatedAt);

  /**
   * Update server info if the row exists (DB-neutral upsert step).
   *
   * @return affected rows (1 if updated, 0 if not found)
   */
  @Update(
      "UPDATE ha_lease SET node_id=#{nodeId}, node_ip=#{nodeIp}, server_info_json=#{serverInfoJson}, updated_at=#{updatedAt} "
          + "WHERE cluster_name=#{clusterName} AND service_name=#{serviceName}")
  int updateServerInfo(
      @Param("clusterName") String clusterName,
      @Param("serviceName") String serviceName,
      @Param("nodeId") String nodeId,
      @Param("nodeIp") String nodeIp,
      @Param("serverInfoJson") String serverInfoJson,
      @Param("updatedAt") Long updatedAt);

  /**
   * Insert server info if absent (DB-neutral upsert step). Sets version=0 for non-leader entries.
   */
  @Insert(
      "INSERT INTO ha_lease (cluster_name, service_name, node_id, node_ip, server_info_json, version, updated_at) "
          + "VALUES (#{clusterName}, #{serviceName}, #{nodeId}, #{nodeIp}, #{serverInfoJson}, 0, #{updatedAt})")
  int insertServerInfoIfAbsent(
      @Param("clusterName") String clusterName,
      @Param("serviceName") String serviceName,
      @Param("nodeId") String nodeId,
      @Param("nodeIp") String nodeIp,
      @Param("serverInfoJson") String serverInfoJson,
      @Param("updatedAt") Long updatedAt);

  /**
   * Upsert server info for non-leader services using MySQL syntax. For Postgres, use: INSERT ... ON
   * CONFLICT (cluster_name, service_name) DO UPDATE SET ...
   *
   * @return affected rows
   */
  @Insert(
      "INSERT INTO ha_lease (cluster_name, service_name, node_id, node_ip, server_info_json, updated_at) "
          + "VALUES (#{clusterName}, #{serviceName}, #{nodeId}, #{nodeIp}, #{serverInfoJson}, #{updatedAt}) "
          + "ON DUPLICATE KEY UPDATE "
          + "node_id = VALUES(node_id), node_ip = VALUES(node_ip), "
          + "server_info_json = VALUES(server_info_json), updated_at = VALUES(updated_at)")
  int upsertServerInfo(
      @Param("clusterName") String clusterName,
      @Param("serviceName") String serviceName,
      @Param("nodeId") String nodeId,
      @Param("nodeIp") String nodeIp,
      @Param("serverInfoJson") String serverInfoJson,
      @Param("updatedAt") Long updatedAt);
}
