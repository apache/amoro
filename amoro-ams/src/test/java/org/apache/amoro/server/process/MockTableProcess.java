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

package org.apache.amoro.server.process;

import org.apache.amoro.Action;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.TableProcess;

import java.util.Collections;
import java.util.Map;

/** Mock table process for tests. */
public class MockTableProcess extends TableProcess {

  private final Action action;
  private final Map<String, String> processParameters;
  private final Map<String, String> summary;

  MockTableProcess(TableRuntime tableRuntime, ExecuteEngine executeEngine, Action action) {
    this(tableRuntime, executeEngine, action, Collections.emptyMap(), Collections.emptyMap());
  }

  MockTableProcess(
      TableRuntime tableRuntime,
      ExecuteEngine executeEngine,
      Action action,
      Map<String, String> processParameters,
      Map<String, String> summary) {
    super(tableRuntime, executeEngine);
    this.action = action;
    this.processParameters =
        processParameters == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(processParameters);
    this.summary = summary == null ? Collections.emptyMap() : Collections.unmodifiableMap(summary);
  }

  @Override
  public Action getAction() {
    return action;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return processParameters;
  }

  @Override
  public Map<String, String> getSummary() {
    return summary;
  }
}
