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

public class TaskStat {
  private final long startTime;
  private long endTime = Long.MAX_VALUE;
  private int inputFileCnt;
  private long inputTotalSize;
  private int outputFileCnt;
  private long outputTotalSize;

  public TaskStat(long startTime) {
    this.startTime = startTime;
  }

  public boolean finished() {
    return endTime != Long.MAX_VALUE;
  }

  public void finish(long endTime) {
    this.endTime = endTime;
  }

  public int getInputFileCnt() {
    return inputFileCnt;
  }

  public long getInputTotalSize() {
    return inputTotalSize;
  }

  public int getOutputFileCnt() {
    return outputFileCnt;
  }

  public long getOutputTotalSize() {
    return outputTotalSize;
  }

  public long getDuration() {
    if (finished()) {
      return this.endTime - this.startTime;
    } else {
      return System.currentTimeMillis() - this.startTime;
    }
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void recordInputFiles(Iterable<FileStat> files) {
    if (files == null) {
      return;
    }
    for (FileStat file : files) {
      this.inputFileCnt++;
      this.inputTotalSize += file.getFileSize();
    }
  }

  public void recordOutFiles(Iterable<FileStat> files) {
    if (files == null) {
      return;
    }
    for (FileStat file : files) {
      this.outputFileCnt++;
      this.outputTotalSize += file.getFileSize();
    }
  }
  
  public static class FileStat {
    private final long fileSize;

    public FileStat(long fileSize) {
      this.fileSize = fileSize;
    }

    public long getFileSize() {
      return fileSize;
    }

    @Override
    public String toString() {
      return "FileStat(size=" + fileSize + ")";
    }
  }

  @Override
  public String toString() {
    return "TaskStat{" +
        "startTime=" + startTime +
        ", endTime=" + endTime +
        ", inputFileCnt=" + inputFileCnt +
        ", inputTotalSize=" + inputTotalSize +
        ", outputFileCnt=" + outputFileCnt +
        ", outputTotalSize=" + outputTotalSize +
        '}';
  }
}
