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

package org.apache.amoro.server.process;

import org.apache.amoro.Action;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.exception.AmoroRuntimeException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.persistence.NestedSqlSession;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Default table runtime store implementation. */
public class DefaultTableProcessStore extends PersistentBase implements TableProcessStore {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultTableProcessStore.class);
  private final Lock tableProcessLock = new ReentrantLock();
  private final TableProcessMeta meta;
  private final Action action;

  private volatile long processId;
  private TableRuntime tableRuntime;

  public DefaultTableProcessStore(TableRuntime tableRuntime, TableProcessMeta meta, Action action) {
    this.meta = meta;
    this.tableRuntime = tableRuntime;
    this.action = action;
  }

  public DefaultTableProcessStore(
      long processId, TableRuntime tableRuntime, TableProcessMeta meta, Action action) {
    this.processId = processId;
    this.meta = meta;
    this.tableRuntime = tableRuntime;
    this.action = action;
  }

  @Override
  public long getProcessId() {
    return meta.getProcessId();
  }

  @Override
  public long getTableId() {
    return meta.getTableId();
  }

  @Override
  public String getExternalProcessIdentifier() {
    return meta.getExternalProcessIdentifier();
  }

  @Override
  public ProcessStatus getStatus() {
    return meta.getStatus();
  }

  @Override
  public String getProcessType() {
    return meta.getProcessType();
  }

  @Override
  public String getProcessStage() {
    return meta.getProcessStage();
  }

  @Override
  public String getExecutionEngine() {
    return meta.getExecutionEngine();
  }

  @Override
  public int getRetryNumber() {
    return meta.getRetryNumber();
  }

  @Override
  public long getCreateTime() {
    return meta.getCreateTime();
  }

  @Override
  public long getFinishTime() {
    return meta.getFinishTime();
  }

  @Override
  public String getFailMessage() {
    return meta.getFailMessage();
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return meta.getProcessParameters();
  }

  @Override
  public Map<String, String> getSummary() {
    return meta.getSummary();
  }

  @Override
  public Action getAction() {
    return action;
  }

  @Override
  public void dispose() {
    doAsTransaction(
        () ->
            doAs(
                TableProcessMapper.class,
                m -> m.deleteBefore(tableRuntime.getTableIdentifier().getId(), processId)));
  }

  @Override
  public TableProcessOperation begin() {
    return new TableProcessOperationImpl();
  }

  private class TableProcessOperationImpl implements TableProcessOperation {

    private final TableProcessMeta oldMeta;
    private final List<Runnable> operations = Lists.newArrayList();
    private boolean metaOperation = false;

    public TableProcessOperationImpl() {
      this.oldMeta = meta.copy();
    }

    @Override
    public TableProcessOperation updateExternalProcessIdentifier(String externalProcessIdentifier) {
      operations.add(
          () -> {
            oldMeta.setExternalProcessIdentifier(externalProcessIdentifier);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateTableProcessStatus(ProcessStatus status) {
      operations.add(
          () -> {
            oldMeta.setStatus(status);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateTableProcessFailMessage(String failMessage) {
      operations.add(
          () -> {
            oldMeta.setFailMessage(failMessage);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateCreateTime(long createTime) {
      operations.add(
          () -> {
            oldMeta.setCreateTime(createTime);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateFinishTime(long finishTime) {
      operations.add(
          () -> {
            oldMeta.setFinishTime(finishTime);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateProcessParameters(Map<String, String> processParameters) {
      operations.add(
          () -> {
            oldMeta.setProcessParameters(processParameters);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateSummary(Map<String, String> summary) {
      operations.add(
          () -> {
            oldMeta.setSummary(summary);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public TableProcessOperation updateRetryNumber(int retryNumber) {
      operations.add(
          () -> {
            oldMeta.setRetryNumber(retryNumber);
          });
      metaOperation = true;
      return this;
    }

    @Override
    public void commit() {
      try (NestedSqlSession session = beginSession()) {
        try {
          tableProcessLock.lock();
          operations.forEach(Runnable::run);
          persist();
          session.commit();
        } catch (Throwable t) {
          LOG.error("failed to commit operations", t);
          session.rollback();
          throw AmoroRuntimeException.wrap(t, PersistenceException::new);
        } finally {
          tableProcessLock.unlock();
        }
        visitable();
      }
    }

    private void persist() {
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.updateProcess(
                      oldMeta.getTableId(),
                      oldMeta.getProcessId(),
                      oldMeta.getStatus(),
                      oldMeta.getProcessStage(),
                      oldMeta.getFinishTime(),
                      oldMeta.getFailMessage(),
                      oldMeta.getSummary()));
    }

    private void visitable() {
      if (metaOperation) {
        meta.setExternalProcessIdentifier(oldMeta.getExternalProcessIdentifier());
        meta.setStatus(oldMeta.getStatus());
        meta.setFailMessage(oldMeta.getFailMessage());
        meta.setRetryNumber(oldMeta.getRetryNumber());
        meta.setCreateTime(oldMeta.getCreateTime());
        meta.setFinishTime(oldMeta.getFinishTime());
      }
    }
  }
}
