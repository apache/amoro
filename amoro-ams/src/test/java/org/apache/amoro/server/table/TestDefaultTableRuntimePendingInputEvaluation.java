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

package org.apache.amoro.server.table;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class TestDefaultTableRuntimePendingInputEvaluation extends AMSTableTestBase {

  private static final int MAX_PENDING_PARTITIONS = 7;

  public TestDefaultTableRuntimePendingInputEvaluation() {
    super(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(true, false),
        true);
  }

  @Test
  public void testEvaluatePendingInputReceivesConfiguredMaxPendingPartitions() {
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.completeEmptyProcess();

    @SuppressWarnings("unchecked")
    AmoroTable<Object> table = mock(AmoroTable.class);
    AbstractOptimizingEvaluator.PendingInput pendingInput =
        new AbstractOptimizingEvaluator.PendingInput();
    when(table.evaluatePendingInput(any(), anyInt()))
        .thenReturn(Optional.of(new PendingInputResult(pendingInput, true)));

    boolean optimizingNeeded =
        runtime.evaluatePendingInputAndTransition(table, MAX_PENDING_PARTITIONS);

    Assert.assertTrue(optimizingNeeded);
    Assert.assertEquals(OptimizingStatus.PENDING, runtime.getOptimizingStatus());
    verify(table).evaluatePendingInput(any(), eq(MAX_PENDING_PARTITIONS));
  }
}
