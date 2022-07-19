package com.netease.arctic.ams.api.client;

import org.apache.thrift.TServiceClient;

public interface ThriftPingFactory {
  boolean ping(TServiceClient client);
}
