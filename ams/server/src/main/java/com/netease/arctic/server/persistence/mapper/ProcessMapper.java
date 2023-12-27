package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.ams.api.process.OptimizingStage;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.process.DefaultOptimizingState;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProcessMapper {

  @Delete(
      "DELETE FROM table_optimizing_process WHERE table_id = #{tableId} and process_id < #{time}")
  void deleteOptimizingProcessBefore(@Param("tableId") long tableId, @Param("time") long time);

  @Insert(
      "INSERT INTO table_optimizing_process "
          + "(process_id, table_id, catalog_name, db_name, table_name, target_snapshot_id, last_snapshot_id, watermark, stage, stage_start_time, status, optimizing_type, plan_time, end_time, fail_reason, rewrite_input, summary) "
          + "VALUES "
          + "(#{optimizingState.id}, #{optimizingState.tableIdentifier.tableId}, #{optimizingState.tableIdentifier.catalogName}, #{optimizingState.tableIdentifier.dbName}, #{optimizingState.tableIdentifier.tableName}, "
          + "#{optimizingState.targetSnapshotId}, #{optimizingState.lastSnapshotId}, #{optimizingState.watermark}, #{optimizingState.stage}, FROM_UNIXTIME(#{optimizingState.currentStageStartTime}), "
          + "#{optimizingState.status}, #{optimizingState.optimizingType}, FROM_UNIXTIME(#{optimizingState.startTime}), FROM_UNIXTIME(#{optimizingState.endTime}), "
          + "#{optimizingState.failedReason}, #{persistenceHelper.writeRewriteInput(optimizingState)}, #{optimizingState.summary})")
  void insertDefaultOptimizingProcess(
      @Param("optimizingState") DefaultOptimizingState optimizingState);

  @Update(
      "UPDATE table_optimizing_process "
          + "SET table_id = #{optimizingState.tableIdentifier.tableId}, "
          + "catalog_name = #{optimizingState.tableIdentifier.catalogName}, "
          + "db_name = #{optimizingState.tableIdentifier.dbName}, "
          + "table_name = #{optimizingState.tableIdentifier.tableName}, "
          + "target_snapshot_id = #{optimizingState.targetSnapshotId}, "
          + "last_snapshot_id = #{optimizingState.lastSnapshotId}, "
          + "watermark = #{optimizingState.watermark}, "
          + "stage = #{optimizingState.stage}, "
          + "stage_start_time = FROM_UNIXTIME(#{optimizingState.currentStageStartTime}), "
          + "status = #{optimizingState.status}, "
          + "optimizing_type = #{optimizingState.optimizingType}, "
          + "plan_time = FROM_UNIXTIME(#{optimizingState.startTime}), "
          + "end_time = FROM_UNIXTIME(#{optimizingState.endTime}), "
          + "fail_reason = #{optimizingState.failedReason}, "
          + "rewrite_input = #{persistenceHelper.writeRewriteInput(optimizingState)}, "
          + "summary = #{optimizingState.summary} "
          + "WHERE process_id = #{optimizingState.id}")
  void updateDefaultOptimizingProcess(
      @Param("optimizingState") DefaultOptimizingState optimizingState);

  @Update(
      "UPDATE table_optimizing_process "
          + "stage = #{stage}, "
          + "stage_start_time = FROM_UNIXTIME(#{currentStageStartTime}) "
          + "WHERE process_id = #{id} and table_id = #{tableId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("stage") OptimizingStage stage,
      @Param("currentStageStartTime") long currentStageStartTime);

  @Update(
      "UPDATE table_optimizing_process "
          + "SET id = #{id}, "
          + "table_id = #{id1}, "
          + "status = #{status}, "
          + "stage = #{stage}, "
          + "stage_start_time = FROM_UNIXTIME(#{currentStageStartTime}), "
          + "end_time = FROM_UNIXTIME(#{endTime}), "
          + "fail_reason = #{failedReason} "
          + "WHERE process_id = #{id} and table_id = #{tableId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("status") ProcessStatus status,
      @Param("stage") OptimizingStage stage,
      @Param("currentStageStartTime") long currentStageStartTime,
      @Param("endTime") long endTime,
      @Param("failedReason") String failedReason);

  @Update(
      "UPDATE table_optimizing_process "
          + "SET status = #{status}, "
          + "stage = #{stage}, "
          + "stage_start_time = FROM_UNIXTIME(#{currentStageStartTime}), "
          + "end_time = FROM_UNIXTIME(#{endTime}), "
          + "task_count = #{taskCount}, "
          + "summary = #{summary}, "
          + "fail_reason = #{failedReason} "
          + "WHERE process_id = #{processId} and table_id = #{tableId}")
  void updateOptimizingProcess(
      @Param("tableId") long tableId,
      @Param("processId") long processId,
      @Param("status") ProcessStatus status,
      @Param("stage") OptimizingStage stage,
      @Param("currentStageStartTime") long currentStageStartTime,
      @Param("endTime") long endTime,
      @Param("taskCount") int taskCount,
      @Param("summary") String summary,
      @Param("failedReason") String failedReason);
}
