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

import org.apache.amoro.server.persistence.converter.Long2TsConverter;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.table.cleanup.TableCleanupProcessMeta;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TableCleanupMapper {
  String TABLE_NAME = "table_cleanup_process";

  /** Insert a new table cleanup process record */
  @Insert(
      "INSERT INTO "
          + TABLE_NAME
          + " (cleanup_process_id, table_id, catalog_name, db_name, table_name, cleanup_operation_code, last_cleanup_end_time) "
          + "VALUES (#{cleanupProcessMeta.cleanupProcessId},"
          + "  #{cleanupProcessMeta.tableId}, #{cleanupProcessMeta.catalogName}, "
          + "#{cleanupProcessMeta.dbName}, #{cleanupProcessMeta.tableName},"
          + "#{cleanupProcessMeta.cleanupOperation,"
          + "   typeHandler=org.apache.amoro.server.persistence.converter.CleanupOperationConverter},"
          + "#{cleanupProcessMeta.lastCleanupEndTime,"
          + "     typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter})")
  void insertTableCleanupProcess(
      @Param("cleanupProcessMeta") TableCleanupProcessMeta cleanupProcessMeta);

  /** Update lastCleanupEndTime field by table_id and cleanup_operation_code */
  @Update(
      "UPDATE "
          + TABLE_NAME
          + " SET last_cleanup_end_time = #{lastCleanupEndTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter} "
          + "WHERE table_id = #{tableId} AND cleanup_operation_code = #{cleanupOperation,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.CleanupOperationConverter}")
  int updateLastCleanupEndTimeByTableIdAndCleanupOperation(
      @Param("tableId") long tableId,
      @Param("cleanupOperation") CleanupOperation cleanupOperation,
      @Param("lastCleanupEndTime") long lastCleanupEndTime);

  /**
   * Select table_id and last_cleanup_end_time by cleanup_operation_code and table_ids If
   * last_cleanup_end_time is NULL in database, it will be converted to 0L as default value
   */
  @Select(
      "<script>"
          + "SELECT table_id,last_cleanup_end_time "
          + "FROM "
          + TABLE_NAME
          + " WHERE cleanup_operation_code = #{cleanupOperation,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.CleanupOperationConverter}"
          + " AND table_id IN "
          + "<foreach item='tableId' collection='tableIds' open='(' separator=',' close=')'>"
          + "#{tableId}"
          + "</foreach>"
          + "</script>")
  @Results({
    @Result(property = "tableId", column = "table_id"),
    @Result(
        property = "lastCleanupEndTime",
        column = "last_cleanup_end_time",
        typeHandler = Long2TsConverter.class)
  })
  List<TableCleanupProcessMeta> selectTableIdAndLastCleanupEndTime(
      @Param("tableIds") List<Long> tableIds,
      @Param("cleanupOperation") CleanupOperation cleanupOperation);

  /** Delete all cleanup process records for a specific table */
  @Delete("DELETE FROM " + TABLE_NAME + " WHERE table_id = #{tableId}")
  void deleteTableCleanupProcesses(@Param("tableId") long tableId);
}
