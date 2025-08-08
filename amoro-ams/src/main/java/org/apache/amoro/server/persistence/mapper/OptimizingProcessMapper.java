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

import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.converter.JsonObjectConverter;
import org.apache.amoro.server.persistence.converter.Long2TsConverter;
import org.apache.amoro.server.persistence.converter.Map2StringConverter;
import org.apache.amoro.server.persistence.converter.Object2ByteArrayConvert;
import org.apache.amoro.server.persistence.converter.TaskDescriptorTypeConverter;
import org.apache.amoro.server.persistence.extension.InListExtendedLanguageDriver;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface OptimizingProcessMapper {
  @Insert(
      "INSERT INTO optimizing_process_state "
          + "(process_id, table_id, target_snapshot_id, target_change_snapshot_id, "
          + "from_sequence, to_sequence) "
          + "VALUES (#{processId}, #{tableId}, #{targetSnapshotId}, #{targetChangeSnapshotId}, "
          + " #{fromSequence, typeHandler=org.apache.amoro.server.persistence.converter.MapLong2StringConverter},"
          + " #{toSequence, typeHandler=org.apache.amoro.server.persistence.converter.MapLong2StringConverter})")
  void insertInternalProcessState(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("targetSnapshotId") long targetSnapshotId,
      @Param("targetChangeSnapshotId") long targetChangeSnapshotId,
      @Param("fromSequence") Map<String, Long> fromSequence,
      @Param("toSequence") Map<String, Long> toSequence);

  @Delete(
      "DELETE FROM optimizing_process_state WHERE process_id <= #{processId} AND table_id = #{tableId}")
  void deleteProcessStateBefore(@Param("tableId") long tableId, @Param("processId") long processId);

  @Select("SELECT rewrite_input FROM optimizing_process_state WHERE process_id = #{processId}")
  @Results({@Result(column = "rewrite_input", jdbcType = JdbcType.BLOB)})
  List<byte[]> selectProcessInputFiles(@Param("processId") long processId);

  @Update(
      "UPDATE optimizing_process_state SET rewrite_input = #{input, jdbcType=BLOB,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Object2ByteArrayConvert}"
          + " WHERE process_id = #{processId}")
  void updateProcessInputFiles(
      @Param("processId") long processId, @Param("input") Map<Integer, RewriteFilesInput> input);

  /** Optimizing TaskRuntime operation below */
  @Insert({
    "<script>",
    "INSERT INTO task_runtime (process_id, task_id, retry_num, table_id, partition_data, start_time, "
        + "end_time, status, fail_reason, optimizer_token, thread_id, rewrite_output, metrics_summary, properties) "
        + "VALUES ",
    "<foreach collection='taskRuntimes' item='taskRuntime' index='index' separator=','>",
    "(#{taskRuntime.taskId.processId}, #{taskRuntime.taskId.taskId}, #{taskRuntime.runTimes},"
        + " #{taskRuntime.taskDescriptor.tableId}, #{taskRuntime.taskDescriptor.partition}, "
        + "#{taskRuntime.startTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
        + " #{taskRuntime.endTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}, "
        + "#{taskRuntime.status}, #{taskRuntime.failReason, jdbcType=VARCHAR},"
        + " #{taskRuntime.token, jdbcType=VARCHAR}, #{taskRuntime.threadId, "
        + "jdbcType=INTEGER}, #{taskRuntime.taskDescriptor.output, jdbcType=BLOB, "
        + " typeHandler=org.apache.amoro.server.persistence.converter.Object2ByteArrayConvert},"
        + " #{taskRuntime.taskDescriptor.summary, typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter},"
        + "#{taskRuntime.taskDescriptor.properties, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter})",
    "</foreach>",
    "</script>"
  })
  void insertTaskRuntimes(@Param("taskRuntimes") List<TaskRuntime<RewriteStageTask>> taskRuntimes);

  @Select(
      "SELECT process_id, task_id, 'executing' as stage, retry_num, table_id, partition_data,  create_time, start_time, end_time,"
          + " status, fail_reason, optimizer_token, thread_id, rewrite_output, metrics_summary, properties FROM "
          + "task_runtime WHERE table_id = #{table_id} AND process_id = #{process_id}")
  @Results({
    @Result(property = "taskId.processId", column = "process_id"),
    @Result(property = "taskId.taskId", column = "task_id"),
    @Result(
        property = "taskDescriptor",
        column = "stage",
        typeHandler = TaskDescriptorTypeConverter.class),
    @Result(property = "runTimes", column = "retry_num"),
    @Result(property = "taskDescriptor.tableId", column = "table_id"),
    @Result(property = "taskDescriptor.partition", column = "partition_data"),
    @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConverter.class),
    @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConverter.class),
    @Result(property = "status", column = "status"),
    @Result(property = "failReason", column = "fail_reason"),
    @Result(property = "token", column = "optimizer_token"),
    @Result(property = "threadId", column = "thread_id"),
    @Result(
        property = "taskDescriptor.output",
        column = "rewrite_output",
        typeHandler = Object2ByteArrayConvert.class),
    @Result(
        property = "taskDescriptor.summary",
        column = "metrics_summary",
        typeHandler = JsonObjectConverter.class),
    @Result(
        property = "taskDescriptor.properties",
        column = "properties",
        typeHandler = Map2StringConverter.class)
  })
  List<TaskRuntime<RewriteStageTask>> selectTaskRuntimes(
      @Param("table_id") long tableId, @Param("process_id") long processId);

  @Select(
      "<script>"
          + "SELECT process_id, task_id, retry_num, table_id, partition_data, create_time, start_time, end_time, "
          + "cost_time, status, fail_reason, optimizer_token, thread_id, metrics_summary, properties FROM task_runtime "
          + "WHERE process_id IN"
          + "<foreach item='item' index='index' collection='processIds' open='(' separator=',' close=')'>"
          + "#{item}"
          + "</foreach>"
          + "</script>")
  @Results({
    @Result(property = "processId", column = "process_id"),
    @Result(property = "taskId", column = "task_id"),
    @Result(property = "retryNum", column = "retry_num"),
    @Result(property = "tableId", column = "table_id"),
    @Result(property = "partitionData", column = "partition_data"),
    @Result(property = "createTime", column = "create_time", typeHandler = Long2TsConverter.class),
    @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConverter.class),
    @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConverter.class),
    @Result(property = "costTime", column = "cost_time"),
    @Result(property = "status", column = "status"),
    @Result(property = "failReason", column = "fail_reason"),
    @Result(property = "optimizerToken", column = "optimizer_token"),
    @Result(property = "threadId", column = "thread_id"),
    @Result(
        property = "metricsSummary",
        column = "metrics_summary",
        typeHandler = JsonObjectConverter.class),
    @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class)
  })
  List<OptimizingTaskMeta> selectOptimizeTaskMetas(@Param("processIds") List<Long> processIds);

  @Update(
      "UPDATE task_runtime SET retry_num = #{taskRuntime.runTimes}, "
          + "start_time = #{taskRuntime.startTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " end_time = #{taskRuntime.endTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " cost_time = #{taskRuntime.costTime}, status = #{taskRuntime.status},"
          + " fail_reason = #{taskRuntime.failReason, jdbcType=VARCHAR},"
          + " optimizer_token = #{taskRuntime.token, jdbcType=VARCHAR},"
          + " thread_id = #{taskRuntime.threadId, jdbcType=INTEGER},"
          + " rewrite_output = #{taskRuntime.taskDescriptor.output, jdbcType=BLOB,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Object2ByteArrayConvert},"
          + " metrics_summary = #{taskRuntime.taskDescriptor.summary,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter},"
          + " properties = #{taskRuntime.taskDescriptor.properties,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter}"
          + " WHERE process_id = #{taskRuntime.taskId.processId} AND "
          + "task_id = #{taskRuntime.taskId.taskId}")
  void updateTaskRuntime(
      @Param("taskRuntime") TaskRuntime<? extends StagedTaskDescriptor<?, ?, ?>> taskRuntime);

  @Delete("DELETE FROM task_runtime WHERE table_id = #{tableId} AND process_id < #{expireId}")
  void deleteTaskRuntimesBefore(@Param("tableId") long tableId, @Param("expireId") long expireId);

  /** Optimizing task quota operations below */
  @Select(
      "SELECT process_id, task_id, retry_num, table_id, start_time, end_time, fail_reason "
          + "FROM optimizing_task_quota WHERE table_id = #{tableId} AND process_id >= #{minProcessId}")
  @Results(
      id = "taskQuota",
      value = {
        @Result(property = "processId", column = "process_id"),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "retryNum", column = "retry_num"),
        @Result(property = "tableId", column = "table_id"),
        @Result(
            property = "startTime",
            column = "start_time",
            typeHandler = Long2TsConverter.class),
        @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConverter.class),
        @Result(property = "failReason", column = "fail_reason")
      })
  List<TaskRuntime.TaskQuota> selectTaskQuotasByTime(
      @Param("tableId") long tableId, @Param("minProcessId") long minProcessId);

  @Select(
      "SELECT process_id, task_id, retry_num, table_id, start_time, end_time, fail_reason "
          + "FROM optimizing_task_quota WHERE table_id in (#{tables::number[]}) AND process_id >= #{minProcessId}")
  @Lang(InListExtendedLanguageDriver.class)
  @ResultMap("taskQuota")
  List<TaskRuntime.TaskQuota> selectTableQuotas(
      @Param("tables") Collection<Long> tables, @Param("minProcessId") long minProcessId);

  @Insert(
      "INSERT INTO optimizing_task_quota (process_id, task_id, retry_num, table_id, start_time, end_time,"
          + " fail_reason) VALUES (#{taskQuota.processId}, #{taskQuota.taskId}, #{taskQuota.retryNum},"
          + " #{taskQuota.tableId},"
          + " #{taskQuota.startTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}, "
          + " #{taskQuota.endTime, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " #{taskQuota.failReason, jdbcType=VARCHAR})")
  void insertTaskQuota(@Param("taskQuota") TaskRuntime.TaskQuota taskQuota);

  @Delete(
      "DELETE FROM optimizing_task_quota WHERE table_id = #{table_id} AND process_id < #{expireId}")
  void deleteOptimizingQuotaBefore(
      @Param("table_id") long tableId, @Param("expireId") long expireId);
}
