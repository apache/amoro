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

package com.netease.arctic.ams.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.InvalidObjectException;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OptimizeManager;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.config.ConfigFileProperties;
import com.netease.arctic.ams.server.config.Configuration;
import com.netease.arctic.ams.server.handler.impl.ArcticTableMetastoreHandler;
import com.netease.arctic.ams.server.handler.impl.OptimizeManagerHandler;
import com.netease.arctic.ams.server.model.Container;
import com.netease.arctic.ams.server.model.OptimizeQueueMeta;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.DerbyService;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.service.impl.OptimizeExecuteService;
import com.netease.arctic.ams.server.service.impl.RuntimeDataExpireService;
import com.netease.arctic.ams.server.utils.SecurityUtils;
import com.netease.arctic.ams.server.utils.ThreadPool;
import com.netease.arctic.ams.server.utils.YamlUtils;
import org.apache.hadoop.util.StringUtils;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ArcticMetaStore {
  public static final Logger LOG = LoggerFactory.getLogger(ArcticMetaStore.class);

  public static Configuration conf;
  private static JSONObject yamlConfig;

  public static void main(String[] args) throws Throwable {
    try {
      String configPath = System.getenv(ArcticMetaStoreConf.ARCTIC_HOME.key()) + "/conf/config.yaml";
      yamlConfig = YamlUtils.load(configPath);
      startMetaStore(initSystemConfig());
    } catch (Throwable t) {
      LOG.error("MetaStore Thrift Server threw an exception...", t);
      throw t;
    }
  }

  public static void startMetaStore(Configuration conf) throws Throwable {
    try {
      ArcticMetaStore.conf = conf;
      long maxMessageSize = conf.getLong(ArcticMetaStoreConf.SERVER_MAX_MESSAGE_SIZE);
      int minWorkerThreads = conf.getInteger(ArcticMetaStoreConf.SERVER_MIN_THREADS);
      int maxWorkerThreads = conf.getInteger(ArcticMetaStoreConf.SERVER_MAX_THREADS);
      boolean useCompactProtocol = conf.get(ArcticMetaStoreConf.USE_THRIFT_COMPACT_PROTOCOL);
      int port = conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);

      if (conf.getString(ArcticMetaStoreConf.DB_TYPE).equals("derby")) {
        DerbyService derbyService = new DerbyService();
        derbyService.createTable();
      }

      LOG.info("Starting arctic metastore on port " + port);

      TMultiplexedProcessor processor = new TMultiplexedProcessor();
      final TProtocolFactory protocolFactory;
      final TProtocolFactory inputProtoFactory;
      if (useCompactProtocol) {
        protocolFactory = new TCompactProtocol.Factory();
        inputProtoFactory = new TCompactProtocol.Factory(maxMessageSize, maxMessageSize);
      } else {
        protocolFactory = new TBinaryProtocol.Factory();
        inputProtoFactory = new TBinaryProtocol.Factory(true, true, maxMessageSize, maxMessageSize);
      }

      ArcticTableMetastore.Processor<ArcticTableMetastoreHandler> tableMetastoreProcessor =
          new ArcticTableMetastore.Processor<>(
              ServiceContainer.getTableMetastoreHandler());
      processor.registerProcessor("TableMetastore", tableMetastoreProcessor);

      // register OptimizeManager
      OptimizeManager.Processor<OptimizeManagerHandler> optimizeManagerProcessor =
          new OptimizeManager.Processor<>(ServiceContainer.getOptimizeManagerHandler());
      processor.registerProcessor("OptimizeManager", optimizeManagerProcessor);

      TNonblockingServerSocket serverTransport = SecurityUtils.getServerSocket("0.0.0.0", port);
      TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverTransport)
          .processor(processor)
          .transportFactory(new TFramedTransport.Factory())
          .protocolFactory(protocolFactory)
          .inputProtocolFactory(inputProtoFactory)
          .workerThreads(maxWorkerThreads);
      TServer server = new TThreadedSelectorServer(args);
      LOG.info("Started the new meta server on port [" + port + "]...");
      LOG.info("Options.minWorkerThreads = " + minWorkerThreads);
      LOG.info("Options.maxWorkerThreads = " + maxWorkerThreads);

      // start meta store worker thread
      Lock metaStoreThreadsLock = new ReentrantLock();
      Condition startCondition = metaStoreThreadsLock.newCondition();
      AtomicBoolean startedServing = new AtomicBoolean();
      ThreadPool.initialize(conf);
      initCatalogConfig();
      initContainerConfig();
      initOptimizeGroupConfig();
      startMetaStoreThreads(conf, metaStoreThreadsLock, startCondition, startedServing);
      signalOtherThreadsToStart(server, metaStoreThreadsLock, startCondition, startedServing);
      syncAndExpiredFileInfoCache(server);
      server.serve();
    } catch (Throwable t) {
      LOG.error("ams start error", t);
      throw t;
    }
  }

  private static void startMetaStoreThreads(
      Configuration conf,
      final Lock startLock,
      final Condition startCondition,
      final AtomicBoolean startedServing) {
    Integer httpPort = conf.getInteger(ArcticMetaStoreConf.HTTP_SERVER_PORT);

    Thread t = new Thread(() -> {
      startLock.lock();
      try {
        while (!startedServing.get()) {
          startCondition.await();
        }

        startOptimizeCheck(conf.getLong(ArcticMetaStoreConf.OPTIMIZE_CHECK_STATUS_INTERVAL));
        startOptimizeCommit();
        startExpiredClean();
        startOrphanClean();
        monitorOptimizerStatus();
        tableRuntimeDataExpire();
        AmsRestServer.startRestServer(httpPort);
      } catch (Throwable t1) {
        LOG.error("Failure when starting the worker threads, compact、checker、clean may not happen, " +
            StringUtils.stringifyException(t1));
      } finally {
        startLock.unlock();
      }
    });

    t.setName("Metastore threads starter thread");
    t.start();
  }

  private static void signalOtherThreadsToStart(
      final TServer server, final Lock startLock,
      final Condition startCondition,
      final AtomicBoolean startedServing) {
    // A simple thread to wait until the server has started and then signal the other threads to
    // begin
    Thread t = new Thread(() -> {
      do {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOG.warn("Signalling thread was interrupted: " + e.getMessage());
        }
      } while (!server.isServing());
      startLock.lock();
      try {
        startedServing.set(true);
        startCondition.signalAll();
        LOG.info("service has started succefully!!!");
      } finally {
        startLock.unlock();
      }
    });
    t.start();
  }

  private static void startOptimizeCheck(final long checkInterval) {
    ThreadPool.getPool(ThreadPool.Type.OPTIMIZE_CHECK).scheduleWithFixedDelay(
        () -> ServiceContainer.getOptimizeService().checkOptimizeCheckTasks(checkInterval),
        3 * 1000L,
        60 * 1000L,
        TimeUnit.MILLISECONDS);
  }

  private static void startOptimizeCommit() {
    ThreadPool.getPool(ThreadPool.Type.COMMIT).scheduleWithFixedDelay(
        ServiceContainer.getOptimizeService()::checkOptimizeCommitTasks,
        3 * 1000L,
        60 * 1000L,
        TimeUnit.MILLISECONDS);
  }

  private static void startExpiredClean() {
    ThreadPool.getPool(ThreadPool.Type.EXPIRE).scheduleWithFixedDelay(
        ServiceContainer.getTableExpireService()::checkTableExpireTasks,
        3 * 1000L,
        60 * 1000L,
        TimeUnit.MILLISECONDS);
  }

  private static void startOrphanClean() {
    ThreadPool.getPool(ThreadPool.Type.ORPHAN).scheduleWithFixedDelay(
        ServiceContainer.getOrphanFilesCleanService()::checkOrphanFilesCleanTasks,
        3 * 1000L,
        60 * 1000L,
        TimeUnit.MILLISECONDS);
  }

  private static void monitorOptimizerStatus() {
    OptimizeExecuteService.OptimizerMonitor monitor = new OptimizeExecuteService.OptimizerMonitor();
    ThreadPool.getPool(ThreadPool.Type.OPTIMIZER_MONITOR).scheduleWithFixedDelay(
        monitor::monitorStatus,
        3 * 1000L,
        60 * 1000L,
        TimeUnit.MILLISECONDS);
  }

  private static void tableRuntimeDataExpire() {
    RuntimeDataExpireService runtimeDataExpireService = ServiceContainer.getRuntimeDataExpireService();
    ThreadPool.getPool(ThreadPool.Type.TABLE_RUNTIME_DATA_EXPIRE).scheduleWithFixedDelay(
        runtimeDataExpireService::doExpire,
        3 * 1000L,
        60 * 60 * 1000L,
        TimeUnit.MILLISECONDS);
  }

  private static void syncAndExpiredFileInfoCache(final TServer server) {
    Thread t = new Thread(() -> {
      while (true) {
        while (server.isServing()) {
          try {
            FileInfoCacheService.SyncAndExpireFileCacheTask task =
                new FileInfoCacheService.SyncAndExpireFileCacheTask();
            task.doTask();
          } catch (Exception e) {
            LOG.error("sync and expired file info cache error", e);
          }
          try {
            Thread.sleep(5 * 60 * 1000);
          } catch (InterruptedException e) {
            LOG.warn("sync and expired file info cache thread was interrupted: " + e.getMessage());
          }
        }
        try {
          Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
          LOG.warn("sync and expired file info cache thread was interrupted: " + e.getMessage());
        }
      }
    });
    t.start();
  }

  private static Configuration initSystemConfig() {
    JSONObject systemConfig = yamlConfig.getJSONObject(ConfigFileProperties.SYSTEM_CONFIG);
    Configuration config = new Configuration();
    config.setString(ArcticMetaStoreConf.ARCTIC_HOME, System.getenv(ArcticMetaStoreConf.ARCTIC_HOME.key()));
    config.setInteger(
        ArcticMetaStoreConf.THRIFT_BIND_PORT, systemConfig.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT.key()));
    config.setString(
        ArcticMetaStoreConf.THRIFT_BIND_HOST, systemConfig.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST.key()));
    config.setInteger(
        ArcticMetaStoreConf.HTTP_SERVER_PORT, systemConfig.getInteger(ArcticMetaStoreConf.HTTP_SERVER_PORT.key()));
    config.setInteger(
        ArcticMetaStoreConf.OPTIMIZE_CHECK_THREAD_POOL_SIZE,
        systemConfig.getInteger(ArcticMetaStoreConf.OPTIMIZE_CHECK_THREAD_POOL_SIZE.key()));
    config.setInteger(
        ArcticMetaStoreConf.OPTIMIZE_COMMIT_THREAD_POOL_SIZE,
        systemConfig.getInteger(ArcticMetaStoreConf.OPTIMIZE_COMMIT_THREAD_POOL_SIZE.key()));
    config.setInteger(
        ArcticMetaStoreConf.EXPIRE_THREAD_POOL_SIZE,
        systemConfig.getInteger(ArcticMetaStoreConf.EXPIRE_THREAD_POOL_SIZE.key()));
    config.setInteger(
        ArcticMetaStoreConf.ORPHAN_CLEAN_THREAD_POOL_SIZE,
        systemConfig.getInteger(ArcticMetaStoreConf.ORPHAN_CLEAN_THREAD_POOL_SIZE.key()));
    config.setInteger(
        ArcticMetaStoreConf.SYNC_FILE_INFO_CACHE_THREAD_POOL_SIZE,
        systemConfig.getInteger(ArcticMetaStoreConf.SYNC_FILE_INFO_CACHE_THREAD_POOL_SIZE.key()));

    config.setString(
        ArcticMetaStoreConf.DB_TYPE,
        systemConfig.getString(ArcticMetaStoreConf.DB_TYPE.key()));
    config.setString(
        ArcticMetaStoreConf.MYBATIS_CONNECTION_URL,
        systemConfig.getString(ArcticMetaStoreConf.MYBATIS_CONNECTION_URL.key()));
    config.setString(
        ArcticMetaStoreConf.MYBATIS_CONNECTION_DRIVER_CLASS_NAME,
        systemConfig.getString(ArcticMetaStoreConf.MYBATIS_CONNECTION_DRIVER_CLASS_NAME.key()));
    if (systemConfig.getString(ArcticMetaStoreConf.DB_TYPE.key()).equalsIgnoreCase("mysql")) {
      config.setString(
          ArcticMetaStoreConf.MYBATIS_CONNECTION_PASSWORD,
          systemConfig.getString(ArcticMetaStoreConf.MYBATIS_CONNECTION_PASSWORD.key()));
      config.setString(
          ArcticMetaStoreConf.MYBATIS_CONNECTION_USER_NAME,
          systemConfig.getString(ArcticMetaStoreConf.MYBATIS_CONNECTION_USER_NAME.key()));
    }
    //extension properties
    String extensionPro = yamlConfig.getString(ConfigFileProperties.SYSTEM_EXTENSION_CONFIG) == null ? "" :
        yamlConfig.getString(ConfigFileProperties.SYSTEM_EXTENSION_CONFIG);
    config.setString(ArcticMetaStoreConf.SYSTEM_EXTENSION_PROPERTIES, extensionPro);
    return config;
  }

  private static void initCatalogConfig() throws IOException {
    JSONArray catalogs = yamlConfig.getJSONArray(ConfigFileProperties.CATALOG_LIST);
    List<CatalogMeta> catalogMetas = Lists.newArrayList();
    for (int i = 0; i < catalogs.size(); i++) {
      CatalogMeta catalogMeta = new CatalogMeta();
      JSONObject catalog = catalogs.getJSONObject(i);
      catalogMeta.catalogName = catalog.getString(ConfigFileProperties.CATALOG_NAME);
      catalogMeta.catalogType = catalog.getString(ConfigFileProperties.CATALOG_TYPE);

      if (catalog.containsKey(ConfigFileProperties.CATALOG_STORAGE_CONFIG)) {
        Map<String, String> storageConfig = new HashMap<>();
        JSONObject catalogStorageConfig = catalog.getJSONObject(ConfigFileProperties.CATALOG_STORAGE_CONFIG);
        if (catalogStorageConfig.containsKey(ConfigFileProperties.CATALOG_STORAGE_TYPE)) {
          storageConfig.put(
              CatalogMetaProperties.STORAGE_CONFIGS_KEY_TYPE,
              catalogStorageConfig.getString(ConfigFileProperties.CATALOG_STORAGE_TYPE));
        }
        storageConfig.put(
            CatalogMetaProperties.STORAGE_CONFIGS_KEY_CORE_SITE,
            encodeXmlBase64(catalogStorageConfig.getString(ConfigFileProperties.CATALOG_CORE_SITE)));
        storageConfig.put(
            CatalogMetaProperties.STORAGE_CONFIGS_KEY_HDFS_SITE,
            encodeXmlBase64(catalogStorageConfig.getString(ConfigFileProperties.CATALOG_HDFS_SITE)));
        storageConfig.put(
            CatalogMetaProperties.STORAGE_CONFIGS_KEY_HIVE_SITE,
            encodeXmlBase64(catalogStorageConfig.getString(ConfigFileProperties.CATALOG_HIVE_SITE)));
        catalogMeta.storageConfigs = storageConfig;
      }

      if (catalog.containsKey(ConfigFileProperties.CATALOG_AUTH_CONFIG)) {
        Map<String, String> authConfig = new HashMap<>();
        JSONObject catalogAuthConfig = catalog.getJSONObject(ConfigFileProperties.CATALOG_AUTH_CONFIG);
        if (catalogAuthConfig.containsKey(ConfigFileProperties.CATALOG_AUTH_TYPE)) {
          authConfig.put(
              CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE,
              catalogAuthConfig.getString(ConfigFileProperties.CATALOG_AUTH_TYPE));
        }
        if (catalogAuthConfig.getString(ConfigFileProperties.CATALOG_AUTH_TYPE)
            .equalsIgnoreCase(CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_SIMPLE)) {
          if (catalogAuthConfig.containsKey(ConfigFileProperties.CATALOG_SIMPLE_HADOOP_USERNAME)) {
            authConfig.put(
                CatalogMetaProperties.AUTH_CONFIGS_KEY_HADOOP_USERNAME,
                catalogAuthConfig.getString(ConfigFileProperties.CATALOG_SIMPLE_HADOOP_USERNAME));
          }
        } else if (catalogAuthConfig.getString(ConfigFileProperties.CATALOG_AUTH_TYPE)
            .equalsIgnoreCase(CatalogMetaProperties.AUTH_CONFIGS_VALUE_TYPE_KERBEROS)) {
          if (catalogAuthConfig.containsKey(ConfigFileProperties.CATALOG_KEYTAB)) {
            authConfig.put(
                CatalogMetaProperties.AUTH_CONFIGS_KEY_KEYTAB,
                encodeBase64(catalogAuthConfig.getString(ConfigFileProperties.CATALOG_KEYTAB)));
          }
          if (catalogAuthConfig.containsKey(ConfigFileProperties.CATALOG_KRB5)) {
            authConfig.put(
                CatalogMetaProperties.AUTH_CONFIGS_KEY_KRB5,
                encodeBase64(catalogAuthConfig.getString(ConfigFileProperties.CATALOG_KRB5)));
          }
          if (catalogAuthConfig.containsKey(ConfigFileProperties.CATALOG_PRINCIPAL)) {
            authConfig.put(
                CatalogMetaProperties.AUTH_CONFIGS_KEY_PRINCIPAL,
                catalogAuthConfig.getString(ConfigFileProperties.CATALOG_PRINCIPAL));
          }
        }
        catalogMeta.authConfigs = authConfig;
      }

      if (catalog.containsKey(ConfigFileProperties.CATALOG_PROPERTIES)) {
        catalogMeta.catalogProperties = catalog.getObject(ConfigFileProperties.CATALOG_PROPERTIES, Map.class);
      }
      catalogMetas.add(catalogMeta);
    }
    ServiceContainer.getCatalogMetadataService().addCatalog(catalogMetas);
  }

  private static String encodeBase64(String filePath) throws IOException {
    if (filePath == null || "".equals(filePath.trim())) {
      return null;
    } else {
      return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
    }
  }

  private static String encodeXmlBase64(String filePath) throws IOException {
    if (filePath == null || "".equals(filePath.trim())) {
      return Base64.getEncoder().encodeToString("<configuration></configuration>".getBytes(StandardCharsets.UTF_8));
    } else {
      return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
    }
  }

  private static void initContainerConfig() {
    JSONArray containers = yamlConfig.getJSONArray(ConfigFileProperties.CONTAINER_LIST);
    for (int i = 0; i < containers.size(); i++) {
      JSONObject optimize = containers.getJSONObject(i);
      Container container = new Container();
      container.setName(optimize.getString(ConfigFileProperties.CONTAINER_NAME));
      container.setType(optimize.getString(ConfigFileProperties.CONTAINER_TYPE));
      container.setProperties(optimize.getObject(ConfigFileProperties.CONTAINER_PROPERTIES, Map.class));

      ServiceContainer.getOptimizeQueueService().insertContainer(container);
    }
  }

  private static void initOptimizeGroupConfig() throws MetaException, NoSuchObjectException, InvalidObjectException {
    JSONArray optimizeGroups = yamlConfig.getJSONArray(ConfigFileProperties.OPTIMIZE_GROUP_LIST);
    List<OptimizeQueueMeta> optimizeQueueMetas = ServiceContainer.getOptimizeQueueService().getQueues();
    for (int i = 0; i < optimizeGroups.size(); i++) {
      JSONObject optimizeGroup = optimizeGroups.getJSONObject(i);
      OptimizeQueueMeta optimizeQueueMeta = new OptimizeQueueMeta();
      optimizeQueueMeta.name = optimizeGroup.getString(ConfigFileProperties.OPTIMIZE_GROUP_NAME);
      optimizeQueueMeta.container = optimizeGroup.getString(ConfigFileProperties.OPTIMIZE_GROUP_CONTAINER);

      List<Container> containers = ServiceContainer.getOptimizeQueueService().getContainers();

      boolean checkContainer =
          containers.stream()
              .anyMatch(e -> e.getName()
                  .equalsIgnoreCase(optimizeGroup.getString(ConfigFileProperties.OPTIMIZE_GROUP_CONTAINER)));
      if (!checkContainer) {
        throw new NoSuchObjectException(
            "can not find such container config named" +
                optimizeGroup.getString(ConfigFileProperties.OPTIMIZE_GROUP_CONTAINER));
      }
      optimizeQueueMeta.properties = optimizeGroup.getObject(ConfigFileProperties.OPTIMIZE_GROUP_PROPERTIES, Map.class);
      boolean updated = false;
      for (OptimizeQueueMeta meta : optimizeQueueMetas) {
        if (meta.name.equals(optimizeQueueMeta.name)) {
          optimizeQueueMeta.queueId = meta.queueId;
          ServiceContainer.getOptimizeQueueService().updateQueue(optimizeQueueMeta);
          updated = true;
          break;
        }
      }
      if (!updated) {
        if (optimizeQueueMeta.queueId == 0) {
          optimizeQueueMeta.setQueueId(1);
        }
        ServiceContainer.getOptimizeQueueService().createQueue(optimizeQueueMeta);
      }
    }
  }
}