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

import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.persistence.converter.Long2TsConverter;
import org.apache.amoro.server.persistence.converter.Map2StringConverter;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ResourceMapper {

  @Select("SELECT group_name, properties, container_name FROM resource_group")
  @Results({
    @Result(property = "name", column = "group_name"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
    @Result(property = "container", column = "container_name")
  })
  List<ResourceGroup> selectResourceGroups();

  @Select(
      "SELECT group_name, properties, container_name FROM resource_group WHERE group_name = #{resourceGroup}")
  @Results({
    @Result(property = "name", column = "group_name"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
    @Result(property = "container", column = "container_name")
  })
  ResourceGroup selectResourceGroup(@Param("resourceGroup") String groupName);

  @Select(
      "SELECT resource_id, group_name, container_name, start_time, thread_count, total_memory, properties"
          + " FROM resource WHERE group_name = #{resourceGroup}")
  @Results({
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "group", column = "group_name"),
    @Result(property = "container", column = "container_name"),
    @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConverter.class),
    @Result(property = "threadCount", column = "thread_count"),
    @Result(property = "totalMemory", column = "total_memory"),
    @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class)
  })
  List<Resource> selectResourcesByGroup(@Param("resourceGroup") String groupName);

  @Update(
      "UPDATE resource_group SET container_name = #{resourceGroup.container},"
          + " properties = #{resourceGroup.properties,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter}"
          + " WHERE group_name = #{resourceGroup.name}")
  void updateResourceGroup(@Param("resourceGroup") ResourceGroup resourceGroup);

  @Insert(
      "INSERT INTO resource_group (group_name, container_name, properties)"
          + " VALUES (#{resourceGroup.name}, #{resourceGroup.container},"
          + " #{resourceGroup.properties, typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter})")
  void insertResourceGroup(@Param("resourceGroup") ResourceGroup resourceGroup);

  @Delete("DELETE FROM resource_group WHERE group_name = #{name}")
  void deleteResourceGroup(@Param("name") String groupName);

  @Insert(
      "INSERT INTO resource (resource_id, group_name, container_name, thread_count, total_memory, properties)"
          + " VALUES (#{resource.resourceId}, #{resource.groupName}, #{resource.containerName},"
          + " #{resource.threadCount}, #{resource.memoryMb},"
          + " #{resource.properties, typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter})")
  void insertResource(@Param("resource") Resource resource);

  @Delete("DELETE FROM resource WHERE resource_id = #{resourceId}")
  void deleteResource(@Param("resourceId") String resourceId);

  @Select("SELECT * FROM resource WHERE resource_id = #{resourceId}")
  @Results({
    @Result(property = "resourceId", column = "resource_id"),
    @Result(property = "containerName", column = "container_name"),
    @Result(property = "groupName", column = "group_name"),
    @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConverter.class),
    @Result(property = "threadCount", column = "thread_count"),
    @Result(property = "memoryMb", column = "total_memory"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
  })
  OptimizerInstance selectResource(@Param("resourceId") String resourceId);
}
