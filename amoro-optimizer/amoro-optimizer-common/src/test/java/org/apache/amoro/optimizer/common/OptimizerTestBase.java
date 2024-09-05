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

package org.apache.amoro.optimizer.common;

import org.apache.amoro.TestAms;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class OptimizerTestBase {
  @ClassRule public static TestAms TEST_AMS = new TestAms();

  @BeforeClass
  public static void reduceCallAmsInterval() {
    OptimizerTestHelpers.setCallAmsIntervalForTest();
  }

  @Before
  public void clearTasks() {
    TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().clear();
    TEST_AMS.getOptimizerHandler().getPendingTasks().clear();
    TEST_AMS.getOptimizerHandler().getExecutingTasks().clear();
    TEST_AMS.getOptimizerHandler().getCompletedTasks().clear();
  }
}
