package com.netease.arctic.server.process;

public interface QuotaConsumer {

  long getId();

  long getStartTime();

  long getQuotaRuntime();
}
