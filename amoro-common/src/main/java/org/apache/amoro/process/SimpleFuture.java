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

import org.apache.amoro.exception.AmoroRuntimeException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** A simple wrapper of CompletableFuture for better code readability. */
public class SimpleFuture {

  private final CompletableFuture<?> completedFuture = new CompletableFuture<>();
  private final Map<Runnable, CompletableFuture<?>> callbackMap = new LinkedHashMap<>();
  private CompletableFuture<?> triggerFuture;
  private CompletableFuture<?> callbackFuture;

  public SimpleFuture() {
    this(new CompletableFuture<>());
  }

  protected SimpleFuture(CompletableFuture<?> triggerFuture) {
    this.triggerFuture = triggerFuture;
    this.callbackFuture = triggerFuture;
    whenCompleted(() -> {});
  }

  /**
   * Only proceed the callback if there is no exception. Exceptions would be thrown in the
   * complete() method and will not trigger join return.
   */
  public void whenCompleted(Runnable runnable) {
    callbackFuture =
        callbackFuture.whenComplete(
            (v, e) -> {
              if (e == null) {
                runnable.run();
                if (callbackMap.get(runnable) == callbackFuture) {
                  completedFuture.complete(null);
                }
              } else {
                throw AmoroRuntimeException.wrap(e);
              }
            });
    callbackMap.put(runnable, callbackFuture);
  }

  public boolean isDone() {
    return completedFuture.isDone();
  }

  public void reset() {
    triggerFuture = new CompletableFuture<>();
    callbackFuture = triggerFuture;
    callbackMap.keySet().forEach(this::whenCompleted);
  }

  /**
   * This method will trigger all callback functions in the same thread and wait until all callback
   * functions are completed. If throws exception, completedFuture is not done. Pay attention that
   * reset could be called during whenCompleted runnable, should ignore waiting
   */
  public void complete() {
    try {
      CompletableFuture<?> originalFuture = triggerFuture;
      if (triggerFuture.complete(null)) {
        if (triggerFuture == originalFuture) {
          callbackFuture.get();
        }
      }
    } catch (Throwable throwable) {
      throw normalize(throwable);
    }
  }

  /** Return until completedFuture is done, which means task or process is truly finished */
  public void join() {
    try {
      completedFuture.join();
    } catch (Throwable throwable) {
      throw normalize(throwable);
    }
  }

  private AmoroRuntimeException normalize(Throwable throwable) {
    if (throwable instanceof ExecutionException && throwable.getCause() != null) {
      return AmoroRuntimeException.wrap(throwable.getCause());
    }
    return AmoroRuntimeException.wrap(throwable);
  }

  public SimpleFuture or(SimpleFuture anotherFuture) {
    return new SimpleFuture(
        CompletableFuture.anyOf(completedFuture, anotherFuture.completedFuture));
  }

  public SimpleFuture and(SimpleFuture anotherFuture) {
    return new SimpleFuture(
        CompletableFuture.allOf(completedFuture, anotherFuture.completedFuture));
  }

  public static SimpleFuture allOf(List<SimpleFuture> futures) {
    return new SimpleFuture(
        CompletableFuture.allOf(
            futures.stream().map(f -> f.completedFuture).toArray(CompletableFuture[]::new)));
  }

  public static SimpleFuture anyOf(List<SimpleFuture> futures) {
    return new SimpleFuture(
        CompletableFuture.anyOf(
            futures.stream().map(f -> f.completedFuture).toArray(CompletableFuture[]::new)));
  }
}
