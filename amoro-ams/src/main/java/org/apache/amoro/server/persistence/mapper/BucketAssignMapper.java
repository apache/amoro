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

import org.apache.amoro.server.persistence.BucketAssignmentMeta;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** MyBatis mapper for bucket_assignments table. */
public interface BucketAssignMapper {

  @Insert(
      "INSERT INTO bucket_assignments (cluster_name, node_key, server_info_json, assignments_json, last_update_time) "
          + "VALUES (#{clusterName}, #{nodeKey}, #{serverInfoJson}, #{assignmentsJson}, #{lastUpdateTime})")
  int insert(@Param("meta") BucketAssignmentMeta meta);

  @Update(
      "UPDATE bucket_assignments SET server_info_json = #{serverInfoJson}, assignments_json = #{assignmentsJson}, last_update_time = #{lastUpdateTime} "
          + "WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  int update(
      @Param("clusterName") String clusterName,
      @Param("nodeKey") String nodeKey,
      @Param("serverInfoJson") String serverInfoJson,
      @Param("assignmentsJson") String assignmentsJson,
      @Param("lastUpdateTime") Long lastUpdateTime);

  @Update(
      "UPDATE bucket_assignments SET last_update_time = #{lastUpdateTime} "
          + "WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  int updateLastUpdateTime(
      @Param("clusterName") String clusterName,
      @Param("nodeKey") String nodeKey,
      @Param("lastUpdateTime") Long lastUpdateTime);

  @Select(
      "SELECT cluster_name, node_key, server_info_json, assignments_json, last_update_time "
          + "FROM bucket_assignments WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  @Results(
      id = "BucketAssignmentMetaMap",
      value = {
        @Result(column = "cluster_name", property = "clusterName"),
        @Result(column = "node_key", property = "nodeKey"),
        @Result(column = "server_info_json", property = "serverInfoJson"),
        @Result(column = "assignments_json", property = "assignmentsJson"),
        @Result(column = "last_update_time", property = "lastUpdateTime")
      })
  BucketAssignmentMeta selectByNode(
      @Param("clusterName") String clusterName, @Param("nodeKey") String nodeKey);

  @Select(
      "SELECT cluster_name, node_key, server_info_json, assignments_json, last_update_time "
          + "FROM bucket_assignments WHERE cluster_name = #{clusterName}")
  @ResultMap("BucketAssignmentMetaMap")
  List<BucketAssignmentMeta> selectAllByCluster(@Param("clusterName") String clusterName);

  @Delete(
      "DELETE FROM bucket_assignments WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  int deleteByNode(@Param("clusterName") String clusterName, @Param("nodeKey") String nodeKey);
}
