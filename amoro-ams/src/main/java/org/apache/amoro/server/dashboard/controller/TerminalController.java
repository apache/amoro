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

import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.model.LatestSessionInfo;
import org.apache.amoro.server.dashboard.model.SessionInfo;
import org.apache.amoro.server.dashboard.model.SqlExample;
import org.apache.amoro.server.dashboard.model.SqlResult;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.terminal.TerminalManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** The controller that handles terminal requests. */
public class TerminalController {

  private final TerminalManager terminalManager;

  public TerminalController(TerminalManager terminalManager) {
    this.terminalManager = terminalManager;
  }

  /** Get sql example list */
  public void getExamples(Context ctx) {
    List<String> examples =
        Arrays.stream(SqlExample.values()).map(SqlExample::getName).collect(Collectors.toList());
    ctx.json(OkResponse.of(examples));
  }

  /** Get sql examples */
  public void getSqlExamples(Context ctx) {
    String exampleName = ctx.pathParam("exampleName");

    for (SqlExample example : SqlExample.values()) {
      if (example.getName().equals(exampleName)) {
        ctx.json(OkResponse.of(example.getSql()));
        return;
      }
    }
    throw new IllegalArgumentException("can not get example name : " + exampleName);
  }

  /** Execute some sql */
  public void executeScript(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    Map<String, String> bodyParams = ctx.bodyAsClass(Map.class);
    String sql = bodyParams.get("sql");
    String terminalId = ctx.cookie("JSESSIONID");
    if (terminalId == null) {
      terminalId = UUID.randomUUID().toString();
    }
    String sessionId = terminalManager.executeScript(terminalId, catalog, sql);

    ctx.json(OkResponse.of(new SessionInfo(sessionId)));
  }

  /** Get execute logs of some session */
  public void getLogs(Context ctx) {
    String sessionId = ctx.pathParamAsClass("sessionId", String.class).get();
    ctx.json(OkResponse.of(terminalManager.getExecutionLog(sessionId)));
  }

  /** Get execute result of some session */
  public void getSqlResult(Context ctx) {
    String sessionId = ctx.pathParamAsClass("sessionId", String.class).get();
    List<SqlResult> results = terminalManager.getExecutionResults(sessionId);
    ctx.json(OkResponse.of(results));
  }

  /** Stop some sql */
  public void stopSql(Context ctx) {
    String sessionId = ctx.pathParamAsClass("sessionId", String.class).get();
    terminalManager.cancelExecution(sessionId);
    ctx.json(OkResponse.ok());
  }

  /** Get latest sql information * */
  public void getLatestInfo(Context ctx) {
    String terminalId = ctx.cookie("JSESSIONID");
    LatestSessionInfo sessionInfo = terminalManager.getLastSessionInfo(terminalId);
    ctx.json(OkResponse.of(sessionInfo));
  }
}
