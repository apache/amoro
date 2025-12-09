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

package org.apache.amoro.process;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apache.amoro.exception.AmoroRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestSimpleFuture {

  private SimpleFuture simpleFuture;
  private int[] callbackNum;
  private boolean[] calledFlag;

  private void resetCallbackData() {
    callbackNum = new int[] {-1, -1, -1, -1, -1};
    calledFlag = new boolean[] {false, false, false, false, false};
  }

  @BeforeEach
  void setUp() {
    resetCallbackData();
    simpleFuture = new SimpleFuture();
    long threadId = Thread.currentThread().getId();
    for (int i = 0; i < callbackNum.length; i++) {
      final int num = i;
      simpleFuture.whenCompleted(
          () -> {
            for (int j = num; j < calledFlag.length; j++) {
              Assertions.assertFalse(
                  calledFlag[j], "callback " + j + " should not be called before " + num);
            }
            // Trigger error if callbackNum[num] == 0
            if (callbackNum[num] == 0) {
              throw new RuntimeException("Callback error");
            }
            callbackNum[num] = num;
            Assertions.assertEquals(
                threadId,
                Thread.currentThread().getId(),
                "Callback should be run in the same thread");
          });
    }
  }

  @Test
  void testComplete() {
    simpleFuture.complete();

    for (int i = 0; i < 5; i++) {
      Assertions.assertEquals(i, callbackNum[i], "Current callback num: " + i);
    }
    Assertions.assertTrue(
        simpleFuture.isDone(), "SimpleFuture should complete if callback no error");
  }

  @Test
  void testCompleteTwiceButNotReset() {
    simpleFuture.complete();
    // The second call will not trigger any callback
    for (int i = 0; i < 5; i++) {
      callbackNum[i] = -1;
    }
    simpleFuture.complete();
    for (int i = 0; i < 5; i++) {
      Assertions.assertEquals(-1, callbackNum[i], "Current callback num: " + i);
    }
    Assertions.assertTrue(
        simpleFuture.isDone(), "SimpleFuture should not complete if callback error");
  }

  // Additional tests for edge cases and error conditions
  @Test
  void testCallbackError() {
    callbackNum[2] = 0; // Trigger error in callbackNum[2]

    try {
      simpleFuture.complete();
    } catch (Throwable throwable) {
      Assertions.assertTrue(throwable instanceof AmoroRuntimeException, "Should catch the error");
      Assertions.assertTrue(
          throwable.getCause() instanceof RuntimeException, "Should catch the error");
      Assertions.assertEquals(
          "Callback error", throwable.getCause().getMessage(), "Should catch the error");
    }
    for (int i = 0; i < 5; i++) {
      if (i < 2) {
        Assertions.assertEquals(i, callbackNum[i], "Current callback num: " + i);
      } else if (i == 2) {
        Assertions.assertEquals(0, callbackNum[i], "Current callback num: " + i);
      } else {
        Assertions.assertEquals(-1, callbackNum[i], "Current callback num: " + i);
      }
    }
    Assertions.assertFalse(
        simpleFuture.isDone(), "SimpleFuture should not complete if callback error");
  }

  @Test
  void testReset() {
    callbackNum[2] = 0; // Trigger error in callbackNum[2]
    try {
      simpleFuture.complete();
    } catch (Throwable throwable) {
      Assertions.assertTrue(throwable instanceof AmoroRuntimeException, "Should catch the error");
    }
    Assertions.assertFalse(
        simpleFuture.isDone(), "SimpleFuture should not complete if callback error");

    resetCallbackData(); // Trigger normal callback
    simpleFuture.reset();
    simpleFuture.complete();
    for (int i = 0; i < 5; i++) {
      Assertions.assertEquals(i, callbackNum[i], "Current callback num: " + i);
    }
    Assertions.assertTrue(
        simpleFuture.isDone(), "SimpleFuture should not complete if callback error");
  }

  @Test
  void testIsDone() {
    simpleFuture.complete();
    Assertions.assertTrue(simpleFuture.isDone(), "Future should be completed");
  }

  @Test
  void testCompleteException() throws ExecutionException, InterruptedException {
    CompletableFuture<?> future = mock(CompletableFuture.class);
    doReturn(true).when(future).complete(null);
    doThrow(new RuntimeException()).when(future).get();
    SimpleFuture simpleFuture = new SimpleFuture(future);

    Assertions.assertThrows(
        AmoroRuntimeException.class,
        () -> {
          simpleFuture.complete();
        });
  }

  @Test
  void testJoin() {
    simpleFuture.complete();
    simpleFuture.join();
    Assertions.assertTrue(simpleFuture.isDone(), "Future should be completed");
  }

  @Test
  void testJoinException() throws ExecutionException, InterruptedException {
    CompletableFuture<?> future = mock(CompletableFuture.class);
    doThrow(new RuntimeException()).when(future).get();
    SimpleFuture simpleFuture = new SimpleFuture(future);
    simpleFuture.reset();
    simpleFuture.complete();
    simpleFuture.join();
    Assertions.assertTrue(simpleFuture.isDone(), "Future should be completed");
  }

  @Test
  void testOr() {
    SimpleFuture anotherFuture = new SimpleFuture();
    SimpleFuture combinedFuture = simpleFuture.or(anotherFuture);

    simpleFuture.complete();
    Assertions.assertTrue(
        combinedFuture.isDone(),
        "Combined future should be completed when either future completes");
  }

  @Test
  void testAnd() {
    SimpleFuture anotherFuture = new SimpleFuture();
    SimpleFuture combinedFuture = simpleFuture.and(anotherFuture);

    simpleFuture.complete();
    anotherFuture.complete();
    Assertions.assertTrue(
        combinedFuture.isDone(), "Combined future should be completed when both futures complete");
  }

  @Test
  void testAllOf() {
    List<SimpleFuture> futures =
        Arrays.asList(new SimpleFuture(), new SimpleFuture(), new SimpleFuture());
    SimpleFuture combinedFuture = SimpleFuture.allOf(futures);

    futures.forEach(SimpleFuture::complete);
    Assertions.assertTrue(
        combinedFuture.isDone(), "Combined future should be completed when all futures complete");
  }

  @Test
  void testAnyOf() {
    List<SimpleFuture> futures = Arrays.asList(new SimpleFuture(), new SimpleFuture());
    SimpleFuture combinedFuture = SimpleFuture.anyOf(futures);

    futures.get(0).complete();
    Assertions.assertTrue(
        combinedFuture.isDone(), "Combined future should be completed when any future completes");
  }

  // Test for when the future is already completed before calling complete()
  @Test
  void testCompleteAlreadyCompleted() {
    simpleFuture.complete();
    try {
      simpleFuture.complete();
    } catch (Throwable throwable) {
      Assertions.fail(throwable.getMessage());
    }
  }

  // Test for when the future is already completed before calling join()
  @Test
  void testJoinAlreadyCompleted() {
    simpleFuture.complete();
    try {
      simpleFuture.join();
    } catch (Throwable throwable) {
      Assertions.fail(throwable.getMessage());
    }
  }
}
