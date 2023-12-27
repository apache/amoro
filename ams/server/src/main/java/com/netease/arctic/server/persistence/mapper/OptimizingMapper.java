package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.server.persistence.OptimizingStatePersistency;
import com.netease.arctic.server.persistence.TaskRuntimePersistency;
import com.netease.arctic.server.persistence.converter.JsonObjectConverter;
import com.netease.arctic.server.persistence.converter.Long2TsConverter;
import com.netease.arctic.server.persistence.converter.Map2StringConverter;
import com.netease.arctic.server.persistence.converter.MapLong2StringConverter;
import com.netease.arctic.server.persistence.converter.Object2ByteArrayConvert;
import com.netease.arctic.server.process.DefaultOptimizingState;
import com.netease.arctic.server.process.OptimizingSummary;
import com.netease.arctic.server.process.OptimizingType;
import com.netease.arctic.server.process.TaskRuntime;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Map;

public interface OptimizingMapper {

  /** OptimizingProcess operation below */
  @Delete(
      "DELETE FROM table_optimizing_process WHERE table_id = #{tableId} and process_id < #{time}")
  void deleteOptimizingProcessBefore(@Param("tableId") long tableId, @Param("time") long time);

  void insertOptimizingProcess(@Param("optimizingState") DefaultOptimizingState optimizingState);

  @Select(
      "SELECT a.process_id, a.table_id, a.catalog_name, a.db_name, a.table_name, a.target_snapshot_id,"
          + " a.target_change_snapshot_id, a.status, a.optimizing_type, a.plan_time, a.end_time,"
          + " a.fail_reason, a.summary, a.from_sequence, a.to_sequence FROM table_optimizing_process a"
          + " INNER JOIN table_identifier b ON a.table_id = b.table_id"
          + " WHERE a.catalog_name = #{catalogName} AND a.db_name = #{dbName} AND a.table_name = #{tableName}"
          + " AND b.catalog_name = #{catalogName} AND b.db_name = #{dbName} AND b.table_name = #{tableName}"
          + " ORDER BY process_id desc")
  @Results({
    @Result(property = "processId", column = "process_id"),
    @Result(property = "tableId", column = "table_id"),
    @Result(property = "catalogName", column = "catalog_name"),
    @Result(property = "dbName", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "targetSnapshotId", column = "target_snapshot_id"),
    @Result(property = "targetChangeSnapshotId", column = "target_change_snapshot_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "optimizingType", column = "optimizing_type"),
    @Result(property = "planTime", column = "plan_time", typeHandler = Long2TsConverter.class),
    @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConverter.class),
    @Result(property = "failReason", column = "fail_reason"),
    @Result(property = "summary", column = "summary", typeHandler = JsonObjectConverter.class),
    @Result(
        property = "fromSequence",
        column = "from_sequence",
        typeHandler = MapLong2StringConverter.class),
    @Result(
        property = "toSequence",
        column = "to_sequence",
        typeHandler = MapLong2StringConverter.class)
  })
  List<OptimizingStatePersistency> selectOptimizingProcesses(
      @Param("catalogName") String catalogName,
      @Param("dbName") String dbName,
      @Param("tableName") String tableName);

  /** Optimizing TaskRuntime operation below */
  @Insert({
    "<script>",
    "INSERT INTO task_runtime (process_id, task_id, retry_num, table_id, partition_data, start_time, "
        + "end_time, status, fail_reason, optimizer_token, thread_id, rewrite_output, metrics_summary, properties) "
        + "VALUES ",
    "<foreach collection='taskRuntimes' item='taskRuntime' index='index' separator=','>",
    "(#{taskRuntime.taskId.processId}, #{taskRuntime.taskId.taskId}, #{taskRuntime.runTimes},"
        + " #{taskRuntime.tableId}, #{taskRuntime.partition}, "
        + "#{taskRuntime.startTime, typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConverter},"
        + " #{taskRuntime.endTime, typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConverter}, "
        + "#{taskRuntime.status}, #{taskRuntime.failReason, jdbcType=VARCHAR},"
        + " #{taskRuntime.token, jdbcType=VARCHAR}, #{taskRuntime.threadId, "
        + "jdbcType=INTEGER}, #{taskRuntime.output, jdbcType=BLOB, "
        + " typeHandler=com.netease.arctic.server.persistence.converter.Object2ByteArrayConvert},"
        + " #{taskRuntime.summary, typeHandler=com.netease.arctic.server.persistence.converter.JsonObjectConverter},"
        + "#{taskRuntime.properties, typeHandler=com.netease.arctic.server.persistence.converter.Map2StringConverter})",
    "</foreach>",
    "</script>"
  })
  void insertOptimizingTasks(
      @Param("taskRuntimes") List<TaskRuntime<RewriteFilesInput, RewriteFilesOutput>> taskRuntimes);

