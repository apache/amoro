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

import org.apache.amoro.server.persistence.BucketAssignMeta;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** MyBatis mapper for bucket assignment table. Provides concrete SQL in annotations. */
public interface BucketAssignMapper {

  /**
   * Insert or update bucket assignments for a node.
   *
   * @param meta bucket assignment entity
   * @return affected rows
   */
  @Insert(
      "INSERT INTO bucket_assign "
          + "(cluster_name, node_key, bucket_ids_json, last_update_time) "
          + "VALUES (#{meta.clusterName}, #{meta.nodeKey}, #{meta.bucketIdsJson}, #{meta.lastUpdateTime}) "
          + "ON DUPLICATE KEY UPDATE "
          + "bucket_ids_json = VALUES(bucket_ids_json), last_update_time = VALUES(last_update_time)")
  int upsertAssignments(@Param("meta") BucketAssignMeta meta);

  /**
   * Select bucket assignments for a specific node.
   *
   * @param clusterName cluster name
   * @param nodeKey node key (host:thriftBindPort)
   * @return bucket assignment or null
   */
  @Select(
      "SELECT cluster_name, node_key, bucket_ids_json, last_update_time "
          + "FROM bucket_assign "
          + "WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  @Results(
      id = "BucketAssignMetaMap",
      value = {
        @Result(column = "cluster_name", property = "clusterName"),
        @Result(column = "node_key", property = "nodeKey"),
        @Result(column = "bucket_ids_json", property = "bucketIdsJson"),
        @Result(column = "last_update_time", property = "lastUpdateTime")
      })
  BucketAssignMeta selectAssignments(
      @Param("clusterName") String clusterName, @Param("nodeKey") String nodeKey);

  /**
   * Select all bucket assignments for a cluster.
   *
   * @param clusterName cluster name
   * @return list of bucket assignments
   */
  @Select(
      "SELECT cluster_name, node_key, bucket_ids_json, last_update_time "
          + "FROM bucket_assign "
          + "WHERE cluster_name = #{clusterName}")
  @ResultMap("BucketAssignMetaMap")
  List<BucketAssignMeta> selectAllAssignments(@Param("clusterName") String clusterName);

  /**
   * Delete bucket assignments for a specific node.
   *
   * @param clusterName cluster name
   * @param nodeKey node key (host:thriftBindPort)
   * @return affected rows
   */
  @Delete(
      "DELETE FROM bucket_assign "
          + "WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  int deleteAssignments(@Param("clusterName") String clusterName, @Param("nodeKey") String nodeKey);

  /**
   * Update last update time for a node.
   *
   * @param clusterName cluster name
   * @param nodeKey node key (host:thriftBindPort)
   * @param lastUpdateTime last update timestamp
   * @return affected rows
   */
  @Update(
      "UPDATE bucket_assign SET last_update_time = #{lastUpdateTime} "
          + "WHERE cluster_name = #{clusterName} AND node_key = #{nodeKey}")
  int updateLastUpdateTime(
      @Param("clusterName") String clusterName,
      @Param("nodeKey") String nodeKey,
      @Param("lastUpdateTime") Long lastUpdateTime);
}
