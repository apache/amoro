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

package org.apache.amoro.optimizer.flink;

import org.apache.amoro.TestAms;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;

public class TestFlinkOptimizerExecutor {

  private static final TestAms TEST_AMS = new TestAms();

  @BeforeAll
  public static void startTestAms() throws Exception {
    TEST_AMS.before();
  }

  @AfterAll
  public static void stopTestAms() {
    TEST_AMS.after();
  }

  @Test
  public void testBestEffortTouchWorksWhileStoppedAndSwallowsErrors()
      throws CmdLineException, TException {
    OptimizerConfig config =
        new OptimizerConfig(new String[] {"-a", TEST_AMS.getServerUrl(), "-p", "1", "-g", "g1"});
    FlinkOptimizerExecutor executor = new FlinkOptimizerExecutor(config, 0);
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    executor.setToken(token);
    executor.stop();

    // Must work after stop: the drain in FlinkExecutor.close() uses it to keep the
    // registration alive while the in-flight task finishes.
    executor.bestEffortTouch();

    // Must swallow failures: unknown token (AMS already expired us) and missing token.
    TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().clear();
    executor.bestEffortTouch();
    executor.setToken(null);
    executor.bestEffortTouch();
  }
}
