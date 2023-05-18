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
import com.google.common.annotations.VisibleForTesting;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.Environments;
import com.netease.arctic.ams.api.OptimizingService;
import com.netease.arctic.ams.api.PropertyNames;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.dashboard.DashboardServer;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.exception.ArcticRuntimeException;
import com.netease.arctic.server.persistence.SqlSessionFactoryProvider;
import com.netease.arctic.server.resource.ContainerMetadata;
import com.netease.arctic.server.resource.ResourceContainers;
import com.netease.arctic.server.table.DefaultTableService;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.server.table.executor.AsyncTableExecutors;
import com.netease.arctic.server.table.executor.BlockerExpiringExecutor;
import com.netease.arctic.server.table.executor.HiveCommitSyncExecutor;
import com.netease.arctic.server.table.executor.OptimizingCommitExecutor;
import com.netease.arctic.server.table.executor.OptimizingExpiringExecutor;
import com.netease.arctic.server.table.executor.OrphanFilesCleaningExecutor;
import com.netease.arctic.server.table.executor.SnapshotsExpiringExecutor;
import com.netease.arctic.server.table.executor.TableRuntimeRefreshExecutor;
import com.netease.arctic.server.utils.Configurations;
import org.apache.flink.configuration.IllegalConfigurationException;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
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
  private Configurations serviceConfig;
  private TServer server;
  private DashboardServer dashboardServer;

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
    optimizingService = new DefaultOptimizingService(tableService, resourceGroups);

    LOG.info("Setting up AMS table executors...");
    AsyncTableExecutors.getInstance().setup(tableService, serviceConfig);
    tableService.addHandlerChain(optimizingService.getTableRuntimeHandler());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getSnapshotsExpiringExecutor());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getOrphanFilesCleaningExecutor());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getOptimizingCommitExecutor());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getOptimizingExpiringExecutor());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getBlockerExpiringExecutor());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getHiveCommitSyncExecutor());
    tableService.addHandlerChain(AsyncTableExecutors.getInstance().getTableRefreshingExecutor());
    tableService.initialize();
    LOG.info("AMS table service have been initialized");

    initThriftService();
    startThriftService();
    startHttpService();
  }

  public void dispose() {
    server.stop();
    dashboardServer.stopRestServer();
    tableService.dispose();
    tableService = null;
    optimizingService = null;
  }

  public boolean isStarted() {
    return server != null && server.isServing();
  }

  @VisibleForTesting
  void setConfig(ConfigOption option, Object value) {
    serviceConfig.set(option, value);
  }

  @VisibleForTesting
  TableService getTableService() {
    return this.tableService;
  }

  @VisibleForTesting
  DefaultOptimizingService getOptimizingService() {
    return this.optimizingService;
  }

  private void initConfig() throws IllegalConfigurationException, FileNotFoundException {
    LOG.info("initializing configurations...");
    new ConfigurationHelper().init();
  }

  private void startThriftService() {
    Thread thread = new Thread(() -> {
      server.serve();
      LOG.info("Thrift services have been started");
    }, "Thrift-server-thread");
    thread.setDaemon(true);
    thread.start();
  }

  private void startHttpService() {
    LOG.info("Initializing dashboard service...");
    dashboardServer = new DashboardServer(serviceConfig, tableService, optimizingService);
    dashboardServer.startRestServer();
    LOG.info("Dashboard service has been started on port: " +
        serviceConfig.getInteger(ArcticManagementConf.HTTP_SERVER_PORT));
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
            new TableManagementService(tableService), ArcticRuntimeException::transformCompatibleException));
    processor.registerProcessor(Constants.THRIFT_TABLE_SERVICE_NAME, tableMetastoreProcessor);

    OptimizingService.Processor<OptimizingService.Iface> optimizerManagerProcessor =
        new OptimizingService.Processor<>(ThriftServiceProxy.createProxy(OptimizingService.Iface.class,
            optimizingService, ArcticRuntimeException::transform2ArcticException));
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
    server = new TThreadedSelectorServer(args);
    LOG.info("Initialized the thrift server on port [" + port + "]...");
    LOG.info("Options.thriftWorkerThreads = " + workerThreads);
    LOG.info("Options.thriftSelectorThreads = " + selectorThreads);
    LOG.info("Options.queueSizePerSelector = " + queueSizePerSelector);
  }

  private class ConfigurationHelper {

    private JSONObject yamlConfig;

    public void init() throws IllegalConfigurationException, FileNotFoundException {
      initServiceConfig();
      initContainerConfig();
      initResourceGroupConfig();
    }

    @SuppressWarnings("unchecked")
    private void initResourceGroupConfig() throws IllegalConfigurationException {
      LOG.info("initializing resource group configuration...");
      JSONArray optimizeGroups = yamlConfig.getJSONArray(ArcticManagementConf.OPTIMIZER_GROUP_LIST);
      for (int i = 0; i < optimizeGroups.size(); i++) {
        JSONObject groupConfig = optimizeGroups.getJSONObject(i);
        ResourceGroup.Builder groupBuilder = new ResourceGroup.Builder(
            groupConfig.getString(ArcticManagementConf.OPTIMIZER_GROUP_NAME),
            groupConfig.getString(ArcticManagementConf.OPTIMIZER_GROUP_CONTAINER));
        if (!ResourceContainers.contains(groupBuilder.getContainer())) {
          throw new IllegalConfigurationException(
              "can not find such container config named" +
                  groupBuilder.getContainer());
        }
        if (groupConfig.containsKey(ArcticManagementConf.OPTIMIZER_GROUP_PROPERTIES)) {
          groupBuilder.addProperties(groupConfig.getObject(ArcticManagementConf.OPTIMIZER_GROUP_PROPERTIES,
              Map.class));
        }
        resourceGroups.add(groupBuilder.build());
      }
    }

    @SuppressWarnings("unchecked")
    private void initServiceConfig() throws FileNotFoundException {
      LOG.info("initializing service configuration...");
      String configPath = Environments.getArcticHome() + SERVER_CONFIG_PATH;
      yamlConfig = new JSONObject(new Yaml().loadAs(new FileInputStream(configPath), Map.class));
      JSONObject systemConfig = yamlConfig.getJSONObject(ArcticManagementConf.SYSTEM_CONFIG);
      Map<String, Object> expandedConfigurationMap = Maps.newHashMap();
      expandConfigMap(systemConfig, "", expandedConfigurationMap);
      validateConfig(expandedConfigurationMap);
      serviceConfig = Configurations.fromObjectMap(expandedConfigurationMap);
      SqlSessionFactoryProvider.getInstance().init(serviceConfig);
    }

    private void validateConfig(Map<String, Object> systemConfig) {
      if (!systemConfig.containsKey(ArcticManagementConf.SERVER_EXPOSE_HOST.key())) {
        throw new RuntimeException(
            "configuration " + ArcticManagementConf.SERVER_EXPOSE_HOST.key() + " must be set");
      }
      InetAddress inetAddress = AmsUtil.lookForBindHost(
          (String) systemConfig.get(ArcticManagementConf.SERVER_EXPOSE_HOST.key()));
      systemConfig.put(ArcticManagementConf.SERVER_EXPOSE_HOST.key(), inetAddress.getHostAddress());

      //mysql config
      if (((String) systemConfig.get(ArcticManagementConf.DB_TYPE.key()))
          .equalsIgnoreCase(ArcticManagementConf.DB_TYPE_MYSQL)) {
        if (!systemConfig.containsKey(ArcticManagementConf.DB_PASSWORD.key()) ||
            !systemConfig.containsKey(ArcticManagementConf.DB_USER_NAME.key())) {
          throw new RuntimeException("username and password must be configured if the database type is mysql");
        }
      }

      //HA config
      if (systemConfig.containsKey(ArcticManagementConf.HA_ENABLE.key()) &&
          ((Boolean) systemConfig.get(ArcticManagementConf.HA_ENABLE.key()))) {
        if (!systemConfig.containsKey(ArcticManagementConf.HA_ZOOKEEPER_ADDRESS.key())) {
          throw new RuntimeException(
              ArcticManagementConf.HA_ZOOKEEPER_ADDRESS.key() + " must be configured when you enable " +
                  "the ams high availability");
        }
      }
    }

    @SuppressWarnings("unchecked")
    private void initContainerConfig() throws IllegalConfigurationException {
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
}