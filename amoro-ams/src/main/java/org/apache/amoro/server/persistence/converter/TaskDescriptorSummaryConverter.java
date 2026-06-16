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

package org.apache.amoro.server.persistence.converter;

import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Restores {@code task_runtime.metrics_summary} into the descriptor-specific summary class. */
public class TaskDescriptorSummaryConverter implements TypeHandler<Object> {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  @Override
  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter == null) {
      ps.setString(i, null);
      return;
    }
    try {
      ps.setString(i, MAPPER.writeValueAsString(parameter));
    } catch (JsonProcessingException e) {
      throw new SQLException("Error converting task descriptor summary to JSON string", e);
    }
  }

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    return parse(rs.getString(columnName), TaskDescriptorRecoveryTypes.readFactoryImpl(rs));
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    return parse(rs.getString(columnIndex), TaskDescriptorRecoveryTypes.readFactoryImpl(rs));
  }

  @Override
  public Object getResult(CallableStatement cs, int columnIndex) {
    throw new UnsupportedOperationException(
        "TaskDescriptorSummaryConverter.getResult(CallableStatement) is not supported. "
            + "ResultSet-based mapping should be used instead.");
  }

  private Object parse(String jsonString, String factoryImpl) throws SQLException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return null;
    }
    Class<?> summaryClass = TaskDescriptorRecoveryTypes.resolve(factoryImpl).summaryClass();
    try {
      return MAPPER.readValue(jsonString, summaryClass);
    } catch (JsonProcessingException e) {
      throw new SQLException("Error parsing task descriptor summary JSON string", e);
    }
  }
}
