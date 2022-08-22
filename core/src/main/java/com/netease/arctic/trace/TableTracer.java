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

package com.netease.arctic.trace;

import com.netease.arctic.data.UpdateColumn;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.Schema;

import java.util.Map;

/**
 * Tracing table changes.
 */
public interface TableTracer {

  /**
   * Add a {@link DataFile} into table
   * @param dataFile file to add
   */
  void addDataFile(DataFile dataFile);

  /**
   * Delete a {@link DataFile} from table
   * @param dataFile file to delete
   */
  void deleteDataFile(DataFile dataFile);

  /**
   * Add a {@link DeleteFile} into table
   * @param deleteFile file to add
   */
  void addDeleteFile(DeleteFile deleteFile);

  /**
   * Add a {@link DataFile} into table
   * @param deleteFile file to delete
   */
  void deleteDeleteFile(DeleteFile deleteFile);

  /**
   * Replace some properties of table
   * @param newProperties properties to replace
   */
  void replaceProperties(Map<String, String> newProperties);

  /**
   * update column of table
   * @param updateColumn updated column info
   */
  void updateColumn(UpdateColumn updateColumn);

  /**
   * Commit table changes.
   */
  void commit();
}
