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

import org.apache.amoro.TableRuntime;

/**
 * An abstract table process to handle table state.
 *
 * @param <T>
 */
public abstract class TableProcess<T extends TableProcessState> implements AmoroProcess<T> {

  protected final T state;
  protected final TableRuntime tableRuntime;
  private final SimpleFuture submitFuture = new SimpleFuture();
  private final SimpleFuture completeFuture = new SimpleFuture();
  private volatile ProcessStatus status = ProcessStatus.ACTIVE;
  private volatile String failedReason;

  protected TableProcess(T state, TableRuntime tableRuntime) {
    this.state = state;
    this.tableRuntime = tableRuntime;
    this.completeFuture.whenCompleted(
        () -> {
          if (status == ProcessStatus.FAILED) {
            state.setFailedReason(failedReason);
          } else {
            state.setStatus(status);
          }
        });
  }

  protected void completeSubmitting() {
    submitFuture.complete();
  }

  protected void complete() {
    status = ProcessStatus.SUCCESS;
    completeFuture.complete();
  }

  protected void complete(String errorMessage) {
    status = ProcessStatus.FAILED;
    failedReason = errorMessage;
    completeFuture.complete();
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  @Override
  public T getState() {
    return state;
  }

  @Override
  public void close() {
    closeInternal();
    status = ProcessStatus.CLOSED;
    completeFuture.complete();
  }

  @Override
  public ProcessStatus getStatus() {
    return status;
  }

  protected abstract void closeInternal();

  @Override
  public SimpleFuture getSubmitFuture() {
    return submitFuture.anyOf(completeFuture);
  }

  @Override
  public SimpleFuture getCompleteFuture() {
    return completeFuture;
  }
}
