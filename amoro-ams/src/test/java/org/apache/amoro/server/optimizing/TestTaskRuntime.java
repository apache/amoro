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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.api.OptimizingTaskId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestTaskRuntime {

  @Test
  void taskQuotaCopiesFailReason() {
    TaskRuntime<?> task = Mockito.mock(TaskRuntime.class);
    Mockito.when(task.getTaskId()).thenReturn(new OptimizingTaskId(1L, 1));
    Mockito.when(task.getFailReason()).thenReturn("task failed");

    TaskRuntime.TaskQuota quota = new TaskRuntime.TaskQuota(task);

    Assertions.assertEquals("task failed", quota.getFailReason());
  }
}
