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

import org.apache.amoro.process.TableProcessState;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

public interface ProcessStateMapper {

  @Insert(
      "INSERT INTO table_process_state "
          + "(process_id, action, table_id, retry_num, status, start_time, end_time, fail_reason, summary) "
          + "VALUES "
          + "(#{id}, #{action}, #{tableId}, #{retryNumber}, #{status}, #{startTime}, #{endTime}, #{failedReason}, #{summary})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void createProcessState(TableProcessState state);

  @Update(
      "UPDATE table_process_state "
          + "SET status = #{status}, start_time = #{startTime} "
          + "WHERE process_id = #{id} and retry_num = #{retryNumber}")
  void updateProcessRunning(TableProcessState state);

  @Update(
      "UPDATE table_process_state "
          + "SET status = #{status}, end_time = #{endTime} "
          + "WHERE process_id = #{id} and retry_num = #{retryNumber}")
  void updateProcessCompleted(TableProcessState state);

  @Update(
      "UPDATE table_process_state "
          + "SET status = #{status}, end_time = #{endTime}, fail_reason = #{failedReason} "
          + "WHERE process_id = #{id} and retry_num = #{retryNumber}")
  void updateProcessFailed(TableProcessState state);

  @Select(
      "SELECT process_id, action, table_id, retry_num, status, start_time, end_time, fail_reason, summary "
          + "FROM table_process_state "
          + "WHERE process_id = #{processId}")
  @Results(
      id = "TableProcessStateResultMap",
      value = {
        @Result(property = "id", column = "process_id"),
        @Result(property = "action", column = "action"),
        @Result(property = "tableId", column = "table_id"),
        @Result(property = "retryNumber", column = "retry_num"),
        @Result(property = "status", column = "status"),
        @Result(property = "startTime", column = "start_time"),
        @Result(property = "endTime", column = "end_time"),
        @Result(property = "failedReason", column = "fail_reason"),
        @Result(property = "summary", column = "summary", javaType = Map.class)
      })
  TableProcessState getProcessStateById(@Param("processId") long processId);

  /** Query TableProcessState by table_id */
  @Select(
      "SELECT process_id, action, table_id, retry_num, status, start_time, end_time, fail_reason, summary "
          + "FROM table_process_state "
          + "WHERE table_id = #{tableId}")
  @ResultMap("TableProcessStateResultMap")
  TableProcessState getProcessStateByTableId(@Param("tableId") long tableId);
}
