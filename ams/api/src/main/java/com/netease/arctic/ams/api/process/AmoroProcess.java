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

import com.netease.arctic.ams.api.Action;

public interface AmoroProcess<T extends ProcessState> {

  void submit();

  SimpleFuture getSubmitFuture();

  SimpleFuture getCompleteFuture();

  void close();

  T getState();

  String getSummary();

  default String getFailedReason() {
    return getState().getFailedReason();
  }

  default ProcessStatus getStatus() {
    return getState().getStatus();
  }

  default boolean isClosed() {
    return getState().getStatus() == ProcessStatus.CLOSED;
  }

  default long id() {
    return getState().getId();
  }

  default Action getAction() {
    return getState().getAction();
  }

  default long getStartTime() {
    return getState().getStartTime();
  }

  default long getId() {
    return getState().getId();
  }

  default void whenCompleted(Runnable runnable) {
    getCompleteFuture().whenCompleted(runnable);
  }
}
