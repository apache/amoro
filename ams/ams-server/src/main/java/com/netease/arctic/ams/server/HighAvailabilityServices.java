package com.netease.arctic.ams.server;

import java.nio.charset.StandardCharsets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class HighAvailabilityServices {
  private LeaderLatch leaderLatch;


  public static class ZookeeperService {
    private CuratorFramework zkClient;

    public ZookeeperService(String zkServerAddress) {
      ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
      this.zkClient = CuratorFrameworkFactory.builder()
          .connectString(zkServerAddress)
          .sessionTimeoutMs(5000)
          .connectionTimeoutMs(5000)
          .retryPolicy(retryPolicy)
          .build();
    }

    public boolean exist(String path) throws Exception {
      Stat stat = zkClient.checkExists().forPath(path);
      return stat != null;
    }

    public void create(String path) throws Exception {
      if (!exist(path)) {
        zkClient.create().withMode(CreateMode.PERSISTENT).forPath(path);
      }
    }

    public void setData(String path, String data) throws Exception {
      zkClient.setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
    }

    public String getData(String path) throws Exception {
      return new String(zkClient.getData().forPath(path), StandardCharsets.UTF_8);
    }

    public void delete(String path) throws Exception {
      zkClient.delete().forPath(path);
    }
  }
}
