package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.server.persistence.TableProcessPersistence;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TableProcessMapper {

  @Insert(
      "INSERT INTO table_live_process "
          + "(table_id, table_action, process_id, retry_num) "
          + "VALUES (#{tableId}, #{action}, #{processId})")
  void insertLiveProcess(
      @Param("tableId") long tableId,
      @Param("action") Action action,
      @Param("processId") long processId,
      @Param("isDefault") boolean isDefault);

  @Delete(
      "DELETE FROM table_live_process "
          + "WHERE table_id = #{tableId} AND table_action = #{action} AND process_id = #{processId}")
  void deleteLiveProcess(
      @Param("tableId") long tableId,
      @Param("action") Action action,
      @Param("processId") long processId);

  @Update(
      "UPDATE table_live_process SET retry_num = #{retryCount} "
          + "WHERE table_id = #{tableId} AND table_action = #{action} AND process_id = #{processId}")
  void updateProcessAction(
      @Param("tableId") long tableId,
      @Param("action") Action action,
      @Param("processId") long processId,
      @Param("retryCount") int retryCount);

  @Select(
      "SELECT tlp.table_id, tlp.table_action, tlp.process_id, tlp.retry_num, tlp.is_default, tap.process_name, "
          + "ap.status, tap.start_time, tap.end_time, tap.fail_reason, tap.summary FROM table_live_process tlp "
          + "LEFT JOIN table_arbitrary_process tap ON tlp.process_id = tap.process_id "
          + "WHERE tlp.table_id = #{tableId} "
          + "AND tlp.table_action = #{action}")
  @Results({
    @Result(property = "tableId", column = "table_id"),
    @Result(property = "tableAction", column = "table_action"),
    @Result(property = "processId", column = "process_id"),
    @Result(property = "status", column = "status"),
    @Result(property = "retryNum", column = "retry_num"),
    @Result(property = "startTime", column = "start_time"),
    @Result(property = "endTime", column = "end_time"),
    @Result(property = "processName", column = "process_name"),
    @Result(property = "failReason", column = "fail_reason"),
    @Result(property = "summary", column = "summary"),
    @Result(property = "isDefault", column = "is_default")
  })
  List<TableProcessPersistence> selectLiveProcesses(
      @Param("tableId") long tableId, @Param("action") Action action);
}
