package com.netease.arctic.ams.server;

import com.netease.arctic.ams.api.client.ZookeeperService;
import com.netease.arctic.ams.api.properties.AmsHAProperties;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HighAvailabilityServices {

  public static final Logger LOG = LoggerFactory.getLogger(HighAvailabilityServices.class);

  private final LeaderLatch leaderLatch;
  private static volatile HighAvailabilityServices instance;
  private static String namespace;
  private static final List<LeaderLatchListener> listeners = new ArrayList<>();

  private HighAvailabilityServices(String zkServerAddress, String namespace) {
    ZookeeperService zkService = new ZookeeperService(zkServerAddress);
    this.namespace = namespace;
    String lockPath = AmsHAProperties.getLeaderPath(namespace);
    try {
      zkService.create(lockPath);
    } catch (Exception e) {
      e.printStackTrace();
    }
    leaderLatch = new LeaderLatch(zkService.getZkClient(), lockPath);
  }

  public static HighAvailabilityServices getInstance(String zkServerAddress, String namespace) {
    Preconditions.checkNotNull(namespace, "cluster name cannot be null");

    if (instance == null) {
      synchronized (HighAvailabilityServices.class) {
        if (instance == null) {
          instance = new HighAvailabilityServices(zkServerAddress, namespace);
        }
      }
    }
    return instance;
  }

  public void addListener(LeaderLatchListener listener) {
    listeners.add(listener);
  }

  public void leaderLatch() throws Exception {
    listeners.forEach(leaderLatch::addListener);
    leaderLatch.start();
    leaderLatch.await();
  }
}
