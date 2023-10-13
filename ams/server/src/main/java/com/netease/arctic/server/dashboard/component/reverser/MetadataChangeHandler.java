/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard.component.reverser;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An interface for reverse engineering the original DDL statement based on changes to the metadata.
 */
interface MetadataChangeHandler {

  /**
   * Change and add properties.
   * @param diffProperties the properties to be changed and added
   * @return DDL statement
   */
  String changeAndAddProperties(Map<String, String> diffProperties);

  /**
   * Remove properties.
   * @param removeKeys the properties to be removed
   * @return DDL statement
   */
  String removeProperties(Set<String> removeKeys);

  /**
   * Add new columns.
   * @param newSchemas  the new columns to be added
   * @return  DDL statement
   */
  String addNewColumns(List<TableMetaExtract.InternalSchema> newSchemas);

  /**
   * Rename column.
   * @param oldName the old column name
   * @param newName the new column name
   * @return DDL statement
   */
  String renameColumnName(String oldName, String newName);

  /**
   * Drop columns.
   * @param dropColumns the columns to be dropped
   * @return DDL statement
   */
  String dropColumns(Set<String> dropColumns);

  /**
   * Change columns' require.
   * @param columnName the column name
   * @param required true if the column is required, false otherwise
   * @return DDL statement
   */
  String changeColumnsRequire(String columnName, boolean required);

  /**
   * Change columns' comment.
   * @param columnName the column name
   * @param comment the comment of column
   * @return DDL statement
   */
  String changeColumnsComment(String columnName, String comment);

  /**
   * Change columns' type.
   * @param columnName the column name
   * @param newType the new type of column
   * @return DDL statement
   */
  String changeColumnType(String columnName, String newType);
}
