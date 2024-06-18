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

import org.apache.amoro.SingletonResourceUtil;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.config.Configurations;
import org.apache.amoro.api.resource.ResourceGroup;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.hive.HMSMockServer;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.optimizer.standalone.StandaloneOptimizer;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.resource.ResourceContainers;
import org.apache.amoro.server.table.DefaultTableService;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.io.MoreFiles;
import org.apache.amoro.shade.guava32.com.google.common.io.RecursiveDeleteOption;
import org.apache.amoro.shade.thrift.org.apache.thrift.server.TServer;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TTransportException;
import org.apache.amoro.table.TableIdentifier;
import org.apache.commons.io.FileUtils;
import org.apache.iceberg.common.DynFields;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AmsEnvironment {

  private static final Logger LOG = LoggerFactory.getLogger(AmsEnvironment.class);
  private static AmsEnvironment integrationInstances = null;
  private final String rootPath;
  private static final String DEFAULT_ROOT_PATH = "/tmp/amoro_integration";
  private static final String OPTIMIZE_GROUP = "default";
  private final AmoroServiceContainer serviceContainer;
  private Configurations serviceConfig;
  private DefaultTableService tableService;
  private final AtomicBoolean amsExit;
  private int tableServiceBindPort;
  private int optimizingServiceBindPort;
  private final HMSMockServer testHMS;
  private final Map<String, MixedFormatCatalog> catalogs = new HashMap<>();

  public static final String INTERNAL_ICEBERG_CATALOG = "internal_iceberg_catalog";
  public static final String INTERNAL_ICEBERG_CATALOG_WAREHOUSE = "/internal_iceberg/warehouse";
  public static final String ICEBERG_CATALOG = "iceberg_catalog";
  public static String ICEBERG_CATALOG_DIR = "/iceberg/warehouse";
  public static final String INTERNAL_MIXED_ICEBERG_CATALOG = "internal_mixed_iceberg_catalog";
  public static String MIXED_ICEBERG_CATALOG_DIR = "/mixed_iceberg/warehouse";
  public static final String MIXED_HIVE_CATALOG = "mixed_hive_catalog";
  private boolean started = false;
  private boolean optimizingStarted = false;
  private boolean singleton = false;

  public static AmsEnvironment getIntegrationInstances() {
    synchronized (AmsEnvironment.class) {
      if (integrationInstances == null) {
        TemporaryFolder baseDir = new TemporaryFolder();
        try {
          baseDir.create();
          integrationInstances = new AmsEnvironment(baseDir.newFolder().getAbsolutePath());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        integrationInstances.singleton();
      }
    }
    return integrationInstances;
  }

  public static void main(String[] args) throws Exception {
    AmsEnvironment amsEnvironment = new AmsEnvironment();
    amsEnvironment.start();
    amsEnvironment.startOptimizer();
  }

  public AmsEnvironment() throws Exception {
    this(DEFAULT_ROOT_PATH);
  }

  public AmsEnvironment(String rootPath) throws Exception {
    this.rootPath = rootPath;
    LOG.info("ams environment root path: {}", rootPath);
    String path =
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath();
    FileUtils.writeStringToFile(new File(rootPath + "/conf/config.yaml"), getAmsConfig());
    System.setProperty(Environments.AMORO_HOME, rootPath);
    System.setProperty("derby.init.sql.dir", path + "../classes/sql/derby/");
    amsExit = new AtomicBoolean(false);
    serviceContainer = new AmoroServiceContainer();
    TemporaryFolder hiveDir = new TemporaryFolder();
    hiveDir.create();
    testHMS = new HMSMockServer(hiveDir.newFile());
  }

  public void singleton() {
    this.singleton = true;
  }

  public void start() throws Exception {
    if (started) {
      return;
    }

    testHMS.start();
    startAms();
    DynFields.UnboundField<DefaultTableService> amsTableServiceField =
        DynFields.builder().hiddenImpl(AmoroServiceContainer.class, "tableService").build();
    tableService = amsTableServiceField.bind(serviceContainer).get();
    DynFields.UnboundField<CompletableFuture<Boolean>> tableServiceField =
        DynFields.builder().hiddenImpl(DefaultTableService.class, "initialized").build();
    boolean tableServiceIsStart = false;
    long startTime = System.currentTimeMillis();
    while (!tableServiceIsStart) {
      if (System.currentTimeMillis() - startTime > 10000) {
        throw new RuntimeException("table service not start yet after 10s");
      }
      try {
        tableServiceField.bind(tableService).get().get();
        tableServiceIsStart = true;
      } catch (RuntimeException e) {
        LOG.info("table service not start yet");
      }
      Thread.sleep(1000);
    }

    initCatalog();
    started = true;
  }

  public void stop() throws IOException {
    if (!started) {
      return;
    }
    if (singleton && SingletonResourceUtil.isUseSingletonResource()) {
      return;
    }

    stopOptimizer();
    if (this.serviceContainer != null) {
      this.serviceContainer.dispose();
    }
    testHMS.stop();
    MoreFiles.deleteRecursively(Paths.get(rootPath), RecursiveDeleteOption.ALLOW_INSECURE);
  }

  public MixedFormatCatalog catalog(String name) {
    return catalogs.get(name);
  }

  public void createDatabaseIfNotExists(String catalog, String database) {
    MixedFormatCatalog mixedCatalog = catalogs.get(catalog);
    if (mixedCatalog.listDatabases().contains(database)) {
      return;
    }
    mixedCatalog.createDatabase(database);
  }

  public AmoroServiceContainer serviceContainer() {
    return this.serviceContainer;
  }

  public boolean tableExist(TableIdentifier tableIdentifier) {
    return tableService.tableExist(tableIdentifier.buildTableIdentifier());
  }

  public HMSMockServer getTestHMS() {
    return testHMS;
  }

  private void initCatalog() {
    createExternalIcebergCatalog();
    createInternalIceberg();
    createInternalMixIcebergCatalog();
    createMixHiveCatalog();
  }

  private void createInternalIceberg() {
    String warehouseDir = rootPath + INTERNAL_ICEBERG_CATALOG_WAREHOUSE;
    Map<String, String> properties = Maps.newHashMap();
    createDirIfNotExist(warehouseDir);
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, warehouseDir);
    CatalogMeta catalogMeta =
        CatalogTestHelpers.buildCatalogMeta(
            INTERNAL_ICEBERG_CATALOG,
            CatalogMetaProperties.CATALOG_TYPE_AMS,
            properties,
            TableFormat.ICEBERG);

    tableService.createCatalog(catalogMeta);
  }

  private void createExternalIcebergCatalog() {
    String warehouseDir = rootPath + ICEBERG_CATALOG_DIR;
    Map<String, String> properties = Maps.newHashMap();
    createDirIfNotExist(warehouseDir);
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, warehouseDir);
    CatalogMeta catalogMeta =
        CatalogTestHelpers.buildCatalogMeta(
            ICEBERG_CATALOG,
            CatalogMetaProperties.CATALOG_TYPE_HADOOP,
            properties,
            TableFormat.ICEBERG);
    tableService.createCatalog(catalogMeta);
  }

  private void createInternalMixIcebergCatalog() {
    String warehouseDir = rootPath + MIXED_ICEBERG_CATALOG_DIR;
    Map<String, String> properties = Maps.newHashMap();
    createDirIfNotExist(warehouseDir);
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, warehouseDir);
    CatalogMeta catalogMeta =
        CatalogTestHelpers.buildCatalogMeta(
            INTERNAL_MIXED_ICEBERG_CATALOG,
            CatalogMetaProperties.CATALOG_TYPE_AMS,
            properties,
            TableFormat.MIXED_ICEBERG);
    tableService.createCatalog(catalogMeta);
    catalogs.put(
        INTERNAL_MIXED_ICEBERG_CATALOG,
        CatalogLoader.load(getTableServiceUrl() + "/" + INTERNAL_MIXED_ICEBERG_CATALOG));
  }

  private void createMixHiveCatalog() {
    Map<String, String> properties = Maps.newHashMap();
    CatalogMeta catalogMeta =
        CatalogTestHelpers.buildHiveCatalogMeta(
            MIXED_HIVE_CATALOG, properties, testHMS.hiveConf(), TableFormat.MIXED_HIVE);
    tableService.createCatalog(catalogMeta);
    catalogs.put(
        MIXED_HIVE_CATALOG, CatalogLoader.load(getTableServiceUrl() + "/" + MIXED_HIVE_CATALOG));
  }

  private void createDirIfNotExist(String warehouseDir) {
    try {
      Files.createDirectories(Paths.get(warehouseDir));
    } catch (IOException e) {
      LOG.error("failed to create iceberg warehouse dir {}", warehouseDir, e);
      throw new RuntimeException(e);
    }
  }

  public void startOptimizer() {
    if (optimizingStarted) {
      return;
    }
    OptimizerManager optimizerManager = serviceContainer.getOptimizingService();
    optimizerManager.createResourceGroup(
        new ResourceGroup.Builder("default", "localContainer")
            .addProperty("memory", "1024")
            .build());
    new Thread(
            () -> {
              String[] startArgs = {"-a", getOptimizingServiceUrl(), "-p", "1", "-g", "default"};
              try {
                StandaloneOptimizer.main(startArgs);
              } catch (CmdLineException e) {
                throw new RuntimeException(e);
              }
            })
        .start();
    optimizingStarted = true;
  }

  public void stopOptimizer() {
    DynFields.UnboundField<DefaultOptimizingService> field =
        DynFields.builder().hiddenImpl(AmoroServiceContainer.class, "optimizingService").build();
    field
        .bind(serviceContainer)
        .get()
        .listOptimizers()
        .forEach(
            resource -> {
              ResourceContainers.get(resource.getContainerName()).releaseOptimizer(resource);
            });
  }

  public String getTableServiceUrl() {
    return "thrift://127.0.0.1:" + tableServiceBindPort;
  }

  public String getOptimizingServiceUrl() {
    return "thrift://127.0.0.1:" + optimizingServiceBindPort;
  }

  public String getHttpUrl() {
    return "http://127.0.0.1:1630";
  }

  private void startAms() throws Exception {
    Thread amsRunner =
        new Thread(
            () -> {
              int retry = 10;
              try {
                while (true) {
                  try {
                    LOG.info("start ams");
                    genThriftBindPort();
                    DynFields.UnboundField<Configurations> field =
                        DynFields.builder()
                            .hiddenImpl(AmoroServiceContainer.class, "serviceConfig")
                            .build();
                    serviceConfig = field.bind(serviceContainer).get();
                    serviceConfig.set(
                        AmoroManagementConf.TABLE_SERVICE_THRIFT_BIND_PORT, tableServiceBindPort);
                    serviceConfig.set(
                        AmoroManagementConf.OPTIMIZING_SERVICE_THRIFT_BIND_PORT,
                        optimizingServiceBindPort);
                    serviceConfig.set(
                        AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL, 1000L);
                    serviceContainer.startService();
                    break;
                  } catch (TTransportException e) {
                    if (e.getCause() instanceof BindException) {
                      LOG.error("start ams failed", e);
                      if (retry-- < 0) {
                        throw e;
                      } else {
                        Thread.sleep(1000);
                      }
                    } else {
                      throw e;
                    }
                  } catch (Throwable e) {
                    throw e;
                  }
                }
              } catch (Throwable t) {
                LOG.error("start ams failed", t);
              } finally {
                amsExit.set(true);
              }
            },
            "ams-runner");
    amsRunner.start();

    DynFields.UnboundField<TServer> tableManagementServerField =
        DynFields.builder()
            .hiddenImpl(AmoroServiceContainer.class, "tableManagementServer")
            .build();
    DynFields.UnboundField<TServer> optimizingServiceServerField =
        DynFields.builder()
            .hiddenImpl(AmoroServiceContainer.class, "optimizingServiceServer")
            .build();
    while (true) {
      if (amsExit.get()) {
        LOG.error("ams exit");
        break;
      }
      TServer tableManagementServer = tableManagementServerField.bind(serviceContainer).get();
      TServer optimizingServiceServer = optimizingServiceServerField.bind(serviceContainer).get();
      if (tableManagementServer != null
          && tableManagementServer.isServing()
          && optimizingServiceServer != null
          && optimizingServiceServer.isServing()) {
        LOG.info("ams start");
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        LOG.warn("interrupt ams");
        amsRunner.interrupt();
        break;
      }
    }
  }

  private void genThriftBindPort() {
    // create a random port between 14000 - 18000
    Random random = new Random();
    this.tableServiceBindPort = random.nextInt(4000) + 14000;
    this.optimizingServiceBindPort = random.nextInt(4000) + 14000;
  }

  private String getAmsConfig() {
    return "ams:\n"
        + "  admin-username: \"admin\"\n"
        + "  admin-passowrd: \"admin\"\n"
        + "  server-bind-host: \"0.0.0.0\"\n"
        + "  server-expose-host: \"127.0.0.1\"\n"
        + "  refresh-external-catalog-interval: 180000 # 3min\n"
        + "  refresh-table-thread-count: 10\n"
        + "  refresh-table-interval: 60000 #1min\n"
        + "  expire-table-thread-count: 10\n"
        + "  clean-orphan-file-thread-count: 10\n"
        + "  sync-hive-tables-thread-count: 10\n"
        + "\n"
        + "  thrift-server:\n"
        + "    max-message-size: 104857600 # 100MB\n"
        + "    selector-thread-count: 2\n"
        + "    selector-queue-size: 4\n"
        + "    table-service:\n"
        + "      bind-port: 1260\n"
        + "      worker-thread-count: 20\n"
        + "    optimizing-service:\n"
        + "      bind-port: 1261\n"
        + "\n"
        + "  http-server:\n"
        + "    bind-port: 1630\n"
        + "\n"
        + "  self-optimizing:\n"
        + "    commit-thread-count: 10\n"
        + "    runtime-data-keep-days: 30\n"
        + "    runtime-data-expire-interval-hours: 1\n"
        + "\n"
        + "  database:\n"
        + "    type: \"derby\"\n"
        + "    jdbc-driver-class: \"org.apache.derby.jdbc.EmbeddedDriver\"\n"
        + "    url: \"jdbc:derby:"
        + rootPath.replace("\\", "\\\\")
        + "/derby;create=true\"\n"
        + "\n"
        + "  terminal:\n"
        + "    backend: local\n"
        + "    local.spark.sql.session.timeZone: UTC\n"
        + "    local.spark.sql.iceberg.handle-timestamp-without-timezone: false\n"
        + "\n"
        + "containers:\n"
        + "  - name: localContainer\n"
        + "    container-impl: org.apache.amoro.server.manager.LocalOptimizerContainer\n"
        + "    properties:\n"
        + "      memory: \"1024\"\n"
        + "      hadoop_home: /opt/hadoop\n"
        + "      # java_home: /opt/java\n"
        + "\n"
        + "optimizer_groups:\n"
        + "  - name: "
        + OPTIMIZE_GROUP
        + "\n"
        + "    container: localContainer\n"
        + "    properties:\n"
        + "      memory: 1024 # MB\n"
        + "\n"
        + "metric-reporters:\n"
        + "  - name: mocked-reporter\n"
        + "    properties:\n";
  }
}
