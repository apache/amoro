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

import org.apache.amoro.Action;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.persistence.converter.Action2StringConverter;
import org.apache.amoro.server.persistence.converter.Long2TsConverter;
import org.apache.amoro.server.persistence.converter.Map2StringConverter;
import org.apache.amoro.server.persistence.extension.InListExtendedLanguageDriver;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TableProcessMapper {

  @Delete("DELETE FROM table_process WHERE process_id <= #{processId} AND table_id = #{tableId}")
  void deleteBefore(@Param("tableId") long tableId, @Param("processId") long processId);

  @Insert(
      "INSERT INTO table_process "
          + "(process_id, table_id, external_process_identifier, status, process_type, process_stage, execution_engine, retry_number, "
          + "create_time, process_parameters, summary) "
          + "VALUES (#{processId}, #{tableId}, #{externalProcessIdentifier}, #{status}, "
          + "#{action, typeHandler=org.apache.amoro.server.persistence.converter.Action2StringConverter}, #{processStage}, "
          + "#{executionEngine}, #{retryNumber}, "
          + "#{createTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}, "
          + "#{processParameters, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter}, "
          + "#{summary, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter})")
  void insertProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("externalProcessIdentifier") String externalProcessIdentifier,
      @Param("status") ProcessStatus status,
      @Param("action") Action action,
      @Param("processStage") String processStage,
      @Param("executionEngine") String executionEngine,
      @Param("retryNumber") int retryNumber,
      @Param("createTime") long createTime,
      @Param("processParameters") Map<String, String> processParameters,
      @Param("summary") Map<String, String> summary);

  @Update(
      "UPDATE table_process SET external_process_identifier = #{externalProcessIdentifier},"
          + "status = #{status}, "
          + "process_stage = #{processStage}, "
          + "retry_number = #{retryNumber}, "
          + "finish_time = #{finishTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}, "
          + "fail_message = #{failMessage, jdbcType=VARCHAR}, "
          + "process_parameters = #{processParameters, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter}, "
          + "summary = #{summary, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter} "
          + "WHERE process_id = #{processId} AND table_id = #{tableId}")
  void updateProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("externalProcessIdentifier") String externalProcessIdentifier,
      @Param("status") ProcessStatus status,
      @Param("processStage") String processStage,
      @Param("retryNumber") int retryNumber,
      @Param("finishTime") long finishTime,
      @Param("failMessage") String failMessage,
      @Param("processParameters") Map<String, String> processParameters,
      @Param("summary") Map<String, String> summary);

  @Select(
      "SELECT process_id, table_id, external_process_identifier, status, process_type, process_stage, execution_engine, retry_number, "
          + "create_time, finish_time, fail_message, process_parameters, summary "
          + "FROM table_process WHERE process_id = #{processId}")
  @Results(
      id = "tableProcessMap",
      value = {
        @Result(column = "process_id", property = "processId"),
        @Result(column = "table_id", property = "tableId"),
        @Result(column = "external_process_identifier", property = "externalProcessIdentifier"),
        @Result(column = "status", property = "status"),
        @Result(
            column = "process_type",
            property = "action",
            typeHandler = Action2StringConverter.class),
        @Result(column = "process_stage", property = "processStage"),
        @Result(column = "execution_engine", property = "executionEngine"),
        @Result(column = "retry_number", property = "retryNumber"),
        @Result(
            column = "create_time",
            property = "createTime",
            typeHandler = Long2TsConverter.class),
        @Result(
            column = "finish_time",
            property = "finishTime",
            typeHandler = Long2TsConverter.class),
        @Result(column = "fail_message", property = "failMessage"),
        @Result(
            column = "process_parameters",
            property = "processParameters",
            typeHandler = Map2StringConverter.class),
        @Result(column = "summary", property = "summary", typeHandler = Map2StringConverter.class)
      })
  TableProcessMeta getProcessMeta(@Param("processId") long processId);

  @Select(
      "<script>"
          + "SELECT process_id, table_id, external_process_identifier, status, process_type, process_stage, execution_engine, retry_number, "
          + "create_time, finish_time, fail_message, process_parameters, summary "
          + "FROM table_process WHERE table_id = #{tableId} "
          + " <if test='action != null'> AND process_type = #{action.name}</if>"
          + " <if test='status != null'> AND status = #{status}</if>"
          + " ORDER BY process_id desc"
          + "</script>")
  @ResultMap("tableProcessMap")
  List<TableProcessMeta> listProcessMeta(
      @Param("tableId") long tableId,
      @Param("action") Action action,
      @Param("status") ProcessStatus optimizingStatus);

  @Select(
      "SELECT max(process_id) FROM table_process "
          + "WHERE table_id in (#{tables::number[]}) "
          + "GROUP BY table_id ")
  @Lang(InListExtendedLanguageDriver.class)
  List<Long> selectTableMaxProcessIds(@Param("tables") Collection<Long> tables);

  @Select(
      "SELECT process_id, table_id, external_process_identifier, status, process_type, process_stage, execution_engine, retry_number, "
          + "create_time, finish_time, fail_message, process_parameters, summary "
          + "FROM table_process WHERE status in ('SUBMITTED', 'RUNNING')")
  @ResultMap("tableProcessMap")
  List<TableProcessMeta> selectAllActiveProcesses();
}
