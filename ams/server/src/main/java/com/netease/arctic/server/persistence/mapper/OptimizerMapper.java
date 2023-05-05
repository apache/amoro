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

package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.server.resource.OptimizerInstance;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * optimize mapper.
 */
@Mapper
public interface OptimizerMapper {

  @Insert("INSERT INTO optimizer (token, resource_id, group_name, start_time, touch_time, thread_count, total_memory," +
      " properties) VALUES (#{optimizer.token}, #{optimizer.resourceId},  #{optimizer.groupName}," +
      "#{optimizer.startTime, typeHandler=com.netease.arctic.ams.server.persistence.converter.Long2TsConvertor}, " +
      "#{optimizer.touchTime, typeHandler=com.netease.arctic.ams.server.persistence.converter.Long2TsConvertor}, " +
      "#{optimizer.threadCount}, #{optimizer.memoryMb}, " +
      "#{optimizer.properties, typeHandler=com.netease.arctic.ams.server.persistence.converter.JsonSummaryConverter})")
  void insertOptimizer(@Param("optimizer") OptimizerInstance optimizer);

  @Update("update optimizer set touch_time=CURRENT_TIMESTAMP where token = #{token}")
  void updateTouchTime(@Param("token") String token);

  @Delete("delete from optimizer where token = #{token}")
  void deleteOptimizer(@Param("token") String token);
}

