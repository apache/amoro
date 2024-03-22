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

package com.netease.arctic.server.persistence;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class CasStatedPersistentBase<T> extends StatedPersistentBase {

  private final AtomicReference<T> casRef;
  private final T initialValue;

  protected CasStatedPersistentBase() {
    this((T) null);
  }

  protected CasStatedPersistentBase(T initialValue) {
    super();
    this.initialValue = initialValue;
    this.casRef = new AtomicReference<>(initialValue);
  }

  protected final void invokeConsistencyWithCas(T expected, T target, Runnable runnable) {
    if (casRef.compareAndSet(expected, target)) {
      super.invokeConsistency(runnable);
    } else {
      throw new IllegalStateException(
          "State mismatch in CAS operation, expected: "
              + expected
              + ", target: "
              + target
              + ", actual: "
              + casRef.get());
    }
  }

  protected final <R> R invokeConsistencyWithCas(T expected, T target, Supplier<R> supplier) {
    if (casRef.compareAndSet(expected, target)) {
      return super.invokeConsistency(supplier);
    } else {
      throw new IllegalStateException(
          "State mismatch in CAS operation, expected: "
              + expected
              + ", target: "
              + target
              + ", actual: "
              + casRef.get());
    }
  }

  protected void resetCasRef() {
    casRef.set(initialValue);
  }
}
