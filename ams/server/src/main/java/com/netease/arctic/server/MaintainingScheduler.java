package com.netease.arctic.server;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.process.AmoroProcess;
import com.netease.arctic.server.process.ArbitraryProcess;
import com.netease.arctic.server.process.ProcessStatus;
import com.netease.arctic.server.process.TableProcess;
import com.netease.arctic.server.process.TableState;
import com.netease.arctic.server.process.TaskRuntime;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaintainingScheduler extends AbstractScheduler<TableState> {

  private final Set<Action> targetActions;
  private final Queue<TableMaintenance> tableQueue = new PriorityQueue<>();
  private final Map<ServerTableIdentifier, Action> tableActionMap = new HashMap<>();
  private final AtomicLong maxProcessId = new AtomicLong(0);

  public MaintainingScheduler(ResourceGroup optimizerGroup) {
    super(optimizerGroup);
    targetActions = optimizerGroup.getActions();
  }

  @Override
  protected TableProcess<TableState> createProcess(TableRuntime tableRuntime, Action action) {
    if (tableActionMap.get(tableRuntime.getTableIdentifier()) == action) {
      tableActionMap.remove(tableRuntime.getTableIdentifier());
    }
    return new ArbitraryProcess(maxProcessId.incrementAndGet(), action, tableRuntime, buildTaskRuntime());
  }

  @Override
  protected TableProcess<TableState> recoverProcess(TableRuntime tableRuntime, Action action, TableState state) {
    maxProcessId.set(Math.max(maxProcessId.get(), state.getId()));
    if (state.getStatus() == ProcessStatus.RUNNING) {
      if (tableActionMap.get(tableRuntime.getTableIdentifier()) != action) {
        tableActionMap.put(tableRuntime.getTableIdentifier(), action);
      }
      return new ArbitraryProcess(state, tableRuntime);
    } else {
      return null;
    }
  }

  @Override
  public Pair<TableRuntime, Action> scheduleTable() {
    return Optional.ofNullable(tableQueue.poll())
        .map(maintenance -> Pair.of(
            maintenance.getTableRuntime(), maintenance.getAction()))
        .orElse(null);
  }

  @Override
  public void refreshTable(TableRuntime tableRuntime) {
    schedulerLock.lock();
    try {
      releaseTable(tableRuntime);
      TableMaintenance maintenance = tryCreateMaintenance(tableRuntime);
      if (maintenance != null) {
        tableQueue.add(maintenance);
        tableActionMap.put(tableRuntime.getTableIdentifier(), maintenance.getAction());
      }
    } finally {
      schedulerLock.unlock();
    }
  }

  @Override
  public void releaseTable(TableRuntime tableRuntime) {
    schedulerLock.lock();
    try {
      if (tableActionMap.remove(tableRuntime.getTableIdentifier()) != null) {
        tableQueue.removeIf(maintenance -> maintenance.getTableRuntime() == tableRuntime);
      }
    } finally {
      schedulerLock.unlock();
    }
  }

  @Override
  public boolean containsTable(ServerTableIdentifier identifier) {
    schedulerLock.lock();
    try {
      return tableActionMap.containsKey(identifier);
    } finally {
      schedulerLock.unlock();
    }
  }

  @Override
  public List<TableRuntime> listTables() {
    return Stream.concat(
        tableQueue.stream().map(TableMaintenance::getTableRuntime),
            tableProcessQueue.stream().map(TableProcess::getTableRuntime))
        .collect(Collectors.toList());
  }

  private TaskRuntime<?, ?> buildTaskRuntime() {
    return null;
  }

  private TableMaintenance tryCreateMaintenance(TableRuntime tableRuntime) {
    Map<Action, Long> minTriggerIntervals = tableRuntime.getTableConfiguration()
        .getActionMinIntervals(targetActions);
    Map<Action, Long> nextTriggerTimes = tableRuntime.getLastCompletedTimes(minTriggerIntervals.keySet()).entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() + minTriggerIntervals.get(entry.getKey())));
    return nextTriggerTimes.entrySet()
        .stream()
        .min(Map.Entry.comparingByValue())
        .map(entry -> new TableMaintenance(tableRuntime, entry.getKey(), entry.getValue()))
        .orElse(null);
  }


  private static class TableMaintenance implements Delayed {
    private final TableRuntime tableRuntime;
    private final Action action;
    private final long delayActionTime;

    public TableMaintenance(TableRuntime tableRuntime, Action action, long delayActionTime) {
      this.tableRuntime = tableRuntime;
      this.action = action;
      this.delayActionTime = delayActionTime;
    }

    public Action getAction() {
      return action;
    }

    public AmoroProcess<? extends TableState> run() {
      return tableRuntime.runAction(action);
    }

    public TableRuntime getTableRuntime() {
      return tableRuntime;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      return unit.convert(delayActionTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
      TableMaintenance other = (TableMaintenance) o;
      return Long.compare(delayActionTime, other.delayActionTime);
    }
  }
}
