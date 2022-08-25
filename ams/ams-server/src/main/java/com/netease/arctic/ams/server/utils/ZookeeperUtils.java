package com.netease.arctic.ams.server.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

public class ZookeeperUtils {
  private final CuratorFramework zkClient;
  private static volatile ZookeeperUtils instance;

  public ZookeeperUtils(String zkServerAddress) {
    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
    this.zkClient = CuratorFrameworkFactory.builder()
        .connectString(zkServerAddress)
        .sessionTimeoutMs(5000)
        .connectionTimeoutMs(5000)
        .retryPolicy(retryPolicy)
        .build();
    zkClient.start();
  }

  public static ZookeeperUtils getInstance(String zkServerAddress) {
    if (instance == null) {
      synchronized (ZookeeperUtils.class) {
        if (instance == null) {
          instance = new ZookeeperUtils(zkServerAddress);
        }
      }
    }
    return instance;
  }

  public CuratorFramework getZkClient() {
    return this.zkClient;
  }

  public boolean exist(String path) throws Exception {
    Stat stat = zkClient.checkExists().forPath(path);
    return stat != null;
  }

  public void create(String path) throws Exception {
    StringBuilder tmpPath = new StringBuilder();
    for (String p : path.split("/")) {
      if (!p.isEmpty()) {
        tmpPath.append("/");
        tmpPath.append(p);
        if (!exist(tmpPath.toString())) {
          zkClient.create().withMode(CreateMode.PERSISTENT).forPath(tmpPath.toString());
        }
      }
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
