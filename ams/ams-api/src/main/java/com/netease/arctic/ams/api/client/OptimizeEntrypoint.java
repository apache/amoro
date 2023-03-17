/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.api.client;

import com.netease.arctic.ams.api.JobId;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OptimizeTask;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.ams.api.OptimizerDescriptor;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizerStateReport;
import com.netease.arctic.ams.api.TableIdentifier;
import org.apache.thrift.TException;

public interface OptimizeEntrypoint {

  void startOptimize(TableIdentifier tableIdentifier) throws Exception;

  void stopOptimize(TableIdentifier tableIdentifier) throws Exception;

  OptimizerDescriptor registerOptimizer(OptimizerRegisterInfo registerInfo) throws Exception;

  void reportOptimizerState(OptimizerStateReport reportData) throws Exception;

  void reportOptimizeResult(OptimizeTaskStat optimizeTaskStat) throws Exception;

  OptimizeTask pollTask(int queueId, JobId jobId, String attemptId, long waitTime)

      throws NoSuchObjectException, TException;
  void ping() throws Exception;
}
