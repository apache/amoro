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

package com.netease.arctic.ams.server.mapper.derby;

import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.server.mapper.FileInfoCacheMapper;
import com.netease.arctic.ams.server.model.CacheFileInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;

public interface DerbyFileInfoCacheMapper extends FileInfoCacheMapper {
  String TABLE_NAME = "file_info_cache";

  @Insert("MERGE INTO " + TABLE_NAME +
          " USING SYSIBM.SYSDUMMY1 " +
          "ON " + TABLE_NAME +
          ".table_identifier = #{cacheFileInfo.tableIdentifier, " +
          "typeHandler=com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter} and " + TABLE_NAME +
          ".file_path = #{cacheFileInfo.filePath}" +
          " and " + TABLE_NAME + ".inner_table = #{cacheFileInfo.innerTable}" +
          " WHEN matched THEN update set add_snapshot_id =#{cacheFileInfo.addSnapshotId}, " +
          " delete_snapshot_id = #{cacheFileInfo.deleteSnapshotId, jdbcType=BIGINT}, " +
          " file_type = #{cacheFileInfo.fileType, jdbcType=VARCHAR}," +
          " file_size = #{cacheFileInfo.fileSize}," +
          " file_mask = #{cacheFileInfo.fileMask}," +
          " file_index = #{cacheFileInfo.fileIndex}," +
          " spec_id = #{cacheFileInfo.specId}," +
          " record_count = #{cacheFileInfo.recordCount}," +
          " action = #{cacheFileInfo.action, jdbcType=VARCHAR}," +
          " partition_name = #{cacheFileInfo.partitionName, jdbcType=VARCHAR}" +
          "WHEN NOT MATCHED THEN INSERT " +
          "(table_identifier, add_snapshot_id, delete_snapshot_id, inner_table, file_path," +
          " file_type, file_size, file_mask, file_index, spec_id, record_count, action, partition_name, commit_time, " +
          "watermark) values(#{cacheFileInfo.tableIdentifier, typeHandler=com" +
          ".netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter}, " +
          "#{cacheFileInfo.addSnapshotId}, #{cacheFileInfo" +
          ".deleteSnapshotId, jdbcType=BIGINT}, #{cacheFileInfo.innerTable, jdbcType=VARCHAR}, " +
          "#{cacheFileInfo.filePath, jdbcType=VARCHAR}, #{cacheFileInfo.fileType, jdbcType=VARCHAR}, " +
          "#{cacheFileInfo.fileSize}, #{cacheFileInfo.fileMask}, #{cacheFileInfo.fileIndex}, " +
          "#{cacheFileInfo.specId}, #{cacheFileInfo.recordCount}, #{cacheFileInfo.action, jdbcType=VARCHAR}, " +
          "#{cacheFileInfo.partitionName, jdbcType=VARCHAR}, " +
          "#{cacheFileInfo.commitTime, typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor}, " +
          "#{cacheFileInfo.watermark, typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor})"
  )
  void insertCache(@Param("cacheFileInfo") CacheFileInfo cacheFileInfo);

  @Select(
          "select watermark from " + TABLE_NAME + " where " +
                  "table_identifier = #{tableIdentifier, typeHandler=com.netease.arctic.ams.server.mybatis" +
                  ".TableIdentifier2StringConverter} and inner_table = #{innerTable} order by " +
                  "watermark desc FETCH FIRST ROW ONLY")
  Timestamp getWatermark(@Param("tableIdentifier") TableIdentifier tableIdentifier,
      @Param("innerTable") String innerTable);
}