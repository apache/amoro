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

package com.netease.arctic.optimizer.flink;

import com.netease.arctic.ams.api.OptimizeManager;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.ams.api.OptimizerMetric;
import com.netease.arctic.ams.api.client.OptimizeManagerClientPools;
import com.netease.arctic.ams.api.properties.OptimizerProperties;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.TaskWrapper;
import com.netease.arctic.optimizer.metric.TaskRecorder;
import com.netease.arctic.optimizer.metric.TaskStat;
import com.netease.arctic.optimizer.operator.BaseTaskExecutor;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FlinkExecuteFunction extends ProcessFunction<TaskWrapper, OptimizeTaskStat>
    implements BaseTaskExecutor.ExecuteListener {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkExecuteFunction.class);
  private static final String INFLUXDB_TAG_NAME = "arctic_task_id";

  private final BaseTaskExecutor executor;
  private final OptimizerConfig config;
  // to record latest(default=256) task stats
  private final TaskRecorder taskRecorder;

  private Meter inputFlowRateMeter;
  private Meter outputFlowRateMeter;
  private Meter inputFileCntMeter;
  private Meter outputFileCntMeter;

  private volatile long lastUsageCheckTime;

  private final Map<String, String> metrics = new HashMap<>();

  private boolean stopped = false;

  private Thread thread;

  FlinkExecuteFunction(OptimizerConfig config) {
    this.config = config;
    this.executor = new BaseTaskExecutor(config, this);
    this.taskRecorder = new TaskRecorder();
  }

  public FlinkExecuteFunction(BaseTaskExecutor executor,
                              OptimizerConfig config) {
    this.executor = executor;
    this.config = config;
    this.taskRecorder = new TaskRecorder();
  }

  @Override
  public void processElement(TaskWrapper compactTaskTaskWrapper,
                             ProcessFunction<TaskWrapper, OptimizeTaskStat>.Context context,
                             Collector<OptimizeTaskStat> collector) throws Exception {
    OptimizeTaskStat result = executor.execute(compactTaskTaskWrapper);
    collector.collect(result);
  }

  @Override
  public void onTaskStart(Iterable<ContentFile<?>> inputFiles) {
    this.taskRecorder.recordNewTaskStat(getFileStats(inputFiles));

    int size = Iterables.size(inputFiles);
    long sum = 0;
    for (ContentFile<?> inputFile : inputFiles) {
      sum += inputFile.fileSizeInBytes();
    }
    // file cnt rate /min
    this.inputFileCntMeter.markEvent(size * 60L);
    this.inputFlowRateMeter.markEvent(sum);
    LOG.info("record metrics inputFlowRate={}, InputFileCnt={}", sum, size);
  }

  @Override
  public void onTaskFinish(Iterable<ContentFile<?>> outputFiles) {
    this.taskRecorder.finishCurrentTask(getFileStats(outputFiles));

    int size = 0;
    long sum = 0;
    if (outputFiles != null) {
      size = Iterables.size(outputFiles);
      for (ContentFile<?> outputFile : outputFiles) {
        sum += outputFile.fileSizeInBytes();
      }
    }
    // file cnt rate /min
    this.outputFileCntMeter.markEvent(size * 60L);
    this.outputFlowRateMeter.markEvent(sum);
    LOG.info("record metrics outputFlowRate={}, outputFileCnt={}", sum, size);
  }

  @NotNull
  private List<TaskStat.FileStat> getFileStats(Iterable<ContentFile<?>> files) {
    List<TaskStat.FileStat> fileStats;
    if (files == null) {
      fileStats = Collections.emptyList();
    } else {
      fileStats = Streams.stream(files)
          .map(f -> new TaskStat.FileStat(f.fileSizeInBytes()))
          .collect(Collectors.toList());
    }
    return fileStats;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    ExecutionConfig.GlobalJobParameters globalJobParameters =
        getRuntimeContext().getExecutionConfig().getGlobalJobParameters();

    reportMetrics(String.valueOf(getRuntimeContext().getIndexOfThisSubtask()));

    String taskId = Objects.nonNull(globalJobParameters.toMap().get(INFLUXDB_TAG_NAME)) ?
        globalJobParameters.toMap().get(INFLUXDB_TAG_NAME) : config.getOptimizerId();
    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("last-5-input-file-size", () -> getLastNFileSize(5, true));
    LOG.info("add Gauge metrics last-5-input-file-size");

    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("last-10-input-file-size", () -> getLastNFileSize(10, true));
    LOG.info("add Gauge metrics last-10-input-file-size");

    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("last-5-output-file-size", () -> getLastNFileSize(5, false));
    LOG.info("add Gauge metrics last-5-output-file-size");

    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("last-10-output-file-size", () -> getLastNFileSize(10, false));
    LOG.info("add Gauge metrics last-10-output-file-size");

    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("last-5-average-time", () -> getLastNAverageTime(5));
    LOG.info("add Gauge metrics last-5-average-time");

    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("last-10-average-time", () -> getLastNAverageTime(10));
    LOG.info("add Gauge metrics last-10-average-time");

    getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .gauge("usage-percentage", this::getUsagePercentage);
    LOG.info("add Gauge metrics usage-percentage");

    final int flowRateTimeSpanInSeconds = 5;
    this.inputFlowRateMeter = getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .meter("input-flow-rate", new MeterView(flowRateTimeSpanInSeconds));
    LOG.info("add Meter metrics input-flow-rate with timeSpanInSeconds = {}s", flowRateTimeSpanInSeconds);

    this.outputFlowRateMeter = getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .meter("output-flow-rate", new MeterView(flowRateTimeSpanInSeconds));
    LOG.info("add Meter metrics output-flow-rate with timeSpanInSeconds = {}s", flowRateTimeSpanInSeconds);

    final int fileCntRateTimeSpanInSeconds = 5;
    this.inputFileCntMeter = getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .meter("input-file-cnt-rate", new MeterView(fileCntRateTimeSpanInSeconds));
    LOG.info("add Meter metrics input-file-cnt-rate with timeSpanInSeconds = {}s", fileCntRateTimeSpanInSeconds);

    this.outputFileCntMeter = getRuntimeContext()
        .getMetricGroup()
        .addGroup(INFLUXDB_TAG_NAME, taskId)
        .meter("output-file-cnt-rate", new MeterView(fileCntRateTimeSpanInSeconds));
    LOG.info("add Meter metrics output-file-cnt-rate with timeSpanInSeconds = {}s", fileCntRateTimeSpanInSeconds);
  }

  public void close() throws Exception {
    super.close();
    this.stopped = true;
    if (thread != null) {
      thread.interrupt();
    }
  }

  private long getLastNFileSize(int n, boolean input) {
    if (n <= 0) {
      return 0;
    }
    int fileCnt = 0;
    long totalFileSize = 0;
    for (TaskStat taskStat : taskRecorder.getLastNTaskStat(n)) {
      if (input) {
        fileCnt += taskStat.getInputFileCnt();
        totalFileSize += taskStat.getInputTotalSize();
      } else {
        fileCnt += taskStat.getOutputFileCnt();
        totalFileSize += taskStat.getOutputTotalSize();
      }
    }
    return fileCnt == 0 ? 0 : totalFileSize / fileCnt;
  }

  private long getLastNAverageTime(int n) {
    if (n <= 0) {
      return 0;
    }
    int taskCnt = 0;
    long totalTime = 0;
    for (TaskStat taskStat : taskRecorder.getLastNTaskStat(n)) {
      totalTime += taskStat.getDuration();
      taskCnt++;
    }
    return taskCnt == 0 ? 0 : totalTime / taskCnt;
  }

  private double getUsagePercentage() {
    long now = System.currentTimeMillis();
    double usage = 0.0;
    if (lastUsageCheckTime == 0) {
      LOG.info("init lastUsageCheckTime, get usage=0.0");
    } else {
      usage = getUsage(this.lastUsageCheckTime, now);
    }
    this.lastUsageCheckTime = now;
    metrics.put(OptimizerProperties.QUOTA_USAGE, String.valueOf(usage * 100));
    return usage * 100;
  }

  public double getUsage(long begin, long end) {
    return taskRecorder.getUsage(begin, end);
  }

  private void reportMetrics(String subtaskId) {
    this.thread = new Thread(() -> {
      while (!stopped) {
        try {
          Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
          break;
        }
        List<OptimizerMetric> metricList = new ArrayList<>();
        metrics.forEach((key, value) -> {
          OptimizerMetric metric = new OptimizerMetric();
          metric.setOptimizerId(Long.parseLong(config.getOptimizerId()));
          metric.setSubtaskId(subtaskId);
          metric.setMetricName(key);
          metric.setMetricValue(value);
          metricList.add(metric);
        });
        try {
          OptimizeManager.Iface compactManager = OptimizeManagerClientPools.getClient(config.getAmsUrl());
          compactManager.reportOptimizerMetric(metricList);
        } catch (Throwable t) {
          LOG.error("failed to sending result, optimizer: {} subtaskId: {}", config.getOptimizerId(), subtaskId, t);
        }
      }
    });
    thread.start();
  }

}