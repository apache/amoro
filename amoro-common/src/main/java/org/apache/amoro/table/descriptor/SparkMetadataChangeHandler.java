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

package org.apache.amoro.table.descriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Translate to Spark's DDL syntax. */
public class SparkMetadataChangeHandler implements MetadataChangeHandler {
  private static final String ALTER_TABLE = "ALTER TABLE %s";
  private static final String ADD_COLUMN = " ADD COLUMNS %s";
  private static final String ALTER_COLUMN = " ALTER COLUMN %s";
  private static final String RENAME_COLUMN = " RENAME COLUMN %s TO %s";
  private static final String DROP_COLUMNS = " DROP COLUMN %s";
  private static final String SET_PROPERTIES = " SET TBLPROPERTIES (%s)";
  private static final String UNSET_PROPERTIES = " UNSET TBLPROPERTIES (%s)";
  private static final String IS_OPTIONAL = " DROP NOT NULL";
  private static final String NOT_OPTIONAL = " SET NOT NULL";
  private static final String DOC = " COMMENT '%s'";
  private static final String TYPE = " TYPE %s";

  private final String tableName;

  /**
   * Escapes a raw value so it can be spliced between single quotes in a Spark SQL literal. Spark
   * uses backslash escapes rather than the SQL-standard doubled apostrophe, so backslashes are
   * doubled first and apostrophes escaped second.
   */
  private static String escapeLiteral(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  public SparkMetadataChangeHandler(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public String changeAndAddProperties(Map<String, String> diffProperties) {
    String template = ALTER_TABLE + SET_PROPERTIES;
    String properties =
        diffProperties.entrySet().stream()
            .map(
                entry ->
                    String.format(
                        "'%s' = '%s'",
                        escapeLiteral(entry.getKey()), escapeLiteral(entry.getValue())))
            .collect(Collectors.joining(","));
    return String.format(template, tableName, properties);
  }

  @Override
  public String removeProperties(Set<String> removeKeys) {
    String template = ALTER_TABLE + UNSET_PROPERTIES;
    String properties =
        removeKeys.stream()
            .map(key -> String.format("'%s'", escapeLiteral(key)))
            .collect(Collectors.joining(","));
    return String.format(template, tableName, properties);
  }

  @Override
  public String addNewColumns(List<TableMetaExtract.InternalSchema> newSchemas) {
    String template = ALTER_TABLE + ADD_COLUMN;
    return String.format(
        template,
        tableName,
        newSchemas.stream()
            .map(TableMetaExtract.InternalSchema::columnString)
            .collect(Collectors.joining(",", "(", ")")));
  }

  @Override
  public String renameColumnName(String oldName, String newName) {
    String template = ALTER_TABLE + RENAME_COLUMN;
    return String.format(template, tableName, oldName, newName);
  }

  @Override
  public String dropColumns(Set<String> dropColumns) {
    String template = ALTER_TABLE + DROP_COLUMNS;
    return String.format(template, tableName, String.join(",", dropColumns));
  }

  @Override
  public String changeColumnsRequire(String columnName, boolean required) {
    String template = ALTER_TABLE + ALTER_COLUMN + (required ? IS_OPTIONAL : NOT_OPTIONAL);
    return String.format(template, tableName, columnName);
  }

  @Override
  public String changeColumnsComment(String columnName, String comment) {
    String template = ALTER_TABLE + ALTER_COLUMN + DOC;
    // A null comment means the comment is being removed, which Spark spells as an empty literal.
    return String.format(
        template, tableName, columnName, escapeLiteral(comment == null ? "" : comment));
  }

  @Override
  public String changeColumnType(String columnName, String newType) {
    String template = ALTER_TABLE + ALTER_COLUMN + TYPE;
    return String.format(template, tableName, columnName, newType);
  }
}
