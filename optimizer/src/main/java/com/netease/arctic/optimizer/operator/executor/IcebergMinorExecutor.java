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

package com.netease.arctic.optimizer.operator.executor;

import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcebergMinorExecutor<F extends ContentFile<F>> extends BaseExecutor<F> {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergExecutor.class);

  private final NodeTask task;
  private final ArcticTable table;
  private final long startTime;
  private final OptimizerConfig config;

  public IcebergMinorExecutor(NodeTask nodeTask,
      ArcticTable table,
      long startTime,
      OptimizerConfig config) {
    this.task = nodeTask;
    this.table = table;
    this.startTime = startTime;
    this.config = config;
  }

  @Override
  public OptimizeTaskResult<F> execute() throws Exception {
    LOG.info("start process iceberg table optimize task: {}", task);

    if (task.icebergDataFiles().size() > 0) {
      // optimize iceberg delete files.

    } else if (task.icebergSmallDataFiles().size() > 0) {
      // optimize iceberg small data files.

    }

    return null;
  }

  @Override
  public void close() {

  }
}
