package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.server.dashboard.model.TableOptimizingProcess;
import com.netease.arctic.server.optimizing.MetricsSummary;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.persistence.converter.JsonSummaryConverter;
import com.netease.arctic.server.persistence.converter.Long2TsConvertor;
import com.netease.arctic.server.table.ServerTableIdentifier;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface OptimizingMapper {

  /**
   * OptimizingProcess operation below
   */
  @Delete("delete from table_optimizing_process where table_id =#{tableId} and process_id < #{time}")
  void deleteOptimizingProcessBefore(@Param("tableId") long tableId, @Param("time") long time);

  @Insert("insert into table_optimizing_process(table_id,catalog_name,db_name,table_name,process_id," +
      "target_snapshot_id,status,optimizing_type,plan_time,summary) values (#{table.id},#{table.catalog},#{table" +
      ".database},#{table.tableName},#{processId},#{targetSnapshotId},#{status}, #{optimizingType}," +
      "#{planTime, typeHandler=com.netease.arctic.ams.server.persistence.converter.Long2TsConvertor}," +
      "#{summary, typeHandler=com.netease.arctic.ams.server.persistence.converter.JsonSummaryConverter})")
  void insertOptimizingProcess(
      @Param("table") ServerTableIdentifier tableIdentifier,
      @Param("processId") long processId,
      @Param("targetSnapshotId") long targetSnapshotId,
      @Param("status") OptimizingProcess.Status status,
      @Param("optimizingType") OptimizingType optimizingType,
      @Param("planTime") long planTime,
      @Param("summary") MetricsSummary summary);

  @Update("UPDATE optimizing_table_process SET status = #{status}, endTime = #{endTime} " +
      "WHERE tableId = #{tableId} AND processId = #{processId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("optimizingStatus") OptimizingProcess.Status status,
      @Param("endTime") long endTime);

  @Select("SELECT process_id, table_id, catalog_name, db_name, table_name, target_snapshot_id, status, " +
      "optimizing_type, plan_time, end_time, fail_reason, summary " +
      "FROM table_optimizing_process " +
      "WHERE table_id = #{tableId}")
  @Results({
      @Result(property = "processId", column = "process_id"),
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "catalogName", column = "catalog_name"),
      @Result(property = "dbName", column = "db_name"),
      @Result(property = "tableName", column = "table_name"),
      @Result(property = "targetSnapshotId", column = "target_snapshot_id"),
      @Result(property = "status", column = "status"),
      @Result(property = "optimizingType", column = "optimizing_type"),
      @Result(property = "planTime", column = "plan_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "failReason", column = "fail_reason"),
      @Result(property = "summary", column = "summary")
  })
  List<TableOptimizingProcess> selectOptimizingProcesses(@Param("tableId") long tableId);

  @Select("SELECT process_id, table_id, catalog_name, db_name, table_name, target_snapshot_id, status, " +
      "optimizing_type, plan_time, end_time, fail_reason, summary FROM table_optimizing_process " +
      "WHERE catalog_name = #{catalogName} and db_name = #{dbName} and table_name = #{tableName}")
  @Results({
      @Result(property = "processId", column = "process_id"),
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "catalogName", column = "catalog_name"),
      @Result(property = "dbName", column = "db_name"),
      @Result(property = "tableName", column = "table_name"),
      @Result(property = "targetSnapshotId", column = "target_snapshot_id"),
      @Result(property = "status", column = "status"),
      @Result(property = "optimizingType", column = "optimizing_type"),
      @Result(property = "planTime", column = "plan_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "failReason", column = "fail_reason"),
      @Result(property = "summary", column = "summary")
  })
  List<TableOptimizingProcess> selectOptimizingProcessesByTable(@Param("catalogName") String catalogName, @Param(
      "dbName") String dbName, @Param("tableName") String tableName);

  /**
   * Optimizing TaskRuntime operation below
   */
  @Insert({
      "<script>",
      "INSERT INTO task_runtime (process_id, task_id, retry_num, table_id, `partition`, start_time, " +
          "end_time, status, fail_reason, optimizer_token, thread_id, rewrite_output, metrics_summary) VALUES ",
      "<foreach collection='taskRuntimes' item='taskRuntime' index='index' separator=','>",
      "(#{taskRuntime.taskId.processId}, #{taskRuntime.taskId.taskId}, #{taskRuntime.retry}, #{taskRuntime" +
          ".tableId}, #{taskRuntime.partition}, #{taskRuntime.startTime}, #{taskRuntime" +
          ".endTime}, #{taskRuntime.status}, #{taskRuntime.failReason}, #{taskRuntime.optimizingThread" +
          ".token}, #{taskRuntime.optimizingThread.threadId}, #{taskRuntime.outputBytes, typeHandler=com.netease" +
          ".arctic.ams.server.persistence.converter.JsonSummaryConverter}, #{taskRuntime.summary, typeHandler=com" +
          ".netease.arctic.ams.server.persistence.converter.JsonSummaryConverter})",
      "</foreach>",
      "</script>"
  })
  void insertTaskRuntimes(@Param("taskRuntimes") List<TaskRuntime> taskRuntimes);

  @Select("SELECT process_id, task_id, retry_num, table_id, `partition`,  create_time, start_time, end_time, status, " +
      "fail_reason, optimizer_token, thread_id, rewrite_output, metrics_summary FROM task_runtime WHERE table_id = " +
      "#{table_id} AND process_id = #{process_id}")
  @Results({
      @Result(property = "taskId.processId", column = "process_id"),
      @Result(property = "taskId.taskId", column = "task_id"),
      @Result(property = "retry", column = "retry_num"),
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "partition", column = "partition"),
      @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "status", column = "status"),
      @Result(property = "failReason", column = "fail_reason"),
      @Result(property = "optimizerId", column = "optimizer_id"),
      @Result(property = "threadId", column = "thread_id"),
      @Result(property = "outputBytes", column = "rewrite_output", typeHandler = JsonSummaryConverter.class),
      @Result(property = "summary", column = "metrics_summary", typeHandler = JsonSummaryConverter.class)
  })
  List<TaskRuntime> selectTaskRuntimes(@Param("table_id") long tableId, @Param("process_id") long processId);

  @Update("UPDATE task_runtime SET retry_num = #{taskRuntime.retry}, " +
      "start_time = #{taskRuntime.startTime, typeHandler=com.netease.arctic.ams.server.persistence.converter" +
      ".Long2TsConvertor}, end_time = #{taskRuntime.endTime, typeHandler=com.netease.arctic.ams.server.persistence" +
      ".converter.Long2TsConvertor}, " +
      "cost_time = #{taskRuntime.costTime}, status = #{taskRuntime.status}, " +
      "fail_reason = #{taskRuntime.failReason}, rewrite_output = #{taskRuntime.outputBytes, typeHandler=com.netease" +
      ".arctic.ams.server.persistence.converter.JsonSummaryConverter} " +
      "WHERE process_id = #{taskRuntime.taskId.processId} AND task_id = #{taskRuntime.taskId.taskId}")
  void updateTaskRuntime(@Param("taskRuntime") TaskRuntime taskRuntime);

  @Update("UPDATE task_runtime SET status = #{status} WHERE process_id = #{taskRuntime.taskId.processId} AND " +
      "task_id = #{taskRuntime.taskId.taskId}")
  void updateTaskStatus(@Param("taskRuntime") TaskRuntime taskRuntime, TaskRuntime.Status status);

  @Delete("delete from task_runtime where table_id = #{tableId} and process_id < #{time}")
  void deleteTaskRuntimesBefore(@Param("tableId") long tableId, @Param("time") long time);

  /**
   * Optimizing rewrite input and output operations below
   */
  @Update("UPDATE table_optimizing_process SET rewrite_input = #{fileSerialization} WHERE process_id = #{processId}")
  void updateProcessInputFiles(
      @Param("processId") long processId,
      @Param("fileSerialization") byte[] content);

  @Select("SELECT rewrite_input FROM table_optimizing_process WHERE process_id = #{processId}")
  byte[] selectProcessInputFiles(@Param("processId") long processId);

  /**
   * Optimizing task quota operations below
   */
  @Select("SELECT process_id, task_id, retry_num, table_id, start_time, end_time, fail_reason " +
      "FROM optimizing_task_quota " +
      "WHERE table_id = #{tableId} AND process_id >= #{startTime}")
  @Results({
      @Result(property = "processId", column = "process_id"),
      @Result(property = "taskId", column = "task_id"),
      @Result(property = "retryNum", column = "retry_num"),
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "failReason", column = "fail_reason")
  })
  List<TaskRuntime.TaskQuota> selectTaskQuotasByTime(
      @Param("tableId") long tableId,
      @Param("startTime") long startTime);

  @Insert("replace INTO optimizing_task_quota (process_id, task_id, retry_num, table_id, start_time, end_time, " +
      "fail_reason) VALUES (#{taskQuota.processId}, #{taskQuota.taskId}, #{taskQuota.retryNum}, #{taskQuota.tableId}," +
      "#{taskQuota.startTime, typeHandler=com.netease.arctic.ams.server.persistence.converter.Long2TsConvertor}, " +
      "#{taskQuota.endTime, typeHandler=com.netease.arctic.ams.server.persistence.converter.Long2TsConvertor}, " +
      "#{taskQuota.failReason})")
  void insertTaskQuota(@Param("taskQuota") TaskRuntime.TaskQuota taskQuota);

  @Delete("DELETE FROM optimizing_task_quota " +
      "WHERE table_id = #{table_id} AND process_id < #{time}")
  void deleteOptimizingQuotaBefore(@Param("table_id") long tableId, @Param("time") long timestamp);
}