  @Select(
      "SELECT process_id, task_id, retry_num, table_id, partition_data,  create_time, start_time, end_time,"
          + " status, fail_reason, optimizer_token, thread_id, rewrite_output, metrics_summary, properties FROM "
          + "task_runtime WHERE table_id = #{table_id} AND process_id = #{process_id}")
  @Results({
    @Result(property = "taskId.processId", column = "process_id"),
    @Result(property = "taskId.taskId", column = "task_id"),
    @Result(property = "runTimes", column = "retry_num"),
    @Result(property = "tableId", column = "table_id"),
    @Result(property = "partition", column = "partition_data"),
    @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConverter.class),
    @Result(property = "endTime", column = "end_time", typeHandler = Long2TsConverter.class),
    @Result(property = "status", column = "status"),
    @Result(property = "failReason", column = "fail_reason"),
    @Result(property = "token", column = "optimizer_token"),
    @Result(property = "threadId", column = "thread_id"),
    @Result(
        property = "output",
        column = "rewrite_output",
        typeHandler = Object2ByteArrayConvert.class),
    @Result(
        property = "summary",
        column = "metrics_summary",
        typeHandler = JsonObjectConverter.class),
    @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class)
  })
  List<TaskRuntime<?, ?>> selectTaskRuntimes(
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
  List<TaskRuntimePersistency> selectOptimizeTaskMetas(@Param("processIds") List<Long> processIds);

  @Update(
      "UPDATE task_runtime SET retry_num = #{taskRuntime.runTimes}, "
          + "start_time = #{taskRuntime.startTime,"
          + " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConverter},"
          + " end_time = #{taskRuntime.endTime,"
          + " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConverter},"
          + " cost_time = #{taskRuntime.costTime}, status = #{taskRuntime.status},"
          + " fail_reason = #{taskRuntime.failReason, jdbcType=VARCHAR},"
          + " optimizer_token = #{taskRuntime.token, jdbcType=VARCHAR},"
          + " thread_id = #{taskRuntime.threadId, jdbcType=INTEGER},"
          + " rewrite_output = #{taskRuntime.output, jdbcType=BLOB,"
          + " typeHandler=com.netease.arctic.server.persistence.converter.Object2ByteArrayConvert},"
          + " metrics_summary = #{taskRuntime.summary,"
          + " typeHandler=com.netease.arctic.server.persistence.converter.JsonObjectConverter},"
          + " properties = #{taskRuntime.properties,"
          + " typeHandler=com.netease.arctic.server.persistence.converter.Map2StringConverter}"
          + " WHERE process_id = #{taskRuntime.taskId.processId} AND "
          + "task_id = #{taskRuntime.taskId.taskId}")
  void updateTaskRuntime(@Param("taskRuntime") TaskRuntime taskRuntime);

  @Update(
      "UPDATE task_runtime SET status = #{status} WHERE process_id = #{taskRuntime.taskId.processId} AND "
          + "task_id = #{taskRuntime.taskId.taskId}")
  void updateTaskStatus(
      @Param("taskRuntime") TaskRuntime taskRuntime, @Param("status") TaskRuntime.Status status);

  @Delete("DELETE FROM task_runtime WHERE table_id = #{tableId} AND process_id < #{time}")
  void deleteTaskRuntimesBefore(@Param("tableId") long tableId, @Param("time") long time);

  /** Optimizing rewrite input and output operations below */
  @Update(
      "UPDATE table_optimizing_process SET rewrite_input = #{input, jdbcType=BLOB,"
          + " typeHandler=com.netease.arctic.server.persistence.converter.Object2ByteArrayConvert}"
          + " WHERE process_id = #{processId}")
  void updateProcessInputFiles(
      @Param("processId") long processId, @Param("input") Map<Integer, RewriteFilesInput> input);

  @Select("SELECT rewrite_input FROM table_optimizing_process WHERE process_id = #{processId}")
  @Results({@Result(column = "rewrite_input", jdbcType = JdbcType.BLOB)})
  List<byte[]> selectProcessInputFiles(@Param("processId") long processId);

  @Delete("DELETE FROM optimizing_task_quota WHERE table_id = #{table_id} AND process_id < #{time}")
  void deleteOptimizingQuotaBefore(@Param("table_id") long tableId, @Param("time") long timestamp);

  @Update(
      "UPDATE table_optimizing_process SET status = #{optimizingStatus},"
          + " WHERE table_id = #{tableId} AND process_id = #{processId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("status") ProcessStatus status);

  /** update table_process optimizing_type field */
  @Update(
      "UPDATE table_optimizing_process SET status = #{optimizingStatus},"
          + "summary = #{summary, typeHandler=com.netease.arctic.server.persistence.converter.JsonObjectConverter}, "
          + " WHERE table_id = #{tableId} AND process_id = #{processId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("status") ProcessStatus status,
      @Param("summary") OptimizingSummary summary);

  @Update(
      "UPDATE table_optimizing_process SET status = #{processStatus},"
          + " end_time = #{endTime, typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConverter}, "
          + "summary = #{summary, typeHandler=com.netease.arctic.server.persistence.converter.JsonObjectConverter}, "
          + "fail_reason = #{failedReason, jdbcType=VARCHAR}"
          + " WHERE table_id = #{tableId} AND process_id = #{processId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("processStatus") ProcessStatus status,
      @Param("endTime") long endTime,
      @Param("failedReason") String failedReason);

  @Update(
      "UPDATE table_optimizing_process SET status = #{optimizingStatus},"
          + " optimizing_type = #{optimizingType},"
          + " task_count = #{taskCount},"
          + " end_time = #{endTime, typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConverter}, "
          + "summary = #{summary}, "
          + "fail_reason = #{failedReason, jdbcType=VARCHAR}"
          + " WHERE table_id = #{tableId} AND process_id = #{processId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("optimizingType") OptimizingType optimizingType,
      @Param("endTime") long endTime,
      @Param("status") ProcessStatus status,
      @Param("taskCount") int taskCount,
      @Param("summary") String summary,
      @Param("failedReason") String failedReason);

  void insertTaskRuntime(TaskRuntime<?, ?> committingTask);
}
