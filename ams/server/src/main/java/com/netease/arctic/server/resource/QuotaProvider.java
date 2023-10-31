package com.netease.arctic.server.resource;

import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.table.TableRuntime;

public interface QuotaProvider {

  public int getTotalQuota(String resourceGroup);
}
