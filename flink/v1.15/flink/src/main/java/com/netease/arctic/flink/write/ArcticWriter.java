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

package com.netease.arctic.flink.write;

import com.netease.arctic.flink.metric.MetricsGenerator;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.flink.util.ArcticUtils;
import com.netease.arctic.table.ArcticTable;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.runtime.state.StateInitializationContext;
import org.apache.flink.runtime.state.StateSnapshotContext;
import org.apache.flink.streaming.api.graph.StreamConfig;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.BoundedOneInput;
import org.apache.flink.streaming.api.operators.Input;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.api.operators.Output;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.runtime.streamrecord.LatencyMarker;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.runtime.tasks.StreamTask;
import org.apache.flink.streaming.runtime.watermarkstatus.WatermarkStatus;
import org.apache.flink.table.data.RowData;
import org.apache.flink.util.OutputTag;
import org.apache.iceberg.relocated.com.google.common.io.BaseEncoding;

import java.util.Objects;

/**
 * This is the general entry of an arctic writer that wraps different operators insides.
 *
 * @param <OUT>
 */
public class ArcticWriter<OUT> extends AbstractStreamOperator<OUT>
    implements OneInputStreamOperator<RowData, OUT>, BoundedOneInput {

  private transient Meter meterFlowRate;

  private transient Meter meterSpeed;

  private byte[] arcticJobId;
  /**
   * flink jobId
   */
  private transient String jobId;
  private transient long checkpointId = 0;
  private transient Long transactionId = null;
  /**
   * Load table in runtime, because that table's refresh method will be invoked in serialization.
   * And it will set {@link org.apache.hadoop.security.UserGroupInformation#authenticationMethod} to KERBEROS
   * if Arctic's table is KERBEROS enabled. It will cause ugi relevant exception when deploy to yarn cluster.
   */
  private transient ArcticTable table;

  private final AbstractStreamOperator fileWriter;
  private final ArcticLogWriter logWriter;
  private final MetricsGenerator metricsGenerator;
  private final ArcticTableLoader tableLoader;

  private static final String INFLUXDB_TAG_NAME = "arctic_task_id";

  public ArcticWriter(ArcticLogWriter logWriter,
                      AbstractStreamOperator fileWriter,
                      ArcticTableLoader tableLoader,
                      MetricsGenerator metricsGenerator) {
    this.logWriter = logWriter;
    this.fileWriter = fileWriter;
    this.tableLoader = tableLoader;
    this.metricsGenerator = metricsGenerator;
  }

  private Long getTransactionId() {
    Long transaction;
    String signature = BaseEncoding.base16().encode((jobId + checkpointId).getBytes());
    transaction = table.beginTransaction(signature);
    LOG.info("get new TransactionId. table:{}, signature:{}, transactionId:{}. From jobId:{}, ckpId:{}",
        table.name(), signature, transaction, jobId, checkpointId);
    transactionId = transaction;
    return transaction;
  }

  @Override
  public void setup(StreamTask<?, ?> containingTask, StreamConfig config, Output<StreamRecord<OUT>> output) {
    super.setup(containingTask, config, output);
    if (logWriter != null) {
      logWriter.setup(containingTask, config, EMPTY_OUTPUT);
    }
    if (fileWriter != null) {
      fileWriter.setup(containingTask, config, output);
    }
  }

  @Override
  public void open() throws Exception {
    ExecutionConfig.GlobalJobParameters globalJobParameters =
        getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
    String taskId = Objects.nonNull(globalJobParameters.toMap().get(INFLUXDB_TAG_NAME)) ?
        globalJobParameters.toMap().get(INFLUXDB_TAG_NAME) : "null";
    // latency
    if (metricsGenerator.enable()) {
      getRuntimeContext()
          .getMetricGroup()
          .addGroup(INFLUXDB_TAG_NAME, taskId)
          .gauge("record-latency", metricsGenerator::getCurrentLatency);
      LOG.info("add metrics record-latency");
    }
    if (metricsGenerator.isMetricEnable()) {
      // speed
      meterFlowRate = getRuntimeContext().getMetricGroup()
          .addGroup(INFLUXDB_TAG_NAME, taskId)
          .meter("record-meter", new MeterView(60));
      LOG.info("add metrics record-meter");
      // rate of flow
      meterSpeed = getRuntimeContext().getMetricGroup()
          .addGroup(INFLUXDB_TAG_NAME, taskId)
          .meter("record-count", new MeterView(60));
      LOG.info("add metrics record-count");
    }

    this.jobId = getContainingTask().getEnvironment().getJobID().toString();
    table = ArcticUtils.loadArcticTable(tableLoader);
    
    if (logWriter != null) {
      logWriter.open();
    }
    if (fileWriter != null) {
      fileWriter.open();
    }
  }

  @Override
  public void initializeState(StateInitializationContext context) throws Exception {
    if (logWriter != null) {
      logWriter.initializeState(context);
    }
    if (fileWriter != null) {
      fileWriter.initializeState(context);
    }
  }

  @Override
  public void prepareSnapshotPreBarrier(long checkpointId) throws Exception {
    this.checkpointId = checkpointId;
    transactionId = null;
    
    if (logWriter != null) {
      logWriter.prepareSnapshotPreBarrier(checkpointId);
    }
    if (fileWriter != null) {
      fileWriter.prepareSnapshotPreBarrier(checkpointId);
    }
  }

  @VisibleForTesting
  public long getCheckpointId() {
    return checkpointId;
  }

  @Override
  public void snapshotState(StateSnapshotContext context) throws Exception {
    if (logWriter != null) {
      logWriter.snapshotState(context);
    }
    if (fileWriter != null) {
      fileWriter.snapshotState(context);
    }
  }

  @Override
  public void endInput() throws Exception {
    if (logWriter != null) {
      logWriter.endInput();
    }
    if (fileWriter instanceof BoundedOneInput) {
      ((BoundedOneInput) fileWriter).endInput();
    }
  }

  @Override
  public void processElement(StreamRecord<RowData> element) throws Exception {
    if (transactionId == null) {
      transactionId = getTransactionId();
      // setTransactionId should be invoked before log/file writer#processElement
      if (logWriter != null) {
        logWriter.setTransactionId(transactionId);
      }
      if (fileWriter != null && fileWriter instanceof TransactionIdAware) {
        ((TransactionIdAware) fileWriter).setTransactionId(transactionId);
      }
    }
    if (metricsGenerator.isMetricEnable()) {
      meterSpeed.markEvent();
    }
    if (logWriter != null) {
      logWriter.processElement(element);
    }
    if (fileWriter instanceof Input) {
      ((Input) fileWriter).processElement(element);
    }
    metricsGenerator.recordLatency(element);
  }

  @Override
  public void processWatermark(Watermark mark) throws Exception {
    if (logWriter != null) {
      logWriter.processWatermark(mark);
    }
    if (fileWriter instanceof Input) {
      ((Input) fileWriter).processWatermark(mark);
    }
    super.processWatermark(mark);
  }

  @Override
  public void close() throws Exception {
    super.close();
    if (logWriter != null) {
      logWriter.close();
    }
    if (fileWriter != null) {
      fileWriter.close();
    }
  }

  private static final Output<StreamRecord<RowData>> EMPTY_OUTPUT = new Output<StreamRecord<RowData>>() {
    @Override
    public void emitWatermark(Watermark watermark) {
    }

    @Override
    public void emitWatermarkStatus(WatermarkStatus watermarkStatus) {
    }

    @Override
    public <X> void collect(OutputTag<X> outputTag, StreamRecord<X> streamRecord) {
    }

    @Override
    public void collect(StreamRecord<RowData> rowDataStreamRecord) {
    }

    @Override
    public void emitLatencyMarker(LatencyMarker latencyMarker) {
    }

    @Override
    public void close() {
    }
  };
}
