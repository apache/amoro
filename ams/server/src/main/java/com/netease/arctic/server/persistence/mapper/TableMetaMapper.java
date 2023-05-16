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

  @Insert("insert into database_metadata(catalog_name, db_name) values( #{catalogName}, #{dbName})")
  void insertDatabase(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Select("select db_name from database_metadata where catalog_name = #{catalogName}")
  List<String> selectDatabases(@Param("catalogName") String catalogName);

  @Select("select db_name from database_metadata where catalog_name = #{catalogName} and db_name=#{dbName}")
  String selectDatabase(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Delete("delete from database_metadata where catalog_name = #{catalogName} and db_name = #{dbName}" +
      " and table_count=0")
  Integer dropDb(@Param("catalogName") String catalogName, @Param("dbName") String dbName);

  @Update("update database_metadata set table_count=table_count+#{tableCount} where db_name=#{databaseName}")
  Integer incTableCount(@Param("tableCount") Integer tableCount, @Param("databaseName") String databaseName);

  @Update("update database_metadata set table_count=table_count-#{tableCount} where db_name=#{databaseName}")
  Integer decTableCount(@Param("tableCount") Integer tableCount, @Param("databaseName") String databaseName);

  @Select("select table_count from database_metadata where db_name=#{databaseName}")
  Integer selectTableCount(@Param("databaseName") String databaseName);

  @Select("select table_id, table_name, db_name, catalog_name, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties from table_metadata")
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
      @Result(property = "krbKeyteb", column = "krb_keytab"),
      @Result(property = "krbConf", column = "krb_conf"),
      @Result(property = "krbPrincipal", column = "krb_principal"),
      @Result(property = "properties", column = "properties",
          typeHandler = Map2StringConverter.class)
  })
  List<TableMetadata> selectTableMetas();

  @Select("select table_identifier.table_id as table_id, table_identifier.catalog_name as catalog_name, " +
      "table_identifier.db_name as db_name, table_identifier.table_name as table_name, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties from table_metadata " +
      "inner join table_identifier on table_metadata.table_id=table_identifier.table_id where " +
      "table_identifier.catalog_name=#{catalogName} and table_identifier.db_name=#{database}")
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

  @Insert("insert into table_metadata(table_id, table_name, db_name, catalog_name, primary_key, " +
      " table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      " auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties)" +
      " values(" +
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

  @Delete("delete from table_metadata where table_id = #{tableId}")
  void deleteTableMetaById(@Param("tableId") long tableId);

  @Update("update table_metadata set " +
      "properties = #{properties, typeHandler=com.netease.arctic.server.persistence.converter" +
      ".Map2StringConverter} where table_id = #{tableId}")
  void updateTableProperties(
      @Param("tableId") long tableId,
      @Param("properties") Map<String, String> properties);

  @Update("update table_metadata set " +
      "current_tx_id = #{txId} where " +
      "table_id = #{tableId}")
  void updateTableTxId(
      @Param("tableId") long tableId,
      @Param("txId") Long txId);

  @Select("select table_id, table_name, db_name, catalog_name, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties, current_tx_id from " +
      "table_metadata where table_id = #{tableId}")
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
          typeHandler = Map2StringConverter.class),
      @Result(property = "currentTxId", column = "current_tx_id")
  })
  TableMetadata selectTableMetaById(@Param("tableId") long tableId);

  @Select("select table_id, primary_key, " +
      "table_location, base_location, change_location, meta_store_site, hdfs_site, core_site, " +
      "auth_method, hadoop_username, krb_keytab, krb_conf, krb_principal, properties, current_tx_id from " +
      "table_metadata inner join table_identifier on table_metadata.table_id=table_identifier.table_id where " +
      " table_identifier.catalog_name=#{catalogName} and table_identifier.db_name=#{database} and " +
      "table_name=#{tableName}")
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
          typeHandler = Map2StringConverter.class),
      @Result(property = "currentTxId", column = "current_tx_id")
  })
  TableMetadata selectTableMetaByName(String catalogName, String databaseName, String tableName);

  @Insert("insert into table_identifier(catalog_name, db_name, table_name) values(" +
      " #{tableIdentifier.catalog}," +
      " #{tableIdentifier.database}," +
      " #{tableIdentifier.tableName}" +
      " )")
  @Options(useGeneratedKeys = true, keyProperty = "tableIdentifier.id")
  void insertTable(@Param("tableIdentifier") ServerTableIdentifier tableIdentifier);

  @Delete("delete from table_identifier where table_id = #{tableId}")
  Integer deleteTableIdById(@Param("tableIdentifier") long tableId);

  @Delete("delete from table_identifier where catalog_name=#{catalogName} and db_name=#{databaseName} and " +
      "table_name=#{tableName}")
  Integer deleteTableIdByName(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName,
      @Param("tableName") String tableName);

  @Select("select table_id, catalog_name, db_name, table_name from table_identifier where " +
      "catalog_name=#{catalogName} and db_name=#{databaseName} and table_name=#{tableName}")
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

  @Select("select table_id, catalog_name, db_name, table_name from table_identifier where " +
      "catalog_name=#{catalogName} and db_name=#{databaseName}")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "catalog", column = "catalog_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "tableName", column = "table_name")
  })
  List<ServerTableIdentifier> selectTableIdentifiersByDb(
      @Param("catalogName") String catalogName,
      @Param("databaseName") String databaseName);

  @Select("select table_id, catalog_name, db_name, table_name from table_identifier where catalog_name=#{catalogName}")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "catalog", column = "catalog_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "tableName", column = "table_name")
  })
  List<ServerTableIdentifier> selectTableIdentifiersByCatalog(@Param("catalogName") String catalogName);

  @Select("select table_id, catalog_name, db_name, table_name from table_identifier")
  @Results({
      @Result(property = "id", column = "table_id"),
      @Result(property = "catalog", column = "catalog_name"),
      @Result(property = "database", column = "db_name"),
      @Result(property = "tableName", column = "table_name")
  })
  List<ServerTableIdentifier> selectAllTableIdentifiers();

  @Update("update table_runtime set current_snapshot_id = #{runtime.currentSnapshotId}, current_change_snapshotId = " +
      "#{runtime.currentChangeSnapshotId}, last_optimized_snapshotId = #{runtime.lastOptimizedSnapshotId}, " +
      "last_major_optimizing_time = #{runtime.lastMajorOptimizingTime, " +
      "typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}, last_minor_optimizing_time " +
      "= #{runtime.lastMinorOptimizingTime, typeHandler=com.netease.arctic.server.persistence.converter" +
      ".Long2TsConvertor}, last_full_optimizing_time = #{runtime.lastFullOptimizingTime, typeHandler=com.netease" +
      ".arctic.server.persistence.converter.Long2TsConvertor}, optimizing_status = #{runtime.optimizingStatus}, " +
      "optimizing_status_start_time = #{runtime.currentStatusStartTime, typeHandler=com.netease.arctic.server" +
      ".persistence.converter.Long2TsConvertor}, optimizing_process_id = #{runtime.processId}, optimizer_group = " +
      "#{runtime.optimizerGroup}, table_config = #{runtime.tableConfiguration, typeHandler=com.netease.arctic" +
      ".server.persistence.converter.JsonSummaryConverter} where table_id = #{runtime.tableIdentifier.id}")
  void updateTableRuntime(@Param("runtime") TableRuntime runtime);

  @Delete("delete from table_runtime where table_id = #{tableId}")
  void deleteOptimizingRuntime(@Param("tableId") long tableId);

  @Insert("insert into table_runtime (table_id,catalog_name,db_name,table_name,current_snapshot_id," +
      "current_change_snapshotId,last_optimized_snapshotId,last_major_optimizing_time,last_minor_optimizing_time," +
      "last_full_optimizing_time," +
      "optimizing_status,optimizing_status_start_time,optimizing_process_id,optimizer_group,table_config) values " +
      "(#{runtime.tableIdentifier.id}, #{runtime.tableIdentifier.catalog}, #{runtime.tableIdentifier.database}," +
      "#{runtime.tableIdentifier.tableName},#{runtime.currentSnapshotId}, #{runtime.currentChangeSnapshotId}," +
      "#{runtime.lastOptimizedSnapshotId}," +
      "#{runtime.lastMajorOptimizingTime, typeHandler=com.netease.arctic.server.persistence.converter" +
      ".Long2TsConvertor},#{runtime.lastMinorOptimizingTime, typeHandler=com.netease.arctic.server.persistence" +
      ".converter.Long2TsConvertor},#{runtime.lastFullOptimizingTime, typeHandler=com.netease.arctic.server" +
      ".persistence.converter.Long2TsConvertor},#{runtime.optimizingStatus}, #{runtime.currentStatusStartTime, " +
      "typeHandler=com.netease.arctic.server.persistence.converter.Long2TsConvertor}, #{runtime.processId}," +
      "#{runtime.optimizerGroup}, #{runtime.tableConfiguration, typeHandler=com.netease.arctic.server.persistence" +
      ".converter.JsonSummaryConverter})")
  void insertTableRuntime(@Param("runtime") TableRuntime runtime);

  @Select("select * from table_runtime")
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

  @Select("select * from table_runtime where table_id=#{tableId}")
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
