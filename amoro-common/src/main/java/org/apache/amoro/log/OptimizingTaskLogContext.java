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
 *
 * Modified by Datazip Inc. in 2026
 */

package org.apache.amoro.log;

import org.apache.logging.log4j.ThreadContext;

// manages per-thread logging context using Log4j2 thread context: setting "processId" and "taskId"
// isContextSet: prevent overwriting an already-initialized MDC context
public class OptimizingTaskLogContext {

  public static final String PROCESS_ID_KEY = "processId";
  public static final String TASK_ID_KEY = "taskId";
  public static final String LOG_FILE_PATH_KEY = "logFilePath";

  // per-thread flag indicating whether a caller has already set up logging context.
  private static final ThreadLocal<Boolean> CONTEXT_SET = ThreadLocal.withInitial(() -> false);

  public static void setContext(long processId, int taskId) {
    CONTEXT_SET.set(true);
    ThreadContext.put(PROCESS_ID_KEY, String.valueOf(processId));
    ThreadContext.put(TASK_ID_KEY, String.valueOf(taskId));
  }

  public static void clearContext() {
    CONTEXT_SET.remove();
    ThreadContext.remove(PROCESS_ID_KEY);
    ThreadContext.remove(TASK_ID_KEY);
    ThreadContext.remove(LOG_FILE_PATH_KEY);
  }

  public static boolean isContextSet() {
    return CONTEXT_SET.get();
  }
}
