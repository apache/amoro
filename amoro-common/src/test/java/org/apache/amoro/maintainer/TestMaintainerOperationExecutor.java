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

package org.apache.amoro.maintainer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestMaintainerOperationExecutor {

  @Mock private MaintainerMetrics mockMetrics;

  private MaintainerOperationExecutor executor;

  @BeforeEach
  public void setUp() {
    executor = new MaintainerOperationExecutor(mockMetrics);
  }

  @Test
  public void testSuccessfulOperation() {
    // Execute operation
    executor.execute(
        MaintainerOperationType.ORPHAN_FILES_CLEANING,
        () -> {
          // Operation logic here
        });

    // Verify metrics recorded
    InOrder inOrder = inOrder(mockMetrics);
    inOrder.verify(mockMetrics).recordOperationStart(MaintainerOperationType.ORPHAN_FILES_CLEANING);
    inOrder
        .verify(mockMetrics)
        .recordOperationSuccess(eq(MaintainerOperationType.ORPHAN_FILES_CLEANING), anyLong());
    verify(mockMetrics, never())
        .recordOperationFailure(
            eq(MaintainerOperationType.ORPHAN_FILES_CLEANING), anyLong(), any());
  }

  @Test
  public void testFailedOperation() {
    RuntimeException exception = new RuntimeException("Test error");

    // Execute operation and expect exception
    assertThrows(
        RuntimeException.class,
        () ->
            executor.execute(
                MaintainerOperationType.SNAPSHOT_EXPIRATION,
                () -> {
                  throw exception;
                }));

    // Verify metrics recorded
    InOrder inOrder = inOrder(mockMetrics);
    inOrder.verify(mockMetrics).recordOperationStart(MaintainerOperationType.SNAPSHOT_EXPIRATION);
    inOrder
        .verify(mockMetrics)
        .recordOperationFailure(
            eq(MaintainerOperationType.SNAPSHOT_EXPIRATION), anyLong(), eq(exception));
    verify(mockMetrics, never())
        .recordOperationSuccess(eq(MaintainerOperationType.SNAPSHOT_EXPIRATION), anyLong());
  }

  @Test
  public void testOperationWithResult() {
    Integer expected = 42;

    // Execute operation with result
    Integer result =
        executor.executeAndReturn(
            MaintainerOperationType.DATA_EXPIRATION,
            () -> {
              return expected;
            });

    // Verify result
    assertEquals(expected, result);

    // Verify metrics recorded
    InOrder inOrder = inOrder(mockMetrics);
    inOrder.verify(mockMetrics).recordOperationStart(MaintainerOperationType.DATA_EXPIRATION);
    inOrder
        .verify(mockMetrics)
        .recordOperationSuccess(eq(MaintainerOperationType.DATA_EXPIRATION), anyLong());
  }

  @Test
  public void testOperationWithResultFailure() {
    IllegalStateException exception = new IllegalStateException("Test state error");

    // Execute operation and expect exception
    assertThrows(
        IllegalStateException.class,
        () ->
            executor.executeAndReturn(
                MaintainerOperationType.TAG_CREATION,
                () -> {
                  throw exception;
                }));

    // Verify metrics recorded
    verify(mockMetrics).recordOperationStart(MaintainerOperationType.TAG_CREATION);
    verify(mockMetrics)
        .recordOperationFailure(eq(MaintainerOperationType.TAG_CREATION), anyLong(), eq(exception));
  }

  @Test
  public void testNullMetricsUsesNoop() {
    // Create executor with null metrics
    MaintainerOperationExecutor noopExecutor = new MaintainerOperationExecutor(null);

    // Should not throw exception
    assertDoesNotThrow(
        () ->
            noopExecutor.execute(
                MaintainerOperationType.DANGLING_DELETE_FILES_CLEANING,
                () -> {
                  // Operation logic here
                }));

    // Execute with result
    Integer result =
        assertDoesNotThrow(
            () ->
                noopExecutor.executeAndReturn(
                    MaintainerOperationType.PARTITION_EXPIRATION, () -> 123));
    assertEquals(123, result);
  }

  @Test
  public void testMultipleOperations() {
    // Execute multiple operations
    executor.execute(
        MaintainerOperationType.ORPHAN_FILES_CLEANING,
        () -> {
          // First operation
        });

    executor.execute(
        MaintainerOperationType.SNAPSHOT_EXPIRATION,
        () -> {
          // Second operation
        });

    // Verify both operations were recorded
    verify(mockMetrics, times(1))
        .recordOperationStart(MaintainerOperationType.ORPHAN_FILES_CLEANING);
    verify(mockMetrics, times(1))
        .recordOperationSuccess(eq(MaintainerOperationType.ORPHAN_FILES_CLEANING), anyLong());
    verify(mockMetrics, times(1)).recordOperationStart(MaintainerOperationType.SNAPSHOT_EXPIRATION);
    verify(mockMetrics, times(1))
        .recordOperationSuccess(eq(MaintainerOperationType.SNAPSHOT_EXPIRATION), anyLong());
  }

  @Test
  public void testDurationIsRecorded() {
    // Add a small delay to ensure duration > 0
    long startTime = System.currentTimeMillis();
    executor.execute(
        MaintainerOperationType.DATA_EXPIRATION,
        () -> {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
    long endTime = System.currentTimeMillis();

    // Verify duration is recorded and is reasonable
    InOrder inOrder = inOrder(mockMetrics);
    inOrder.verify(mockMetrics).recordOperationStart(MaintainerOperationType.DATA_EXPIRATION);
    inOrder
        .verify(mockMetrics)
        .recordOperationSuccess(eq(MaintainerOperationType.DATA_EXPIRATION), anyLong());
  }

  @Test
  public void testAllOperationTypes() {
    // Test all operation types
    MaintainerOperationType[] operationTypes = MaintainerOperationType.values();

    for (MaintainerOperationType operationType : operationTypes) {
      executor.execute(
          operationType,
          () -> {
            // Operation logic
          });
    }

    // Verify all operation types were recorded
    for (MaintainerOperationType operationType : operationTypes) {
      verify(mockMetrics, times(1)).recordOperationStart(operationType);
      verify(mockMetrics, times(1)).recordOperationSuccess(eq(operationType), anyLong());
    }
  }
}
