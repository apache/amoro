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

import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.server.persistence.converter.List2StringConverter;
import org.apache.amoro.server.persistence.converter.Long2TsConverter;
import org.apache.amoro.server.persistence.converter.Map2StringConverter;
import org.apache.amoro.server.table.blocker.TableBlocker;
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

  @Select(
      "SELECT blocker_id,catalog_name,db_name,table_name,operations,create_time,"
          + "expiration_time,properties FROM "
          + TABLE_NAME
          + " "
          + "WHERE catalog_name = #{catalog} "
          + "AND db_name = #{database} "
          + "AND table_name = #{tableName} "
          + "AND expiration_time > #{now, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}")
  @Results({
    @Result(property = "blockerId", column = "blocker_id"),
    @Result(property = "catalog", column = "catalog_name"),
    @Result(property = "database", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(
        property = "operations",
        column = "operations",
        typeHandler = List2StringConverter.class),
    @Result(property = "createTime", column = "create_time", typeHandler = Long2TsConverter.class),
    @Result(
        property = "expirationTime",
        column = "expiration_time",
        typeHandler = Long2TsConverter.class),
    @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class)
  })
  List<TableBlocker> selectBlockers(
      @Param("catalog") String catalog,
      @Param("database") String database,
      @Param("tableName") String tableName,
      @Param("now") long now);

  @Select(
      "SELECT blocker_id,catalog_name,db_name,table_name,operations,create_time,"
          + "expiration_time,properties FROM "
          + TABLE_NAME
          + " "
          + "WHERE blocker_id = #{blockerId} "
          + "AND expiration_time > #{now, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}")
  @Results({
    @Result(property = "blockerId", column = "blocker_id"),
    @Result(property = "catalog", column = "catalog_name"),
    @Result(property = "database", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(
        property = "operations",
        column = "operations",
        typeHandler = List2StringConverter.class),
    @Result(property = "createTime", column = "create_time", typeHandler = Long2TsConverter.class),
    @Result(
        property = "expirationTime",
        column = "expiration_time",
        typeHandler = Long2TsConverter.class),
    @Result(property = "properties", column = "properties", typeHandler = Map2StringConverter.class)
  })
  TableBlocker selectBlocker(@Param("blockerId") long blockerId, @Param("now") long now);

  @Update(
      "UPDATE "
          + TABLE_NAME
          + "SET "
          + "expiration_time = #{expiration, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}"
          + "WHERE blocker_id = #{blockerId} "
          + "AND expiration_time > #{now, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}")
  int renewBlocker(
      @Param("blockerId") long blockerId,
      @Param("now") long now,
      @Param("expiration") long expiration);

  @Insert(
      "INSERT INTO "
          + TABLE_NAME
          + " (catalog_name,db_name,table_name,operations,create_time,expiration_time,properties) "
          + "SELECT "
          + "#{blocker.catalog},"
          + "#{blocker.database},"
          + "#{blocker.tableName},"
          + "#{blocker.operations,typeHandler=org.apache.amoro.server.persistence.converter.List2StringConverter},"
          + "#{blocker.createTime,typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + "#{blocker.expirationTime,typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + "#{blocker.properties,typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter} "
          + "FROM "
          + TABLE_NAME
          + " WHERE NOT EXISTS (SELECT 1 FROM "
          + TABLE_NAME
          + " WHERE "
          + "catalog_name = #{blocker.catalog} "
          + "AND db_name = #{blocker.database} "
          + "AND table_name = #{blocker.tableName} "
          + "AND expiration_time > #{now, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter})")
  int insertWhenNotExists(@Param("blocker") TableBlocker blocker, @Param("now") long now);

  @Insert(
      "INSERT INTO "
          + TABLE_NAME
          + "(catalog_name,db_name,table_name,operations,create_time,expiration_time,properties)"
          + "SELECT "
          + "#{blocker.catalog},"
          + "#{blocker.database},"
          + "#{blocker.tableName},"
          + "#{blocker.operations,typeHandler=org.apache.amoro.server.persistence.converter.List2StringConverter},"
          + "#{blocker.createTime,typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + "#{blocker.expirationTime,typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + "#{blocker.properties,typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter} "
          + "FROM "
          + TABLE_NAME
          + " WHERE (SELECT max(blocker_id) FROM "
          + TABLE_NAME
          + " WHERE "
          + "catalog_name = #{blocker.tableIdentifier.catalog} AND db_name = #{blocker.tableIdentifier.database} "
          + "table_name = #{blocker.tableIdentifier.tableName}) = #{maxId}")
  @Options(useGeneratedKeys = true, keyProperty = "blocker.blockerId")
  int insertWithMaxBlockerId(@Param("blocker") TableBlocker blocker, @Param("maxId") Long maxId);

  @Delete("DELETE FROM " + TABLE_NAME + " " + "WHERE blocker_id = #{blockerId}")
  void deleteBlocker(@Param("blockerId") long blockerId);

  @Delete(
      "DELETE FROM "
          + TABLE_NAME
          + " "
          + "WHERE catalog_name = #{catalog} AND db_name = #{database} "
          + "AND table_name = #{tableName} "
          + "AND expiration_time <= #{now, typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter}")
  int deleteExpiredBlockers(
      @Param("tableIdentifier") ServerTableIdentifier tableIdentifier, @Param("now") long now);

  @Delete(
      "DELETE FROM "
          + TABLE_NAME
          + " "
          + "WHERE catalog_name = #{catalog} AND db_name = #{database} "
          + "AND table_name = #{tableName}")
  int deleteBlockers(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier);
}
