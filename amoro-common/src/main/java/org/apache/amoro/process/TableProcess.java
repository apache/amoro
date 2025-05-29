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

  protected TableProcess(T state, TableRuntime tableRuntime) {
    this.state = state;
    this.tableRuntime = tableRuntime;
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  @Override
  public T getState() {
    return state;
  }

  @Override
  public ProcessStatus getStatus() {
    return state.getStatus();
  }

  protected abstract void closeInternal();

  @Override
  public SimpleFuture getSubmitFuture() {
    return submitFuture.or(completeFuture);
  }

  @Override
  public SimpleFuture getCompleteFuture() {
    return completeFuture;
  }
}
