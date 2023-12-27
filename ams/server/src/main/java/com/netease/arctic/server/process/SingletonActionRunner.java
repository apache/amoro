package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.process.AmoroProcess;
import com.netease.arctic.ams.api.process.ProcessFactory;
import com.netease.arctic.ams.api.process.ProcessState;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableProcessMapper;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class SingletonActionRunner<T extends ProcessState> extends PersistentBase {

  protected final Map<Long, AmoroProcess<T>> externalProcesses = new HashMap<>();
  protected final Lock lock = new ReentrantLock();
  protected final ProcessFactory<T> defaultProcessFactory;
  protected final TableRuntime tableRuntime;
  protected final Action action;
  protected volatile AmoroProcess<T> defaultProcess;
  private volatile long lastTriggerTime;
  private volatile long lastCompletedTime;
  private int retryCount;

  public SingletonActionRunner(
      TableRuntime tableRuntime,
      ProcessFactory<T> defaultProcessFactory,
      Action action,
      boolean recover) {
    this.tableRuntime = tableRuntime;
    this.defaultProcessFactory = defaultProcessFactory;
    this.action = action;
    if (recover) {
      recoverProcesses();
    }
  }

  protected abstract void recoverProcesses();

  protected abstract void handleCompleted(AmoroProcess<T> process);

  protected abstract void handleSubmitted(AmoroProcess<T> process);

  public AmoroProcess<T> run() {
    // TODO: throw a reasonable exception
    Preconditions.checkState(externalProcesses.isEmpty());
    lock.lock();
    try {
      closeDefaultProcess();
      defaultProcess = defaultProcessFactory.create(tableRuntime, action);
      if (defaultProcess != null) {
        submitProcess(defaultProcess);
      }
      return defaultProcess;
    } finally {
      lock.unlock();
    }
  }

  public AmoroProcess<T> run(ProcessFactory<T> processFactory) {
    lock.lock();
    try {
      closeDefaultProcess();
      AmoroProcess<T> process = processFactory.create(tableRuntime, action);
      if (process != null) {
        submitProcess(process);
      }
      return process;
    } finally {
      lock.unlock();
    }
  }

  protected void submitProcess(AmoroProcess<T> process) {
    if (process != defaultProcess && externalProcesses.containsKey(process.getId())) {
      throw new IllegalStateException("Process " + process.getId() + " has already been submitted");
    }
    doAsTransaction(
        () -> {
          doAs(
              TableProcessMapper.class,
              mapper ->
                  mapper.insertLiveProcess(
                      tableRuntime.getTableIdentifier().getId(),
                      action,
                      process.getId(),
                      defaultProcess == process));
          process.whenCompleted(() -> processCompleted(process));
          if (process == defaultProcess) {
            defaultProcess = process;
            lastTriggerTime = process.getState().getStartTime();
          } else {
            externalProcesses.put(process.getId(), process);
          }
          process.submit();
          handleSubmitted(process);
        });
  }

  private void processCompleted(AmoroProcess<T> process) {
    lock.lock();
    try {
      if (process.getStatus() == ProcessStatus.SUCCESS || defaultProcess != process) {
        doAs(
            TableProcessMapper.class,
            mapper ->
                mapper.deleteLiveProcess(
                    tableRuntime.getTableIdentifier().getId(), action, process.getId()));
        if (defaultProcess == process && process.getStatus() == ProcessStatus.SUCCESS) {
          lastCompletedTime = System.currentTimeMillis();
          retryCount = 0;
          defaultProcess = null;
        } else {
          externalProcesses.remove(process.getId());
        }
      } else {
        doAs(
            TableProcessMapper.class,
            mapper ->
                mapper.updateProcessAction(
                    tableRuntime.getTableIdentifier().getId(),
                    action,
                    process.getId(),
                    retryCount + 1));
        retryCount++;
      }
      handleCompleted(process);
    } finally {
      lock.unlock();
    }
  }

  public long getLastTriggerTime() {
    return lastTriggerTime;
  }

  public long getLastCompletedTime() {
    return lastCompletedTime;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public AmoroProcess<T> getDefaultProcess() {
    return defaultProcess;
  }

  public void closeDefaultProcess() {
    AmoroProcess<T> defaultProcess = this.defaultProcess;
    if (defaultProcess != null) {
      /** this operation should trigger defaultProcess = null in whenCompleted callback */
      defaultProcess.close();
    }
  }

  public void close() {
    lock.lock();
    try {
      closeDefaultProcess();
      externalProcesses.values().forEach(AmoroProcess::close);
    } finally {
      lock.unlock();
    }
  }

  public void close(long processId) {
    lock.lock();
    try {
      if (defaultProcess != null && defaultProcess.getId() == processId) {
        closeDefaultProcess();
      } else {
        Optional.ofNullable(externalProcesses.remove(processId)).ifPresent(AmoroProcess::close);
      }
    } finally {
      lock.unlock();
    }
  }

  public List<T> getStates() {
    AmoroProcess<T> process = this.defaultProcess;
    if (process != null) {
      return Lists.newArrayList(process.getState());
    } else {
      return getExternalStates();
    }
  }

  private List<T> getExternalStates() {
    lock.lock();
    try {
      return externalProcesses.values().stream()
          .map(AmoroProcess::getState)
          .collect(Collectors.toList());
    } finally {
      lock.unlock();
    }
  }
}
