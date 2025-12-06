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

package org.apache.amoro.server.dashboard.controller;

import static org.apache.amoro.server.AmsServiceMetrics.AMS_ACTIVE_TAG;

import io.javalin.http.Context;
import org.apache.amoro.server.AmoroServiceContainer;
import org.apache.amoro.server.dashboard.response.OkResponse;

import java.util.HashMap;
import java.util.Map;

/** The controller that handles health check requests. */
public class HealthCheckController {

  private AmoroServiceContainer ams;

  public HealthCheckController(AmoroServiceContainer ams) {
    this.ams = ams;
  }

  public void healthCheck(Context ctx) {
    Map<String, String> healthCheckResult = new HashMap<>(4);
    healthCheckResult.put(AMS_ACTIVE_TAG, ams.getHaState().toString());
    ctx.json(OkResponse.of(healthCheckResult));
  }
}
