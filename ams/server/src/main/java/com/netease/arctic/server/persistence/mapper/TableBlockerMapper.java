package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.server.persistence.converter.List2StringConverter;
import com.netease.arctic.server.persistence.converter.Long2TsConvertor;
import com.netease.arctic.server.persistence.converter.Map2StringConverter;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.blocker.TableBlocker;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TableBlockerMapper {
  String TABLE_NAME = "table_blocker";

  @Select("select blocker_id,catalog_name,db_name,table_name,operations,create_time," +
      "expiration_time,properties from " + TABLE_NAME + " " +
      "where catalog_name = #{tableIdentifier.catalog} and db_name = #{tableIdentifier.database} " +
      "and table_name = #{tableIdentifier.tableName} " +
      "and expiration_time > #{now, typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor}")
  @Results({
      @Result(property = "blockerId", column = "blocker_id"),
      @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
      @Result(property = "tableIdentifier.database", column = "db_name"),
      @Result(property = "tableIdentifier.tableName", column = "table_name"),
      @Result(property = "operations", column = "operations",
          typeHandler = List2StringConverter.class),
      @Result(property = "createTime", column = "create_time",
          typeHandler = Long2TsConvertor.class),
      @Result(property = "expirationTime", column = "expiration_time",
          typeHandler = Long2TsConvertor.class),
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  List<TableBlocker> selectBlockers(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier,
                                    @Param("now") long now);

  @Select("select blocker_id,catalog_name,db_name,table_name,operations,create_time," +
      "expiration_time,properties from " + TABLE_NAME + " " +
      "where blocker_id = #{blockerId} " +
      "and expiration_time > #{now, typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor}")
  @Results({
      @Result(property = "blockerId", column = "blocker_id"),
      @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
      @Result(property = "tableIdentifier.database", column = "db_name"),
      @Result(property = "tableIdentifier.tableName", column = "table_name"),
      @Result(property = "operations", column = "operations",
          typeHandler = List2StringConverter.class),
      @Result(property = "createTime", column = "create_time",
          typeHandler = Long2TsConvertor.class),
      @Result(property = "expirationTime", column = "expiration_time",
          typeHandler = Long2TsConvertor.class),
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  TableBlocker selectBlocker(@Param("blockerId") long blockerId, @Param("now") long now);

  @Insert("insert into " + TABLE_NAME + " (catalog_name,db_name,table_name,operations,create_time," +
      "expiration_time,properties) values (" +
      "#{blocker.tableIdentifier.catalog}," +
      "#{blocker.tableIdentifier.database}," +
      "#{blocker.tableIdentifier.tableName}," +
      "#{blocker.operations,typeHandler=com.netease.arctic.ams.server.mybatis.List2StringConverter}," +
      "#{blocker.createTime,typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor}," +
      "#{blocker.expirationTime,typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor}," +
      "#{blocker.properties,typeHandler=com.netease.arctic.ams.server.mybatis.Map2StringConverter}" +
      ")")
  @Options(useGeneratedKeys = true, keyProperty = "blocker.blockerId")
  void insertBlocker(@Param("blocker") TableBlocker blocker);

  @Update("update " + TABLE_NAME + " set " +
      "expiration_time = #{expirationTime,typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor} " +
      "where blocker_id = #{blockerId}")
  void updateBlockerExpirationTime(@Param("blockerId") long blockerId, @Param("expirationTime") long expirationTime);

  @Delete("delete from " + TABLE_NAME + " " +
      "where blocker_id = #{blockerId}")
  void deleteBlocker(@Param("blockerId") long blockerId);

  @Delete("delete from " + TABLE_NAME + " " +
      "where catalog_name = #{tableIdentifier.catalog} and db_name = #{tableIdentifier.database} " +
      "and table_name = #{tableIdentifier.tableName} " +
      "and expiration_time <= #{now, typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor}")
  int deleteExpiredBlockers(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier, @Param("now") long now);

  @Delete("delete from " + TABLE_NAME + " " +
      "where catalog_name = #{tableIdentifier.catalog} and db_name = #{tableIdentifier.database} " +
      "and table_name = #{tableIdentifier.tableName}")
  int deleteBlockers(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier);
}
