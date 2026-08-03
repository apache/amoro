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

package org.apache.amoro.server.las;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableMetaStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Validated network configuration and client factories shared by future LAS OpenAPI handlers. */
public final class LasIntegrationContext {

  private static final Set<String> HTTP_SCHEMES = new HashSet<>(Arrays.asList("http", "https"));
  private static final Set<String> OPTIMIZER_JAR_SCHEMES =
      new HashSet<>(Arrays.asList("tos", "http", "https"));

  private final boolean enabled;
  private final URI hmsUri;
  private final URI tosEndpoint;
  private final URI iamEndpoint;
  private final URI emrServerlessEndpoint;
  private final URI optimizerJarUri;
  private final String region;
  private final String emrServerlessService;
  private final Duration connectTimeout;
  private final Duration readTimeout;

  private LasIntegrationContext(Configurations configurations) {
    this.enabled = configurations.getBoolean(LasIntegrationConfig.ENABLED);
    this.region = configurations.getString(LasIntegrationConfig.REGION);
    this.emrServerlessService =
        configurations.getString(LasIntegrationConfig.EMR_SERVERLESS_SERVICE);
    this.connectTimeout = configurations.get(LasIntegrationConfig.CONNECT_TIMEOUT);
    this.readTimeout = configurations.get(LasIntegrationConfig.READ_TIMEOUT);

    if (!enabled) {
      this.hmsUri = null;
      this.tosEndpoint = null;
      this.iamEndpoint = null;
      this.emrServerlessEndpoint = null;
      this.optimizerJarUri = null;
      return;
    }

    this.hmsUri =
        requiredUri(configurations, LasIntegrationConfig.HMS_URI, Collections.singleton("thrift"));
    this.tosEndpoint = requiredUri(configurations, LasIntegrationConfig.TOS_ENDPOINT, HTTP_SCHEMES);
    this.iamEndpoint = requiredUri(configurations, LasIntegrationConfig.IAM_ENDPOINT, HTTP_SCHEMES);
    this.emrServerlessEndpoint =
        requiredUri(configurations, LasIntegrationConfig.EMR_SERVERLESS_ENDPOINT, HTTP_SCHEMES);
    this.optimizerJarUri =
        requiredUri(configurations, LasIntegrationConfig.OPTIMIZER_JAR_URI, OPTIMIZER_JAR_SCHEMES);
    requiredString(configurations, LasIntegrationConfig.REGION);
    requiredString(configurations, LasIntegrationConfig.EMR_SERVERLESS_SERVICE);
    positiveDuration(configurations, LasIntegrationConfig.CONNECT_TIMEOUT);
    positiveDuration(configurations, LasIntegrationConfig.READ_TIMEOUT);

    if (configurations.getBoolean(LasIntegrationConfig.CROSS_VPC_ENABLED)) {
      requiredString(configurations, LasIntegrationConfig.CROSS_VPC_ACCOUNT_ID);
      requiredString(configurations, LasIntegrationConfig.CROSS_VPC_VPC_ID);
      requiredString(configurations, LasIntegrationConfig.CROSS_VPC_SUBNET_IDS);
      requiredString(configurations, LasIntegrationConfig.CROSS_VPC_SECURITY_GROUP_ID);
    }
  }

  public static LasIntegrationContext initialize(Configurations configurations) {
    return new LasIntegrationContext(configurations);
  }

  public boolean enabled() {
    return enabled;
  }

  public CachedHiveClientPool newHmsClientPool() {
    ensureEnabled();
    Configuration configuration = new Configuration();
    configuration.set("hive.metastore.uris", hmsUri.toString());
    TableMetaStore metaStore = TableMetaStore.builder().withConfiguration(configuration).build();
    return new CachedHiveClientPool(metaStore, Maps.newHashMap());
  }

  public Configuration newTosConfiguration() {
    ensureEnabled();
    Configuration configuration = new Configuration(false);
    configuration.set("fs.AbstractFileSystem.tos.impl", "io.proton.fs.ProtonFS");
    configuration.set("fs.tos.impl", "io.proton.fs.ProtonFileSystem");
    configuration.set("fs.tos.endpoint", tosEndpoint.toString());
    return configuration;
  }

  public URI hmsUri() {
    ensureEnabled();
    return hmsUri;
  }

  public URI iamEndpoint() {
    ensureEnabled();
    return iamEndpoint;
  }

  public URI emrServerlessEndpoint() {
    ensureEnabled();
    return emrServerlessEndpoint;
  }

  public URI optimizerJarUri() {
    ensureEnabled();
    return optimizerJarUri;
  }

  public String region() {
    return region;
  }

  public String emrServerlessService() {
    return emrServerlessService;
  }

  public Duration connectTimeout() {
    return connectTimeout;
  }

  public Duration readTimeout() {
    return readTimeout;
  }

  private void ensureEnabled() {
    if (!enabled) {
      throw new IllegalStateException("LAS/EMR integration is disabled");
    }
  }

  private static URI requiredUri(
      Configurations configurations, ConfigOption<String> option, Set<String> allowedSchemes) {
    String value = requiredString(configurations, option);
    try {
      URI uri = new URI(value);
      if (!allowedSchemes.contains(uri.getScheme()) || StringUtils.isBlank(uri.getHost())) {
        throw new IllegalArgumentException(
            String.format("%s must contain a supported scheme and host: %s", option.key(), value));
      }
      return uri;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(option.key() + " is not a valid URI: " + value, e);
    }
  }

  private static String requiredString(Configurations configurations, ConfigOption<String> option) {
    String value = configurations.getString(option);
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException(option.key() + " must be configured");
    }
    return value;
  }

  private static Duration positiveDuration(
      Configurations configurations, ConfigOption<Duration> option) {
    Duration value = configurations.get(option);
    if (value == null || value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException(option.key() + " must be greater than zero");
    }
    return value;
  }
}
