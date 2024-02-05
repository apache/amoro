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

package com.netease.arctic.server.persistence.converter;

import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.persistence.SqlSessionFactoryProvider;
import com.netease.arctic.server.utils.CompressUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.io.ByteArrayInputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class Object2ByteArrayConvert<T> implements TypeHandler<T> {

  @Override
  public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter == null) {
      if (SqlSessionFactoryProvider.getDbType().equals(ArcticManagementConf.DB_TYPE_POSTGRES)) {
        ps.setNull(i, Types.BINARY);
      } else {
        ps.setNull(i, Types.BLOB);
      }
      return;
    }

    ps.setBinaryStream(
        i,
        new ByteArrayInputStream(
            CompressUtil.gzip(SerializationUtil.simpleSerialize(parameter).array())));
  }

  @Override
  public T getResult(ResultSet rs, String columnName) throws SQLException {
    byte[] bytes = rs.getBytes(columnName);
    if (bytes == null) {
      return null;
    }
    return SerializationUtil.simpleDeserialize(CompressUtil.unGzip(bytes));
  }

  @Override
  public T getResult(ResultSet rs, int columnIndex) throws SQLException {
    byte[] bytes = rs.getBytes(columnIndex);
    if (bytes == null) {
      return null;
    }
    return SerializationUtil.simpleDeserialize(CompressUtil.unGzip(bytes));
  }

  @Override
  public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
    byte[] bytes = cs.getBytes(columnIndex);
    if (bytes == null) {
      return null;
    }
    return SerializationUtil.simpleDeserialize(CompressUtil.unGzip(bytes));
  }
}
