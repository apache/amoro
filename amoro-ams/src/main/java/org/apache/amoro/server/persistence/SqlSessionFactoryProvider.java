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

package org.apache.amoro.server.persistence;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.mapper.ApiTokensMapper;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.persistence.mapper.PlatformFileMapper;
import org.apache.amoro.server.persistence.mapper.ResourceMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;

public class SqlSessionFactoryProvider {
  private static final Logger LOG = LoggerFactory.getLogger(SqlSessionFactoryProvider.class);

  private static final String DERBY_INIT_SQL_SCRIPT = "derby/ams-derby-init.sql";
  private static final String MYSQL_INIT_SQL_SCRIPT = "mysql/ams-mysql-init.sql";
  private static final String POSTGRES_INIT_SQL_SCRIPT = "postgres/ams-postgres-init.sql";

  private static final SqlSessionFactoryProvider INSTANCE = new SqlSessionFactoryProvider();

  public static SqlSessionFactoryProvider getInstance() {
    return INSTANCE;
  }

  private static String dbType;

  private volatile SqlSessionFactory sqlSessionFactory;

  public void init(Configurations config) throws SQLException {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setUrl(config.getString(AmoroManagementConf.DB_CONNECTION_URL));
    dataSource.setDriverClassName(config.getString(AmoroManagementConf.DB_DRIVER_CLASS_NAME));
    dbType = config.getString(AmoroManagementConf.DB_TYPE);
    if (AmoroManagementConf.DB_TYPE_MYSQL.equals(dbType)
        || AmoroManagementConf.DB_TYPE_POSTGRES.equals(dbType)) {
      dataSource.setUsername(config.getString(AmoroManagementConf.DB_USER_NAME));
      dataSource.setPassword(config.getString(AmoroManagementConf.DB_PASSWORD));
    }
    dataSource.setDefaultAutoCommit(false);
    dataSource.setMaxTotal(config.getInteger(AmoroManagementConf.DB_CONNECT_MAX_TOTAL));
    dataSource.setMaxIdle(config.getInteger(AmoroManagementConf.DB_CONNECT_MAX_IDLE));
    dataSource.setMinIdle(0);
    dataSource.setMaxWaitMillis(config.getLong(AmoroManagementConf.DB_CONNECT_MAX_WAIT_MILLIS));
    dataSource.setLogAbandoned(true);
    dataSource.setRemoveAbandonedOnBorrow(true);
    dataSource.setRemoveAbandonedTimeout(60);
    dataSource.setTimeBetweenEvictionRunsMillis(Duration.ofMillis(10 * 60 * 1000L).toMillis());
    dataSource.setTestOnBorrow(BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW);
    dataSource.setTestWhileIdle(BaseObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE);
    dataSource.setMinEvictableIdleTimeMillis(1000);
    dataSource.setNumTestsPerEvictionRun(BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN);
    dataSource.setTestOnReturn(BaseObjectPoolConfig.DEFAULT_TEST_ON_RETURN);
    dataSource.setSoftMinEvictableIdleTimeMillis(
        BaseObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME.toMillis());
    dataSource.setLifo(BaseObjectPoolConfig.DEFAULT_LIFO);
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("develop", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(TableMetaMapper.class);
    configuration.addMapper(OptimizingMapper.class);
    configuration.addMapper(CatalogMetaMapper.class);
    configuration.addMapper(OptimizerMapper.class);
    configuration.addMapper(ApiTokensMapper.class);
    configuration.addMapper(PlatformFileMapper.class);
    configuration.addMapper(ResourceMapper.class);
    configuration.addMapper(TableBlockerMapper.class);

    DatabaseIdProvider provider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    properties.setProperty("MySQL", "mysql");
    properties.setProperty("PostgreSQL", "postgres");
    properties.setProperty("Derby", "derby");
    provider.setProperties(properties);
    configuration.setDatabaseId(provider.getDatabaseId(dataSource));
    if (sqlSessionFactory == null) {
      synchronized (this) {
        if (sqlSessionFactory == null) {
          sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        }
      }
    }
    createTablesIfNeed(config);
  }

  /**
   * create tables for database
   *
   * @param config
   */
  private void createTablesIfNeed(Configurations config) {
    boolean initSchema = config.getBoolean(AmoroManagementConf.DB_AUTO_CREATE_TABLES);
    if (!initSchema) {
      LOG.info("Skip auto create tables due to configuration");
      return;
    }
    String dbTypeConfig = config.getString(AmoroManagementConf.DB_TYPE);
    String query = "";
    LOG.info("Start create tables, database type:{}", dbTypeConfig);

    try (SqlSession sqlSession = get().openSession(true);
        Connection connection = sqlSession.getConnection();
        Statement statement = connection.createStatement()) {
      if (AmoroManagementConf.DB_TYPE_DERBY.equals(dbTypeConfig)) {
        query = "SELECT 1 FROM SYS.SYSTABLES WHERE TABLENAME = 'CATALOG_METADATA'";
      } else if (AmoroManagementConf.DB_TYPE_MYSQL.equals(dbTypeConfig)) {
        query =
            String.format(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = '%s' AND table_name = '%s'",
                connection.getCatalog(), "catalog_metadata");
      } else if (AmoroManagementConf.DB_TYPE_POSTGRES.equals(dbTypeConfig)) {
        query =
            String.format(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = %s AND table_name = '%s'",
                "current_schema()", "catalog_metadata");
      }
      LOG.info("Start check table creation, using query: {}", query);
      try (ResultSet rs = statement.executeQuery(query)) {
        if (!rs.next()) {
          Path script = Paths.get(getInitSqlScriptPath(dbTypeConfig));
          LOG.info("Table not exists, start run create tables script file:{}", script);
          ScriptRunner runner = new ScriptRunner(connection);
          runner.runScript(
              new InputStreamReader(Files.newInputStream(script), StandardCharsets.UTF_8));
          LOG.info("Tables are created successfully");
        } else {
          LOG.info("Tables are created, skip auto create tables.");
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Create tables failed", e);
    }
  }

  private URI getInitSqlScriptPath(String type) throws URISyntaxException {
    String scriptPath = null;
    if (type.equals(AmoroManagementConf.DB_TYPE_MYSQL)) {
      scriptPath = MYSQL_INIT_SQL_SCRIPT;
    } else if (type.equals(AmoroManagementConf.DB_TYPE_DERBY)) {
      scriptPath = DERBY_INIT_SQL_SCRIPT;
    } else if (type.equals(AmoroManagementConf.DB_TYPE_POSTGRES)) {
      scriptPath = POSTGRES_INIT_SQL_SCRIPT;
    }
    URL scriptUrl = ClassLoader.getSystemResource(scriptPath);
    if (scriptUrl == null) {
      throw new IllegalStateException("Cannot find init sql script:" + scriptPath);
    }
    return scriptUrl.toURI();
  }

  public SqlSessionFactory get() {
    Preconditions.checkState(
        sqlSessionFactory != null, "Persistent configuration is not initialized yet.");
    return sqlSessionFactory;
  }

  public static String getDbType() {
    Preconditions.checkState(
        StringUtils.isNotBlank(dbType), "Persistent configuration is not initialized yet.");
    return dbType;
  }
}
