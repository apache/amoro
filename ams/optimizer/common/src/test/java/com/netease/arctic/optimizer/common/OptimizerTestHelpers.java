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

package com.netease.arctic.optimizer.common;

import org.apache.iceberg.common.DynFields;
import org.kohsuke.args4j.CmdLineException;

public class OptimizerTestHelpers {
  public static final long CALL_AMS_INTERVAL = 500;

  public static OptimizerConfig buildOptimizerConfig(String amsUrl) {
    String[] optimizerArgs = new String[]{"-a", amsUrl, "-p", "2", "-g", "g1",
                                          "-id", "test_id", "-hb", "1000"};
    try {
      return new OptimizerConfig(optimizerArgs);
    } catch (CmdLineException e) {
      throw new RuntimeException("Build optimizer config filed", e);
    }
  }

  public static void setCallAmsIntervalForTest() {
    DynFields.builder().hiddenImpl(AbstractOptimizerOperator.class, "callAmsInterval").build()
        .set(AbstractOptimizerOperator.class, CALL_AMS_INTERVAL);
  }
}
