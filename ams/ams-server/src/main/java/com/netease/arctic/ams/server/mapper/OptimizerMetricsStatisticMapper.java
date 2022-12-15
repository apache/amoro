/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.mapper;

import com.netease.arctic.ams.server.model.OptimizerMetricsStatistic;
import com.netease.arctic.ams.server.mybatis.Long2TsConvertor;
import com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface OptimizerMetricsStatisticMapper {

  String TABLE_NAME = "optimizer_metric_statistics";

  @Insert("insert into " + TABLE_NAME + " (optimizer_id, subtask_id, metric_name, metric_value) values(" +
      "#{metricsStatistic.optimizerId}, #{metricsStatistic.subtaskId}," +
      "#{metricsStatistic.metricName}, #{metricsStatistic.metricValue})")
  void insertMetricsStatistic(@Param("metricsStatistic") OptimizerMetricsStatistic metricsStatistic);

  @Update("update " + TABLE_NAME + " set metric_value = #{metricsStatistic.metricValue}, commit_time = " +
      "#{metricsStatistic.commitTime,typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor} where " +
      "metric_name = #{metricsStatistic.metricName} and optimizer_id = #{metricsStatistic.optimizerId} and subtask_id" +
      " = #{metricsStatistic.subtaskId}")
  void updateMetricsStatistic(@Param("metricsStatistic") OptimizerMetricsStatistic metricsStatistic);

  @Select("select optimizer_id, subtask_id, metric_name, metric_value, commit_time from " + TABLE_NAME + "  " +
      "where metric_name = #{metricsStatistic.metricName} and optimizer_id = #{metricsStatistic.optimizerId} and " +
      "subtask_id = #{metricsStatistic.subtaskId}")
  @Results({
      @Result(column = "optimizer_id", property = "optimizerId",
          typeHandler = TableIdentifier2StringConverter.class),
      @Result(column = "subtask_id", property = "subtaskId"),
      @Result(column = "metric_name", property = "metricName"),
      @Result(column = "metric_value", property = "metricValue"),
      @Result(column = "commit_time", property = "commitTime",
          typeHandler = Long2TsConvertor.class)
  })
  List<OptimizerMetricsStatistic> getMetricsStatistic(
      @Param("metricsStatistic") OptimizerMetricsStatistic metricsStatistic);

  @Insert("insert into metric_statistics_summary (metric_name, metric_value,commit_time) select * from (select " +
      "metric_name, avg(CAST(metric_value AS SIGNED)), #{commitTime, typeHandler=com.netease.arctic.ams.server" +
      ".mybatis.Long2TsConvertor} from " + TABLE_NAME + " where metric_name = #{metricName}) as mna where metric_name" +
      " is not null")
  void summaryMetrics(
      @Param("metricName") String metricName,
      @Param("commitTime") long commitTime);

  @Delete("delete from " + TABLE_NAME + " where optimizer_id = #{optimizerId}")
  void deleteOptimizerMetrics(@Param("optimizerId") long optimizerId);
}
