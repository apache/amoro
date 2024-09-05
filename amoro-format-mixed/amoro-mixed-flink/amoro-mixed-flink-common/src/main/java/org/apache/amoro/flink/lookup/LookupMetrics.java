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

package org.apache.amoro.flink.lookup;

public class LookupMetrics {

  public static final String GROUP_NAME_LOOKUP = "mixed_format_lookup";
  public static final String LOADING_TIME_MS = "lookup_loading_cost_ms";
  public static final String UNIQUE_CACHE_SIZE = "lookup_unique_index_cache_size";
  public static final String SECONDARY_CACHE_SIZE = "lookup_secondary_index_cache_size";
}
