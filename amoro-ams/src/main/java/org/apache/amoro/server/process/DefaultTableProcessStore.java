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
import org.apache.amoro.process.ProcessEvent;
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
  private volatile int maxRetryTime = 1;

  // Concurrency guards for status transitions
  private final java.util.concurrent.locks.ReentrantLock transitionLock =
      new java.util.concurrent.locks.ReentrantLock();
  private final java.util.concurrent.atomic.AtomicInteger highestPriority =
      new java.util.concurrent.atomic.AtomicInteger(0);
  private volatile boolean terminalRequested = false;

  /**
   * Create a store for a table process with initial metadata.
   *
   * @param tableRuntime table runtime
   * @param meta process metadata
   * @param action action type
   */
  public DefaultTableProcessStore(TableRuntime tableRuntime, TableProcessMeta meta, Action action) {
    this.meta = meta;
    this.tableRuntime = tableRuntime;
    this.action = action;
  }

  /**
   * Create a store for a table process with explicit identifiers and retry settings.
   *
   * @param processId process id
   * @param tableRuntime table runtime
   * @param meta process metadata
   * @param action action type
   * @param maxRetryTime maximum retry times allowed
   */
  public DefaultTableProcessStore(
      long processId,
      TableRuntime tableRuntime,
      TableProcessMeta meta,
      Action action,
      int maxRetryTime) {
    this.processId = processId;
    this.meta = meta;
    this.tableRuntime = tableRuntime;
    this.action = action;
    this.maxRetryTime = maxRetryTime;
  }

  /**
   * Get process id.
   *
   * @return process id
   */
  @Override
  public long getProcessId() {
    return meta.getProcessId();
  }

  /**
   * Get table id.
   *
   * @return table id
   */
  @Override
  public long getTableId() {
    return meta.getTableId();
  }

  /**
   * Get external process identifier (e.g., job id on engine).
   *
   * @return external process identifier
   */
  @Override
  public String getExternalProcessIdentifier() {
    return meta.getExternalProcessIdentifier();
  }

  /**
   * Get current process status.
   *
   * @return status
   */
  @Override
  public ProcessStatus getStatus() {
    return meta.getStatus();
  }

  /**
   * Get process type (action name).
   *
   * @return process type
   */
  @Override
  public String getProcessType() {
    return meta.getProcessType();
  }

  /**
   * Get process stage description.
   *
   * @return process stage
   */
  @Override
  public String getProcessStage() {
    return meta.getProcessStage();
  }

  /**
   * Get execution engine name.
   *
   * @return execution engine
   */
  @Override
  public String getExecutionEngine() {
    return meta.getExecutionEngine();
  }

  /**
   * Get retry number of this process.
   *
   * @return retry number
   */
  @Override
  public int getRetryNumber() {
    return meta.getRetryNumber();
  }

  /**
   * Get process create time.
   *
   * @return create time in milliseconds
   */
  @Override
  public long getCreateTime() {
    return meta.getCreateTime();
  }

  /**
   * Get process finish time.
   *
   * @return finish time in milliseconds
   */
  @Override
  public long getFinishTime() {
    return meta.getFinishTime();
  }

  /**
   * Get fail message if process failed.
   *
   * @return fail message
   */
  @Override
  public String getFailMessage() {
    return meta.getFailMessage();
  }

  /**
   * Get process parameters.
   *
   * @return process parameters
   */
  @Override
  public Map<String, String> getProcessParameters() {
    return meta.getProcessParameters();
  }

  /**
   * Get process summary.
   *
   * @return summary map
   */
  @Override
  public Map<String, String> getSummary() {
    return meta.getSummary();
  }

  /**
   * Get {@link Action} of this process.
   *
   * @return action
   */
  @Override
  public Action getAction() {
    return action;
  }

  /** Dispose this store and clean persisted state. */
  @Override
  public void dispose() {
    doAsTransaction(
        () ->
            doAs(
                TableProcessMapper.class,
                m -> m.deleteBefore(tableRuntime.getTableIdentifier().getId(), processId)));
  }

  /**
   * Check whether the given status is terminal.
   *
   * @param s process status
   * @return true if terminal
   */
  protected boolean isTerminal(ProcessStatus s) {
    return s == ProcessStatus.SUCCESS
        || (s == ProcessStatus.FAILED && getRetryNumber() >= maxRetryTime)
        || s == ProcessStatus.KILLED
        || s == ProcessStatus.CLOSED
        || s == ProcessStatus.CANCELED;
  }

  /**
   * Validate whether a transition is allowed under state machine rules.
   *
   * @param processEvent process event
   * @param newStatus target status
   * @return true if valid transition
   */
  protected boolean validTransition(
      org.apache.amoro.process.ProcessEvent processEvent, ProcessStatus newStatus) {
    switch (processEvent) {
      case SUBMIT_REQUESTED:
        return !terminalRequested
            && (getStatus() == ProcessStatus.UNKNOWN
                || getStatus() == ProcessStatus.PENDING
                || getStatus() == ProcessStatus.SUBMITTED
                || getStatus() == ProcessStatus.RUNNING);
      case COMPLETE_SUCCESS:
        return !terminalRequested
            && ((getStatus() == ProcessStatus.SUBMITTED
                    || getStatus() == ProcessStatus.RUNNING
                    || getStatus() == ProcessStatus.PENDING)
                && newStatus == ProcessStatus.SUCCESS);
      case COMPLETE_FAILED:
        return !terminalRequested
            && ((getStatus() == ProcessStatus.SUBMITTED
                    || getStatus() == ProcessStatus.RUNNING
                    || getStatus() == ProcessStatus.PENDING)
                && newStatus == ProcessStatus.FAILED);
      case CANCEL_REQUESTED:
        return (getStatus() == ProcessStatus.UNKNOWN
                || getStatus() == ProcessStatus.SUBMITTED
                || getStatus() == ProcessStatus.RUNNING
                || getStatus() == ProcessStatus.PENDING)
            && newStatus == ProcessStatus.CANCELED;
      case KILL_REQUESTED:
        return (getStatus() == ProcessStatus.UNKNOWN
                || getStatus() == ProcessStatus.SUBMITTED
                || getStatus() == ProcessStatus.RUNNING
                || getStatus() == ProcessStatus.PENDING)
            && newStatus == ProcessStatus.KILLED;
      case RETRY_REQUESTED:
        return !terminalRequested
            && (getStatus() == ProcessStatus.FAILED && newStatus == ProcessStatus.PENDING);
      default:
        return false;
    }
  }

  /**
   * Try to transit the process state and persist the changes atomically.
   *
   * @param newStatus target status
   * @param processEvent process event
   * @param externalProcessIdentifier external identifier from engine
   * @param reason message or reason
   * @param processParameters process parameters
   * @param summary summary map
   * @return true if transition succeeds
   */
  public boolean tryTransitState(
      ProcessStatus newStatus,
      ProcessEvent processEvent,
      String externalProcessIdentifier,
      String reason,
      Map<String, String> processParameters,
      Map<String, String> summary) {
    transitionLock.lock();
    try {
      int priority = processEvent.getPriority();
      if (priority >= ProcessEvent.KILL_REQUESTED.getPriority()) {
        highestPriority.updateAndGet(curr -> Math.max(curr, priority));
        terminalRequested = true;
      }

      ProcessStatus currentStatus = getStatus();
      if (isTerminal(currentStatus)) {
        LOG.warn(
            "Failed to transit status for processEvent: {} with initial status: {}, process: {} have been marked as terminal status: {}.",
            processEvent.getName(),
            getStatus(),
            getProcessId(),
            getStatus());
        return false;
      }

      if (highestPriority.get() >= ProcessEvent.KILL_REQUESTED.getPriority()
          && !(processEvent == org.apache.amoro.process.ProcessEvent.CANCEL_REQUESTED
              || processEvent == org.apache.amoro.process.ProcessEvent.KILL_REQUESTED)) {
        LOG.warn(
            "Failed to transit status for processEvent: {} with initial status: {}, process: {} have been marked as higher priority event.",
            processEvent.getName(),
            getStatus(),
            getProcessId());
        return false;
      }

      if (!validTransition(processEvent, newStatus)) {
        LOG.error(
            "Un support status transition for process: {} processEvent: {} with initial status: {} and target status: {}.",
            getProcessId(),
            processEvent.getName(),
            getStatus(),
            newStatus);
        return false;
      }

      switch (processEvent) {
        case SUBMIT_REQUESTED:
          updateExternalProcessIdentifier(newStatus, externalProcessIdentifier);
          break;
        case COMPLETE_SUCCESS:
          updateTableProcessStatus(newStatus);
          break;
        case COMPLETE_FAILED:
          updateTableProcessStatus(newStatus, reason);
          break;
        case CANCEL_REQUESTED:
          updateTableProcessStatus(newStatus, reason);
          break;
        case KILL_REQUESTED:
          updateTableProcessStatus(newStatus, reason);
          break;
        case RETRY_REQUESTED:
          updateTableProcessRetryTimes(getRetryNumber() + 1);
          break;
        default:
          return false;
      }
    } finally {
      transitionLock.unlock();
    }
    return true;
  }

  /**
   * Update status and persist without message.
   *
   * @param status new status
   */
  private void updateTableProcessStatus(ProcessStatus status) {
    updateTableProcessStatus(status, "");
  }

  /**
   * Update status with message and persist.
   *
   * @param status new status
   * @param message fail or info message
   */
  private void updateTableProcessStatus(ProcessStatus status, String message) {
    switch (status) {
      case SUBMITTED:
      case RUNNING:
      case CANCELING:
        begin().updateTableProcessStatus(status).commit();
        break;
      case SUCCESS:
      case CANCELED:
      case CLOSED:
      case KILLED:
        begin()
            .updateTableProcessStatus(status)
            .updateFinishTime(System.currentTimeMillis())
            .commit();
        break;
      case FAILED:
        begin()
            .updateTableProcessStatus(status)
            .updateTableProcessFailMessage(message)
            .updateFinishTime(System.currentTimeMillis())
            .commit();
        break;
      default:
        throw new IllegalArgumentException(
            String.format(
                "Unsupported process status: %s for process: %s.", status, getProcessId()));
    }
  }

  /**
   * Update retry times and reset pending fields.
   *
   * @param retryTimes new retry times
   */
  private void updateTableProcessRetryTimes(int retryTimes) {
    begin()
        .updateTableProcessStatus(ProcessStatus.PENDING)
        .updateRetryNumber(retryTimes)
        .updateExternalProcessIdentifier("")
        .commit();
  }

  /**
   * Update external process identifier along with status.
   *
   * @param status new status
   * @param externalProcessIdentifier identifier from engine
   */
  private void updateExternalProcessIdentifier(
      ProcessStatus status, String externalProcessIdentifier) {
    begin()
        .updateTableProcessStatus(status)
        .updateExternalProcessIdentifier(externalProcessIdentifier)
        .commit();
  }

  /**
   * Begin a batch update operation on process meta.
   *
   * @return operation builder
   */
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

    /**
     * Set external process identifier.
     *
     * @param externalProcessIdentifier identifier from engine
     * @return this operation
     */
    @Override
    public TableProcessOperation updateExternalProcessIdentifier(String externalProcessIdentifier) {
      operations.add(
          () -> {
            oldMeta.setExternalProcessIdentifier(externalProcessIdentifier);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set process status.
     *
     * @param status new status
     * @return this operation
     */
    @Override
    public TableProcessOperation updateTableProcessStatus(ProcessStatus status) {
      operations.add(
          () -> {
            oldMeta.setStatus(status);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set fail message for the process.
     *
     * @param failMessage message
     * @return this operation
     */
    @Override
    public TableProcessOperation updateTableProcessFailMessage(String failMessage) {
      operations.add(
          () -> {
            oldMeta.setFailMessage(failMessage);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set create time.
     *
     * @param createTime milliseconds
     * @return this operation
     */
    @Override
    public TableProcessOperation updateCreateTime(long createTime) {
      operations.add(
          () -> {
            oldMeta.setCreateTime(createTime);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set finish time.
     *
     * @param finishTime milliseconds
     * @return this operation
     */
    @Override
    public TableProcessOperation updateFinishTime(long finishTime) {
      operations.add(
          () -> {
            oldMeta.setFinishTime(finishTime);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set process parameters.
     *
     * @param processParameters parameters
     * @return this operation
     */
    @Override
    public TableProcessOperation updateProcessParameters(Map<String, String> processParameters) {
      operations.add(
          () -> {
            oldMeta.setProcessParameters(processParameters);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set process summary.
     *
     * @param summary summary map
     * @return this operation
     */
    @Override
    public TableProcessOperation updateSummary(Map<String, String> summary) {
      operations.add(
          () -> {
            oldMeta.setSummary(summary);
          });
      metaOperation = true;
      return this;
    }

    /**
     * Set retry number.
     *
     * @param retryNumber retry count
     * @return this operation
     */
    @Override
    public TableProcessOperation updateRetryNumber(int retryNumber) {
      operations.add(
          () -> {
            oldMeta.setRetryNumber(retryNumber);
          });
      metaOperation = true;
      return this;
    }

    /** Commit all staged operations atomically. */
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

    /** Persist current metadata changes to storage. */
    private void persist() {
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.updateProcess(
                  oldMeta.getTableId(),
                  oldMeta.getProcessId(),
                  oldMeta.getExternalProcessIdentifier(),
                  oldMeta.getStatus(),
                  oldMeta.getProcessStage(),
                  oldMeta.getRetryNumber(),
                  oldMeta.getFinishTime(),
                  oldMeta.getFailMessage(),
                  oldMeta.getProcessParameters(),
                  oldMeta.getSummary()));
    }

    /** Make updated fields visible to outer meta if any changes have been applied. */
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
