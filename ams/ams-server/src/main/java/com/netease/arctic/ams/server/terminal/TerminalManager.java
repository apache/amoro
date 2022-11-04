/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.terminal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalManager {
  private static final Logger LOG = LoggerFactory.getLogger(TerminalManager.class);

  TerminalSessionFactory factory;
  int limit = 1000;
  boolean stopWhenError = false;

  public TerminalManager() {
    // TODO: init factory.
  }

  public TerminalSession getOrCreateSession() {
    return null;
  }

  public long executeScript(TerminalSession session, String script) {
    script = script.replace("\r\n", "");
    List<String> statements = Arrays.asList(script.split(";"));

    return -1;
  }

  public ExecutionStatus getExecutionStatus(long execId) {
    return ExecutionStatus.EXPIRED;
  }

  public Optional<ExecutionResult> getExecutionResult(long execId) {
    return Optional.empty();
  }

  public void cancelExecution(int execId) {

  }

  class ExecutionTask implements Runnable {

    TerminalSession session;
    String script;

    ExecutionResult executionResult;

    @Override
    public void run() {

    }

    void execute() throws IOException {
      LineNumberReader reader = new LineNumberReader(new StringReader(script));
      StringBuilder statementBuilder = null;
      String line;
      int no = -1;

      while ((line = reader.readLine()) != null) {
        if (statementBuilder == null) {
          statementBuilder = new StringBuilder();
        }
        line = line.trim();
        if (line.length() < 1 || line.startsWith("--")) {
          // ignore blank lines and comments.
        } else if (line.endsWith(";")) {
          statementBuilder.append(line);
          no = lineNumber(reader, no);

          boolean error = executeStatement(statementBuilder.toString(), no);
          if (error){
            if (stopWhenError){
              executionResult.appendLog("execution stopped for error happened and stop-when-error config.");
              break;
            }
          }

          statementBuilder = null;
          no = -1;
        } else {
          statementBuilder.append(line);
          statementBuilder.append(" ");
          no = lineNumber(reader, no);
        }
      }
    }

    int lineNumber(LineNumberReader reader, int no) {
      if (no < 0) {
        return reader.getLineNumber();
      }
      return no;
    }

    /**
     * @return - false if any exception happened.
     */
    boolean executeStatement(String statement, int lineNo) {
      executionResult.appendLog("prepare execute statement, line:" + lineNo);
      executionResult.appendLog(statement);

      TerminalSession.ResultSet rs = null;
      long begin = System.currentTimeMillis();
      try {
        rs = session.executeStatement(statement);
      } catch (Throwable t) {
        executionResult.appendLog("meet exception during execution.");
        executionResult.appendLog(getStackTraceAsString(t));
        return false;
      }

      if (rs.empty()) {
        long cost = System.currentTimeMillis() - begin;
        executionResult.appendLog("statement execute down, result is empty, execution cost:" + cost + "ms");
        return true;
      } else {
        StatementResult sr = fetchResults(rs, statement, lineNo);
        long cost = System.currentTimeMillis() - begin;
        executionResult.appendResult(sr);
        executionResult.appendLog(
            "statement execute down, fetch rows:" + sr.getDatas().size() + ", execution cost: " + cost + "ms");
        return sr.isSuccess();
      }
    }

    StatementResult fetchResults(TerminalSession.ResultSet rs, String statement, int lineNo) {
      long count = 0;
      StatementResult sr = new StatementResult(statement, lineNo, rs.columns());
      try {
        while (rs.next()) {
          sr.appendRow(rs.rowData());
          count++;
          if (count >= limit){
            executionResult.appendLog("meet result set limit " + count + ", ignore rows left.");
            break;
          }
        }
      } catch (Throwable t) {
        executionResult.appendLog("meet exception when fetch result data.");
        String log = getStackTraceAsString(t);
        sr.withExceptionLog(log);
        executionResult.appendLog(log);
      } finally {
        try {
          rs.close();
        } catch (Throwable t) {
          // ignore
        }
      }
      return sr;
    }

    String getStackTraceAsString(Throwable t) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(out);
      t.printStackTrace(ps);
      return new String(out.toByteArray(), Charsets.UTF_8);
    }
  }
}
