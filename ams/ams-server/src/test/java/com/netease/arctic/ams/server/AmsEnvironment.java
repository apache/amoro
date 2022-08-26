package com.netease.arctic.ams.server;

import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.util.DerbyTestUtil;
import com.netease.arctic.optimizer.OptimizerConfig;
import com.netease.arctic.optimizer.local.LocalOptimizer;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class AmsEnvironment {
  private static final Logger LOG = LoggerFactory.getLogger(AmsEnvironment.class);
  private LocalOptimizer optimizer;

  public static void main(String[] args) {
    AmsEnvironment amsEnvironment = new AmsEnvironment();
    amsEnvironment.start();
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    amsEnvironment.stop();
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
      DerbyTestUtil.deleteIfExists("/tmp/arctic_integration");
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
    System.setProperty(ArcticMetaStoreConf.ARCTIC_HOME.key(), path + "/config");
    System.setProperty("derby.init.sql.dir", path + "../classes/sql");
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
      if (ArcticMetaStore.getInstance() != null && ArcticMetaStore.getInstance().isStarted()) {
        break;
      }
    }
    LOG.info("ams start");
  }
  
  private void stopAms() {
    if (ArcticMetaStore.getInstance() != null) {
      ArcticMetaStore.getInstance().shutDown();
    }
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

}
