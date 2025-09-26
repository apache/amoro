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

import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis type handler for converting between CleanupOperation enum and database integer values.
 * This handler maps CleanupOperation enum values to their corresponding integer codes for
 * persistence, and converts integer codes back to enum values when reading from the database.
 */
@MappedJdbcTypes(JdbcType.INTEGER)
@MappedTypes(CleanupOperation.class)
public class CleanupOperationConverter extends BaseTypeHandler<CleanupOperation> {
  private static final Logger LOG = LoggerFactory.getLogger(CleanupOperationConverter.class);

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, CleanupOperation parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setInt(i, parameter.getCode());
  }

  @Override
  public CleanupOperation getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return convertFromString(rs.getString(columnName));
  }

  @Override
  public CleanupOperation getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return convertFromString(rs.getString(columnIndex));
  }

  @Override
  public CleanupOperation getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return convertFromString(cs.getString(columnIndex));
  }

  /**
   * Converts a string value to a CleanupOperation enum value. Handles null values and parsing
   * errors gracefully.
   *
   * @param s The string value from the database
   * @return The corresponding CleanupOperation enum value, or null if conversion fails
   */
  private CleanupOperation convertFromString(String s) {
    if (s == null) {
      return null;
    }
    try {
      return CleanupOperation.fromCode(Integer.parseInt(s));
    } catch (NumberFormatException e) {
      LOG.warn("Failed to parse CleanupOperation from string: {}", s, e);
      return null;
    }
  }
}
