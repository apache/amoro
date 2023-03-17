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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.operator.BaseTaskReporter;
import com.netease.arctic.optimizer.operator.BaseToucher;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class FlinkReporter extends AbstractStreamOperator<Void>
        implements OneInputStreamOperator<OptimizeTaskStat, Void> {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkReporter.class);
  public static final String STATE_JOB_ID = "flink-job-id";

  private static final String JOB_OVERVIEW_REST_API = "/jobs/overview";

  private static final String HIGH_AVAILABILITY_CLUSTER_ID = "high-availability.cluster-id";

  private static final String STATUS_IDENTIFICATION = "status_identification";

  private static final String LAST_MODIFICATION = "last-modification";

  private final BaseTaskReporter taskReporter;
  private final BaseToucher toucher;
  private final long heartBeatInterval;

  private String restApiPrefix;

  private volatile boolean stopped = false;
  private Thread thread;

  public FlinkReporter(BaseTaskReporter taskReporter, BaseToucher toucher, OptimizerConfig optimizerConfig) {
    this.taskReporter = taskReporter;
    this.toucher = toucher;
    this.heartBeatInterval = optimizerConfig.getHeartBeat();
  }

  public FlinkReporter(OptimizerConfig config) {
    this.taskReporter = new BaseTaskReporter(config);
    this.toucher = new BaseToucher(config);
    this.heartBeatInterval = config.getHeartBeat();
    this.restApiPrefix = config.getRestApiPrefix();
  }

  @Override
  public void open() throws Exception {
    super.open();
    this.thread = new Thread(() -> {
      while (!stopped) {
        try {
          Thread.sleep(heartBeatInterval);
          Map<String, String> state = Maps.newHashMap();
          try {
            String jobId = getContainingTask().getEnvironment().getJobID().toString();
            state.put(STATE_JOB_ID, jobId);
            addLastModification(state);
          } catch (Exception e) {
            LOG.error("failed to get joId, ignore", e);
          }
          toucher.touch(state);
        } catch (InterruptedException t) {
          break;
        } catch (Throwable t) {
          LOG.error("toucher get unexpected exception", t);
          continue;
        }
      }
    });
    this.thread.start();
  }

  @Override
  public void close() throws Exception {
    super.close();
    stopped = true;
    if (thread != null) {
      thread.interrupt();
    }
  }

  @Override
  public void processElement(StreamRecord<OptimizeTaskStat> element) throws Exception {
    if (element.getValue() != null) {
      taskReporter.report(element.getValue());
      LOG.info("report success {}", element.getValue() == null ? null : element.getValue().getTaskId());
    } else {
      LOG.warn("get empty task stat");
    }
  }

  private void addLastModification(Map<String, String> state) throws IOException {

    String yarnApp = StreamExecutionEnvironment.getExecutionEnvironment()
            .getStreamGraph().getJobGraph().getJobID().toString();

    if (StringUtils.isNotEmpty(restApiPrefix) && StringUtils.isNotEmpty(yarnApp)) {
      //http://knox.inner.youdao.com/gateway/eadhadoop/yarn/proxy/{application_id}/jobs/overview
      String restApiUrl = restApiPrefix + yarnApp + JOB_OVERVIEW_REST_API;

      URL url = new URL(restApiUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      conn.setRequestMethod("GET");
      conn.connect();
      StringBuilder  sb = new StringBuilder();

      if (conn.getResponseCode() == 200) {
        InputStream is  = conn.getInputStream();
        //面对获取的输入流进行读取
        BufferedReader br =  new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }

        String jsonStr = sb.toString();
        JSONArray jobs = JSON.parseObject(jsonStr).getJSONArray("jobs");
        Long lastModification = jobs.getJSONObject(0).getLong(LAST_MODIFICATION);
        state.put(STATUS_IDENTIFICATION, String.valueOf(lastModification));

      }

    }
  }
}
