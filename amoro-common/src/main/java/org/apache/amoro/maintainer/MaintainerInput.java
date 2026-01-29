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

package org.apache.amoro.maintainer;

import java.io.Serializable;
import java.util.Map;

/**
 * Input interface for maintainer operations executed remotely. Follows the same pattern as
 * TableOptimizing.OptimizingInput.
 */
public interface MaintainerInput extends Serializable {

  /** Maintainer operation type */
  enum OperationType {
    SNAPSHOT_EXPIRATION,
    ORPHAN_FILE_CLEANING,
    DANGLING_DELETE_CLEANING,
    DATA_EXPIRATION,
    TAG_CREATION
  }

  /**
   * Get the operation type for this maintainer task.
   *
   * @return the operation type
   */
  OperationType getOperationType();

  /**
   * Get the table identifier.
   *
   * @return table identifier string
   */
  String getTableIdentifier();

  /**
   * Get the table format (ICEBERG, PAIMON, etc.).
   *
   * @return table format string
   */
  String getTableFormat();

  /**
   * Set an option for this maintainer operation.
   *
   * @param name option name
   * @param value option value
   */
  void option(String name, String value);

  /**
   * Set multiple options for this maintainer operation.
   *
   * @param options map of option names to values
   */
  void options(Map<String, String> options);

  /**
   * Get all options for this maintainer operation.
   *
   * @return map of option names to values
   */
  Map<String, String> getOptions();
}
