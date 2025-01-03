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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.converter.JsonObjectConverter;
import org.apache.amoro.server.persistence.converter.Long2TsConverter;
import org.apache.amoro.server.persistence.converter.Map2StringConverter;
import org.apache.amoro.server.persistence.converter.MapLong2StringConverter;
import org.apache.amoro.server.persistence.converter.OptimizingStatusConverter;
import org.apache.amoro.server.persistence.converter.TableFormatConverter;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TableMetaMapper {

  @Insert("INSERT INTO database_metadata(catalog_name, db_name) VALUES( #{catalogName}, #{dbName})")
  void insertDatabase(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Select("SELECT db_name FROM database_metadata WHERE catalog_name = #{catalogName}")
  List<String> selectDatabases(@Param("catalogName") String catalogName);

  @Select(
      "SELECT db_name FROM database_metadata WHERE catalog_name = #{catalogName} AND db_name=#{dbName}")
  String selectDatabase(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Delete(
      "DELETE FROM database_metadata WHERE catalog_name = #{catalogName} AND db_name = #{dbName}"
          + " AND table_count = 0")
  Integer dropDb(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Update(
      "UPDATE database_metadata SET table_count = table_count + #{tableCount} WHERE db_name = #{databaseName}")
  Integer incTableCount(
      @Param("tableCount") Integer tableCount, @Param("databaseName") String databaseName);

  @Update(
      "UPDATE database_metadata SET table_count = table_count - #{tableCount} WHERE db_name = #{databaseName}")
  Integer decTableCount(
      @Param("tableCount") Integer tableCount, @Param("databaseName") String databaseName);

  @Select("SELECT table_count FROM database_metadata WHERE db_name = #{databaseName}")
  Integer selectTableCount(@Param("databaseName") String databaseName);

  @Select(
      "SELECT m.table_id, i.table_name, i.db_name, i.catalog_name, i.format, primary_key, "
          + "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, "
          + "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties, meta_version "
          + "FROM table_metadata m INNER JOIN table_identifier i ON m.table_id = i.table_id ")
  @Results({
    @Result(property = "tableIdentifier.id", column = "table_id"),
    @Result(property = "tableIdentifier.tableName", column = "table_name"),
    @Result(property = "tableIdentifier.database", column = "db_name"),
    @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
    @Result(
        property = "tableIdentifier.format",
        column = "format",
        typeHandler = TableFormatConverter.class),
    @Result(property = "primaryKey", column = "primary_key"),
    @Result(property = "tableLocation", column = "table_location"),
    @Result(property = "baseLocation", column = "base_location"),
    @Result(property = "changeLocation", column = "change_location"),
    @Result(property = "metaStoreSite", column = "meta_store_site"),
    @Result(property = "hdfsSite", column = "hdfs_site"),
    @Result(property = "coreSite", column = "core_site"),
    @Result(property = "authMethod", column = "auth_method"),
    @Result(property = "hadoopUsername", column = "hadoop_username"),
    @Result(property = "krbKeytab", column = "krb_keytab"),
    @Result(property = "krbConf", column = "krb_conf"),
    @Result(property = "krbPrincipal", column = "krb_principal"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
    @Result(property = "metaVersion", column = "meta_version")
  })
  List<TableMetadata> selectTableMetas();

  @Select(
      "SELECT table_identifier.table_id as table_id, table_identifier.catalog_name as catalog_name, "
          + "table_identifier.db_name as db_name, table_identifier.table_name as table_name, table_identifier.format, "
          + "primary_key, "
          + "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, "
          + "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties, meta_version "
          + "FROM table_metadata INNER JOIN table_identifier ON table_metadata.table_id=table_identifier.table_id "
          + "WHERE "
          + "table_identifier.catalog_name=#{catalogName} AND table_identifier.db_name=#{database}")
  @Results({
    @Result(property = "tableIdentifier.id", column = "table_id"),
    @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
    @Result(property = "tableIdentifier.database", column = "db_name"),
    @Result(property = "tableIdentifier.tableName", column = "table_name"),
    @Result(
        property = "tableIdentifier.format",
        column = "format",
        typeHandler = TableFormatConverter.class),
    @Result(property = "primaryKey", column = "primary_key"),
    @Result(property = "tableLocation", column = "table_location"),
    @Result(property = "baseLocation", column = "base_location"),
    @Result(property = "changeLocation", column = "change_location"),
    @Result(property = "metaStoreSite", column = "meta_store_site"),
    @Result(property = "hdfsSite", column = "hdfs_site"),
    @Result(property = "coreSite", column = "core_site"),
    @Result(property = "authMethod", column = "auth_method"),
    @Result(property = "hadoopUsername", column = "hadoop_username"),
    @Result(property = "krbKeytab", column = "krb_keytab"),
    @Result(property = "krbConf", column = "krb_conf"),
    @Result(property = "krbPrincipal", column = "krb_principal"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
    @Result(property = "metaVersion", column = "meta_version")
  })
  List<TableMetadata> selectTableMetasByDb(
      @Param("catalogName") String catalogName, @Param("database") String database);

  @Insert(
      "INSERT INTO table_metadata(table_id, table_name, db_name, catalog_name, primary_key,"
          + " table_location, base_location, change_location, meta_store_site, hdfs_site, core_site,"
          + " auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties, meta_version)"
          + " VALUES("
          + " #{tableMeta.tableIdentifier.id},"
          + " #{tableMeta.tableIdentifier.tableName},"
          + " #{tableMeta.tableIdentifier.database},"
          + " #{tableMeta.tableIdentifier.catalog},"
          + " #{tableMeta.primaryKey, jdbcType=VARCHAR},"
          + " #{tableMeta.tableLocation, jdbcType=VARCHAR},"
          + " #{tableMeta.baseLocation, jdbcType=VARCHAR},"
          + " #{tableMeta.changeLocation, jdbcType=VARCHAR},"
          + " #{tableMeta.metaStoreSite, jdbcType=VARCHAR}, "
          + " #{tableMeta.hdfsSite, jdbcType=VARCHAR},"
          + " #{tableMeta.coreSite, jdbcType=VARCHAR},"
          + " #{tableMeta.authMethod, jdbcType=VARCHAR},"
          + " #{tableMeta.hadoopUsername, jdbcType=VARCHAR},"
          + " #{tableMeta.krbKeytab, jdbcType=VARCHAR},"
          + " #{tableMeta.krbConf, jdbcType=VARCHAR},"
          + " #{tableMeta.krbPrincipal, jdbcType=VARCHAR},"
          + " #{tableMeta.properties, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter},"
          + " #{tableMeta.metaVersion}"
          + " )")
  void insertTableMeta(@Param("tableMeta") TableMetadata tableMeta);

  @Delete("DELETE FROM table_metadata WHERE table_id = #{tableId}")
  void deleteTableMetaById(@Param("tableId") long tableId);

  @Update(
      "UPDATE table_metadata SET properties ="
          + " #{tableMeta.properties, typeHandler=org.apache.amoro.server.persistence.converter.Map2StringConverter},"
          + " meta_version=meta_version + 1 "
          + " WHERE table_id = #{tableId} and meta_version = #{tableMeta.metaVersion} ")
  int commitTableChange(
      @Param("tableId") long tableId, @Param("tableMeta") TableMetadata tableMeta);

  @Select(
      "SELECT i.table_id, i.table_name, i.db_name, i.catalog_name, i.format, primary_key, "
          + "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, "
          + "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties, meta_version FROM "
          + "table_metadata m INNER JOIN table_identifier i ON m.table_id = i.table_id "
          + "WHERE m.table_id = #{tableId}")
  @Results({
    @Result(property = "tableIdentifier.id", column = "table_id"),
    @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
    @Result(property = "tableIdentifier.database", column = "db_name"),
    @Result(property = "tableIdentifier.tableName", column = "table_name"),
    @Result(
        property = "tableIdentifier.format",
        column = "format",
        typeHandler = TableFormatConverter.class),
    @Result(property = "primaryKey", column = "primary_key"),
    @Result(property = "tableLocation", column = "table_location"),
    @Result(property = "baseLocation", column = "base_location"),
    @Result(property = "changeLocation", column = "change_location"),
    @Result(property = "metaStoreSite", column = "meta_store_site"),
    @Result(property = "hdfsSite", column = "hdfs_site"),
    @Result(property = "coreSite", column = "core_site"),
    @Result(property = "authMethod", column = "auth_method"),
    @Result(property = "hadoopUsername", column = "hadoop_username"),
    @Result(property = "krbKeytab", column = "krb_keytab"),
    @Result(property = "krbConf", column = "krb_conf"),
    @Result(property = "krbPrincipal", column = "krb_principal"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
    @Result(property = "metaVersion", column = "meta_version")
  })
  TableMetadata selectTableMetaById(@Param("tableId") long tableId);

  @Select(
      "SELECT table_identifier.table_id as table_id, table_identifier.catalog_name as catalog_name,"
          + " table_identifier.db_name as db_name, table_identifier.table_name as table_name, table_identifier.format, "
          + " primary_key,"
          + " table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, auth_method,"
          + " hadoop_username, krb_keytab, krb_conf, krb_principal, properties, meta_version "
          + " FROM table_metadata INNER JOIN table_identifier ON table_metadata.table_id = table_identifier.table_id"
          + " WHERE table_identifier.catalog_name = #{catalogName} and table_identifier.db_name = #{databaseName}"
          + " AND table_identifier.table_name = #{tableName}")
  @Results({
    @Result(property = "tableIdentifier.id", column = "table_id"),
    @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
    @Result(property = "tableIdentifier.database", column = "db_name"),
    @Result(property = "tableIdentifier.tableName", column = "table_name"),
    @Result(
        property = "tableIdentifier.format",
        column = "format",
        typeHandler = TableFormatConverter.class),
    @Result(property = "primaryKey", column = "primary_key"),
    @Result(property = "tableLocation", column = "table_location"),
    @Result(property = "baseLocation", column = "base_location"),
    @Result(property = "changeLocation", column = "change_location"),
    @Result(property = "metaStoreSite", column = "meta_store_site"),
    @Result(property = "hdfsSite", column = "hdfs_site"),
    @Result(property = "coreSite", column = "core_site"),
    @Result(property = "authMethod", column = "auth_method"),
    @Result(property = "hadoopUsername", column = "hadoop_username"),
    @Result(property = "krbKeytab", column = "krb_keytab"),
    @Result(property = "krbConf", column = "krb_conf"),
    @Result(property = "krbPrincipal", column = "krb_principal"),
    @Result(
        property = "properties",
        column = "properties",
        typeHandler = Map2StringConverter.class),
    @Result(property = "metaVersion", column = "meta_version")
  })
  TableMetadata selectTableMetaByName(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName,
      @Param("tableName") String tableName);

  @Insert(
      "INSERT INTO table_identifier(catalog_name, db_name, table_name, format) VALUES("
          + " #{tableIdentifier.catalog}, #{tableIdentifier.database}, #{tableIdentifier.tableName}, "
          + " #{tableIdentifier.format, typeHandler=org.apache.amoro.server.persistence.converter.TableFormatConverter})")
  @Options(useGeneratedKeys = true, keyProperty = "tableIdentifier.id")
  void insertTable(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier);

  @Delete("DELETE FROM table_identifier WHERE table_id = #{tableId}")
  Integer deleteTableIdById(@Param("tableId") long tableId);

  @Delete(
      "DELETE FROM table_identifier WHERE catalog_name = #{catalogName} AND db_name = #{databaseName}"
          + " AND table_name = #{tableName}")
  Integer deleteTableIdByName(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName,
      @Param("tableName") String tableName);

  @Select(
      "SELECT table_id, catalog_name, db_name, table_name, format FROM table_identifier"
          + " WHERE catalog_name = #{catalogName} AND db_name = #{databaseName} AND table_name = #{tableName}")
  @Results({
    @Result(property = "id", column = "table_id"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "database", column = "db_name"),
    @Result(property = "catalog", column = "catalog_name"),
    @Result(property = "format", column = "format", typeHandler = TableFormatConverter.class)
  })
  ServerTableIdentifier selectTableIdentifier(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName,
      @Param("tableName") String tableName);

  @Select(
      "SELECT table_id, catalog_name, db_name, table_name, format FROM table_identifier"
          + " WHERE catalog_name = #{catalogName} AND db_name = #{databaseName}")
  @Results({
    @Result(property = "id", column = "table_id"),
    @Result(property = "catalog", column = "catalog_name"),
    @Result(property = "database", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "format", column = "format", typeHandler = TableFormatConverter.class)
  })
  List<ServerTableIdentifier> selectTableIdentifiersByDb(
      @Param("catalogName") String catalogName, @Param("databaseName") String databaseName);

  @Select(
      "SELECT table_id, catalog_name, db_name, table_name, format FROM table_identifier"
          + " WHERE catalog_name = #{catalogName}")
  @Results({
    @Result(property = "id", column = "table_id"),
    @Result(property = "catalog", column = "catalog_name"),
    @Result(property = "database", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "format", column = "format", typeHandler = TableFormatConverter.class)
  })
  List<ServerTableIdentifier> selectTableIdentifiersByCatalog(
      @Param("catalogName") String catalogName);

  @Select("SELECT table_id, catalog_name, db_name, table_name, format FROM table_identifier")
  @Results({
    @Result(property = "id", column = "table_id"),
    @Result(property = "catalog", column = "catalog_name"),
    @Result(property = "database", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "format", column = "format", typeHandler = TableFormatConverter.class)
  })
  List<ServerTableIdentifier> selectAllTableIdentifiers();

  @Update(
      "UPDATE table_runtime SET current_snapshot_id = #{runtime.currentSnapshotId},"
          + " current_change_snapshotId =#{runtime.currentChangeSnapshotId},"
          + " last_optimized_snapshotId = #{runtime.lastOptimizedSnapshotId},"
          + " last_optimized_change_snapshotId = #{runtime.lastOptimizedChangeSnapshotId},"
          + " last_major_optimizing_time = #{runtime.lastMajorOptimizingTime, "
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " last_minor_optimizing_time = #{runtime.lastMinorOptimizingTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " last_full_optimizing_time = #{runtime.lastFullOptimizingTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " optimizing_status_code = #{runtime.optimizingStatus,"
          + "typeHandler=org.apache.amoro.server.persistence.converter.OptimizingStatusConverter},"
          + " optimizing_status_start_time = #{runtime.currentStatusStartTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " optimizing_process_id = #{runtime.processId},"
          + " optimizer_group = #{runtime.optimizerGroup},"
          + " table_config = #{runtime.tableConfiguration,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter},"
          + " pending_input = #{runtime.pendingInput, jdbcType=VARCHAR,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter},"
          + " table_summary = #{runtime.tableSummary, jdbcType=VARCHAR,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter}"
          + " WHERE table_id = #{runtime.tableIdentifier.id}")
  void updateTableRuntime(@Param("runtime") TableRuntime runtime);

  @Delete("DELETE FROM table_runtime WHERE table_id = #{tableId}")
  void deleteOptimizingRuntime(@Param("tableId") long tableId);

  @Insert(
      "INSERT INTO table_runtime (table_id, catalog_name, db_name, table_name, current_snapshot_id,"
          + " current_change_snapshotId, last_optimized_snapshotId, last_optimized_change_snapshotId,"
          + " last_major_optimizing_time, last_minor_optimizing_time,"
          + " last_full_optimizing_time, optimizing_status_code, optimizing_status_start_time, optimizing_process_id,"
          + " optimizer_group, table_config, pending_input, table_summary) VALUES"
          + " (#{runtime.tableIdentifier.id}, #{runtime.tableIdentifier.catalog},"
          + " #{runtime.tableIdentifier.database}, #{runtime.tableIdentifier.tableName}, #{runtime"
          + ".currentSnapshotId},"
          + " #{runtime.currentChangeSnapshotId}, #{runtime.lastOptimizedSnapshotId},"
          + " #{runtime.lastOptimizedChangeSnapshotId}, #{runtime.lastMajorOptimizingTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " #{runtime.lastMinorOptimizingTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " #{runtime.lastFullOptimizingTime,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " #{runtime.optimizingStatus,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.OptimizingStatusConverter},"
          + " #{runtime.currentStatusStartTime, "
          + " typeHandler=org.apache.amoro.server.persistence.converter.Long2TsConverter},"
          + " #{runtime.processId}, #{runtime.optimizerGroup},"
          + " #{runtime.tableConfiguration,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter},"
          + " #{runtime.pendingInput, jdbcType=VARCHAR,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter},"
          + " #{runtime.tableSummary, jdbcType=VARCHAR,"
          + " typeHandler=org.apache.amoro.server.persistence.converter.JsonObjectConverter})")
  void insertTableRuntime(@Param("runtime") TableRuntime runtime);

  @Select(
      "SELECT a.table_id, a.catalog_name, a.db_name, a.table_name, i.format, a.current_snapshot_id,"
          + " a.current_change_snapshotId, a.last_optimized_snapshotId, a.last_optimized_change_snapshotId,"
          + " a.last_major_optimizing_time, a.last_minor_optimizing_time, a.last_full_optimizing_time,"
          + " a.optimizing_status_code, a.optimizing_status_start_time, a.optimizing_process_id,"
          + " a.optimizer_group, a.table_config, a.pending_input, a.table_summary, b.optimizing_type, b.target_snapshot_id,"
          + " b.target_change_snapshot_id, b.plan_time, b.from_sequence, b.to_sequence FROM table_runtime a"
          + " INNER JOIN table_identifier i ON a.table_id = i.table_id "
          + " LEFT JOIN table_optimizing_process b ON a.optimizing_process_id = b.process_id")
  @Results({
    @Result(property = "tableId", column = "table_id"),
    @Result(property = "catalogName", column = "catalog_name"),
    @Result(property = "dbName", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "format", column = "format", typeHandler = TableFormatConverter.class),
    @Result(property = "currentSnapshotId", column = "current_snapshot_id"),
    @Result(property = "currentChangeSnapshotId", column = "current_change_snapshotId"),
    @Result(property = "lastOptimizedSnapshotId", column = "last_optimized_snapshotId"),
    @Result(
        property = "lastOptimizedChangeSnapshotId",
        column = "last_optimized_change_snapshotId"),
    @Result(
        property = "lastMajorOptimizingTime",
        column = "last_major_optimizing_time",
        typeHandler = Long2TsConverter.class),
    @Result(
        property = "lastMinorOptimizingTime",
        column = "last_minor_optimizing_time",
        typeHandler = Long2TsConverter.class),
    @Result(
        property = "lastFullOptimizingTime",
        column = "last_full_optimizing_time",
        typeHandler = Long2TsConverter.class),
    @Result(
        property = "tableStatus",
        column = "optimizing_status_code",
        typeHandler = OptimizingStatusConverter.class),
    @Result(
        property = "currentStatusStartTime",
        column = "optimizing_status_start_time",
        typeHandler = Long2TsConverter.class),
    @Result(property = "optimizingProcessId", column = "optimizing_process_id"),
    @Result(property = "optimizerGroup", column = "optimizer_group"),
    @Result(
        property = "tableConfig",
        column = "table_config",
        typeHandler = JsonObjectConverter.class),
    @Result(
        property = "pendingInput",
        column = "pending_input",
        typeHandler = JsonObjectConverter.class),
    @Result(
        property = "tableSummary",
        column = "table_summary",
        typeHandler = JsonObjectConverter.class),
    @Result(property = "optimizingType", column = "optimizing_type"),
    @Result(property = "targetSnapshotId", column = "target_snapshot_id"),
    @Result(property = "targetChangeSnapshotId", column = "target_change_snapshot_id"),
    @Result(property = "planTime", column = "plan_time", typeHandler = Long2TsConverter.class),
    @Result(
        property = "fromSequence",
        column = "from_sequence",
        typeHandler = MapLong2StringConverter.class),
    @Result(
        property = "toSequence",
        column = "to_sequence",
        typeHandler = MapLong2StringConverter.class)
  })
  List<TableRuntimeMeta> selectTableRuntimeMetas();

  @Select(
      "<script>"
          + "<bind name=\"isMySQL\" value=\"_databaseId == 'mysql'\" />"
          + "<bind name=\"isPostgreSQL\" value=\"_databaseId == 'postgres'\" />"
          + "<bind name=\"isDerby\" value=\"_databaseId == 'derby'\" />"
          + "SELECT table_id, catalog_name, db_name, table_name, current_snapshot_id, "
          + "current_change_snapshotId, last_optimized_snapshotId, last_optimized_change_snapshotId, "
          + "last_major_optimizing_time, last_minor_optimizing_time, last_full_optimizing_time, optimizing_status_code, "
          + "optimizing_status_start_time, optimizing_process_id, "
          + "optimizer_group, table_config, pending_input, table_summary FROM table_runtime "
          + "/* Debug: ${_databaseId} */"
          + "WHERE 1=1 "
          + "<if test='optimizerGroup != null'> AND optimizer_group = #{optimizerGroup} </if> "
          + "<if test='fuzzyDbName != null and isMySQL'> AND db_name like CONCAT('%', #{fuzzyDbName, jdbcType=VARCHAR}, '%') </if>"
          + "<if test='fuzzyDbName != null and (isPostgreSQL or isDerby)'> AND db_name like '%' || #{fuzzyDbName, jdbcType=VARCHAR} || '%' </if>"
          + "<if test='fuzzyTableName != null and isMySQL'> AND table_name like CONCAT('%', #{fuzzyTableName, jdbcType=VARCHAR}, '%') </if>"
          + "<if test='fuzzyTableName != null and (isPostgreSQL or isDerby)'> AND table_name like '%' || #{fuzzyTableName, jdbcType=VARCHAR} || '%' </if>"
          + "<if test='statusCodeFilter != null and statusCodeFilter.size() > 0'>"
          + "AND optimizing_status_code IN ("
          + "<foreach item='item' collection='statusCodeFilter' separator=','>"
          + "#{item}"
          + "</foreach> ) </if>"
          + "ORDER BY optimizing_status_code, optimizing_status_start_time DESC "
          + "</script>")
  @Results({
    @Result(property = "tableId", column = "table_id"),
    @Result(property = "catalogName", column = "catalog_name"),
    @Result(property = "dbName", column = "db_name"),
    @Result(property = "tableName", column = "table_name"),
    @Result(property = "currentSnapshotId", column = "current_snapshot_id"),
    @Result(property = "currentChangeSnapshotId", column = "current_change_snapshotId"),
    @Result(property = "lastOptimizedSnapshotId", column = "last_optimized_snapshotId"),
    @Result(
        property = "lastOptimizedChangeSnapshotId",
        column = "last_optimized_change_snapshotId"),
    @Result(
        property = "lastMajorOptimizingTime",
        column = "last_major_optimizing_time",
        typeHandler = Long2TsConverter.class),
    @Result(
        property = "lastMinorOptimizingTime",
        column = "last_minor_optimizing_time",
        typeHandler = Long2TsConverter.class),
    @Result(
        property = "lastFullOptimizingTime",
        column = "last_full_optimizing_time",
        typeHandler = Long2TsConverter.class),
    @Result(
        property = "tableStatus",
        column = "optimizing_status_code",
        typeHandler = OptimizingStatusConverter.class),
    @Result(
        property = "currentStatusStartTime",
        column = "optimizing_status_start_time",
        typeHandler = Long2TsConverter.class),
    @Result(property = "optimizingProcessId", column = "optimizing_process_id"),
    @Result(property = "optimizerGroup", column = "optimizer_group"),
    @Result(
        property = "pendingInput",
        column = "pending_input",
        typeHandler = JsonObjectConverter.class),
    @Result(
        property = "tableSummary",
        column = "table_summary",
        typeHandler = JsonObjectConverter.class),
    @Result(
        property = "tableConfig",
        column = "table_config",
        typeHandler = JsonObjectConverter.class),
  })
  List<TableRuntimeMeta> selectTableRuntimesForOptimizerGroup(
      @Param("optimizerGroup") String optimizerGroup,
      @Param("fuzzyDbName") String fuzzyDbName,
      @Param("fuzzyTableName") String fuzzyTableName,
      @Param("statusCodeFilter") List<Integer> statusCodeFilter);
}
