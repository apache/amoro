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
 * Output interface for maintainer operations executed remotely. Follows the same pattern as
 * TableOptimizing.OptimizingOutput.
 */
public interface MaintainerOutput extends Serializable {

  /**
   * Get a summary of the maintainer operation execution.
   *
   * @return map containing summary information about the operation
   */
  Map<String, String> summary();

  /**
   * Check if the maintainer operation completed successfully.
   *
   * @return true if operation succeeded, false otherwise
   */
  boolean isSuccess();

  /**
   * Get the error message if the operation failed.
   *
   * @return error message, or null if operation succeeded
   */
  String getErrorMessage();
}
