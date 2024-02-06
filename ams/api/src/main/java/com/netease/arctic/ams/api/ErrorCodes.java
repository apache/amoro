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

package com.netease.arctic.ams.api;

public final class ErrorCodes {

  public static final int UNDEFINED_ERROR_CODE = -1;

  public static final int PERSISTENCE_ERROR_CODE = 1000;
  public static final int OBJECT_NOT_EXISTS_ERROR_CODE = 1001;
  public static final int ALREADY_EXISTS_ERROR_CODE = 1002;
  public static final int ILLEGAL_METADATA_ERROR_CODE = 1003;
  public static final int FORBIDDEN_ERROR_CODE = 1004;

  public static final int TASK_NOT_FOUND_ERROR_CODE = 2001;
  public static final int DUPLICATED_TASK_ERROR_CODE = 2002;
  public static final int OPTIMIZING_CLOSED_ERROR_CODE = 2003;
  public static final int ILLEGAL_TASK_STATE_ERROR_CODE = 2004;
  public static final int PLUGIN_AUTH_ERROR_CODE = 2005;
  public static final int PLUGIN_RETRY_AUTH_ERROR_CODE = 2006;

  public static final int BLOCKER_CONFLICT_ERROR_CODE = 3001;
}
