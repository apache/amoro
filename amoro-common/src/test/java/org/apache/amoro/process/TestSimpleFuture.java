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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

  @Before
  public void setUp() {
    resetCallbackData();
    simpleFuture = new SimpleFuture();
    long threadId = Thread.currentThread().getId();
    for (int i = 0; i < callbackNum.length; i++) {
      final int num = i;
      simpleFuture.whenCompleted(
          () -> {
            for (int j = num; j < calledFlag.length; j++) {
              Assert.assertFalse(
                  "callback " + j + " should not be called before " + num, calledFlag[j]);
            }
            // Trigger error if callbackNum[num] == 0
            if (callbackNum[num] == 0) {
              throw new RuntimeException("Callback error");
            }
            callbackNum[num] = num;
            Assert.assertEquals(
                "Callback should be run in the same thread",
                threadId,
                Thread.currentThread().getId());
          });
    }
  }

  @Test
  public void testComplete() {
    simpleFuture.complete();

    for (int i = 0; i < 5; i++) {
      Assert.assertEquals("Current callback num: " + i, i, callbackNum[i]);
    }
    Assert.assertTrue("SimpleFuture should complete if callback no error", simpleFuture.isDone());
  }

  @Test
  public void testCompleteTwiceButNotReset() {
    simpleFuture.complete();
    // The second call will not trigger any callback
    for (int i = 0; i < 5; i++) {
      callbackNum[i] = -1;
    }
    simpleFuture.complete();
    for (int i = 0; i < 5; i++) {
      Assert.assertEquals("Current callback num: " + i, -1, callbackNum[i]);
    }
    Assert.assertTrue("SimpleFuture should not complete if callback error", simpleFuture.isDone());
  }

  // Additional tests for edge cases and error conditions
  @Test
  public void testCallbackError() {
    callbackNum[2] = 0; // Trigger error in callbackNum[2]

    try {
      simpleFuture.complete();
    } catch (Throwable throwable) {
      Assert.assertTrue("Should catch the error", throwable instanceof AmoroRuntimeException);
      Assert.assertTrue("Should catch the error", throwable.getCause() instanceof RuntimeException);
      Assert.assertEquals(
          "Should catch the error", "Callback error", throwable.getCause().getMessage());
    }
    for (int i = 0; i < 5; i++) {
      if (i < 2) {
        Assert.assertEquals("Current callback num: " + i, i, callbackNum[i]);
      } else if (i == 2) {
        Assert.assertEquals("Current callback num: " + i, 0, callbackNum[i]);
      } else {
        Assert.assertEquals("Current callback num: " + i, -1, callbackNum[i]);
      }
    }
    Assert.assertFalse("SimpleFuture should not complete if callback error", simpleFuture.isDone());
  }

  @Test
  public void testReset() {
    callbackNum[2] = 0; // Trigger error in callbackNum[2]
    try {
      simpleFuture.complete();
    } catch (Throwable throwable) {
      Assert.assertTrue("Should catch the error", throwable instanceof AmoroRuntimeException);
    }
    Assert.assertFalse("SimpleFuture should not complete if callback error", simpleFuture.isDone());

    resetCallbackData(); // Trigger normal callback
    simpleFuture.reset();
    simpleFuture.complete();
    for (int i = 0; i < 5; i++) {
      Assert.assertEquals("Current callback num: " + i, i, callbackNum[i]);
    }
    Assert.assertTrue("SimpleFuture should not complete if callback error", simpleFuture.isDone());
  }

  @Test
  public void testIsDone() {
    simpleFuture.complete();
    Assert.assertTrue("Future should be completed", simpleFuture.isDone());
  }

  @Test(expected = AmoroRuntimeException.class)
  public void testCompleteException() throws ExecutionException, InterruptedException {
    CompletableFuture<?> future = mock(CompletableFuture.class);
    doReturn(true).when(future).complete(null);
    doThrow(new RuntimeException()).when(future).get();
    SimpleFuture simpleFuture = new SimpleFuture(future);

    simpleFuture.complete();
  }

  @Test
  public void testJoin() {
    simpleFuture.complete();
    simpleFuture.join();
    Assert.assertTrue("Future should be completed", simpleFuture.isDone());
  }

  @Test
  public void testJoinException() throws ExecutionException, InterruptedException {
    CompletableFuture<?> future = mock(CompletableFuture.class);
    doThrow(new RuntimeException()).when(future).get();
    SimpleFuture simpleFuture = new SimpleFuture(future);
    simpleFuture.reset();
    simpleFuture.complete();
    simpleFuture.join();
    Assert.assertTrue("Future should be completed", simpleFuture.isDone());
  }

  @Test
  public void testOr() {
    SimpleFuture anotherFuture = new SimpleFuture();
    SimpleFuture combinedFuture = simpleFuture.or(anotherFuture);

    simpleFuture.complete();
    Assert.assertTrue(
        "Combined future should be completed when either future completes",
        combinedFuture.isDone());
  }

  @Test
  public void testAnd() {
    SimpleFuture anotherFuture = new SimpleFuture();
    SimpleFuture combinedFuture = simpleFuture.and(anotherFuture);

    simpleFuture.complete();
    anotherFuture.complete();
    Assert.assertTrue(
        "Combined future should be completed when both futures complete", combinedFuture.isDone());
  }

  @Test
  public void testAllOf() {
    List<SimpleFuture> futures =
        Arrays.asList(new SimpleFuture(), new SimpleFuture(), new SimpleFuture());
    SimpleFuture combinedFuture = SimpleFuture.allOf(futures);

    futures.forEach(SimpleFuture::complete);
    Assert.assertTrue(
        "Combined future should be completed when all futures complete", combinedFuture.isDone());
  }

  @Test
  public void testAnyOf() {
    List<SimpleFuture> futures = Arrays.asList(new SimpleFuture(), new SimpleFuture());
    SimpleFuture combinedFuture = SimpleFuture.anyOf(futures);

    futures.get(0).complete();
    Assert.assertTrue(
        "Combined future should be completed when any future completes", combinedFuture.isDone());
  }

  // Test for when the future is already completed before calling complete()
  @Test
  public void testCompleteAlreadyCompleted() {
    simpleFuture.complete();
    try {
      simpleFuture.complete();
    } catch (Throwable throwable) {
      Assert.fail(throwable.getMessage());
    }
  }

  // Test for when the future is already completed before calling join()
  @Test
  public void testJoinAlreadyCompleted() {
    simpleFuture.complete();
    try {
      simpleFuture.join();
    } catch (Throwable throwable) {
      Assert.fail(throwable.getMessage());
    }
  }
}
