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

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.RestExtension;
import org.apache.amoro.server.RestExtensionFactory;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** Bootstrap extension for LAS/EMR network integration. */
public class LasRestExtension implements RestExtension {

  private static final Logger LOG = LoggerFactory.getLogger(LasRestExtension.class);

  private final LasIntegrationContext integrationContext;
  private final LasHmsClient hmsClient;
  private final ServerlessSparkSqlManager sparkSqlManager;
  private final CatalogManager catalogManager;
  private final TableManager tableManager;

  LasRestExtension(
      LasIntegrationContext integrationContext,
      LasHmsClient hmsClient,
      ServerlessSparkSqlManager sparkSqlManager,
      CatalogManager catalogManager,
      TableManager tableManager) {
    this.integrationContext = integrationContext;
    this.hmsClient = hmsClient;
    this.sparkSqlManager = sparkSqlManager;
    this.catalogManager = catalogManager;
    this.tableManager = tableManager;
    LOG.info("LAS/EMR integration initialized, enabled={}", integrationContext.enabled());
  }

  @Override
  public EndpointGroup endpoints() {
    return () -> {
      // Intentionally empty. Add management-plane routes here under /api/ams/v1/las when the
      // OpenAPI contract is ready. Controllers should receive integrationContext, hmsClient,
      // sparkSqlManager, catalogManager, and tableManager from this extension instead of
      // constructing HMS, TOS, IAM, or EMR clients themselves. Routes registered here
      // automatically pass through the existing AMS REST authentication filter; do not add
      // management-plane routes to the URL whitelist.
    };
  }

  @Override
  public boolean needHandleException(Context ctx) {
    return false;
  }

  @Override
  public void handleException(Exception e, Context ctx) {
    throw new UnsupportedOperationException("LAS REST extension does not expose endpoints yet", e);
  }

  /** Factory loaded by the AMS REST extension plugin manager. */
  public static class Factory implements RestExtensionFactory {

    private Configurations serviceConfig;
    private CatalogManager catalogManager;
    private TableManager tableManager;
    private LasIamClient iamClient;

    @Override
    public RestExtensionFactory withServiceConfig(Configurations serviceConfig) {
      this.serviceConfig = serviceConfig;
      return this;
    }

    @Override
    public RestExtensionFactory withCatalogManager(CatalogManager catalogManager) {
      this.catalogManager = catalogManager;
      return this;
    }

    @Override
    public RestExtensionFactory withTableManager(TableManager tableManager) {
      this.tableManager = tableManager;
      return this;
    }

    @Override
    public RestExtension build() {
      Preconditions.checkNotNull(serviceConfig, "serviceConfig is required");
      Preconditions.checkNotNull(catalogManager, "catalogManager is required");
      Preconditions.checkNotNull(tableManager, "tableManager is required");
      LasIntegrationContext context = LasIntegrationContext.initialize(serviceConfig);
      LasHmsClient hmsClient = null;
      ServerlessSparkSqlManager sparkSqlManager = null;
      if (context.enabled()) {
        iamClient = new LasIamClient(context);
        hmsClient = new LasHmsClient(context);
        sparkSqlManager = new ServerlessSparkSqlManager(context, iamClient);
      }
      return new LasRestExtension(
          context, hmsClient, sparkSqlManager, catalogManager, tableManager);
    }

    @Override
    public void open(Map<String, String> properties) {
      LOG.info("Opening LAS/EMR integration extension");
    }

    @Override
    public void close() {
      if (iamClient != null) {
        try {
          iamClient.close();
        } catch (Exception e) {
          LOG.warn("Failed to close LAS IAM client", e);
        }
        iamClient = null;
      }
      LOG.info("Closing LAS/EMR integration extension");
    }

    @Override
    public String name() {
      return "las-integration";
    }
  }
}
