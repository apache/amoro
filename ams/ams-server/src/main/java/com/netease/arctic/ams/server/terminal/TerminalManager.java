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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.Charsets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalManager {
  private static final Logger LOG = LoggerFactory.getLogger(TerminalManager.class);

  TerminalSessionFactory sessionFactory;
  int limit = 1000;
  boolean stopWhenError = false;

  ConcurrentHashMap<String, TerminalSession> sessionMap = new ConcurrentHashMap<>();
  ConcurrentHashMap<String, ExecutionTask> sessionExecutionTask = new ConcurrentHashMap<>();

  ThreadPoolExecutor executionPool = new ThreadPoolExecutor(
      1, 50, 30, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(),
      new ThreadFactory() {
        private AtomicLong threadPoolCount = new AtomicLong();

        @Override
        public Thread newThread(@NotNull Runnable r) {
          Thread thread = newThread(r);
          thread.setName("terminal-execute-" + threadPoolCount.incrementAndGet());
          return thread;
        }
      });

  AtomicLong executionAllocator = new AtomicLong(0);

  public TerminalManager() {
    // TODO: init factory.
  }

  public void executeScript(String script) {
    String sessionId = getSessionId();
    if (sessionId == null) {
      sessionId = UUID.randomUUID().toString();
    }
    TerminalSession session = getOrCreateSession(sessionId);
    ExecutionTask task = new ExecutionTask();
    executionPool.submit(task);
    LOG.info("new sql script submit, current thread pool state. [Active: "
        + executionPool.getActiveCount() + ", PoolSize: " + executionPool.getPoolSize()
    );
  }

  public ExecutionStatus getExecutionStatus(long execId) {
    String sessionId = getSessionId();
    if (sessionId == null) {
      return ExecutionStatus.EXPIRED;
    }
    ExecutionTask task = sessionExecutionTask.get(sessionId);
    return task.status.get();
  }

  /**
   * get sql execution of current session
   */
  public Optional<ExecutionResult> getExecutionResult() {
    String sessionId = getSessionId();
    if (sessionId == null) {
      return Optional.empty();
    }
    ExecutionTask task = sessionExecutionTask.get(sessionId);
    return Optional.of(task.executionResult);
  }

  /**
   * cancel execution
   */
  public void cancelExecution() {
    String sessionId = getSessionId();
    if (sessionId == null) {
      return;
    }
    ExecutionTask task = sessionExecutionTask.get(sessionId);
    if (task != null) {
      task.cancel();
    }
  }

  // ========================== private method =========================

  private String getSessionId() {
    // TODO get session id from cookie
    return null;
  }

  private TerminalSession getOrCreateSession(String sessionId) {
    TerminalSession session = sessionMap.get(sessionId);
    if (session == null){
      session = sessionFactory.create(null);
      sessionMap.put(sessionId, session);
    }
    return session;
  }


  class ExecutionTask implements Runnable {

    TerminalSession session;
    String script;

    ExecutionResult executionResult;

    AtomicReference<ExecutionStatus> status = new AtomicReference<>(ExecutionStatus.CREATE);

    @Override
    public void run() {
      try {
        execute();
      }catch (Throwable t){
        LOG.error("something error when execute script. ", t);
        executionResult.appendLog("something error when execute script.");
        executionResult.appendLog(getStackTraceAsString(t));
        status.compareAndSet(ExecutionStatus.RUNNING, ExecutionStatus.FAILED);
      }
    }

    public void cancel() {
      status.compareAndSet(ExecutionStatus.RUNNING, ExecutionStatus.CANCELED);
    }

    void execute() throws IOException {
      LineNumberReader reader = new LineNumberReader(new StringReader(script));
      StringBuilder statementBuilder = null;
      String line;
      int no = -1;

      while ((line = reader.readLine()) != null) {
        if (ExecutionStatus.CANCELED == status.get()){
          executionResult.appendLog("execution is canceled. ");
          break;
        }
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
          if (error) {
            if (stopWhenError) {
              status.compareAndSet(ExecutionStatus.RUNNING, ExecutionStatus.FAILED);
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
          if (count >= limit) {
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
