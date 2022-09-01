package com.netease.arctic.ams.server;

import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.util.DerbyTestUtil;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.local.LocalOptimizer;
import org.apache.commons.io.FileUtils;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class AmsEnvironment {
  private static final Logger LOG = LoggerFactory.getLogger(AmsEnvironment.class);
  private LocalOptimizer optimizer;
  private final String rootPath;
  private static final String DEFAULT_ROOT_PATH = "/tmp/arctic_integration";

  public static void main(String[] args) {
    AmsEnvironment amsEnvironment = new AmsEnvironment();
    amsEnvironment.start();
    try {
      Thread.sleep(100000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    amsEnvironment.stop();
  }

  public AmsEnvironment() {
    this(DEFAULT_ROOT_PATH);
  }

  public AmsEnvironment(String rootPath) {
    this.rootPath = rootPath;
    LOG.info("ams environment root path: " + rootPath);
  }

  public String getAmsUrl() {
    return "thrift://127.0.0.1:" + ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
  }

  public void start() {
    clear();
    startAms();
    startLocalOptimizer();
    LOG.info("ams environment started");
  }

  public void stop() {
    stopLocalOptimizer();
    stopAms();
    clear();
    LOG.info("ams environment stopped");
  }
  
  private void clear() {
    try {
      DerbyTestUtil.deleteIfExists(rootPath);
    } catch (IOException e) {
      LOG.warn("delete derby db failed", e);
    }
  }

  private static int randomPort() {
    // create a random port between 14000 - 18000
    int port = new Random().nextInt(4000);
    return port + 14000;
  }


  private void startAms() {
    String path = this.getClass().getClassLoader().getResource("").getPath();
    outputToFile(rootPath + "/conf/config.yaml", getAmsConfig());
    System.setProperty(ArcticMetaStoreConf.ARCTIC_HOME.key(), rootPath);
    System.setProperty("derby.init.sql.dir", path + "../classes/sql/derby/");
    AtomicBoolean starterExit = new AtomicBoolean(false);

    new Thread(() -> {
      int retry = 10;
      try {
        while (true) {
          try {
            LOG.info("start ams");
            System.setProperty(ArcticMetaStoreConf.THRIFT_BIND_PORT.key(), randomPort() + "");
            ArcticMetaStore.main(new String[] {});
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
        starterExit.set(true);
      }
    }, "ams-starter").start();

    while (!starterExit.get()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
      if (ArcticMetaStore.isStarted()) {
        break;
      }
    }
    LOG.info("ams start");
  }
  
  private void stopAms() {
    ArcticMetaStore.failover();
    LOG.info("ams stop");
  }

  private void startLocalOptimizer() {
    OptimizerConfig config = new OptimizerConfig();
    config.setExecutorParallel(1);
    config.setOptimizerId("1");
    config.setAmsUrl(getAmsUrl());
    config.setQueueId(1);
    config.setHeartBeat(1000);
    optimizer = new LocalOptimizer();
    optimizer.init(config);
    LOG.info("local optimizer start");
  }
  
  private void stopLocalOptimizer() {
    if (optimizer != null) {
      optimizer.release();
    }
    LOG.info("local optimizer stop");
  }
  
  private static void outputToFile(String fileName, String content) {
    try {
      FileUtils.writeStringToFile(new File(fileName), content);
    } catch (IOException e) {
      LOG.error("output to file failed", e);
    }
  }
  
  private String getAmsConfig() {
    return "ams:\n" +
        "  arctic.ams.server-host.prefix: \"127.\"\n" +
        // "  arctic.ams.server-host: 127.0.0.1\n" +
        "  arctic.ams.thrift.port: 1260 # useless in test, System.getProperty(\"arctic.ams.thrift.port\") is used\n" +
        "  arctic.ams.http.port: 1630\n" +
        "  arctic.ams.optimize.check.thread.pool-size: 1\n" +
        "  arctic.ams.optimize.commit.thread.pool-size: 1\n" +
        "  arctic.ams.expire.thread.pool-size: 1\n" +
        "  arctic.ams.orphan.clean.thread.pool-size: 1\n" +
        "  arctic.ams.file.sync.thread.pool-size: 1\n" +
        "  # derby config.sh\n" +
        "  arctic.ams.mybatis.ConnectionDriverClassName: org.apache.derby.jdbc.EmbeddedDriver\n" +
        "  arctic.ams.mybatis.ConnectionURL: jdbc:derby:" + rootPath + "/derby;create=true\n" +
        "  arctic.ams.database.type: derby\n" +
        "  # mysql config.sh\n" +
        // "  arctic.ams.mybatis.ConnectionURL: jdbc:mysql://localhost:3306/arctic_opensource_local?useUnicode=true" +
        // "&characterEncoding=UTF8&autoReconnect=true&useAffectedRows=true&useSSL=false\n" +
        // "  arctic.ams.mybatis.ConnectionDriverClassName: com.mysql.jdbc.Driver\n" +
        // "  arctic.ams.mybatis.ConnectionUserName: ndc\n" +
        // "  arctic.ams.mybatis.ConnectionPassword: ndc\n" +
        // "  arctic.ams.database.type: mysql" +
        "\n" +
        "# extension properties for like system\n" +
        "extension_properties:\n" +
        "#test.properties: test\n" +
        "\n" +
        "catalogs:\n" +
        "# arctic catalog config. now can't delete catalog by config file\n" +
        "  - name: local_catalog\n" +
        "    # arctic catalog type, now just support hadoop\n" +
        "    type: hadoop\n" +
        "    # file system config.sh\n" +
        "    storage_config:\n" +
        "      storage.type: hdfs\n" +
        "      core-site:\n" +
        "      hdfs-site:\n" +
        "      hive-site:\n" +
        "    # auth config.sh now support SIMPLE and KERBEROS\n" +
        "    auth_config:\n" +
        "      type: SIMPLE\n" +
        "      hadoop_username: root\n" +
        "    properties:\n" +
        "      warehouse.dir: " + rootPath + "/warehouse\n" +
        "\n" +
        "containers:\n" +
        "  # arctic optimizer container config.sh\n" +
        "  - name: localContainer\n" +
        "    type: local\n" +
        "    properties:\n" +
        "      hadoop_home: /opt/hadoop\n" +
        "\n" +
        "\n" +
        "\n" +
        "optimize_group:\n" +
        "  - name: default\n" +
        "    # container name, should equal with the name that containers config.sh\n" +
        "    container: localContainer\n" +
        "    properties:\n" +
        "      # unit MB\n" +
        "      memory: 1024\n";
  }

}
