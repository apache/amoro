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

import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.TaskWrapper;
import com.netease.arctic.optimizer.operator.BaseTaskConsumer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkConsumer extends RichParallelSourceFunction<TaskWrapper> {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkConsumer.class);

  private final BaseTaskConsumer taskConsumer;

  public FlinkConsumer(OptimizerConfig config) {
    this.taskConsumer = new BaseTaskConsumer(config);
  }

  public FlinkConsumer(BaseTaskConsumer taskConsumer) {
    this.taskConsumer = taskConsumer;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
  }

  @Override
  public void run(SourceContext<TaskWrapper> sourceContext) throws Exception {
    int retry = 0;
    while (true) {
      try {
        TaskWrapper task = taskConsumer.pollTask();
        if (task != null) {
          sourceContext.collect(task);
        } else {
          LOG.info("{} poll no task", taskConsumer.getConfig().getOptimizerId());
        }
      } catch (Exception e) {
        // The subscription is abnormal and cannot be restored, and a new consumer can be activated
        LOG.error("failed to poll task, retry {}", retry, e);
        retry++;
      } finally {
        if (retry >= 3) {
          //stop = true;
          retry = 0;
          LOG.error("flink source has tried too many times, and the subscription message is suspended." +
              " Please check for errors");
          Thread.sleep(10000);
        }
      }
    }
  }

  @Override
  public void cancel() {

  }
}
