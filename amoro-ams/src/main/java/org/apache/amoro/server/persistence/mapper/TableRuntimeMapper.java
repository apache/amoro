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

import org.apache.amoro.server.persistence.BucketIdCount;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.TableRuntimeState;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

public interface TableRuntimeMapper {
  String TABLE_NAME = "table_runtime";

  /* ---------- insert ---------- */
  @Insert(
      "INSERT INTO "
          + TABLE_NAME
          + " (table_id, group_name, status_code, table_config, table_summary, bucket_id) "
          + "VALUES (#{tableId}, #{groupName}, #{statusCode}, "
          + "#{tableConfig,typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter}, "
          + "#{tableSummary,typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter}, "
          + "#{bucketId, jdbcType=VARCHAR})")
  int insertRuntime(TableRuntimeMeta meta);

  /* ---------- update ---------- */
  @Update(
      "UPDATE "
          + TABLE_NAME
          + " SET group_name         = #{groupName}, "
          + "     status_code        = #{statusCode}, "
          + "     status_code_update_time = #{statusCodeUpdateTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}, "
          + "     table_config       = #{tableConfig,typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter}, "
          + "     table_summary      = #{tableSummary,typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter}, "
          + "     bucket_id        = #{bucketId, jdbcType=VARCHAR} "
          + " WHERE table_id = #{tableId}")
  int updateRuntime(TableRuntimeMeta meta);

  /* ---------- delete ---------- */
  @Delete("DELETE FROM " + TABLE_NAME + " WHERE table_id = #{tableId}")
  int deleteRuntime(@Param("tableId") Long tableId);

  /* ---------- select ---------- */
  @Select(
      "SELECT table_id, group_name, status_code, status_code_update_time, "
          + "       table_config, table_summary, bucket_id "
          + "FROM "
          + TABLE_NAME
          + " WHERE table_id = #{tableId}")
  @Results(
      id = "tableRuntimeMeta",
      value = {
        @Result(column = "group_name", property = "groupName"),
        @Result(column = "status_code", property = "statusCode"),
        @Result(
            column = "status_code_update_time",
            property = "statusCodeUpdateTime",
            typeHandler = org.apache.amoro.server.persistence.converter.Long2TsConverter.class,
            jdbcType = JdbcType.TIMESTAMP),
        @Result(
            column = "table_config",
            property = "tableConfig",
            typeHandler = org.apache.amoro.server.persistence.converter.Map2StringConverter.class,
            jdbcType = JdbcType.VARCHAR),
        @Result(
            column = "table_summary",
            property = "tableSummary",
            typeHandler = org.apache.amoro.server.persistence.converter.JsonObjectConverter.class,
            jdbcType = JdbcType.VARCHAR),
        @Result(column = "table_id", property = "tableId"),
        @Result(column = "bucket_id", property = "bucketId")
      })
  TableRuntimeMeta selectRuntime(@Param("tableId") Long tableId);

  String SELECT_COLS =
      " table_id, group_name, status_code, status_code_update_time, table_config, table_summary, bucket_id ";

  @Select("SELECT " + SELECT_COLS + "FROM " + TABLE_NAME)
  @ResultMap("tableRuntimeMeta")
  List<TableRuntimeMeta> selectAllRuntimes();

  @Select(
      "<script>"
          + "SELECT "
          + SELECT_COLS
          + "FROM "
          + TABLE_NAME
          + " WHERE bucket_id IN "
          + "<foreach item='item' collection='bucketIds' open='(' separator=',' close=')'>"
          + "#{item}"
          + "</foreach>"
          + " OR bucket_id IS NULL"
          + "</script>")
  @ResultMap("tableRuntimeMeta")
  List<TableRuntimeMeta> selectRuntimesByBucketIds(@Param("bucketIds") List<String> bucketIds);

