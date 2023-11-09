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

package com.netease.arctic.optimizing;

import com.netease.arctic.scan.TableFileScanHelper;

import java.util.Map;

public class TablePlanExecutor implements OptimizingExecutor<TablePlanOutput> {
  private TableFileScanHelper tableFileScanHelper;
  private OptimizingConfig optimizingConfig;
  private Map<String, String> options;

  public TablePlanExecutor(
      TableFileScanHelper tableFileScanHelper,
      OptimizingConfig optimizingConfig,
      Map<String, String> options) {
    this.tableFileScanHelper = tableFileScanHelper;
    this.optimizingConfig = optimizingConfig;
    this.options = options;
  }

  @Override
  public TablePlanOutput execute() {
    return null;
  }
}
