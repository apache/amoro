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

package org.apache.amoro.server.process.context;

import org.apache.amoro.server.process.resource.RunningInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Services only be used in master node. */
public class ProcessServiceContext {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessServiceContext.class);

  public static final AtomicBoolean INIT_STATUS = new AtomicBoolean(false);

  public static int getRunningInstanceNum() {
    return getRunningInstanceManager().getExecutionNum();
  }

  public static RunningInstanceManager getRunningInstanceManager() {
    return ServiceContextHolder.RUNNING_INSTANCE_MANAGER;
  }

  private static ConcurrentHashMap<String, String> syncRunningInstance = new ConcurrentHashMap<>();

  public static void addSyncRunningInstance(String instanceIdentifier, String threadIdentifier) {
    syncRunningInstance.put(instanceIdentifier, threadIdentifier);
  }

  public static void removeSyncRunningInstance(String instanceIdentifier) {
    syncRunningInstance.remove(instanceIdentifier);
  }

  public static int getSyncRunningInstanceNum() {
    return syncRunningInstance.size();
  }

  private static class ServiceContextHolder {

    private static final RunningInstanceManager RUNNING_INSTANCE_MANAGER =
        new RunningInstanceManager();

    private static void init() {}

    private static void close() {}
  }

  public static void init() {}

  public static void close() {}
}
