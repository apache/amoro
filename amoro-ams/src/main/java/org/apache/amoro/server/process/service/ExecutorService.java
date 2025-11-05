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

package org.apache.amoro.server.process.service;

import org.apache.amoro.server.process.context.ProcessServiceContext;
import org.apache.amoro.server.process.executor.BaseEngineActionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorService implements BaseService {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutorService.class);

  private ThreadPoolExecutor executorService;
  private ScheduledExecutorService service;
  private BlockingQueue<BaseEngineActionExecutor> taskQueue;
  private int coreExecuteSize;
  private int maxExecuteSize;

  public void init() {
    LOG.info("Init service: " + this.getClass().getName());
    service = Executors.newSingleThreadScheduledExecutor();
    executorService =
        new ThreadPoolExecutor(
            coreExecuteSize, maxExecuteSize, 60, TimeUnit.SECONDS, new SynchronousQueue<>());
    taskQueue = new LinkedBlockingQueue<>();
    LOG.info("Finish init service: " + this.getClass().getName());
  }

  @Override
  public void startService() {
    LOG.info("Start service: " + this.getClass().getName());
    service.submit(new ExecutionTask());
    LOG.info("Finish start service: " + this.getClass().getName());
  }

  @Override
  public void stop() {
    LOG.info("Stop service: " + this.getClass().getName());
    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdown();
    }
    if (service != null && service.isShutdown()) {
      service.shutdown();
    }
    LOG.info("Finish stop service: " + this.getClass().getName());
  }

  private class ExecutionTask implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          BaseEngineActionExecutor executor = taskQueue.take();
          LOG.info("Start execute: " + executor);
          executorService.execute(executor);
        } catch (InterruptedException interruptedException) {
          LOG.error("Occur exception when exec job: " + interruptedException);
        } catch (Throwable e) {
          LOG.error("Fail execute task", e);
        }
      }
    }
  }

  public void submitTask(BaseEngineActionExecutor task) {
    taskQueue.add(task);
  }

  public int getFreeSize() {
    return maxExecuteSize - ProcessServiceContext.getRunningInstanceNum();
  }
}
