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

package com.netease.arctic.ams.server.persistence;

import com.netease.arctic.ams.server.ArcticManagementConf;
import com.netease.arctic.ams.server.persistence.mapper.ApiTokensMapper;
import com.netease.arctic.ams.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.ams.server.persistence.mapper.OptimizerMapper;
import com.netease.arctic.ams.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.ams.server.persistence.mapper.PlatformFileMapper;
import com.netease.arctic.ams.server.persistence.mapper.ResourceMapper;
import com.netease.arctic.ams.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.ams.server.utils.Configurations;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.time.Duration;

public class SqlSessionFactoryProvider {

  private static final SqlSessionFactoryProvider INSTANCE = new SqlSessionFactoryProvider();

  public static SqlSessionFactoryProvider getInstance() {
    return INSTANCE;
  }

  private volatile SqlSessionFactory sqlSessionFactory;

  public void init(Configurations config) {
    BasicDataSource dataSource = new BasicDataSource();
    //          if (ArcticServiceContainer.conf.getString(ArcticManagementConf.DB_TYPE).equals("derby")) {
    //            dataSource.setUrl(ArcticServiceContainer.conf.getString(ArcticManagementConf.MYBATIS_CONNECTION_URL));
    //            dataSource.setDriverClassName(
    //                ArcticServiceContainer.conf.getString(ArcticManagementConf.MYBATIS_CONNECTION_DRIVER_CLASS_NAME));
    //          } else {
    dataSource.setUsername(config.getString(ArcticManagementConf.DB_USER_NAME));
    dataSource.setPassword(config.getString(ArcticManagementConf.DB_PASSWORD));
    dataSource.setUrl(config.getString(ArcticManagementConf.DB_CONNECTION_URL));
    dataSource.setDriverClassName(config.getString(ArcticManagementConf.DB_DRIVER_CLASS_NAME));
    dataSource.setDefaultAutoCommit(false);
    dataSource.setMaxTotal(20);
    dataSource.setMaxIdle(16);
    dataSource.setMinIdle(0);
    dataSource.setMaxWaitMillis(1000L);
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
    //TODO Derby
    /*  if (ArcticServiceContainer.conf.getString(ArcticManagementConf.DB_TYPE).equals("derby")) {
           configuration.addMapper(DerbyContainerMetadataMapper.class);
           configuration.addMapper(DerbyFileInfoCacheMapper.class);
           configuration.addMapper(DerbyCatalogMetadataMapper.class);
           configuration.addMapper(DerbyTableMetadataMapper.class);
            configuration.addMapper(DerbyOptimizeTasksMapper.class);
           configuration.addMapper(DerbyPlatformFileInfoMapper.class);
         }*/
    if (sqlSessionFactory == null) {
      synchronized (this) {
        if (sqlSessionFactory == null) {
          sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        }
      }
    }
  }

  public SqlSessionFactory get() {
    Preconditions.checkState(
        sqlSessionFactory != null,
        "Persistency configuration is not initialized yet.");

    return sqlSessionFactory;
  }
}
