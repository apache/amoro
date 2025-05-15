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

package org.apache.amoro.server.scheduler;

import org.apache.amoro.Action;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.ManagedProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.SimpleFuture;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.resource.ExternalResourceContainer;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceManager;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.ProcessStateMapper;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;

import java.util.List;

public abstract class PeriodicExternalScheduler extends PeriodicTableScheduler {

  private final ExternalResourceContainer resourceContainer;
  private final ResourceManager resourceManager;
  private final ProcessFactory<? extends TableProcessState> processFactory;

  public PeriodicExternalScheduler(
      ResourceManager resourceManager,
      ExternalResourceContainer resourceContainer,
      Action action,
      TableService tableService,
      int poolSize) {
    super(action, tableService, poolSize);
    this.resourceContainer = resourceContainer;
    this.resourceManager = resourceManager;
    this.processFactory = generateProcessFactory();
  }

  @Override
  protected void initHandler(List<DefaultTableRuntime> tableRuntimeList) {
    tableRuntimeList.forEach(tableRuntime -> tableRuntime.install(getAction(), processFactory));
    super.initHandler(tableRuntimeList);
  }

  @Override
  protected boolean enabled(DefaultTableRuntime tableRuntime) {
    return tableRuntime.enabled(getAction());
  }

  @Override
  protected void execute(DefaultTableRuntime tableRuntime) {
    // Trigger a table process and check conflicts by table runtime
    // Update process state after process completed, the callback must be register first
    AmoroProcess<? extends TableProcessState> process = tableRuntime.trigger(getAction());
    process.getCompleteFuture().whenCompleted(() -> persistTableProcess(process));
    ManagedProcess<? extends TableProcessState> managedProcess = new ManagedTableProcess<>(process);

    // Submit the table process to resource manager, this is a sync operation
    // update process completed and delete related resources
    managedProcess.submit();

    // Trace the table process by async framework so that process can be called back when completed
    trace(process);
  }

  protected int getMaxRetryNumber() {
    return 1;
  }

  protected abstract void trace(AmoroProcess<? extends TableProcessState> process);

  protected ProcessFactory<? extends TableProcessState> generateProcessFactory() {
    return new ExternalProcessFactory();
  }

  protected void persistTableProcess(AmoroProcess<? extends TableProcessState> process) {
    TableProcessState state = process.getState();
    if (state.getStatus() == ProcessStatus.SUBMITTED) {
      new PersistencyHelper().createProcessState(state);
    } else if (state.getStatus() == ProcessStatus.RUNNING) {
      new PersistencyHelper().updateProcessRunning(state);
    } else if (state.getStatus() == ProcessStatus.SUCCESS) {
      new PersistencyHelper().updateProcessCompleted(state);
    } else if (state.getStatus() == ProcessStatus.FAILED) {
      new PersistencyHelper().updateProcessFailed(state);
    }
  }

  private static class PersistencyHelper extends PersistentBase {

    void createProcessState(TableProcessState state) {
      doAs(ProcessStateMapper.class, mapper -> mapper.createProcessState(state));
    }

    void updateProcessRunning(TableProcessState state) {
      doAs(ProcessStateMapper.class, mapper -> mapper.updateProcessRunning(state));
    }

    void updateProcessCompleted(TableProcessState state) {
      doAs(ProcessStateMapper.class, mapper -> mapper.updateProcessCompleted(state));
    }

    void updateProcessFailed(TableProcessState state) {
      doAs(ProcessStateMapper.class, mapper -> mapper.updateProcessFailed(state));
    }
  }

  private class ExternalTableProcess extends TableProcess<TableProcessState> {

    ExternalTableProcess(TableRuntime tableRuntime) {
      super(
          new TableProcessState(
              PeriodicExternalScheduler.this.getAction(), tableRuntime.getTableIdentifier()),
          tableRuntime);
    }

    ExternalTableProcess(TableRuntime tableRuntime, TableProcessState state) {
      super(state, tableRuntime);
    }

    @Override
    protected void closeInternal() {}
  }

  private class ExternalProcessFactory implements ProcessFactory<TableProcessState> {

    @Override
    public AmoroProcess<TableProcessState> create(TableRuntime tableRuntime, Action action) {
      return new ExternalTableProcess(tableRuntime);
    }

    @Override
    public AmoroProcess<TableProcessState> recover(
        TableRuntime tableRuntime, TableProcessState state) {
      return new ExternalTableProcess(tableRuntime, state);
    }
  }

  protected class ManagedTableProcess<T extends TableProcessState> implements ManagedProcess<T> {

    private final AmoroProcess<T> process;

    ManagedTableProcess(AmoroProcess<T> process) {
      this.process = process;
    }

    @Override
    public void submit() {
      Resource resource = resourceContainer.submit(this);
      if (resource == null) {
        throw new IllegalStateException("Submit table process can not return null resource");
      }
      persistTableProcess(this);
      resourceManager.createResource(resource);
      getCompleteFuture()
          .whenCompleted(
              () -> {
                resourceManager.deleteResource(resource.getResourceId());
                if (getState().getStatus() == ProcessStatus.FAILED
                    && getState().getRetryNumber() < getMaxRetryNumber()) {
                  retry();
                }
              });
      getState().setSubmitted();
      getSubmitFuture().complete();
    }

    @Override
    public void complete() {
      process.getState().setCompleted();
      process.getCompleteFuture().complete();
    }

    @Override
    public void complete(String failedReason) {
      process.getState().setCompleted(failedReason);
      process.getCompleteFuture().complete();
    }

    @Override
    public void retry() {
      process.getState().addRetryNumber();
      submit();
    }

    @Override
    public void kill() {
      process.getState().setKilled();
      process.getCompleteFuture().complete();
    }

    @Override
    public SimpleFuture getSubmitFuture() {
      return process.getSubmitFuture();
    }

    @Override
    public SimpleFuture getCompleteFuture() {
      return process.getCompleteFuture();
    }

    @Override
    public T getState() {
      return process.getState();
    }
  }
}
