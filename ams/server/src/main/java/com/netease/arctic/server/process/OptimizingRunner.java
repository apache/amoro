package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.process.AmoroProcess;
import com.netease.arctic.ams.api.process.OptimizingState;
import com.netease.arctic.ams.api.process.ProcessFactory;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.persistence.OptimizingStatePersistency;
import com.netease.arctic.server.persistence.mapper.TableProcessMapper;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.List;

public class OptimizingRunner extends SingletonActionRunner<OptimizingState> {

  private final MinorOptimizingRunner minorOptimizingRunner;
  private QuotaProvider quotaProvider;
  private DefaultOptimizingState minorOptimizingState;
  private DefaultOptimizingState majorOptimizingState;

  @SuppressWarnings("unchecked")
  public OptimizingRunner(
      TableRuntime tableRuntime,
      ProcessFactory<? extends OptimizingState> defaultProcessFactory,
      boolean recover) {
    super(
        tableRuntime,
        (ProcessFactory<OptimizingState>) defaultProcessFactory,
        Action.MAJOR_OPTIMIZING,
        recover);
    this.minorOptimizingRunner =
        new MinorOptimizingRunner(
            tableRuntime, (ProcessFactory<OptimizingState>) defaultProcessFactory, recover);
    if (!recover) {
      quotaProvider = new QuotaProvider(tableRuntime.getTableIdentifier().getId());
      minorOptimizingState =
          new DefaultOptimizingState(tableRuntime.getTableIdentifier(), OptimizingType.MINOR);
      majorOptimizingState =
          new DefaultOptimizingState(tableRuntime.getTableIdentifier(), OptimizingType.MAJOR);
    }
  }

  public QuotaProvider getQuotaProvider() {
    return quotaProvider;
  }

  public DefaultOptimizingState getMinorOptimizingState() {
    return minorOptimizingState;
  }

  public DefaultOptimizingState getMajorOptimizingState() {
    return majorOptimizingState;
  }

  public AmoroProcess<OptimizingState> runMinorOptimizing() {
    Preconditions.checkState(externalProcesses.isEmpty());
    return minorOptimizingRunner.run();
  }

  public AmoroProcess<OptimizingState> runMajorOptimizing() {
    return run();
  }

  @Override
  public void closeDefaultProcess() {
    super.closeDefaultProcess();
    minorOptimizingRunner.closeDefaultProcess();
  }

  @Override
  protected void recoverProcesses() {
    lock.lock();
    try {
      Preconditions.checkState(defaultProcess == null);
      quotaProvider =
          getAs(
              TableProcessMapper.class,
              mapper -> mapper.selectQuotaContainer(tableRuntime.getTableIdentifier().getId()));
      if (quotaProvider == null) {
        quotaProvider = new QuotaProvider(tableRuntime.getTableIdentifier().getId());
      }
      listStatePersistencies()
          .forEach(
              optimizingStatePersistency -> {
                if (optimizingStatePersistency.getOptimizingType() == OptimizingType.MINOR) {
                  minorOptimizingState =
                      new DefaultOptimizingState(
                          tableRuntime.getTableIdentifier(),
                          OptimizingType.MINOR,
                          optimizingStatePersistency);
                  minorOptimizingRunner.defaultProcess =
                      defaultProcessFactory.recover(
                          tableRuntime, Action.MINOR_OPTIMIZING, minorOptimizingState);
                  if (minorOptimizingRunner.defaultProcess != null) {
                    minorOptimizingRunner.submitProcess(minorOptimizingRunner.defaultProcess);
                  }
                } else {
                  majorOptimizingState =
                      new DefaultOptimizingState(
                          tableRuntime.getTableIdentifier(),
                          OptimizingType.MAJOR,
                          optimizingStatePersistency);
                }
                defaultProcess =
                    defaultProcessFactory.recover(
                        tableRuntime, Action.MAJOR_OPTIMIZING, majorOptimizingState);
                if (defaultProcess != null) {
                  submitProcess(defaultProcess);
                }
              });
      if (minorOptimizingState == null) {
        minorOptimizingState =
            new DefaultOptimizingState(tableRuntime.getTableIdentifier(), OptimizingType.MINOR);
      }
      if (majorOptimizingState == null) {
        majorOptimizingState =
            new DefaultOptimizingState(tableRuntime.getTableIdentifier(), OptimizingType.MAJOR);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  protected void handleCompleted(AmoroProcess<OptimizingState> process) {
    if (defaultProcess == process && process instanceof QuotaConsumer) {
      quotaProvider.removeConsumer(
          (QuotaConsumer) process, process.getStatus() == ProcessStatus.SUCCESS);
    }
  }

  @Override
  protected void handleSubmitted(AmoroProcess<OptimizingState> process) {
    if (defaultProcess == process && process instanceof QuotaConsumer) {
      quotaProvider.addConsumer((QuotaConsumer) process);
    }
  }

  private List<OptimizingStatePersistency> listStatePersistencies() {
    return getAs(
        TableProcessMapper.class,
        mapper -> mapper.selectOptimizingPersistencies(tableRuntime.getTableIdentifier().getId()));
  }

  private class MinorOptimizingRunner extends SingletonActionRunner<OptimizingState> {
    public MinorOptimizingRunner(
        TableRuntime tableRuntime,
        ProcessFactory<OptimizingState> defaultProcessFactory,
        boolean recover) {
      super(tableRuntime, defaultProcessFactory, Action.MINOR_OPTIMIZING, recover);
    }

    @Override
    protected void recoverProcesses() {}

    @Override
    protected void handleSubmitted(AmoroProcess<OptimizingState> process) {
      quotaProvider.addConsumer((QuotaConsumer) process);
    }

    @Override
    protected void handleCompleted(AmoroProcess<OptimizingState> process) {
      quotaProvider.removeConsumer((QuotaConsumer) process, false);
    }
  }
}
