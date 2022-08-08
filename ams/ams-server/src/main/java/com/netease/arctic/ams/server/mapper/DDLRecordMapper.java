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


import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.model.DDLInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DDLRecordMapper {
  String TABLE_NAME = "ddl_record";

  @Insert("insert into " + TABLE_NAME + " (table_identifier, schema_id, ddl, ddl_type, commit_time) values" +
      "(#{ddlInfo.tableIdentifier, typeHandler=com.netease.arctic.ams.server.mybatis" +
      ".TableIdentifier2StringConverter}, #{ddlInfo.schemaId}, #{ddlInfo.ddl}, #{ddlInfo.ddlType}, #{ddlInfo" +
      ".commitTime,typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor})")
  void insert(@Param("ddlInfo") DDLInfo ddlInfo);

  @Select("select max(schema_id) from " + TABLE_NAME + " where table_identifier = #{tableIdentifier, typeHandler=com" +
      ".netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter} and schema_id is not null")
  List<Integer> getCurrentSchemaId(@Param("tableIdentifier") TableIdentifier tableIdentifier);
}
