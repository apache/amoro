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

package org.apache.amoro.server;

import static org.apache.amoro.server.AmoroManagementConf.USE_MASTER_SLAVE_MODE;

import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import io.javalin.http.staticfiles.Location;
import org.apache.amoro.Constants;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.api.AmoroTableMetastore;
import org.apache.amoro.api.OptimizingService;
import org.apache.amoro.config.ConfigHelpers;
import org.apache.amoro.config.ConfigurationException;
import org.apache.amoro.config.ConfigurationManager;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.DynamicConfigurations;
import org.apache.amoro.config.shade.utils.ConfigShadeUtils;
import org.apache.amoro.exception.AmoroRuntimeException;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.DefaultCatalogManager;
import org.apache.amoro.server.config.DbConfigurationManager;
import org.apache.amoro.server.config.DynamicConfigStore;
import org.apache.amoro.server.dashboard.DashboardServer;
import org.apache.amoro.server.dashboard.JavalinJsonMapper;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.dashboard.utils.CommonUtil;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.server.ha.HighAvailabilityContainerFactory;
import org.apache.amoro.server.manager.EventsManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.persistence.DataSourceFactory;
import org.apache.amoro.server.persistence.HttpSessionHandlerFactory;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.process.ProcessService;
import org.apache.amoro.server.resource.ContainerMetadata;
import org.apache.amoro.server.resource.Containers;
import org.apache.amoro.server.resource.DefaultOptimizerManager;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.scheduler.inline.InlineTableExecutors;
import org.apache.amoro.server.table.DefaultTableManager;
import org.apache.amoro.server.table.DefaultTableService;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntimeFactoryManager;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.terminal.TerminalManager;
import org.apache.amoro.server.utils.ThriftServiceProxy;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.thrift.org.apache.thrift.TMultiplexedProcessor;
import org.apache.amoro.shade.thrift.org.apache.thrift.TProcessor;
import org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocolFactory;
import org.apache.amoro.shade.thrift.org.apache.thrift.server.THsHaServer;
import org.apache.amoro.shade.thrift.org.apache.thrift.server.TServer;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TTransportException;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TTransportFactory;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.layered.TFramedTransport;
import org.apache.amoro.utils.IcebergThreadPools;
import org.apache.amoro.utils.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AmoroServiceContainer {

  public static final Logger LOG = LoggerFactory.getLogger(AmoroServiceContainer.class);

  public static final String SERVER_CONFIG_FILENAME = "config.yaml";
  private static boolean IS_MASTER_SLAVE_MODE = false;

  private final HighAvailabilityContainer haContainer;
  private DataSource dataSource;
  private CatalogManager catalogManager;
  private TableManager tableManager;
  private OptimizerManager optimizerManager;
  private TableService tableService;
  private DefaultOptimizingService optimizingService;
  private ProcessService processService;
  private TerminalManager terminalManager;
  private Configurations serviceConfig;
  private ConfigurationManager configurationManager;
  private DynamicConfigurations dynamicConfigurations;
  private TServer tableManagementServer;
  private TServer optimizingServiceServer;
  private Javalin httpServer;
  private AmsServiceMetrics amsServiceMetrics;
  private HAState haState = HAState.INITIALIZING;

  public AmoroServiceContainer() throws Exception {
    initConfig();
    haContainer = HighAvailabilityContainerFactory.create(serviceConfig);
  }

  public static void main(String[] args) {
    try {
      AmoroServiceContainer service = new AmoroServiceContainer();
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    LOG.info("AMS service is shutting down...");
                    service.dispose();
                    LOG.info("AMS service has been shut down");
                  }));
      service.startRestServices();
      if (IS_MASTER_SLAVE_MODE) {
        // Even if one does not become the master, it cannot block the subsequent logic.
        service.registAndElect();
        // Regardless of whether tp becomes the master, the service needs to be activated.
        service.startOptimizingService();
      } else {
        while (true) {
          try {
            // Used to block AMS instances that have not acquired leadership
            service.waitLeaderShip();
            service.transitionToLeader();
            // Used to block AMS instances that have acquired leadership
            service.waitFollowerShip();
          } catch (ConfigurationException e) {
            LOG.error("AMS will exit...", e);
            System.exit(1);
          } catch (Exception e) {
            LOG.error("AMS start error", e);
          } finally {
            service.transitionToFollower();
          }
        }
      }
    } catch (Throwable t) {
      LOG.error("AMS encountered an unknown exception, will exit...", t);
      System.exit(1);
    }
  }

  public void registAndElect() throws Exception {
    haContainer.registAndElect();
  }

  public enum HAState {
    INITIALIZING(0),
    FOLLOWER(1),
    LEADER(2);

    private int code;

    HAState(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

  public HAState getHaState() {
    return haState;
  }

  public void waitLeaderShip() throws Exception {
    haContainer.waitLeaderShip();
  }

  public void waitFollowerShip() throws Exception {
    haContainer.waitFollowerShip();
  }

  public void startRestServices() throws Exception {
    EventsManager.getInstance(serviceConfig, configurationManager);
    MetricManager.getInstance(serviceConfig, configurationManager);

    catalogManager = new DefaultCatalogManager(serviceConfig);
    tableManager = new DefaultTableManager(serviceConfig, catalogManager);
    optimizerManager = new DefaultOptimizerManager(serviceConfig, catalogManager);
    if (dynamicConfigurations != null) {
      terminalManager = new TerminalManager(dynamicConfigurations, catalogManager);
    } else {
      terminalManager = new TerminalManager(serviceConfig, catalogManager);
    }

    initHttpService();
    startHttpService();
    registerAmsServiceMetric();
  }

  public void transitionToLeader() throws Exception {
    if (haState == HAState.LEADER) {
      return;
    }
    startOptimizingService();
    haState = HAState.LEADER;
  }

  public void transitionToFollower() {
    if (haState == HAState.FOLLOWER) {
      return;
    }
    haState = HAState.FOLLOWER;
    disposeOptimizingService();
  }

  public void startOptimizingService() throws Exception {
    TableRuntimeFactoryManager tableRuntimeFactoryManager =
        new TableRuntimeFactoryManager(serviceConfig, configurationManager);
    tableRuntimeFactoryManager.initialize();

    if (dynamicConfigurations != null) {
      tableService =
          new DefaultTableService(
              dynamicConfigurations, catalogManager, tableRuntimeFactoryManager);
      optimizingService =
          new DefaultOptimizingService(
              dynamicConfigurations, catalogManager, optimizerManager, tableService);
      processService = new ProcessService(dynamicConfigurations, tableService);
    } else {
      tableService =
          new DefaultTableService(serviceConfig, catalogManager, tableRuntimeFactoryManager);
      optimizingService =
          new DefaultOptimizingService(
              serviceConfig, catalogManager, optimizerManager, tableService);
      processService = new ProcessService(serviceConfig, tableService);
    }

    LOG.info("Setting up AMS table executors...");
    InlineTableExecutors.getInstance().setup(tableService, serviceConfig);
    addHandlerChain(optimizingService.getTableRuntimeHandler());
    addHandlerChain(processService.getTableHandlerChain());
    addHandlerChain(InlineTableExecutors.getInstance().getDataExpiringExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getSnapshotsExpiringExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getOrphanFilesCleaningExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getDanglingDeleteFilesCleaningExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getOptimizingCommitExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getOptimizingExpiringExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getBlockerExpiringExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getHiveCommitSyncExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getTableRefreshingExecutor());
    addHandlerChain(InlineTableExecutors.getInstance().getTagsAutoCreatingExecutor());
    tableService.initialize();
    LOG.info("AMS table service have been initialized");
    tableManager.setTableService(tableService);

    initThriftService();
    startThriftService();
  }

  private void addHandlerChain(RuntimeHandlerChain chain) {
    if (chain != null) {
      tableService.addHandlerChain(chain);
    }
  }

  public void disposeOptimizingService() {
    if (tableManagementServer != null) {
      LOG.info(
          "Stopping table management server[serving:{}] ...", tableManagementServer.isServing());
      tableManagementServer.stop();
    }
    if (optimizingServiceServer != null) {
      LOG.info("Stopping optimizing server[serving:{}] ...", optimizingServiceServer.isServing());
      optimizingServiceServer.stop();
    }
    if (tableService != null) {
      LOG.info("Stopping table service...");
      tableService.dispose();
      tableService = null;
    }
    if (optimizingService != null) {
      LOG.info("Stopping optimizing service...");
      optimizingService.dispose();
      optimizingService = null;
    }
    if (processService != null) {
      LOG.info("Stopping process server...");
      processService.dispose();
    }
  }

  public void disposeRestService() {
    if (httpServer != null) {
      LOG.info("Stopping http server...");
      try {
        httpServer.close();
      } catch (Exception e) {
        LOG.error("Error stopping http server", e);
      }
    }
    if (terminalManager != null) {
      LOG.info("Stopping terminal manager...");
      terminalManager.dispose();
      terminalManager = null;
    }
    if (amsServiceMetrics != null) {
      amsServiceMetrics.unregister();
    }

    EventsManager.dispose();
    MetricManager.dispose();
  }

  public void dispose() {
    disposeOptimizingService();
    disposeRestService();
    if (configurationManager != null) {
      configurationManager.stop();
      configurationManager = null;
      dynamicConfigurations = null;
    }
  }

  private void initConfig() throws Exception {
    LOG.info("initializing configurations...");
    new ConfigurationHelper().init();
    IS_MASTER_SLAVE_MODE = serviceConfig.getBoolean(USE_MASTER_SLAVE_MODE);
  }

  public Configurations getServiceConfig() {
    return serviceConfig;
  }

  private void startThriftService() {
    startThriftServer(tableManagementServer, "thrift-table-management-server-thread");
    startThriftServer(optimizingServiceServer, "thrift-optimizing-server-thread");
  }

  private void startThriftServer(TServer server, String threadName) {
    Thread thread = new Thread(server::serve, threadName);
    thread.setDaemon(true);
    thread.start();
    LOG.info("{} has been started", threadName);
  }

  private void initHttpService() {
    DashboardServer dashboardServer =
        new DashboardServer(
            serviceConfig, catalogManager, tableManager, optimizerManager, terminalManager, this);
    RestCatalogService restCatalogService = new RestCatalogService(catalogManager, tableManager);

    httpServer =
        Javalin.create(
            config -> {
              config.addStaticFiles(dashboardServer.configStaticFiles());
              config.addStaticFiles("/META-INF/resources/webjars", Location.CLASSPATH);
              config.sessionHandler(
                  () -> HttpSessionHandlerFactory.createSessionHandler(dataSource, serviceConfig));
              config.enableCorsForAllOrigins();
              config.jsonMapper(JavalinJsonMapper.createDefaultJsonMapper());
              config.showJavalinBanner = false;
              config.enableWebjars();
            });

    httpServer.routes(
        () -> {
          dashboardServer.endpoints().addEndpoints();
          restCatalogService.endpoints().addEndpoints();
        });

    httpServer.before(
        ctx -> {
          String token = ctx.queryParam("token");
          if (StringUtils.isNotEmpty(token)) {
            CommonUtil.checkSinglePageToken(ctx);
          } else {
            dashboardServer.preHandleRequest(ctx);
          }
        });
    httpServer.exception(
        Exception.class,
        (e, ctx) -> {
          if (restCatalogService.needHandleException(ctx)) {
            restCatalogService.handleException(e, ctx);
          } else {
            dashboardServer.handleException(e, ctx);
          }
        });
    // default response handle
    httpServer.error(
        HttpCode.NOT_FOUND.getStatus(),
        ctx -> {
          if (!restCatalogService.needHandleException(ctx)) {
            ctx.json(new ErrorResponse(HttpCode.NOT_FOUND, "page not found!", ""));
          }
        });

    httpServer.error(
        HttpCode.INTERNAL_SERVER_ERROR.getStatus(),
        ctx -> {
          if (!restCatalogService.needHandleException(ctx)) {
            ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "internal error!", ""));
          }
        });
  }

  private void startHttpService() {
    int port = serviceConfig.getInteger(AmoroManagementConf.HTTP_SERVER_PORT);
    httpServer.start(port);

    LOG.info(
        "\n"
            + "    ___     __  ___ ____   ____   ____ \n"
            + "   /   |   /  |/  // __ \\ / __ \\ / __ \\\n"
            + "  / /| |  / /|_/ // / / // /_/ // / / /\n"
            + " / ___ | / /  / // /_/ // _, _// /_/ / \n"
            + "/_/  |_|/_/  /_/ \\____//_/ |_| \\____/  \n"
            + "                                       \n"
            + "      https://amoro.apache.org/       \n");

    LOG.info("Http server start at {}.", port);
  }

  private void registerAmsServiceMetric() {
    amsServiceMetrics =
        new AmsServiceMetrics(MetricManager.getInstance().getGlobalRegistry(), this);
    amsServiceMetrics.register();
  }

  private void initThriftService() throws TTransportException {
    LOG.info("Initializing thrift service...");
    long maxMessageSize = serviceConfig.get(AmoroManagementConf.THRIFT_MAX_MESSAGE_SIZE).getBytes();
    int selectorThreads = serviceConfig.getInteger(AmoroManagementConf.THRIFT_SELECTOR_THREADS);
    int workerThreads = serviceConfig.getInteger(AmoroManagementConf.THRIFT_WORKER_THREADS);
    int queueSizePerSelector =
        serviceConfig.getInteger(AmoroManagementConf.THRIFT_QUEUE_SIZE_PER_THREAD);
    String bindHost = serviceConfig.getString(AmoroManagementConf.SERVER_BIND_HOST);

    AmoroTableMetastore.Processor<AmoroTableMetastore.Iface> tableManagementProcessor =
        new AmoroTableMetastore.Processor<>(
            ThriftServiceProxy.createProxy(
                AmoroTableMetastore.Iface.class,
                new TableManagementService(catalogManager, tableManager),
                AmoroRuntimeException::normalizeCompatibly));
    tableManagementServer =
        createThriftServer(
            tableManagementProcessor,
            Constants.THRIFT_TABLE_SERVICE_NAME,
            bindHost,
            serviceConfig.getInteger(AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT),
            Executors.newFixedThreadPool(
                workerThreads, getThriftThreadFactory(Constants.THRIFT_TABLE_SERVICE_NAME)),
            selectorThreads,
            queueSizePerSelector,
            maxMessageSize);

    OptimizingService.Processor<OptimizingService.Iface> optimizingProcessor =
        new OptimizingService.Processor<>(
            ThriftServiceProxy.createProxy(
                OptimizingService.Iface.class,
                optimizingService,
                AmoroRuntimeException::normalize));
    optimizingServiceServer =
        createThriftServer(
            optimizingProcessor,
            Constants.THRIFT_OPTIMIZING_SERVICE_NAME,
            bindHost,
            serviceConfig.getInteger(AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT),
            Executors.newCachedThreadPool(
                getThriftThreadFactory(Constants.THRIFT_OPTIMIZING_SERVICE_NAME)),
            selectorThreads,
            queueSizePerSelector,
            maxMessageSize);
  }

  private TServer createThriftServer(
      TProcessor processor,
      String processorName,
      String bindHost,
      int port,
      ExecutorService executorService,
      int selectorThreads,
      int queueSizePerSelector,
      long maxMessageSize)
      throws TTransportException {
    LOG.info("Initializing thrift server: {}", processorName);
    LOG.info("Starting {} thrift server on port: {}", processorName, port);
    TNonblockingServerSocket serverTransport = getServerSocket(bindHost, port);
    final TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
    final TProtocolFactory inputProtoFactory =
        new TBinaryProtocol.Factory(true, true, maxMessageSize, maxMessageSize);
    TTransportFactory transportFactory = new TFramedTransport.Factory();
    TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();
    multiplexedProcessor.registerProcessor(processorName, processor);
    THsHaServer.Args args =
        new THsHaServer.Args(serverTransport)
            .processor(multiplexedProcessor)
            .transportFactory(transportFactory)
            .protocolFactory(protocolFactory)
            .inputProtocolFactory(inputProtoFactory)
            .executorService(executorService);
    LOG.info(
        "The number of selector threads for the {} thrift server is: {}",
        processorName,
        selectorThreads);
    LOG.info(
        "The size of per-selector queue for the {} thrift server is: {}",
        processorName,
        queueSizePerSelector);
    return new THsHaServer(args);
  }

  private ThreadFactory getThriftThreadFactory(String processorName) {
    return new ThreadFactoryBuilder()
        .setDaemon(false)
        .setNameFormat(
            "thrift-server-"
                + String.join("-", StringUtils.splitByCharacterTypeCamelCase(processorName))
                    .toLowerCase(Locale.ROOT)
                + "-%d")
        .build();
  }

  private class ConfigurationHelper {

    private JsonNode yamlConfig;

    public void init() throws Exception {
      Map<String, Object> envConfig = initEnvConfig();
      initServiceConfig(envConfig);
      initDynamicConfiguration();
      setIcebergSystemProperties();
      initContainerConfig();
    }

    private void initServiceConfig(Map<String, Object> envConfig) throws Exception {
      LOG.info("initializing service configuration...");
      String configPath = Environments.getConfigPath() + "/" + SERVER_CONFIG_FILENAME;
      LOG.info("load config from path: {}", configPath);
      yamlConfig =
          JacksonUtil.fromObjects(
              new Yaml().loadAs(Files.newInputStream(Paths.get(configPath)), Map.class));
      Map<String, Object> systemConfig =
          JacksonUtil.getMap(
              yamlConfig,
              AmoroManagementConf.SYSTEM_CONFIG,
              new TypeReference<Map<String, Object>>() {});
      Map<String, Object> expandedConfigurationMap = Maps.newHashMap();
      expandConfigMap(systemConfig, "", expandedConfigurationMap);
      // If same configurations in files and environment variables, environment variables have
      // higher priority.
      expandedConfigurationMap.putAll(envConfig);
      // Decrypt the sensitive configurations if specified
      expandedConfigurationMap = ConfigShadeUtils.decryptConfig(expandedConfigurationMap);
      serviceConfig = Configurations.fromObjectMap(expandedConfigurationMap);
      AmoroManagementConfValidator.validateConfig(serviceConfig);
      dataSource = DataSourceFactory.createDataSource(serviceConfig);
      SqlSessionFactoryProvider.getInstance().init(dataSource);
    }

    private void initDynamicConfiguration() {
      boolean dynamicEnabled = serviceConfig.getBoolean(AmoroManagementConf.DYNAMIC_CONFIG_ENABLED);
      if (!dynamicEnabled) {
        LOG.info(
            "Dynamic configuration is disabled by {}",
            AmoroManagementConf.DYNAMIC_CONFIG_ENABLED.key());
        return;
      }
      java.time.Duration refreshInterval =
          serviceConfig.get(AmoroManagementConf.DYNAMIC_CONFIG_REFRESH_INTERVAL);
      configurationManager = new DbConfigurationManager(new DynamicConfigStore(), refreshInterval);
      configurationManager.start();
      dynamicConfigurations = new DynamicConfigurations(serviceConfig, configurationManager);
      LOG.info(
          "Dynamic configuration enabled for AMS service with refresh interval {} ms",
          refreshInterval.toMillis());
    }

    private Map<String, Object> initEnvConfig() {
      LOG.info("initializing system env configuration...");
      Map<String, String> envs = System.getenv();
      envs.forEach((k, v) -> LOG.info("export {}={}", k, v));
      String prefix = AmoroManagementConf.SYSTEM_CONFIG.toUpperCase();
      return ConfigHelpers.convertConfigurationKeys(prefix, System.getenv());
    }

    /** Override the value of {@link SystemProperties}. */
    private void setIcebergSystemProperties() {
      int workerThreadPoolSize =
          Math.max(
              Runtime.getRuntime().availableProcessors() / 2,
              serviceConfig.getInteger(AmoroManagementConf.TABLE_MANIFEST_IO_THREAD_COUNT));
      System.setProperty(
          SystemProperties.WORKER_THREAD_POOL_SIZE_PROP, String.valueOf(workerThreadPoolSize));
      int planningThreadPoolSize =
          Math.max(
              Runtime.getRuntime().availableProcessors() / 2,
              serviceConfig.getInteger(
                  AmoroManagementConf.TABLE_MANIFEST_IO_PLANNING_THREAD_COUNT));
      int commitThreadPoolSize =
          Math.max(
              Runtime.getRuntime().availableProcessors() / 2,
              serviceConfig.getInteger(AmoroManagementConf.TABLE_MANIFEST_IO_COMMIT_THREAD_COUNT));
      IcebergThreadPools.init(planningThreadPoolSize, commitThreadPoolSize);
    }

    private void initContainerConfig() {
      LOG.info("initializing container configuration...");
      JsonNode containers = yamlConfig.get(AmoroManagementConf.CONTAINER_LIST);
      List<ContainerMetadata> containerList = new ArrayList<>();
      if (containers != null && containers.isArray()) {
        for (final JsonNode containerConfig : containers) {
          ContainerMetadata container =
              new ContainerMetadata(
                  containerConfig.get(AmoroManagementConf.CONTAINER_NAME).asText(),
                  containerConfig.get(AmoroManagementConf.CONTAINER_IMPL).asText());

          Map<String, String> containerProperties =
              new HashMap<>(
                  JacksonUtil.getMap(
                      containerConfig,
                      AmoroManagementConf.CONTAINER_PROPERTIES,
                      new TypeReference<Map<String, String>>() {}));

          // put properties in config.yaml first.
          containerProperties.put(OptimizerProperties.AMS_HOME, Environments.getHomePath());
          containerProperties.putIfAbsent(
              OptimizerProperties.AMS_OPTIMIZER_URI,
              AmsUtil.getAMSThriftAddress(serviceConfig, Constants.THRIFT_OPTIMIZING_SERVICE_NAME));
          // put addition system properties
          container.setProperties(containerProperties);
          containerList.add(container);
        }
      }
      Containers.init(containerList);
    }
  }

  private TNonblockingServerSocket getServerSocket(String bindHost, int portNum)
      throws TTransportException {
    InetSocketAddress serverAddress;
    serverAddress = new InetSocketAddress(bindHost, portNum);
    return new TNonblockingServerSocket(serverAddress);
  }

  @SuppressWarnings("unchecked")
  @VisibleForTesting
  public static void expandConfigMap(
      Map<String, Object> config, String prefix, Map<String, Object> result) {
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

  @VisibleForTesting
  public CatalogManager getCatalogManager() {
    return this.catalogManager;
  }

  @VisibleForTesting
  public OptimizerManager getOptimizerManager() {
    return this.optimizerManager;
  }
}
