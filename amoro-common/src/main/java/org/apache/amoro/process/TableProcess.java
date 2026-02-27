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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An abstract table process to handle table state. */
public abstract class TableProcess implements AmoroProcess {

  public static final Logger LOG = LoggerFactory.getLogger(TableProcess.class);

  protected final TableRuntime tableRuntime;
  protected final TableProcessStore tableProcessStore;
  protected final int maxRetryTime;
  private final SimpleFuture submitFuture = new SimpleFuture();
  private final SimpleFuture completeFuture = new SimpleFuture();

  protected TableProcess(TableRuntime tableRuntime) {
    this(tableRuntime, null, 1);
  }

  protected TableProcess(TableRuntime tableRuntime, TableProcessStore tableProcessStore) {
    this(tableRuntime, tableProcessStore, 1);
  }

  protected TableProcess(
      TableRuntime tableRuntime, TableProcessStore tableProcessStore, int maxRetryTime) {
    this.tableRuntime = tableRuntime;
    this.tableProcessStore = tableProcessStore;
    this.maxRetryTime = maxRetryTime;
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  public String getExternalProcessIdentifier() {
    // TODO: Add a new field to process meta to store external process identifier.(e.g. flink job id
    // or yarn app id)
    return tableProcessStore.getExternalProcessIdentifier();
  }

  @Override
  public TableProcessStore store() {
    return tableProcessStore;
  }

  @Override
  public ProcessStatus getStatus() {
    return tableProcessStore.getStatus();
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
