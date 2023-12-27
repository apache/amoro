package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.server.process.QuotaProvider;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface QuotaMapper {

  @Insert(
      "INSERT INTO table_quota_runtime (table_id, start_process_id, start_time, quota_runtime, quota_target, reset) "
          + "VALUES (#{quota.tableId}, #{quota.startProcessId}, #{quota.startTime}, #{quota.quotaRuntime}, #{quota.quotaTarget}, #{quota.reset})")
  void insertQuota(@Param("quota") QuotaProvider quota);

  @Update(
      "UPDATE table_quota_runtime "
          + "SET start_process_id = #{quota.startProcessId}, "
          + "start_time = #{quota.startTime}, "
          + "quota_runtime = #{quota.quotaRuntime}, "
          + "quota_target = #{quota.quotaTarget}, "
          + "reset = #{quota.reset} "
          + "WHERE table_id = #{quota.tableId}")
  void updateQuota(@Param("quota") QuotaProvider quota);

  @Delete("DELETE FROM table_quota_runtime WHERE table_id = #{tableId}")
  void deleteQuota(@Param("tableId") long tableId);
}
