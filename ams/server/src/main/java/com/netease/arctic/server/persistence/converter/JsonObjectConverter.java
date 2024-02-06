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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonObjectConverter<T> extends BaseTypeHandler<T> {

  private static final ObjectMapper mapper = new ObjectMapper();

  private final Class<T> clazz;

  public JsonObjectConverter(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Type argument cannot be null");
    }
    this.clazz = clazz;
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      ps.setString(i, mapper.writeValueAsString(parameter));
    } catch (JsonProcessingException e) {
      throw new SQLException("Error converting object to JSON string", e);
    }
  }

  @Override
  public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String jsonString = rs.getString(columnName);
    return parseJsonString(jsonString);
  }

  @Override
  public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String jsonString = rs.getString(columnIndex);
    return parseJsonString(jsonString);
  }

  @Override
  public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String jsonString = cs.getString(columnIndex);
    return parseJsonString(jsonString);
  }

  private T parseJsonString(String jsonString) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return null;
    }
    try {
      return mapper.readValue(jsonString, clazz);
    } catch (IOException e) {
      throw new SQLException("Error parsing JSON string", e);
    }
  }
}
