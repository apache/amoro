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

package org.apache.amoro.server.process.coordinator;

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.process.ProcessService;
import org.apache.amoro.server.process.metric.SchedulerMetrics;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class ActionCoordinator {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long START_DELAY = 10 * 1000L;
  private ScheduledExecutorService service;
  private int initialDelay;
  private int period;
  private String schedulerName;
  private Map<String, String> tags = Maps.newHashMap();
  private SchedulerMetrics.Metric metric;

  public Action action;
  public ProcessFactory processFactory;
  public ProcessService processService;
  private final Map<ServerTableIdentifier, TableProcess> tableProcessMap = new HashMap<>();

  public void init() {}

  public void startService() {}

  public void stop() {}

  public void schedule() {}

  protected boolean enabled() {
    return true;
  }

  protected boolean executable(TableRuntime tableRuntime) {
    return false;
  };

  protected void scheduleForTable(TableRuntime tableRuntime) {};

  public TableProcess createTableProcess(TableRuntime tableRuntime) {
    return null;
  }

  public void submitTableProcess() {}

  public TableProcess queryTableProcess(long id) {
    return null;
  }

  public TableProcess updateTableProcess(long id) {
    return null;
  }

  public TableProcess dropTableProcess(long id) {
    return null;
  }
}
