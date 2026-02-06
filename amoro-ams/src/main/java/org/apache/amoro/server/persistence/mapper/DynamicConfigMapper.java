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

import org.apache.amoro.server.persistence.DynamicConfigEntry;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Mapper for {@code dynamic_conf} table. */
public interface DynamicConfigMapper {

  String TABLE_NAME = "dynamic_conf";

  /**
   * Load all AMS service level overrides.
   *
   * <p>These are the rows with {@code conf_group = 'AMS'}. {@code plugin_name} is ignored.
   */
  @Select(
      "SELECT conf_key, conf_value, conf_group, plugin_name FROM "
          + TABLE_NAME
          + " WHERE conf_group = 'AMS'")
  @Results({
    @Result(property = "confKey", column = "conf_key"),
    @Result(property = "confValue", column = "conf_value"),
    @Result(property = "confGroup", column = "conf_group"),
    @Result(property = "pluginName", column = "plugin_name")
  })
  List<DynamicConfigEntry> selectServerConfigs();

  /**
   * Load overrides for a specific plugin.
   *
   * @param confGroup configuration group, usually {@code "PLUGIN_" + pluginCategory}
   * @param pluginName plugin identifier
   */
  @Select(
      "SELECT conf_key, conf_value, conf_group, plugin_name FROM "
          + TABLE_NAME
          + " WHERE conf_group = #{confGroup} AND plugin_name = #{pluginName}")
  @Results({
    @Result(property = "confKey", column = "conf_key"),
    @Result(property = "confValue", column = "conf_value"),
    @Result(property = "confGroup", column = "conf_group"),
    @Result(property = "pluginName", column = "plugin_name")
  })
  List<DynamicConfigEntry> selectPluginConfigs(
      @Param("confGroup") String confGroup, @Param("pluginName") String pluginName);
}
