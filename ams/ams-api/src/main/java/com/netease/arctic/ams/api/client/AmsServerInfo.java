package com.netease.arctic.ams.api.client;

public class AmsServerInfo {
  private String host;
  private Integer thriftBindPort;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getThriftBindPort() {
    return thriftBindPort;
  }

  public void setThriftBindPort(Integer thriftBindPort) {
    this.thriftBindPort = thriftBindPort;
  }
}
