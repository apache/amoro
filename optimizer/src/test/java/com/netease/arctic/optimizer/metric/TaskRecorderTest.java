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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskRecorderTest {
  private static final double PRECISION = 0.00001;

  @Test
  public void testEmptyRecorder() {
    TaskRecorder taskRecorder = new TaskRecorder();
    List<TaskStat> lastNTaskStat = taskRecorder.getLastNTaskStat(1);
    Assert.assertEquals(0, lastNTaskStat.size());
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.0, usage, PRECISION);
  }

  @Test
  public void test1Current() {
    TaskRecorder taskRecorder = new TaskRecorder();
    taskRecorder.recordNewTaskStat(getFileStats0(), 10);
    List<TaskStat> lastNTaskStat = taskRecorder.getLastNTaskStat(10);
    Assert.assertEquals(0, lastNTaskStat.size());
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.9, usage, PRECISION);
  }

  @Test
  public void testOverwriteCurrent() {
    TaskRecorder taskRecorder = new TaskRecorder();
    taskRecorder.recordNewTaskStat(getFileStats0(), 10);
    taskRecorder.recordNewTaskStat(getFileStats1(), 20);
    List<TaskStat> lastNTaskStat = taskRecorder.getLastNTaskStat(10);
    Assert.assertEquals(0, lastNTaskStat.size());
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.8, usage, PRECISION);
  }

  @Test
  public void test1History() {
    TaskRecorder taskRecorder = new TaskRecorder();
    taskRecorder.recordNewTaskStat(getFileStats0(), 10);
    taskRecorder.finishCurrentTask(getFileStats1(), 30);
    List<TaskStat> lastNTaskStat = taskRecorder.getLastNTaskStat(10);
    Assert.assertEquals(1, lastNTaskStat.size());
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.2, usage, PRECISION);
  }

  @Test
  public void test1Current2History() {
    TaskRecorder taskRecorder = new TaskRecorder();
    taskRecorder.recordNewTaskStat(getFileStats0(), 10);
    taskRecorder.finishCurrentTask(getFileStats1(), 20);
    taskRecorder.recordNewTaskStat(getFileStats0(), 30);
    taskRecorder.finishCurrentTask(getFileStats1(), 40);
    taskRecorder.recordNewTaskStat(getFileStats0(), 80);
    List<TaskStat> lastNTaskStat = taskRecorder.getLastNTaskStat(1);
    Assert.assertEquals(1, lastNTaskStat.size());
    List<TaskStat> lastNTaskStat2 = taskRecorder.getLastNTaskStat(10);
    Assert.assertEquals(2, lastNTaskStat2.size());
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.4, usage, PRECISION);
  }

  @Test
  public void testHistoryExpire() {
    TaskRecorder taskRecorder = new TaskRecorder(2);
    taskRecorder.recordNewTaskStat(getFileStats0(), 10);
    taskRecorder.finishCurrentTask(getFileStats0(), 20);
    taskRecorder.recordNewTaskStat(getFileStats1(), 30);
    taskRecorder.finishCurrentTask(getFileStats1(), 40);
    taskRecorder.recordNewTaskStat(getFileStats1(), 80);
    taskRecorder.finishCurrentTask(getFileStats3(), 100);
    List<TaskStat> lastNTaskStat = taskRecorder.getLastNTaskStat(1);
    Assert.assertEquals(1, lastNTaskStat.size());
    for (TaskStat taskStat : lastNTaskStat) {
      Assert.assertEquals(20L, taskStat.getDuration());
      Assert.assertEquals(1, taskStat.getInputFileCnt());
      Assert.assertEquals(10, taskStat.getInputTotalSize());
      Assert.assertEquals(3, taskStat.getOutputFileCnt());
      Assert.assertEquals(60, taskStat.getOutputTotalSize());
    }
    List<TaskStat> lastNTaskStat2 = taskRecorder.getLastNTaskStat(10);
    Assert.assertEquals(2, lastNTaskStat2.size());
    int cnt = 0;
    for (TaskStat taskStat : lastNTaskStat2) {
      cnt++;
      if (cnt == 1) {
        Assert.assertEquals(20L, taskStat.getDuration());
        Assert.assertEquals(1, taskStat.getInputFileCnt());
        Assert.assertEquals(10, taskStat.getInputTotalSize());
        Assert.assertEquals(3, taskStat.getOutputFileCnt());
        Assert.assertEquals(60, taskStat.getOutputTotalSize());
      } else {
        Assert.assertEquals(10L, taskStat.getDuration());
        Assert.assertEquals(1, taskStat.getInputFileCnt());
        Assert.assertEquals(10, taskStat.getInputTotalSize());
        Assert.assertEquals(1, taskStat.getOutputFileCnt());
        Assert.assertEquals(10, taskStat.getOutputTotalSize());
      }
    }
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.3, usage, PRECISION);
  }
  
  @Test
  public void testUsage() {
    TaskRecorder taskRecorder = new TaskRecorder(3);
    // duration 5
    taskRecorder.recordNewTaskStat(getFileStats0(), 10);
    taskRecorder.finishCurrentTask(getFileStats0(), 15);

    // duration 10
    taskRecorder.recordNewTaskStat(getFileStats1(), 20);
    taskRecorder.finishCurrentTask(getFileStats1(), 30);

    // duration 15
    taskRecorder.recordNewTaskStat(getFileStats1(), 40);
    taskRecorder.finishCurrentTask(getFileStats3(), 55);

    // current
    taskRecorder.recordNewTaskStat(getFileStats1(), 70);

    // all
    double usage = taskRecorder.getUsage(0, 100);
    Assert.assertEquals(0.6, usage, PRECISION);

    // 0
    double usage0 = taskRecorder.getUsage(0, 0);
    Assert.assertEquals(0.0, usage0, PRECISION);
    
    //
    double usage1 = taskRecorder.getUsage(20, 60);
    Assert.assertEquals(25d/40d, usage1, PRECISION);

    //
    double usage2 = taskRecorder.getUsage(25, 85);
    Assert.assertEquals(35d/60d, usage2, PRECISION);
    
    
  }

  private List<TaskStat.FileStat> getFileStats3() {
    List<TaskStat.FileStat> fileStats = new ArrayList<>();
    fileStats.add(new TaskStat.FileStat(10));
    fileStats.add(new TaskStat.FileStat(20));
    fileStats.add(new TaskStat.FileStat(30));
    return fileStats;
  }

  private List<TaskStat.FileStat> getFileStats1() {
    List<TaskStat.FileStat> fileStats = new ArrayList<>();
    fileStats.add(new TaskStat.FileStat(10));
    return fileStats;
  }

  private List<TaskStat.FileStat> getFileStats0() {
    return Collections.emptyList();
  }

  private List<TaskStat.FileStat> getFileStats(int n, long size) {
    List<TaskStat.FileStat> fileStats = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      fileStats.add(new TaskStat.FileStat(size));
    }
    return fileStats;
  }
}