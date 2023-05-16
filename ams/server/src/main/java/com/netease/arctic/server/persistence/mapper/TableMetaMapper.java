package com.netease.arctic.server.persistence.mapper;

import com.netease.arctic.server.persistence.converter.JsonSummaryConverter;
import com.netease.arctic.server.persistence.converter.Long2TsConvertor;
import com.netease.arctic.server.persistence.converter.Map2StringConverter;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface TableMetaMapper {

  @Insert("INSERT INTO database_metadata(catalog_name, db_name) VALUES( #{catalogName}, #{dbName})")
  void insertDatabase(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Select("SELECT db_name FROM database_metadata WHERE catalog_name = #{catalogName}")
  List<String> selectDatabases(@Param("catalogName") String catalogName);

  @Select("SELECT db_name FROM database_metadata WHERE catalog_name = #{catalogName} AND db_name=#{dbName}")
  String selectDatabase(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Delete("DELETE FROM database_metadata WHERE catalog_name = #{catalogName} AND db_name = #{dbName}" +
      " AND table_count=0")
  Integer dropDb(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Update("UPDATE database_metadata SET table_count = table_count + #{tableCount} WHERE db_name = #{databaseName}")
  Integer incTableCount(@Param("tableCount") Integer tableCount, @Param("databaseName") String databaseName);

  @Update("UPDATE database_metadata SET table_count = table_count - #{tableCount} WHERE db_name = #{databaseName}")
  Integer decTableCount(@Param("tableCount") Integer tableCount, @Param("databaseName") String databaseName);

  @Select("SELECT table_count FROM database_metadata WHERE db_name = #{databaseName}")
  Integer selectTableCount(@Param("databaseName") String databaseName);

  @Select("SELECT table_id, table_name, db_name, catalog_name, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties FROM table_metadata")
  @Results({
      @Result(property = "tableIdentifier.id", column = "table_id"),
      @Result(property = "tableIdentifier.tableName", column = "table_name"),
      @Result(property = "tableIdentifier.database", column = "db_name"),
      @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
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
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  List<TableMetadata> selectTableMetas();

  @Select("SELECT table_identifier.table_id as table_id, table_identifier.catalog_name as catalog_name, " +
      "table_identifier.db_name as db_name, table_identifier.table_name as table_name, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties FROM table_metadata " +
      "INNER JOIN table_identifier ON table_metadata.table_id=table_identifier.table_id WHERE " +
      "table_identifier.catalog_name=#{catalogName} AND table_identifier.db_name=#{database}")
  @Results({
      @Result(property = "tableIdentifier.id", column = "table_id"),
      @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
      @Result(property = "tableIdentifier.database", column = "db_name"),
      @Result(property = "tableIdentifier.tableName", column = "table_name"),
      @Result(property = "primaryKey", column = "primary_key"),
      @Result(property = "tableLocation", column = "table_location"),
      @Result(property = "baseLocation", column = "base_location"),
      @Result(property = "changeLocation", column = "change_location"),
      @Result(property = "metaStoreSite", column = "meta_store_site"),
      @Result(property = "hdfsSite", column = "hdfs_site"),
      @Result(property = "coreSite", column = "core_site"),
      @Result(property = "authMethod", column = "auth_method"),
      @Result(property = "hadoopUsername", column = "hadoop_username"),
      @Result(property = "krbKeyteb", column = "krb_keytab"),
      @Result(property = "krbConf", column = "krb_conf"),
      @Result(property = "krbPrincipal", column = "krb_principal"),
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  List<TableMetadata> selectTableMetasByDb(
      @Param("catalogName") String catalogName,
      @Param("database") String database);

  @Insert("INSERT INTO table_metadata(table_id, table_name, db_name, catalog_name, primary_key, " +
      " table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      " auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties)" +
      " Values(" +
      " #{tableMeta.tableIdentifier.id}," +
      " #{tableMeta.tableIdentifier.tableName}," +
      " #{tableMeta.tableIdentifier.database}," +
      " #{tableMeta.tableIdentifier.catalog}," +
      " #{tableMeta.primaryKey, jdbcType=VARCHAR}," +
      " #{tableMeta.tableLocation, jdbcType=VARCHAR}," +
      " #{tableMeta.baseLocation, jdbcType=VARCHAR}," +
      " #{tableMeta.changeLocation, jdbcType=VARCHAR}," +
      " #{tableMeta.metaStoreSite, jdbcType=VARCHAR}, " +
      " #{tableMeta.hdfsSite, jdbcType=VARCHAR}," +
      " #{tableMeta.coreSite, jdbcType=VARCHAR}," +
      " #{tableMeta.authMethod, jdbcType=VARCHAR}," +
      " #{tableMeta.hadoopUsername, jdbcType=VARCHAR}," +
      " #{tableMeta.krbKeyteb, jdbcType=VARCHAR}," +
      " #{tableMeta.krbConf, jdbcType=VARCHAR}," +
      " #{tableMeta.krbPrincipal, jdbcType=VARCHAR}," +
      " #{tableMeta.properties, typeHandler=com.netease.arctic.server.persistence.converter.Map2StringConverter}" +
      " )")
  void insertTableMeta(@Param("tableMeta") TableMetadata tableMeta);

  @Delete("DELETE FROM table_metadata WHERE table_id = #{tableId}")
  void deleteTableMetaById(@Param("tableId") long tableId);

  @Update("UPDATE table_metadata SET properties =" +
      " #{properties, typeHandler=com.netease.arctic.server.persistence.converter.Map2StringConverter}" +
      " WHERE table_id = #{tableId}")
  void updateTableProperties(
      @Param("tableId") long tableId,
      @Param("properties") Map<String, String> properties);

  @Select("SELECT table_id, table_name, db_name, catalog_name, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties FROM " +
      "table_metadata WHERE table_id = #{tableId}")
  @Results({
      @Result(property = "tableIdentifier.id", column = "table_id"),
      @Result(property = "tableIdentifier.catalog", column = "catalog_name"),
      @Result(property = "tableIdentifier.database", column = "db_name"),
      @Result(property = "tableIdentifier.tableName", column = "table_name"),
      @Result(property = "primaryKey", column = "primary_key"),
      @Result(property = "tableLocation", column = "table_location"),
      @Result(property = "baseLocation", column = "base_location"),
      @Result(property = "changeLocation", column = "change_location"),
      @Result(property = "metaStoreSite", column = "meta_store_site"),
      @Result(property = "hdfsSite", column = "hdfs_site"),
      @Result(property = "coreSite", column = "core_site"),
      @Result(property = "authMethod", column = "auth_method"),
      @Result(property = "hadoopUsername", column = "hadoop_username"),
      @Result(property = "krbKeyteb", column = "krb_keytab"),
      @Result(property = "krbConf", column = "krb_conf"),
      @Result(property = "krbPrincipal", column = "krb_principal"),
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  TableMetadata selectTableMetaById(@Param("tableId") long tableId);

  @Select("SELECT table_id, primary_key, table_location, base_location, change_location, meta_store_site, hdfs_site," +
      " core_site, auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties " +
      " FROM table_metadata INNER JOIN table_identifier ON table_metadata.table_id = table_identifier.table_id" +
      " WHERE table_identifier.catalog_name = #{catalogName} and table_identifier.db_name = #{database}" +
      " AND table_name = #{tableName}")
  @Results({
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "primaryKey", column = "primary_key"),
      @Result(property = "tableLocation", column = "table_location"),
      @Result(property = "baseLocation", column = "base_location"),
      @Result(property = "changeLocation", column = "change_location"),
      @Result(property = "metaStoreSite", column = "meta_store_site"),
      @Result(property = "hdfsSite", column = "hdfs_site"),
      @Result(property = "coreSite", column = "core_site"),
      @Result(property = "authMethod", column = "auth_method"),
      @Result(property = "hadoopUsername", column = "hadoop_username"),
      @Result(property = "krbKeyteb", column = "krb_keytab"),
      @Result(property = "krbConf", column = "krb_conf"),
      @Result(property = "krbPrincipal", column = "krb_principal"),
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  TableMetadata selectTableMetaByName(String catalogName, String databaseName, String tableName);

  @Insert("INSERT INTO table_identifier(catalog_name, db_name, table_name) VALUES(" +
      " #{tableIdentifier.catalog}, #{tableIdentifier.database}, #{tableIdentifier.tableName})")
  @Options(useGeneratedKeys = true, keyProperty = "tableIdentifier.id")
  void insertTable(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier);

  @Delete("DELETE FROM table_identifier WHERE table_id = #{tableId}")
  Integer deleteTableIdById(@Param("tableIdentifier") long tableId);

  @Delete("DELETE FROM table_identifier WHERE catalog_name = #{catalogName} AND db_name = #{databaseName}" +
      " AND table_name = #{tableName}")
  Integer deleteTableIdByName(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName,
      @Param("tableName") String tableName);

  @Select("SELECT table_id, catalog_name, db_name, table_name FROM table_identifier" +
      " WHERE catalog_name = #{catalogName} AND db_name = #{databaseName} AND table_name = #{tableName}")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "tableName", column = "table_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "catalog", column = "catalog_name")
  })
  ServerTableIdentifier selectTableIdentifier(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName,
      @Param("tableName") String tableName);

  @Select("SELECT table_id, catalog_name, db_name, table_name FROM table_identifier" +
      " WHERE catalog_name = #{catalogName} AND db_name = #{databaseName}")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "catalog", column = "catalog_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "tableName", column = "table_name")
  })
  List<ServerTableIdentifier> selectTableIdentifiersByDb(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName);

  @Select("SELECT table_id, catalog_name, db_name, table_name FROM table_identifier" +
      " WHERE catalog_name = #{catalogName}")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "catalog", column = "catalog_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "tableName", column = "table_name")
  })
  List<ServerTableIdentifier> selectTableIdentifiersByCatalog(@Param("catalogName") String catalogName);

  @Select("SELECT table_id, catalog_name, db_name, table_name FROM table_identifier")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "catalog", column = "catalog_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "tableName", column = "table_name")
  })
  List<ServerTableIdentifier> selectAllTableIdentifiers();

  @Update("UPDATE table_runtime SET current_snapshot_id = #{runtime.currentSnapshotId}," +
      " current_change_snapshotId =#{runtime.currentChangeSnapshotId}," +
      " last_optimized_snapshotId = #{runtime.lastOptimizedSnapshotId}," +
      " last_major_optimizing_time = #{runtime.lastMajorOptimizingTime, " +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " last_minor_optimizing_time = #{runtime.lastMinorOptimizingTime," +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " last_full_optimizing_time = #{runtime.lastFullOptimizingTime," +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " optimizing_status = #{runtime.optimizingStatus}," +
      " optimizing_status_start_time = #{runtime.currentStatusStartTime," +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " optimizing_process_id = #{runtime.processId}," +
      " optimizer_group = #{runtime.optimizerGroup}," +
      " table_config = #{runtime.tableConfiguration," +
      " typeHandler=com.netease.arctic.server.persistence.converter.JsonSummaryConverter}" +
      " WHERE table_id = #{runtime.tableIdentifier.id}")
  void updateTableRuntime(@Param("runtime") TableRuntime runtime);

  @Delete("DELETE FROM table_runtime WHERE table_id = #{tableId}")
  void deleteOptimizingRuntime(@Param("tableId") long tableId);

  @Insert("INSERT INTO table_runtime (table_id, catalog_name, db_name, table_name, current_snapshot_id," +
      " current_change_snapshotId, last_optimized_snapshotId, last_major_optimizing_time, last_minor_optimizing_time," +
      " last_full_optimizing_time, optimizing_status, optimizing_status_start_time, optimizing_process_id," +
      " optimizer_group, table_config) VALUES (#{runtime.tableIdentifier.id}, #{runtime.tableIdentifier.catalog}," +
      " #{runtime.tableIdentifier.database}, #{runtime.tableIdentifier.tableName},#{runtime.currentSnapshotId}," +
      " #{runtime.currentChangeSnapshotId}, #{runtime.lastOptimizedSnapshotId}, #{runtime.lastMajorOptimizingTime, " +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " #{runtime.lastMinorOptimizingTime," +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " #{runtime.lastFullOptimizingTime," +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " #{runtime.optimizingStatus}," +
      " #{runtime.currentStatusStartTime, " +
      " typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}," +
      " #{runtime.processId}, #{runtime.optimizerGroup}," +
      " #{runtime.tableConfiguration," +
      " typeHandler=com.netease.arctic.server.persistence.converter.JsonSummaryConverter})")
  void insertTableRuntime(@Param("runtime") TableRuntime runtime);

  @Select("SELECT * FROM table_runtime")
  @Results({
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "catalogName", column = "catalog_name"),
      @Result(property = "dbName", column = "db_name"),
      @Result(property = "tableName", column = "table_name"),
      @Result(property = "currentSnapshotId", column = "current_snapshot_id"),
      @Result(property = "currentChangeSnapshotId", column = "current_change_snapshotId"),
      @Result(property = "lastOptimizedSnapshotId", column = "last_optimized_snapshotId"),
      @Result(property = "lastMajorOptimizingTime", column = "last_major_optimizing_time", typeHandler =
          Long2TsConvertor.class),
      @Result(property = "lastMinorOptimizingTime", column = "last_minor_optimizing_time", typeHandler =
          Long2TsConvertor.class),
      @Result(property = "lastFullOptimizingTime", column = "last_full_optimizing_time", typeHandler =
          Long2TsConvertor.class),
      @Result(property = "tableStatus", column = "optimizing_status"),
      @Result(property = "currentStatusStartTime", column = "optimizing_status_start_time", typeHandler =
          Long2TsConvertor.class),
      @Result(property = "optimizingProcessId", column = "optimizing_process_id"),
      @Result(property = "optimizerGroup", column = "optimizer_group"),
      @Result(property = "tableConfig", column = "table_config", typeHandler = JsonSummaryConverter.class)
  })
  List<TableRuntimeMeta> selectTableRuntimeMetas();

  @Select("SELECT * FROM table_runtime WHERE table_id = #{tableId}")
  @Results({
      @Result(property = "tableId", column = "table_id"),
      @Result(property = "catalogName", column = "catalog_name"),
      @Result(property = "dbName", column = "db_name"),
      @Result(property = "tableName", column = "table_name"),
      @Result(property = "currentSnapshotId", column = "current_snapshot_id"),
      @Result(property = "currentChangeSnapshotId", column = "current_change_snapshotId"),
      @Result(property = "lastMajorOptimizingTime", column = "last_major_optimizing_time"),
      @Result(property = "lastMinorOptimizingTime", column = "last_minor_optimizing_time"),
      @Result(property = "lastFullOptimizingTime", column = "last_full_optimizing_time"),
      @Result(property = "tableStatus", column = "optimizing_status"),
      @Result(property = "currentStatusStartTime", column = "optimizing_status_start_time"),
      @Result(property = "optimizingProcessId", column = "optimizing_process_id"),
      @Result(property = "optimizerGroup", column = "optimizer_group"),
      @Result(property = "tableConfig", column = "table_config", typeHandler = JsonSummaryConverter.class)
  })
  List<TableRuntimeMeta> selectTableRuntimeMeta(@Param("tableId") long tableId);
}
