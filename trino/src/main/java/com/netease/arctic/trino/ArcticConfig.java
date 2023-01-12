
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.trino;

import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;

/**
 * Arctic config
 */
public class ArcticConfig {
  private String catalogUrl;
  private boolean hdfsImpersonationEnabled;
  private boolean enableSpillMap = false;
  private long maxInMemorySizeInBytes = 524288000;
  private String rocksDBBasePath;

  public String getCatalogUrl() {
    return catalogUrl;
  }

  public boolean getHdfsImpersonationEnabled() {
    return hdfsImpersonationEnabled;
  }

  public boolean isEnableSpillMap() {
    return enableSpillMap;
  }

  public long getMaxInMemorySizeInBytes() {
    return maxInMemorySizeInBytes;
  }

  public String getRocksDBBasePath() {
    return rocksDBBasePath;
  }

  @Config("arctic.url")
  public void setCatalogUrl(String catalogUrl) {
    this.catalogUrl = catalogUrl;
  }

  @Config("arctic.hdfs.impersonation.enabled")
  public void setHdfsImpersonationEnabled(boolean enabled) {
    this.hdfsImpersonationEnabled = enabled;
  }

  @Config("arctic.spill-map.enable")
  @ConfigDescription("Whether enable spill map in delete filter")
  public void setEnableSpillMap(boolean enableSpillMap) {
    this.enableSpillMap = enableSpillMap;
  }

  @Config("arctic.spill-map.max.memory.size")
  @ConfigDescription("Max delete map byte size in memory")
  public void setMaxInMemorySizeInBytes(long maxInMemorySizeInBytes) {
    this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
  }

  @Config("arctic.spill-map.path")
  @ConfigDescription("Rocks db base path")
  public void setRocksDBBasePath(String rocksDBBasePath) {
    this.rocksDBBasePath = rocksDBBasePath;
  }
}
