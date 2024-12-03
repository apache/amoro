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

import static com.github.pagehelper.page.PageAutoDialect.registerDialectAlias;

import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.dialect.helper.MySqlDialect;
import com.github.pagehelper.dialect.helper.PostgreSqlDialect;
import com.github.pagehelper.dialect.helper.SqlServer2012Dialect;
import org.apache.amoro.server.persistence.mapper.ApiTokensMapper;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.persistence.mapper.PlatformFileMapper;
import org.apache.amoro.server.persistence.mapper.ResourceMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.Properties;

public class SqlSessionFactoryProvider {
  private static final SqlSessionFactoryProvider INSTANCE = new SqlSessionFactoryProvider();

  public static SqlSessionFactoryProvider getInstance() {
    return INSTANCE;
  }

  private volatile SqlSessionFactory sqlSessionFactory;

  public void init(DataSource dataSource) throws SQLException {
    registerDialectAliases();

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

    PageInterceptor interceptor = new PageInterceptor();
    Properties interceptorProperties = new Properties();
    interceptorProperties.setProperty("reasonable", "false");
    interceptor.setProperties(interceptorProperties);
    configuration.addInterceptor(interceptor);

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
  }

  private void registerDialectAliases() {
    registerDialectAlias("postgres", PostgreSqlDialect.class);
    registerDialectAlias("mysql", MySqlDialect.class);
    registerDialectAlias("derby", SqlServer2012Dialect.class);
  }

  public SqlSessionFactory get() {
    Preconditions.checkState(
        sqlSessionFactory != null, "Persistent configuration is not initialized yet.");
    return sqlSessionFactory;
  }
}
