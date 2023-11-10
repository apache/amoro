package com.netease.arctic.server.resource;

public interface QuotaProvider {

  public int getTotalQuota(String resourceGroup);
}
