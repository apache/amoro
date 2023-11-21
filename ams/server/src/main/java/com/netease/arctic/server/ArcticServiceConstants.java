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

package com.netease.arctic.server;

public class ArcticServiceConstants {

  public static final long OPTIMIZER_CHECK_INTERVAL = 10 * 1000;
  public static final long OPTIMIZER_METRICS_CALCULATE_INTERVAL = 5 * 60 * 1000; // 5 min

  public static final long INVALID_TIME = 0;

  public static final long QUOTA_LOOK_BACK_TIME = 60 * 60 * 1000;

  public static final long INVALID_SNAPSHOT_ID = -1L;
}
