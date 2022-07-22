package com.netease.arctic.ams.api.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.netease.arctic.ams.api.ArcticTableMetastore;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;

/**
 * Client pool cache for different ams server, sharing in jvm.
 */
public class AmsClientPools {

  private static final int CLIENT_POOL_MIN = 0;
  private static final int CLIENT_POOL_MAX = 5;
  private static final String serverFlag = "ArcticTableMetastore";

  private static final LoadingCache<String, ThriftClientPool<ArcticTableMetastore.Client>> CLIENT_POOLS
      = Caffeine.newBuilder()
      .build(buildClientPool(serverFlag));

  public static ThriftClientPool<ArcticTableMetastore.Client> getClientPool() {
    return CLIENT_POOLS.get(serverFlag);
  }

  public static void cleanAll() {
    CLIENT_POOLS.cleanUp();
  }

  private static ThriftClientPool<ArcticTableMetastore.Client> buildClientPool(String zkUrl) {
    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
    CuratorFramework zkClient = CuratorFrameworkFactory.builder()
        .connectString(zkUrl)
        .sessionTimeoutMs(5000)
        .connectionTimeoutMs(5000)
        .retryPolicy(retryPolicy)
        .build();
    String masterPath = "/ams/master";
    String url = null;
    try {
      url = new String(zkClient.getData().forPath(masterPath), StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ArcticThriftUrl arcticThriftUrl = ArcticThriftUrl.parse(url);
    PoolConfig poolConfig = new PoolConfig();
    poolConfig.setTimeout(arcticThriftUrl.socketTimeout());
    poolConfig.setFailover(true);
    poolConfig.setMinIdle(CLIENT_POOL_MIN);
    poolConfig.setMaxIdle(CLIENT_POOL_MAX);
    return new ThriftClientPool<>(
        Collections.singletonList(
            new ServiceInfo(arcticThriftUrl.host(), arcticThriftUrl.port())),
        s -> {
          TProtocol protocol = new TBinaryProtocol(s);
          ArcticTableMetastore.Client tableMetastore = new ArcticTableMetastore.Client(
              new TMultiplexedProtocol(protocol, "TableMetastore"));
          return tableMetastore;
        },
        c -> {
          try {
            ((ArcticTableMetastore.Client) c).ping();
          } catch (TException e) {
            return false;
          }
          return true;
        }, new PoolConfig());
  }
}
