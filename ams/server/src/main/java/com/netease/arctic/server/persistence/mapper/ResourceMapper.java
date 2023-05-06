package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.persistence.converter.Long2TsConvertor;
import com.netease.arctic.server.persistence.converter.Map2StringConverter;
import com.netease.arctic.server.resource.OptimizerInstance;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ResourceMapper {

  @Select("select group_name, properties, container_name from resource_group")
  @Results({
      @Result(property = "name", column = "group_name"),
      @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class),
      @Result(property = "container", column = "container_name")
  })
  List<ResourceGroup> selectResourceGroups();

  @Select("select group_name, properties, container_name from resource_group where group_name = #{resourceGroup}")
  @Results({
      @Result(property = "name", column = "group_name"),
      @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class),
      @Result(property = "container", column = "container_name")
  })
  ResourceGroup selectResourceGroup(@Param("resourceGroup") String groupName);

  @Select("select resource_id, group_name, container_name, start_time, thread_count, total_memory, properties " +
      "from resource where group_name = #{resourceGroup}")
  @Results({
      @Result(property = "resourceId", column = "resource_id"),
      @Result(property = "group", column = "group_name"),
      @Result(property = "container", column = "container_name"),
      @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "threadCount", column = "thread_count"),
      @Result(property = "totalMemory", column = "total_memory"),
      @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class)
  })
  List<Resource> selectResourcesByGroup(@Param("resourceGroup") String groupName);

  @Update("update resource_group set container_name = #{resourceGroup.container}, properties = #{resourceGroup" +
      ".properties, typeHandler=com.netease.arctic.server.persistence.converter.JsonSummaryConverter} where " +
      "group_name = #{resourceGroup.name}")
  void updateResourceGroup(@Param("resourceGroup") ResourceGroup resourceGroup);

  @Insert("insert into resource_group (group_name, container_name, properties) values (#{resourceGroup.name}, " +
      "#{resourceGroup.container}, #{resourceGroup.properties, typeHandler=com.netease.arctic.server" +
      ".persistence.converter.JsonSummaryConverter})")
  void insertResourceGroup(@Param("resourceGroup") ResourceGroup resourceGroup);

  @Delete("delete from resource_group where group_name = #{resourceGroup.name}")
  void deleteResourceGroup(@Param("resourceGroup") String groupName);

  @Insert("insert into resource (resource_id, group_name, container_name, thread_count, total_memory, " +
      "properties) values (#{resource.resourceId}, #{resource.groupName}, #{resource.containerName}, " +
      "#{resource.threadCount}, #{resource.memoryMb}, #{resource.properties, typeHandler=com.netease.arctic" +
      ".server.persistence.converter.JsonSummaryConverter})")
  void insertResource(@Param("resource") Resource resource);

  @Delete("delete from optimizer_instance where instance_id = #{resourceId}")
  void deleteResource(@Param("resourceId") String resourceId);

  @Select("select * from resource where resource_id = #{resourceId}")
  @Results({
      @Result(property = "resourceId", column = "resource_id"),
      @Result(property = "containerName", column = "container_name"),
      @Result(property = "groupName", column = "group_name"),
      @Result(property = "startTime", column = "start_time", typeHandler = Long2TsConvertor.class),
      @Result(property = "threadCount", column = "thread_count"),
      @Result(property = "memoryMb", column = "total_memory"),
      @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class),
  })
  OptimizerInstance selectResource(@Param("resourceId") String resourceId);
}
