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

package com.netease.arctic.optimizer.metric;

import com.netease.arctic.optimizer.util.CircularArray;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Util to record recent task stats, thread-safe.
 */
public class TaskRecorder implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(TaskRecorder.class);
  private static final int DEFAULT_HISTORY_SIZE = 256;
  private TaskStat currentTaskStat;
  private final CircularArray<TaskStat> latestTaskStats;

  public TaskRecorder() {
    this(DEFAULT_HISTORY_SIZE);
  }

  public TaskRecorder(int size) {
    this.latestTaskStats = new CircularArray<>(size);
  }

  /**
   * start record a task with current time
   *
   * @param inputFiles - inputFiles of this task
   */
  public void recordNewTaskStat(Iterable<TaskStat.FileStat> inputFiles) {
    recordNewTaskStat(inputFiles, System.currentTimeMillis());
  }

  synchronized void recordNewTaskStat(Iterable<TaskStat.FileStat> inputFiles, long startTime) {
    TaskStat taskStat = new TaskStat(startTime);
    taskStat.recordInputFiles(inputFiles);
    if (this.currentTaskStat != null) {
      LOG.warn("overwrite current task {} with {}", this.currentTaskStat, taskStat);
    }
    this.currentTaskStat = taskStat;
  }

  /**
   * finish recording a task with current time
   *
   * @param outputFiles - outputFiles of this task
   */
  public void finishCurrentTask(Iterable<TaskStat.FileStat> outputFiles) {
    finishCurrentTask(outputFiles, System.currentTimeMillis());
  }

  synchronized void finishCurrentTask(Iterable<TaskStat.FileStat> outputFiles, long endTime) {
    if (this.currentTaskStat == null) {
      LOG.warn("current task is null, ignore finish");
      return;
    }
    this.currentTaskStat.recordOutFiles(outputFiles);
    this.currentTaskStat.finish(endTime);
    this.latestTaskStats.add(this.currentTaskStat);
    this.currentTaskStat = null;
  }

  /**
   * get usage in (beginTime, endTime)
   *
   * @param begin - beginTime
   * @param end   - endTime
   * @return the usage in (beginTime, endTime), result in [0.0, 1.0]
   */
  public double getUsage(long begin, long end) {
    return calculateUsage(begin, end, getAllTasks());
  }

  /**
   * get last n task
   *
   * @param n - limit, must > 0
   * @return n task if tasks.size >= n, else return all tasks
   */
  public synchronized List<TaskStat> getLastNTaskStat(int n) {
    Preconditions.checkArgument(n > 0, "must n > 0");
    int cnt = 0;
    List<TaskStat> taskStats = new ArrayList<>();
    for (TaskStat taskStat : this.latestTaskStats) {
      taskStats.add(taskStat);
      cnt++;
      if (cnt == n) {
        break;
      }
    }
    return taskStats;
  }

  private double calculateUsage(long begin, long end, List<TaskStat> taskStats) {
    if (end < begin) {
      LOG.warn("end {} is not after begin {}, return usage 0.0", end, begin);
      return 0.0;
    }
    if (end == begin) {
      return 0.0;
    }
    if (taskStats.isEmpty()) {
      return 0.0;
    }
    long duration = 0;
    for (TaskStat task : taskStats) {
      long taskDuration;
      if (task.getEndTime() <= task.getStartTime()) {
        LOG.warn("ignore wrong task {}", task);
        continue;
      }
      if (task.getEndTime() <= begin) {
        // case1: task end before begin time, ignore
        taskDuration = 0;
      } else if (task.getStartTime() >= end) {
        // case2: task start after end time, ignore
        taskDuration = 0;
      } else {
        // case3: task(startTime, endTime) overlap with (begin, end)
        long taskStartTime = Math.max(task.getStartTime(), begin);
        long taskEndTime = Math.min(task.getEndTime(), end);
        taskDuration = taskEndTime - taskStartTime;
      }
      duration += taskDuration;
    }
    long totalDuration = end - begin;
    double usage;
    if (duration > totalDuration) {
      LOG.warn("duration {} is bigger than total duration {}", duration, totalDuration);
      usage = 1.0;
    } else if (duration == totalDuration) {
      usage = 1.0;
    } else {
      usage = (double) duration / totalDuration;
    }
    LOG.info("get usage = {}%, execute duration = {}, totalDuration = {}",
        new BigDecimal(usage * 100).setScale(4, RoundingMode.HALF_UP).doubleValue(), duration,
        totalDuration);
    return usage;
  }

  private synchronized List<TaskStat> getAllTasks() {
    List<TaskStat> taskStats = new ArrayList<>();
    for (TaskStat latestTaskStat : latestTaskStats) {
      taskStats.add(latestTaskStat);
    }
    if (this.currentTaskStat != null) {
      taskStats.add(this.currentTaskStat);
    }
    return taskStats;
  }

  /**
   * clear
   */
  public synchronized void clear() {
    this.latestTaskStats.clear();
    this.currentTaskStat = null;
  }

}
