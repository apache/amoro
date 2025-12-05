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
import org.apache.amoro.SupportsProcessPlugins;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.AmoroProcess;
import org.apache.amoro.process.ManagedProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.SimpleFuture;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.resource.ExternalResourceContainer;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceManager;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.DefaultTableProcessStore;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.List;
import java.util.Optional;

public abstract class PeriodicExternalScheduler extends PeriodicTableScheduler {

  private final ExternalResourceContainer resourceContainer;
  private final ResourceManager resourceManager;
  private final ProcessFactory processFactory;

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
  protected void initHandler(List<TableRuntime> tableRuntimeList) {
    tableRuntimeList.stream()
        .filter(t -> t instanceof SupportsProcessPlugins)
        .map(t -> (SupportsProcessPlugins) t)
        .forEach(tableRuntime -> tableRuntime.install(getAction(), processFactory));
    super.initHandler(tableRuntimeList);
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return Optional.of(tableRuntime)
        .filter(t -> t instanceof SupportsProcessPlugins)
        .map(t -> (SupportsProcessPlugins) t)
        .map(t -> t.enabled(getAction()))
        .orElse(false);
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    Preconditions.checkArgument(tableRuntime instanceof SupportsProcessPlugins);
    SupportsProcessPlugins runtimeSupportProcessPlugin = (SupportsProcessPlugins) tableRuntime;
    // Trigger a table process and check conflicts by table runtime
    // Update process state after process completed, the callback must be register first
    AmoroProcess process = runtimeSupportProcessPlugin.trigger(getAction());
    process.getCompleteFuture().whenCompleted(() -> persistTableProcess(process));
    ManagedProcess managedProcess = new ManagedTableProcess(process);

    // Submit the table process to resource manager, this is a sync operation
    // update process completed and delete related resources
    managedProcess.submit();

    // Trace the table process by async framework so that process can be called back when completed
    trace(process);
  }

  protected int getMaxRetryNumber() {
    return 1;
  }

  protected abstract void trace(AmoroProcess process);

  protected ProcessFactory generateProcessFactory() {
    return new ExternalProcessFactory();
  }

  protected void persistTableProcess(AmoroProcess process) {
    TableProcessStore store = process.store();
    if (store.getStatus() == ProcessStatus.SUBMITTED) {
      new PersistencyHelper().createProcessState(store);
    } else {
      new PersistencyHelper().updateProcessStatus(store);
    }
  }

  private static class PersistencyHelper extends PersistentBase {

    void createProcessState(TableProcessStore store) {
      TableProcessMeta meta = TableProcessMeta.fromTableProcessStore(store);
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.updateProcess(
                  meta.getTableId(),
                  meta.getProcessId(),
                  meta.getExternalProcessIdentifier(),
                  meta.getStatus(),
                  meta.getProcessStage(),
                  meta.getRetryNumber(),
                  meta.getFinishTime(),
                  meta.getFailMessage(),
                  meta.getProcessParameters(),
                  meta.getSummary()));
    }

    void updateProcessStatus(TableProcessStore store) {
      TableProcessMeta meta = TableProcessMeta.fromTableProcessStore(store);
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.updateProcess(
                  meta.getTableId(),
                  meta.getProcessId(),
                  meta.getExternalProcessIdentifier(),
                  meta.getStatus(),
                  meta.getProcessStage(),
                  meta.getRetryNumber(),
                  meta.getFinishTime(),
                  meta.getFailMessage(),
                  meta.getProcessParameters(),
                  meta.getSummary()));
    }
  }

  private class ExternalTableProcess extends TableProcess {

    ExternalTableProcess(TableRuntime tableRuntime) {
      super(
          tableRuntime,
          new DefaultTableProcessStore(
              tableRuntime, new TableProcessMeta(), PeriodicExternalScheduler.this.getAction()));
    }

    ExternalTableProcess(TableRuntime tableRuntime, TableProcessState state) {
      super(
          tableRuntime,
          new DefaultTableProcessStore(
              tableRuntime,
              TableProcessMeta.fromTableProcessState(state),
              PeriodicExternalScheduler.this.getAction()));
    }

    @Override
    protected void closeInternal() {}
  }

  private class ExternalProcessFactory implements ProcessFactory {

    @Override
    public AmoroProcess create(TableRuntime tableRuntime, Action action) {
      return new ExternalTableProcess(tableRuntime);
    }

    @Override
    public AmoroProcess recover(TableRuntime tableRuntime, TableProcessState state) {
      return new ExternalTableProcess(tableRuntime, state);
    }
  }

  protected class ManagedTableProcess implements ManagedProcess {

    private final AmoroProcess process;

    ManagedTableProcess(AmoroProcess process) {
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
                if (store().getStatus() == ProcessStatus.FAILED
                    && store().getRetryNumber() < getMaxRetryNumber()) {
                  retry();
                }
              });
      store().begin().updateTableProcessStatus(ProcessStatus.SUBMITTED).commit();
      getSubmitFuture().complete();
    }

    @Override
    public void complete() {
      store()
          .begin()
          .updateTableProcessStatus(ProcessStatus.SUCCESS)
          .updateFinishTime(System.currentTimeMillis())
          .commit();
      process.getCompleteFuture().complete();
    }

    @Override
    public void complete(String failedReason) {
      store()
          .begin()
          .updateTableProcessStatus(ProcessStatus.FAILED)
          .updateTableProcessFailMessage(failedReason)
          .updateFinishTime(System.currentTimeMillis())
          .commit();
      process.getCompleteFuture().complete();
    }

    @Override
    public void retry() {
      store()
          .begin()
          .updateTableProcessStatus(ProcessStatus.PENDING)
          .updateRetryNumber(store().getRetryNumber() + 1)
          .updateExternalProcessIdentifier("")
          .commit();
      submit();
    }

    @Override
    public void kill() {
      store()
          .begin()
          .updateTableProcessStatus(ProcessStatus.KILLED)
          .updateFinishTime(System.currentTimeMillis())
          .commit();
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
    public TableProcessStore store() {
      return process.store();
    }
  }
}
