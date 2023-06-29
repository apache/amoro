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

package com.netease.arctic.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.Environments;
import com.netease.arctic.ams.api.OptimizingService;
import com.netease.arctic.ams.api.PropertyNames;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.dashboard.DashboardServer;
import com.netease.arctic.server.dashboard.response.ErrorResponse;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.dashboard.utils.CommonUtil;
import com.netease.arctic.server.exception.ArcticRuntimeException;
import com.netease.arctic.server.persistence.SqlSessionFactoryProvider;
import com.netease.arctic.server.resource.ContainerMetadata;
import com.netease.arctic.server.resource.ResourceContainers;
import com.netease.arctic.server.table.DefaultTableService;
import com.netease.arctic.server.table.RuntimeHandlerChain;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.server.table.executor.AsyncTableExecutors;
import com.netease.arctic.server.terminal.TerminalManager;
import com.netease.arctic.server.utils.ConfigOption;
import com.netease.arctic.server.utils.Configurations;
import com.netease.arctic.server.utils.ThriftServiceProxy;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcticServiceContainer {

  public static final Logger LOG = LoggerFactory.getLogger(ArcticServiceContainer.class);

  public static final String SERVER_CONFIG_PATH = "/conf/config.yaml";

  private final List<ResourceGroup> resourceGroups = new ArrayList<>();
  private final HighAvailabilityContainer haContainer;
  private DefaultTableService tableService;
  private DefaultOptimizingService optimizingService;
  private TerminalManager terminalManager;
  private Configurations serviceConfig;
  private TServer thriftServer;
  private Javalin httpServer;

  public ArcticServiceContainer() throws Exception {
    initConfig();
    haContainer = new HighAvailabilityContainer(serviceConfig);
  }

  public static void main(String[] args) {
    try {
      ArcticServiceContainer service = new ArcticServiceContainer();
      while (true) {
        try {
          service.waitLeaderShip();
          service.startService();
          service.waitFollowerShip();
        } catch (Exception e) {
          LOG.error("AMS start error", e);
        } finally {
          service.dispose();
        }
      }
    } catch (Throwable t) {
      LOG.error("AMS encountered an unknown exception, will exist", t);
      System.exit(1);
    }
  }

  public void waitLeaderShip() throws Exception {
    haContainer.waitLeaderShip();
  }

  public void waitFollowerShip() throws Exception {
    haContainer.waitFollowerShip();
  }

  public void startService() throws Exception {
    tableService = new DefaultTableService(serviceConfig);
    optimizingService = new DefaultOptimizingService(serviceConfig, tableService, resourceGroups);

    LOG.info("Setting up AMS table executors...");
    AsyncTableExecutors.getInstance().setup(tableService, serviceConfig);
    addHandlerChain(optimizingService.getTableRuntimeHandler());
    addHandlerChain(AsyncTableExecutors.getInstance().getSnapshotsExpiringExecutor());
    addHandlerChain(AsyncTableExecutors.getInstance().getOrphanFilesCleaningExecutor());
    addHandlerChain(AsyncTableExecutors.getInstance().getOptimizingCommitExecutor());
    addHandlerChain(AsyncTableExecutors.getInstance().getOptimizingExpiringExecutor());
    addHandlerChain(AsyncTableExecutors.getInstance().getBlockerExpiringExecutor());
    addHandlerChain(AsyncTableExecutors.getInstance().getHiveCommitSyncExecutor());
    addHandlerChain(AsyncTableExecutors.getInstance().getTableRefreshingExecutor());
    tableService.initialize();
    LOG.info("AMS table service have been initialized");
    terminalManager = new TerminalManager(serviceConfig, tableService);

    initThriftService();
    startThriftService();

    initHttpService();
    startHttpService();
  }

  private void addHandlerChain(RuntimeHandlerChain chain) {
    if (chain != null) {
      tableService.addHandlerChain(chain);
    }
  }

  public void dispose() {
    if (thriftServer != null) {
      thriftServer.stop();
    }
    if (httpServer != null) {
      httpServer.stop();
    }
    if (tableService != null) {
      tableService.dispose();
      tableService = null;
    }
    optimizingService = null;
  }

  private void initConfig() throws IOException {
    LOG.info("initializing configurations...");
    new ConfigurationHelper().init();
  }

  private void startThriftService() {
    Thread thread = new Thread(() -> {
      thriftServer.serve();
      LOG.info("Thrift services have been started");
    }, "Thrift-server-thread");
    thread.setDaemon(true);
    thread.start();
  }


  private void initHttpService() {
    DashboardServer dashboardServer = new DashboardServer(
        serviceConfig, tableService, optimizingService, terminalManager);
    IcebergRestCatalogService restCatalogService = new IcebergRestCatalogService(tableService);

    httpServer = Javalin.create(config -> {
      config.addStaticFiles(dashboardServer.configStaticFiles());
      config.sessionHandler(SessionHandler::new);
      config.enableCorsForAllOrigins();
    });
    httpServer.routes(() -> {
      dashboardServer.endpoints().addEndpoints();
      restCatalogService.endpoints().addEndpoints();
    });

    httpServer.before(ctx -> {
      String token = ctx.queryParam("token");
      if (StringUtils.isNotEmpty(token)) {
        CommonUtil.checkSinglePageToken(ctx);
      } else {
        dashboardServer.preHandleRequest(ctx);
      }
    });
    httpServer.exception(Exception.class, (e, ctx) -> {
      if (restCatalogService.needHandleException(ctx)) {
        restCatalogService.handleException(e, ctx);
      } else {
        dashboardServer.handleException(e, ctx);
      }
    });
    // default response handle
    httpServer.error(HttpCode.NOT_FOUND.getStatus(), ctx -> {
      if (!restCatalogService.needHandleException(ctx)) {
        ctx.json(new ErrorResponse(HttpCode.NOT_FOUND, "page not found!", ""));
      }
    });

    httpServer.error(HttpCode.INTERNAL_SERVER_ERROR.getStatus(), ctx -> {
      if (!restCatalogService.needHandleException(ctx)) {
        ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "internal error!", ""));
      }
    });
  }

  private void startHttpService() {
    int port = serviceConfig.getInteger(ArcticManagementConf.HTTP_SERVER_PORT);
    httpServer.start(port);
    LOG.info("Http server start at {}!!!", port);
  }

  private void initThriftService() throws TTransportException {
    LOG.info("Initializing thrift service...");
    long maxMessageSize = serviceConfig.getLong(ArcticManagementConf.THRIFT_MAX_MESSAGE_SIZE);
    int selectorThreads = serviceConfig.getInteger(ArcticManagementConf.THRIFT_SELECTOR_THREADS);
    int workerThreads = serviceConfig.getInteger(ArcticManagementConf.THRIFT_WORKER_THREADS);
    int queueSizePerSelector = serviceConfig.getInteger(ArcticManagementConf.THRIFT_QUEUE_SIZE_PER_THREAD);
    int port = serviceConfig.getInteger(ArcticManagementConf.THRIFT_BIND_PORT);
    String bindHost = serviceConfig.getString(ArcticManagementConf.SERVER_BIND_HOST);

    LOG.info("Starting thrift server on port:" + port);

    TMultiplexedProcessor processor = new TMultiplexedProcessor();
    final TProtocolFactory protocolFactory;
    final TProtocolFactory inputProtoFactory;
    protocolFactory = new TBinaryProtocol.Factory();
    inputProtoFactory = new TBinaryProtocol.Factory(true, true, maxMessageSize, maxMessageSize);

    ArcticTableMetastore.Processor<ArcticTableMetastore.Iface> tableMetastoreProcessor =
        new ArcticTableMetastore.Processor<>(ThriftServiceProxy.createProxy(ArcticTableMetastore.Iface.class,
            new TableManagementService(tableService), ArcticRuntimeException::normalizeCompatibly));
    processor.registerProcessor(Constants.THRIFT_TABLE_SERVICE_NAME, tableMetastoreProcessor);

    OptimizingService.Processor<OptimizingService.Iface> optimizerManagerProcessor =
        new OptimizingService.Processor<>(ThriftServiceProxy.createProxy(OptimizingService.Iface.class,
            optimizingService, ArcticRuntimeException::normalize));
    processor.registerProcessor(Constants.THRIFT_OPTIMIZING_SERVICE_NAME, optimizerManagerProcessor);

    TNonblockingServerSocket serverTransport = getServerSocket(bindHost, port);
    TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverTransport)
        .processor(processor)
        .transportFactory(new TFramedTransport.Factory())
        .protocolFactory(protocolFactory)
        .inputProtocolFactory(inputProtoFactory)
        .workerThreads(workerThreads)
        .selectorThreads(selectorThreads)
        .acceptQueueSizePerThread(queueSizePerSelector);
    thriftServer = new TThreadedSelectorServer(args);
    LOG.info("Initialized the thrift server on port [" + port + "]...");
    LOG.info("Options.thriftWorkerThreads = " + workerThreads);
    LOG.info("Options.thriftSelectorThreads = " + selectorThreads);
    LOG.info("Options.queueSizePerSelector = " + queueSizePerSelector);
  }

  private class ConfigurationHelper {

    private JSONObject yamlConfig;

    public void init() throws IOException {
      initServiceConfig();
      initContainerConfig();
      initResourceGroupConfig();
    }

    @SuppressWarnings("unchecked")
    private void initResourceGroupConfig() {
      LOG.info("initializing resource group configuration...");
      JSONArray optimizeGroups = yamlConfig.getJSONArray(ArcticManagementConf.OPTIMIZER_GROUP_LIST);
      for (int i = 0; i < optimizeGroups.size(); i++) {
        JSONObject groupConfig = optimizeGroups.getJSONObject(i);
        ResourceGroup.Builder groupBuilder = new ResourceGroup.Builder(
            groupConfig.getString(ArcticManagementConf.OPTIMIZER_GROUP_NAME),
            groupConfig.getString(ArcticManagementConf.OPTIMIZER_GROUP_CONTAINER));
        if (!ResourceContainers.contains(groupBuilder.getContainer())) {
          throw new IllegalStateException(
              "can not find such container config named" +
                  groupBuilder.getContainer());
        }
        if (groupConfig.containsKey(ArcticManagementConf.OPTIMIZER_GROUP_PROPERTIES) &&
            groupConfig.get(ArcticManagementConf.OPTIMIZER_GROUP_PROPERTIES) != null) {
          groupBuilder.addProperties(groupConfig.getObject(
              ArcticManagementConf.OPTIMIZER_GROUP_PROPERTIES,
              Map.class));
        }
        resourceGroups.add(groupBuilder.build());
      }
    }

    @SuppressWarnings("unchecked")
    private void initServiceConfig() throws IOException {
      LOG.info("initializing service configuration...");
      String configPath = Environments.getArcticHome() + SERVER_CONFIG_PATH;
      LOG.info("load config from path: {}", configPath);
      yamlConfig = new JSONObject(new Yaml().loadAs(Files.newInputStream(Paths.get(configPath)), Map.class));
      JSONObject systemConfig = yamlConfig.getJSONObject(ArcticManagementConf.SYSTEM_CONFIG);
      Map<String, Object> expandedConfigurationMap = Maps.newHashMap();
      expandConfigMap(systemConfig, "", expandedConfigurationMap);
      validateConfig(expandedConfigurationMap);
      serviceConfig = Configurations.fromObjectMap(expandedConfigurationMap);
      SqlSessionFactoryProvider.getInstance().init(serviceConfig);
    }

    private void validateConfig(Map<String, Object> systemConfig) {
      if (!systemConfig.containsKey(ArcticManagementConf.SERVER_EXPOSE_HOST.key())) {
        throw new IllegalArgumentException(
            "configuration " + ArcticManagementConf.SERVER_EXPOSE_HOST.key() + " must be set");
      }
      InetAddress inetAddress = AmsUtil.lookForBindHost(
          (String) systemConfig.get(ArcticManagementConf.SERVER_EXPOSE_HOST.key()));
      systemConfig.put(ArcticManagementConf.SERVER_EXPOSE_HOST.key(), inetAddress.getHostAddress());

      // mysql config
      if (((String) systemConfig.get(ArcticManagementConf.DB_TYPE.key()))
          .equalsIgnoreCase(ArcticManagementConf.DB_TYPE_MYSQL)) {
        if (!systemConfig.containsKey(ArcticManagementConf.DB_PASSWORD.key()) ||
            !systemConfig.containsKey(ArcticManagementConf.DB_USER_NAME.key())) {
          throw new IllegalArgumentException("username and password must be configured if the database type is mysql");
        }
      }

      // HA config
      if (systemConfig.containsKey(ArcticManagementConf.HA_ENABLE.key()) &&
          ((Boolean) systemConfig.get(ArcticManagementConf.HA_ENABLE.key()))) {
        if (!systemConfig.containsKey(ArcticManagementConf.HA_ZOOKEEPER_ADDRESS.key())) {
          throw new IllegalArgumentException(
              ArcticManagementConf.HA_ZOOKEEPER_ADDRESS.key() + " must be configured when you enable " +
                  "the ams high availability");
        }
      }
      // terminal config
      String terminalBackend = systemConfig.getOrDefault(ArcticManagementConf.TERMINAL_BACKEND.key(), "")
          .toString().toLowerCase();
      if (!Arrays.asList("local", "kyuubi", "custom").contains(terminalBackend)) {
        throw new IllegalArgumentException(
            String.format("Illegal terminal implement: %s, local, kyuubi, custom is available", terminalBackend));
      }

      validateThreadCount(systemConfig, ArcticManagementConf.REFRESH_TABLES_THREAD_COUNT);
      validateThreadCount(systemConfig, ArcticManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT);

      if (enabled(systemConfig, ArcticManagementConf.EXPIRE_SNAPSHOTS_ENABLED)) {
        validateThreadCount(systemConfig, ArcticManagementConf.EXPIRE_SNAPSHOTS_THREAD_COUNT);
      }

      if (enabled(systemConfig, ArcticManagementConf.CLEAN_ORPHAN_FILES_ENABLED)) {
        validateThreadCount(systemConfig, ArcticManagementConf.CLEAN_ORPHAN_FILES_THREAD_COUNT);
      }
      if (enabled(systemConfig, ArcticManagementConf.SYNC_HIVE_TABLES_ENABLED)) {
        validateThreadCount(systemConfig, ArcticManagementConf.SYNC_HIVE_TABLES_THREAD_COUNT);
      }
    }

    private boolean enabled(Map<String, Object> systemConfig, ConfigOption<Boolean> config) {
      return (boolean) systemConfig.getOrDefault(config.key(), config.defaultValue());
    }

    private void validateThreadCount(Map<String, Object> systemConfig, ConfigOption<Integer> config) {
      int threadCount = (int) systemConfig.getOrDefault(config.key(), config.defaultValue());
      if (threadCount <= 0) {
        throw new IllegalArgumentException(
            String.format("%s(%s) must > 0, actual value = %d", config.key(), config.description(), threadCount));
      }
    }

    @SuppressWarnings("unchecked")
    private void initContainerConfig() {
      LOG.info("initializing container configuration...");
      JSONArray containers = yamlConfig.getJSONArray(ArcticManagementConf.CONTAINER_LIST);
      List<ContainerMetadata> containerList = new ArrayList<>();
      for (int i = 0; i < containers.size(); i++) {
        JSONObject containerConfig = containers.getJSONObject(i);
        ContainerMetadata container = new ContainerMetadata(
            containerConfig.getString(ArcticManagementConf.CONTAINER_NAME),
            containerConfig.getString(ArcticManagementConf.CONTAINER_IMPL));
        Map<String, String> containerProperties = new HashMap<>();
        containerProperties.put(PropertyNames.AMS_HOME, Environments.getArcticHome());
        containerProperties.put(PropertyNames.OPTIMIZER_AMS_URL, AmsUtil.getAMSThriftAddress(serviceConfig));
        if (containerConfig.containsKey(ArcticManagementConf.CONTAINER_PROPERTIES)) {
          containerProperties.putAll(containerConfig.getObject(ArcticManagementConf.CONTAINER_PROPERTIES, Map.class));
        }
        container.setProperties(containerProperties);
        containerList.add(container);
      }
      ResourceContainers.init(containerList);
    }
  }

  private TNonblockingServerSocket getServerSocket(String bindHost, int portNum) throws TTransportException {
    InetSocketAddress serverAddress;
    serverAddress = new InetSocketAddress(bindHost, portNum);
    return new TNonblockingServerSocket(serverAddress);
  }

  @SuppressWarnings("unchecked")
  private void expandConfigMap(Map<String, Object> config, String prefix, Map<String, Object> result) {
    for (Map.Entry<String, Object> entry : config.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
      if (value instanceof Map) {
        Map<String, Object> subMap = (Map<String, Object>) value;
        expandConfigMap(subMap, fullKey, result);
      } else {
        result.put(fullKey, value);
      }
    }
  }


  @VisibleForTesting
  public TableService getTableService() {
    return this.tableService;
  }
}