  @Select(
      "<script>"
          + "<bind name=\"isMySQL\" value=\"_databaseId == 'mysql'\" />"
          + "<bind name=\"isPostgreSQL\" value=\"_databaseId == 'postgres'\" />"
          + "<bind name=\"isDerby\" value=\"_databaseId == 'derby'\" />"
          + "SELECT r.table_id, group_name, status_code, status_code_update_time, table_config, table_summary, bucket_id FROM "
          + TABLE_NAME
          + " r JOIN table_identifier i "
          + " ON r.table_id = i.table_id "
          + "/* Debug: ${_databaseId} */"
          + "WHERE 1=1 "
          + "<if test='groupName != null'> AND r.group_name = #{groupName} </if> "
          + "<if test='fuzzyDbName != null and isMySQL'> AND i.db_name like CONCAT('%', #{fuzzyDbName, jdbcType=VARCHAR}, '%') </if>"
          + "<if test='fuzzyDbName != null and (isPostgreSQL or isDerby)'> AND i.db_name like '%' || #{fuzzyDbName, jdbcType=VARCHAR} || '%' </if>"
          + "<if test='fuzzyTableName != null and isMySQL'> AND i.table_name like CONCAT('%', #{fuzzyTableName, jdbcType=VARCHAR}, '%') </if>"
          + "<if test='fuzzyTableName != null and (isPostgreSQL or isDerby)'> AND i.table_name like '%' || #{fuzzyTableName, jdbcType=VARCHAR} || '%' </if>"
          + "<if test='statusCodeFilter != null and statusCodeFilter.size() > 0'>"
          + "AND status_code IN ("
          + "<foreach item='item' collection='statusCodeFilter' separator=','>"
          + "#{item}"
          + "</foreach> ) </if>"
          + "ORDER BY status_code, status_code_update_time DESC "
          + "</script>")
  @ResultMap("tableRuntimeMeta")
  List<TableRuntimeMeta> queryForGroups(
      @Param("groupName") String groupName,
      @Param("fuzzyDbName") String fuzzyDbName,
      @Param("fuzzyTableName") String fuzzyTableName,
      @Param("statusCodeFilter") List<Integer> statusCodeFilter);

  String STATE_TABLE_NAME = "table_runtime_state";

  @Update(
      "UPDATE "
          + STATE_TABLE_NAME
          + " SET state_value = #{stateValue}, state_version = state_version + 1 "
          + " WHERE table_id = #{tableId} AND state_key = #{stateKey}")
  void setStateValue(
      @Param("tableId") long stateId,
      @Param("stateKey") String stateKey,
      @Param("stateValue") String value);

  @Select(
      "SELECT state_id, table_id, state_key, state_value, state_version FROM "
          + STATE_TABLE_NAME
          + " WHERE table_id = #{tableId} AND state_key = #{stateKey}")
  @Results(
      id = "runtimeState",
      value = {
        @Result(column = "state_id", property = "stateId"),
        @Result(column = "table_id", property = "tableId"),
        @Result(column = "state_key", property = "stateKey"),
        @Result(column = "state_value", property = "stateValue"),
        @Result(column = "state_version", property = "stateVersion"),
      })
  TableRuntimeState getState(@Param("tableId") long tableId, @Param("stateKey") String stateKey);

  @Select(
      "SELECT state_id, table_id, state_key, state_value, state_version FROM " + STATE_TABLE_NAME)
  @ResultMap("runtimeState")
  List<TableRuntimeState> selectAllStates();

  @Insert(
      "INSERT INTO "
          + STATE_TABLE_NAME
          + " (table_id, state_key, state_value, state_version)"
          + " VALUES "
          + "(#{tableId}, #{stateKey}, #{stateValue}, 0)")
  void saveState(
      @Param("tableId") long tableId,
      @Param("stateKey") String stateKey,
      @Param("stateValue") String stateValue);

  @Delete("DELETE FROM " + STATE_TABLE_NAME + " WHERE state_id = #{stateId}")
  void removeState(@Param("stateId") long stateId);

  @Delete("DELETE FROM " + STATE_TABLE_NAME + " WHERE table_id = #{tableId}")
  void removeAllTableStates(@Param("tableId") long tableId);

  /**
   * Count tables per bucketId. Returns a map where key is bucketId and value is the count of tables
   * for that bucketId. Only counts non-null and non-empty bucketIds.
   */
  @Select(
      "SELECT bucket_id, COUNT(*) as table_count FROM "
          + TABLE_NAME
          + " WHERE bucket_id IS NOT NULL AND bucket_id != '' "
          + "GROUP BY bucket_id")
  @Results({
    @Result(column = "bucket_id", property = "bucketId"),
    @Result(column = "table_count", property = "count")
  })
  List<BucketIdCount> countTablesByBucketId();
}
