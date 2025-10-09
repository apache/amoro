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

package org.apache.amoro.table;

/** Detailed table partition properties list. */
public class TablePartitionDetailProperties {
  public static final String FILE_SIZE_SQUARED_ERROR_SUM = "file-size-squared-error-sum";
  public static final double FILE_SIZE_SQUARED_ERROR_SUM_DEFAULT = 0.0;
  public static final String BASE_FILE_COUNT = "base-file-count";
  public static final long BASE_FILE_COUNT_DEFAULT = 0;
  public static final String INSERT_FILE_COUNT = "insert-file-count";
  public static final long INSERT_FILE_COUNT_DEFAULT = 0;
  public static final String EQ_DELETE_FILE_COUNT = "eq-delete-file-count";
  public static final long EQ_DELETE_FILE_COUNT_DEFAULT = 0;
  public static final String POS_DELETE_FILE_COUNT = "pos-delete-file-count";
  public static final long POS_DELETE_FILE_COUNT_DEFAULT = 0;
}
