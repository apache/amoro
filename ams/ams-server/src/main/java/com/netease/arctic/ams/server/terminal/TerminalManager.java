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

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.config.ConfigOptions;
import com.netease.arctic.ams.server.config.Configuration;
import com.netease.arctic.ams.server.model.LogInfo;
import com.netease.arctic.ams.server.model.SqlResult;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.terminal.kyuubi.KyuubiTerminalSessionFactory;
import com.netease.arctic.ams.server.terminal.local.LocalSessionFactory;
import com.netease.arctic.table.TableMetaStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalManager {
  private static final Logger LOG = LoggerFactory.getLogger(TerminalManager.class);
  private final AtomicLong threadPoolCount = new AtomicLong();
  TerminalSessionFactory sessionFactory;
  int resultLimits = 1000;
  boolean stopOnError = false;

  ConcurrentHashMap<String, TerminalSession> sessionMap = new ConcurrentHashMap<>();
  ConcurrentHashMap<String, ExecutionTask> sessionExecutionTask = new ConcurrentHashMap<>();

  ThreadPoolExecutor executionPool = new ThreadPoolExecutor(
      1, 50, 30, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(),
      r -> new Thread(null, r, "terminal-execute-" + threadPoolCount.incrementAndGet()));

  public TerminalManager(Configuration conf) {
    this.resultLimits = conf.getInteger(ArcticMetaStoreConf.TERMINAL_RESULT_LIMIT);
    this.stopOnError = conf.getBoolean(ArcticMetaStoreConf.TERMINAL_STOP_ON_ERROR);
    this.sessionFactory = loadTerminalSessionFactory(conf);
  }

  /**
   * execute script, return terminal sessionId
   *
   * @param script
   * @return
   */
  public String executeScript(String loginId, String catalog, String script) {
    Optional<CatalogMeta> optCatalogMeta = ServiceContainer.getCatalogMetadataService().getCatalog(catalog);
    if (!optCatalogMeta.isPresent()) {
      throw new IllegalArgumentException("catalog " + catalog + " is not validea");
    }
    CatalogMeta catalogMeta = optCatalogMeta.get();
    TableMetaStore metaStore = getCatalogTableMetaStore(catalogMeta);
    String sessionId = getSessionId(loginId, metaStore);
    ExecutionTask task = new ExecutionTask(metaStore, script, sessionId);
    sessionExecutionTask.put(sessionId, task);
    executionPool.submit(task);
    LOG.info("new sql script submit, current thread pool state. [Active: "
        + executionPool.getActiveCount() + ", PoolSize: " + executionPool.getPoolSize() + "]"
    );
    return sessionId;
  }

  /**
   * get execution status and logs
   */
  public LogInfo getExecutionLog(String sessionId) {
    if (sessionId == null) {
      return new LogInfo(ExecutionStatus.Expired.name(), Lists.newArrayList());
    }
    ExecutionTask task = sessionExecutionTask.get(sessionId);
    if (task == null) {
      return new LogInfo(ExecutionStatus.Expired.name(), Lists.newArrayList());
    }
    return new LogInfo(task.status.get().name(), task.executionResult.getLogs());
  }

  public List<SqlResult> getExecutionResults(String sessionId) {
    if (sessionId == null) {
      return Lists.newArrayList();
    }
    ExecutionTask task = sessionExecutionTask.get(sessionId);
    if (task == null) {
      return Lists.newArrayList();
    }
    return task.executionResult.getResults().stream().map(statement -> {
      SqlResult sql = new SqlResult();
      sql.setId("line:" + statement.getLineNumber() + " - " + statement.getStatement());
      sql.setColumns(sql.getColumns());
      sql.setRowData(statement.getDataAsStringList());
      sql.setStatus(statement.isSuccess() ? ExecutionStatus.Finished.name(): ExecutionStatus.Failed.name());
      return sql;
    }).collect(Collectors.toList());
  }

  /**
   * cancel execution
   */
  public void cancelExecution(String sessionId) {
    if (sessionId == null) {
      return;
    }
    ExecutionTask task = sessionExecutionTask.get(sessionId);
    if (task != null) {
      task.cancel();
    }
  }

  // ========================== private method =========================

  private String getSessionId(String loginId, TableMetaStore auth) {
    String authName = auth.getHadoopUsername();
    if (TableMetaStore.AUTH_METHOD_KERBEROS.equalsIgnoreCase(auth.getAuthMethod())) {
      authName = auth.getKrbPrincipal();
    }
    return loginId + "-" + auth.getAuthMethod() + "-" + authName;
  }

  private TableMetaStore getCatalogTableMetaStore(CatalogMeta catalogMeta) {
    TableMetaStore.Builder builder = TableMetaStore.builder()
        .withBase64MetaStoreSite(
            catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE))
        .withBase64CoreSite(
            catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE))
        .withBase64HdfsSite(
            catalogMeta.getStorageConfigs().get(CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE));
    if (catalogMeta.getAuthConfigs()
        .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE)
        .equalsIgnoreCase(CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE)) {
      builder.withSimpleAuth(catalogMeta.getAuthConfigs()
          .get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME));
    } else {
      builder.withBase64Auth(
          catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE),
          catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME),
          catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB),
          catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5),
          catalogMeta.getAuthConfigs().get(CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL));
    }
    return builder.build();
  }

  private TerminalSession getOrCreateSession(String sessionId, TableMetaStore metaStore) {
    TerminalSession session = sessionMap.get(sessionId);
    if (session == null) {
      session = sessionFactory.create(metaStore);
      sessionMap.put(sessionId, session);
    }
    return session;
  }

  TerminalSessionFactory loadTerminalSessionFactory(Configuration conf) {
    String backend = conf.get(ArcticMetaStoreConf.TERMINAL_BACKEND);
    if (backend == null) {
      throw new IllegalArgumentException("lack terminal implement config.");
    }
    String backendImplement;
    switch (backend.toLowerCase()) {
      case "local":
        backendImplement = LocalSessionFactory.class.getName();
        break;
      case "kyuubi":
        backendImplement = KyuubiTerminalSessionFactory.class.getName();
        break;
      case "custom":
        Optional<String> customFactoryClz = conf.getOptional(ArcticMetaStoreConf.TERMINAL_SESSION_FACTORY);
        if (!customFactoryClz.isPresent()) {
          throw new IllegalArgumentException("terminal backend type is custom, but terminal session factory is not " +
              "configured");
        }
        backendImplement = customFactoryClz.get();
        break;
      default:
        throw new IllegalArgumentException("illegal terminal implement: " + backend + ", local, kyuubi, " +
            "custom is available");
    }
    TerminalSessionFactory factory;
    try {
      factory = (TerminalSessionFactory) Class.forName(backendImplement).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException("failed to init session factory", e);
    }

    Map<String, String> factoryConfig = Maps.newHashMap();
    String factoryPropertiesPrefix = ArcticMetaStoreConf.TERMINAL_PREFIX + backend + ".";

    for (String key : conf.keySet()) {
      if (!key.startsWith(ArcticMetaStoreConf.TERMINAL_PREFIX)) {
        continue;
      }
      String value = conf.getValue(ConfigOptions.key(key).stringType().noDefaultValue());
      key = key.substring(factoryPropertiesPrefix.length());
      factoryConfig.put(key, value);
    }
    factoryConfig.put("result.limit", this.resultLimits + "");
    factory.initialize(factoryConfig);
    return factory;
  }

  class ExecutionTask implements Runnable {

    final String script;

    final TableMetaStore tableMetaStore;

    final ExecutionResult executionResult = new ExecutionResult();

    final AtomicReference<ExecutionStatus> status = new AtomicReference<>(ExecutionStatus.Created);

    final String sessionId;

    public ExecutionTask(TableMetaStore metaStore, String script, String sessionId) {
      this.script = script;
      this.tableMetaStore = metaStore;
      this.sessionId = sessionId;
    }

    @Override
    public void run() {
      try {

        tableMetaStore.doAs(() -> {
          TerminalSession session = getOrCreateSession(sessionId, tableMetaStore);
          execute(session);
          return null;
        });

        status.compareAndSet(ExecutionStatus.Running, ExecutionStatus.Finished);
      } catch (Throwable t) {
        LOG.error("something error when execute script. ", t);
        executionResult.appendLog("something error when execute script.");
        executionResult.appendLog(getStackTraceAsString(t));
        status.compareAndSet(ExecutionStatus.Running, ExecutionStatus.Failed);
      }
    }

    public void cancel() {
      status.compareAndSet(ExecutionStatus.Running, ExecutionStatus.Canceled);
    }

    void execute(TerminalSession session) throws IOException {
      LineNumberReader reader = new LineNumberReader(new StringReader(script));
      StringBuilder statementBuilder = null;
      String line;
      int no = -1;

      while ((line = reader.readLine()) != null) {
        if (ExecutionStatus.Canceled == status.get()) {
          executionResult.appendLog("execution is canceled. ");
          break;
        }
        if (statementBuilder == null) {
          statementBuilder = new StringBuilder();
        }
        line = line.trim();
        if (line.length() < 1 || line.startsWith("--")) {
          // ignore blank lines and comments.
          continue;
        } else if (line.endsWith(";")) {
          statementBuilder.append(line);
          no = lineNumber(reader, no);

          boolean error = executeStatement(session, statementBuilder.toString(), no);
          if (error) {
            if (stopOnError) {
              status.compareAndSet(ExecutionStatus.Running, ExecutionStatus.Failed);
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
    boolean executeStatement(TerminalSession session, String statement, int lineNo) {
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
          if (count >= resultLimits) {
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

  static class TerminalSessionContext {
    private TerminalSession session;
    private String authIdentifier;
  }
}
