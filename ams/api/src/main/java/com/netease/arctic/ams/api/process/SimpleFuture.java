/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.netease.arctic.ams.api.process;

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;

public class SimpleFuture {

  protected CompletableFuture<Object> future;

  public SimpleFuture() {
    future = new CompletableFuture<>();
  }

  private SimpleFuture(CompletableFuture<Object> future) {
    this.future = future;
  }

  public void whenCompleted(Runnable runnable) {
    future = future.whenComplete(
        (v, e)  -> {
          Preconditions.checkState(e == null);
          runnable.run();
        });
  }

  public SimpleFuture complete() {
    future.complete(null);
    return this;
  }

  public void join() {
    future.join();
  }

  public SimpleFuture anyOf(SimpleFuture another) {
    return new SimpleFuture(CompletableFuture.anyOf(future, another.future));
  }
}
