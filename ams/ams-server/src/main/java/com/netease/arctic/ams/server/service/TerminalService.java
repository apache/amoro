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

package com.netease.arctic.ams.server.service;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.model.LatestSessionInfo;
import com.netease.arctic.ams.server.model.LogInfo;
import com.netease.arctic.ams.server.model.SesssionInfo;
import com.netease.arctic.ams.server.model.SqlResult;
import com.netease.arctic.ams.server.model.SqlRunningInfo;
import com.netease.arctic.ams.server.model.SqlStatus;
import com.netease.arctic.spark.ArcticSparkCatalog;
import com.netease.arctic.spark.ArcticSparkExtensions;
import com.netease.arctic.table.TableMetaStore;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.types.StructField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TerminalService {
  private static final Logger LOG =
      LoggerFactory.getLogger(TerminalService.class);

  private static int sessionId = 1;
  private static Map<Integer, SqlRunningInfo> sqlSessionInfoCache = new HashMap<>();
  private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  public static LogInfo getLogs(int sessionId) {
    return new LogInfo(sqlSessionInfoCache.get(sessionId).getLogStatus(), sqlSessionInfoCache.get(sessionId).getLogs());
  }

  public static List<SqlResult> getSqlStatus(int sessionId) {
    return sqlSessionInfoCache.get(sessionId).getSqlResults();
  }

  public static SesssionInfo executeSql(String catalog, String sqls) {
    sqlSessionInfoCache.put(sessionId, new SqlRunningInfo());
    if (sessionId > 10) {
      sqlSessionInfoCache.remove(sessionId - 10);
    }
    sqlSessionInfoCache.get(sessionId).setSql(sqls);
    sqls.replace("\r\n", "");
    List<String> tempSqls = Arrays.asList(sqls.split(";"));
    List<String> executeSqls = new ArrayList<>(tempSqls);
    executeSqls.removeIf(t -> StringUtils.isBlank(t));
    SesssionInfo sesssionInfo = new SesssionInfo(sessionId, executeSqls.size());
    int threadSession = sessionId;
    Thread sqlExecute = new Thread(() -> {
      SqlRunningInfo sqlRunningInfo = sqlSessionInfoCache.get(threadSession);
      sqlRunningInfo.setLogStatus(SqlStatus.RUNNING.getName());
      TableMetaStore tableMetaStore = null;
      UserGroupInformation ugi = null;
      try {
        CatalogMeta catalogMeta = ServiceContainer.getCatalogMetadataService().getCatalog(catalog);
        TableMetaStore.Builder builder = TableMetaStore.builder()
            .withBase64MetaStoreSite(
                catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE))
            .withBase64CoreSite(
                catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE))
            .withBase64HdfsSite(
                catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE));
        if (catalogMeta.getAuthConfigs()
            .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE)
            .equalsIgnoreCase(CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE)) {
          builder.withSimpleAuth(catalogMeta.getAuthConfigs()
              .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME));
        } else {
          builder.withBase64Auth(
              catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE),
              catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME),
              catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB),
              catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5),
              catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL));
        }
        tableMetaStore = builder.build();
        ugi = tableMetaStore.getUGI();
      } catch (Throwable t) {
        sqlRunningInfo.getLogs().add(df.format(new Date()) + " construct ugi failed " + t);
        while (t != null) {
          sqlRunningInfo.getLogs().addAll(Arrays.asList(t.getStackTrace()).stream()
              .map(StackTraceElement::toString).collect(Collectors.toList()));
          t = t.getCause();
          if (t != null) {
            sqlRunningInfo.getLogs().add("Caused by " + t);
          }
        }
        sqlRunningInfo.setLogStatus(SqlStatus.FINISHED.getName());
        return;
      }
      ugi.doAs((PrivilegedAction<Object>) () -> {
        SparkSession spark;
        try {
          spark = constructSparkSession(threadSession, catalog);
        } catch (Throwable t) {
          LOG.error("construct spack session failed ", t);
          sqlRunningInfo.getLogs().add(df.format(new Date()) + " construct spack session failed " + t);
          while (t != null) {
            sqlRunningInfo.getLogs().addAll(Arrays.asList(t.getStackTrace()).stream()
                .map(StackTraceElement::toString).collect(Collectors.toList()));
            t = t.getCause();
            if (t != null) {
              sqlRunningInfo.getLogs().add("Caused by " + t);
            }
          }
          if (Thread.currentThread().isInterrupted()) {
            sqlRunningInfo.setLogStatus(SqlStatus.CANCELED.getName());
          } else {
            sqlRunningInfo.setLogStatus(SqlStatus.FINISHED.getName());
          }
          return null;
        }
        try {
          sqlRunningInfo.getLogs().add(df.format(new Date()) + " use " + catalog);
          spark.sql("use " + catalog);
        } catch (Throwable t) {
          LOG.error("use catalog " + catalog + " failed ", t);
          sqlRunningInfo.getLogs().add(df.format(new Date()) + " use catalog " + catalog + " failed " + t);
          while (t != null) {
            sqlRunningInfo.getLogs().addAll(Arrays.asList(t.getStackTrace()).stream()
                .map(StackTraceElement::toString).collect(Collectors.toList()));
            t = t.getCause();
            if (t != null) {
              sqlRunningInfo.getLogs().add("Caused by " + t);
            }
          }
          if (Thread.currentThread().isInterrupted()) {
            sqlRunningInfo.setLogStatus(SqlStatus.CANCELED.getName());
          } else {
            sqlRunningInfo.setLogStatus(SqlStatus.FINISHED.getName());
          }
          spark.stop();
          return null;
        }
        for (int i = 0; i < executeSqls.size(); i++) {
          if (Thread.currentThread().isInterrupted()) {
            spark.stop();
            sqlRunningInfo.setLogStatus(SqlStatus.CANCELED.getName());
            return null;
          }
          try {
            String sql = executeSqls.get(i);
            sqlRunningInfo.getSqlResults().add(new SqlResult());
            sqlRunningInfo.getSqlResults().get(i).setId("Result" + (i + 1));
            sqlRunningInfo.getSqlResults().get(i).setStatus(SqlStatus.RUNNING.getName());
            sql.replace("\r\n", "");
            sqlRunningInfo.getLogs().add(df.format(new Date()) + " execute sql " + sql);
            Dataset<Row> result = spark.sql(sql);
            List<Row> rows = result.collectAsList();
            if (rows.size() < 1) {
              sqlRunningInfo.getSqlResults().get(i).setStatus(SqlStatus.FINISHED.getName());
              sqlRunningInfo.getLogs().add((df.format(new Date()) + " empty result"));
            } else {
              List<String> columns = JavaConverters.asJavaCollection(rows.get(0).schema().toList())
                  .stream().map(StructField::name).collect(Collectors.toList());
              List<List<String>> rowData = rows.stream()
                  .map(row -> IntStream.range(0, row.size())
                      .mapToObj(pos -> row.isNullAt(pos) ? null : row.get(pos).toString())
                      .collect(Collectors.toList())
                  ).collect(Collectors.toList());
              sqlRunningInfo.getSqlResults().get(i).setColumns(columns);
              sqlRunningInfo.getSqlResults().get(i).setRowData(rowData);
              sqlRunningInfo.getSqlResults().get(i).setStatus(SqlStatus.FINISHED.getName());
              sqlRunningInfo.getLogs().add(df.format(new Date()) + " execute success");
            }
          } catch (Throwable t) {
            LOG.error("execute sql failed ", t);
            sqlRunningInfo.getLogs().add(df.format(new Date()) + " execute sql failed " + t);
            while (t != null) {
              sqlRunningInfo.getLogs().addAll(Arrays.asList(t.getStackTrace()).stream()
                  .map(StackTraceElement::toString).collect(Collectors.toList()));
              t = t.getCause();
              if (t != null) {
                sqlRunningInfo.getLogs().add("Caused by " + t);
              }
            }
            sqlRunningInfo.getSqlResults().get(i).setStatus(SqlStatus.FAILED.getName());
            spark.stop();
          }
        }
        sqlRunningInfo.getLogs().add(df.format(new Date()) + " all sql executed");
        sqlRunningInfo.setLogStatus(SqlStatus.FINISHED.getName());
        spark.stop();
        ServiceContainer.getOptimizeService().listCachedTables(true);
        return null;
      });
    });
    sqlExecute.start();
    sqlSessionInfoCache.get(sessionId).setExecuteThread(sqlExecute);
    sessionId++;
    return sesssionInfo;
  }

  public static void stopExecute(int sessionId) {
    SqlRunningInfo sqlRunningInfo = sqlSessionInfoCache.get(sessionId);
    sqlRunningInfo.getLogs().add(df.format(new Date()) + " try to stop executing");
    try {
      sqlRunningInfo.getExecuteThread().interrupt();
    } catch (Throwable t) {
      sqlRunningInfo.getLogs().add(df.format(new Date()) + " stop failed " + t);
      sqlRunningInfo.getLogs().addAll(Arrays.asList(t.getStackTrace()).stream()
          .map(StackTraceElement::toString).collect(Collectors.toList()));
      // unsafe
      sqlRunningInfo.getExecuteThread().stop();
      sqlRunningInfo.setLogStatus(SqlStatus.FINISHED.getName());
      return;
    }
    sqlRunningInfo.setLogStatus(SqlStatus.FINISHED.getName());
    sqlRunningInfo.getLogs().add(df.format(new Date()) + " stop success");
  }

  public static LatestSessionInfo getLatestSessionInfo() {
    if (sqlSessionInfoCache.size() > 1) {
      return new LatestSessionInfo(sessionId - 1, sqlSessionInfoCache.get(sessionId - 1).getSql());
    } else {
      return new LatestSessionInfo(0, "");
    }
  }

  private static SparkSession constructSparkSession(int sessionId, String catalog) {
    SqlRunningInfo sqlRunningInfo = sqlSessionInfoCache.get(sessionId);
    SparkConf sparkconf = new SparkConf()
        .setAppName(sessionId + ":" + catalog)
        .setMaster("local");
    Date date = new Date();
    sqlRunningInfo.getLogs().add(df.format(date) + " set spark conf " +
        SQLConf.PARTITION_OVERWRITE_MODE().key() + " dynamic");
    sparkconf.set(SQLConf.PARTITION_OVERWRITE_MODE().key(), "dynamic");

    sqlRunningInfo.getLogs().add(df.format(date) + " set spark conf " +
        "spark.executor.heartbeatInterval" + " 100s");
    sparkconf.set("spark.executor.heartbeatInterval", "100s");

    sqlRunningInfo.getLogs().add(df.format(date) + " set spark conf " +
        "spark.network.timeout" + " 200s");
    sparkconf.set("spark.network.timeout", "200s");

    sqlRunningInfo.getLogs().add(df.format(date) + " set spark conf " +
        "spark.sql.extensions" + " 2.3.7");
    sparkconf.set("spark.sql.extensions", ArcticSparkExtensions.class.getName());

    // if runnning on loacl host may use this param
    // sparkconf.set("spark.testing.memory","471859200");

    sqlRunningInfo.getLogs().add(df.format(date) + " spark.sql.catalog." + catalog +
        ArcticSparkCatalog.class.getName());
    sparkconf.set("spark.sql.catalog." + catalog, ArcticSparkCatalog.class.getName());

    sqlRunningInfo.getLogs().add(df.format(date) + " spark.sql.catalog." + catalog + ".type" + " arctic");
    sparkconf.set("spark.sql.catalog." + catalog + ".type", "hadoop");

    sqlRunningInfo.getLogs().add(df.format(date) + " spark.sql.catalog." + catalog + ".url " +
        String.format(
            "thrift://%s:%d/%s",
            ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST),
            ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT),
            catalog));
    sparkconf.set("spark.sql.catalog." + catalog + ".url", String.format(
        "thrift://%s:%d/%s",
        ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST),
        ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT),
        catalog));

    SparkSession spark = SparkSession
        .builder()
        .config(sparkconf)
        .getOrCreate();
    spark.sparkContext().setLogLevel("WARN");
    return spark;
  }
}