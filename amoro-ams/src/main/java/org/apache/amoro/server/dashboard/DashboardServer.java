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

package org.apache.amoro.server.dashboard;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.staticfiles.StaticFileConfig;
import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.authentication.TokenAuthenticationProvider;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.ForbiddenException;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.RestCatalogService;
import org.apache.amoro.server.authentication.HttpAuthenticationFactory;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.controller.ApiTokenController;
import org.apache.amoro.server.dashboard.controller.CatalogController;
import org.apache.amoro.server.dashboard.controller.HealthCheckController;
import org.apache.amoro.server.dashboard.controller.LoginController;
import org.apache.amoro.server.dashboard.controller.OptimizerController;
import org.apache.amoro.server.dashboard.controller.OptimizerGroupController;
import org.apache.amoro.server.dashboard.controller.OverviewController;
import org.apache.amoro.server.dashboard.controller.PlatformFileInfoController;
import org.apache.amoro.server.dashboard.controller.SettingController;
import org.apache.amoro.server.dashboard.controller.TableController;
import org.apache.amoro.server.dashboard.controller.TerminalController;
import org.apache.amoro.server.dashboard.controller.VersionController;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.terminal.TerminalManager;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Objects;
import java.util.function.Consumer;

public class DashboardServer {

  public static final Logger LOG = LoggerFactory.getLogger(DashboardServer.class);

  private static final String AUTH_TYPE_BASIC = "basic";
  private static final String AUTH_TYPE_BEARER = "bearer";
  private static final String X_REQUEST_SOURCE_HEADER = "X-Request-Source";
  private static final String X_REQUEST_SOURCE_WEB = "Web";
  private final CatalogController catalogController;
  private final HealthCheckController healthCheckController;
  private final LoginController loginController;
  private final OptimizerGroupController optimizerGroupController;
  private final OptimizerController optimizerController;
  private final PlatformFileInfoController platformFileInfoController;
  private final SettingController settingController;
  private final TableController tableController;
  private final TerminalController terminalController;
  private final VersionController versionController;
  private final OverviewController overviewController;
  private final ApiTokenController apiTokenController;

  private final PasswdAuthenticationProvider basicAuthProvider;
  private final TokenAuthenticationProvider bearerAuthProvider;
  private final String proxyClientIpHeader;

  public DashboardServer(
      Configurations serviceConfig,
      CatalogManager catalogManager,
      TableManager tableManager,
      OptimizerManager optimizerManager,
      TerminalManager terminalManager) {
    PlatformFileManager platformFileManager = new PlatformFileManager();
    this.catalogController = new CatalogController(catalogManager, platformFileManager);
    this.healthCheckController = new HealthCheckController();
    this.loginController = new LoginController(serviceConfig);
    this.optimizerGroupController = new OptimizerGroupController(tableManager, optimizerManager);
    this.optimizerController = new OptimizerController(optimizerManager);
    this.platformFileInfoController = new PlatformFileInfoController(platformFileManager);
    this.settingController = new SettingController(serviceConfig, optimizerManager);
    ServerTableDescriptor tableDescriptor =
        new ServerTableDescriptor(catalogManager, tableManager, serviceConfig);
    this.tableController =
        new TableController(catalogManager, tableManager, tableDescriptor, serviceConfig);
    this.terminalController = new TerminalController(terminalManager);
    this.versionController = new VersionController();
    OverviewManager manager = new OverviewManager(serviceConfig);
    this.overviewController = new OverviewController(manager);
    APITokenManager apiTokenManager = new APITokenManager();
    this.apiTokenController = new ApiTokenController(apiTokenManager);

    String authType = serviceConfig.get(AmoroManagementConf.HTTP_SERVER_REST_AUTH_TYPE);
    this.basicAuthProvider =
        AUTH_TYPE_BASIC.equalsIgnoreCase(authType)
            ? HttpAuthenticationFactory.getPasswordAuthenticationProvider(
                serviceConfig.get(AmoroManagementConf.HTTP_SERVER_AUTH_BASIC_PROVIDER),
                serviceConfig)
            : null;
    this.bearerAuthProvider =
        AUTH_TYPE_BEARER.equalsIgnoreCase(authType)
            ? HttpAuthenticationFactory.getBearerAuthenticationProvider(
                serviceConfig.get(AmoroManagementConf.HTTP_SERVER_AUTH_BEARER_PROVIDER),
                serviceConfig)
            : null;
    this.proxyClientIpHeader =
        serviceConfig.get(AmoroManagementConf.HTTP_SERVER_PROXY_CLIENT_IP_HEADER);
  }

  private volatile String indexHtml = null;
  // read index.html content
  public String getIndexFileContent() {
    if (indexHtml == null) {
      synchronized (this) {
        if (indexHtml == null) {
          try (InputStream inputStream =
              DashboardServer.class.getClassLoader().getResourceAsStream("static/index.html")) {
            Preconditions.checkNotNull(inputStream, "Cannot find index file.");
            try (InputStreamReader isr =
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {
              StringBuilder sb = new StringBuilder();
              String line;
              while ((line = br.readLine()) != null) {
                sb.append(line);
              }
              indexHtml = sb.toString();
            }
          } catch (IOException e) {
            throw new UncheckedIOException("Load index html failed", e);
          }
        }
      }
    }
    return indexHtml;
  }

  public Consumer<StaticFileConfig> configStaticFiles() {
    return staticFiles -> {
      staticFiles.hostedPath = "/";
      // change to host files on a sub path, like '/assets'
      staticFiles.directory = "/static";
      // the directory where your files are located
      staticFiles.location = Location.CLASSPATH;
      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
      staticFiles.precompress = false;
      // if the files should be pre-compressed and cached in memory (optimization)
      staticFiles.aliasCheck = null;
      // you can configure this to enable symlinks (= ContextHandler.ApproveAliases())
      // staticFiles.headers = Map.of(...);
      // headers that will be set for the files
      staticFiles.skipFileFunction = req -> false;
      // you can use this to skip certain files in the dir, based on the HttpServletRequest
    };
  }

  public EndpointGroup endpoints() {
    return () -> {
      /*backend routers*/
      path(
          "",
          () -> {
            get(
                "/swagger-docs",
                ctx -> {
                  InputStream openapiStream =
                      getClass().getClassLoader().getResourceAsStream("openapi/openapi.yaml");
                  if (openapiStream == null) {
                    ctx.status(404).result("OpenAPI specification file not found");
                  } else {
                    ctx.result(openapiStream);
                  }
                });
            // static files
            get(
                "/{page}",
                ctx -> {
                  String fileName = ctx.pathParam("page");
                  if (fileName.endsWith("ico")) {
                    ctx.contentType(ContentType.IMAGE_ICO);
                    ctx.result(
                        Objects.requireNonNull(
                            DashboardServer.class
                                .getClassLoader()
                                .getResourceAsStream("static/" + fileName)));
                  } else {
                    ctx.html(getIndexFileContent());
                  }
                });
            get("/hive-tables/upgrade", ctx -> ctx.html(getIndexFileContent()));
          });

      // for dashboard api
      path(
          "/api/ams/v1",
          () -> {
            // login controller
            get("/login/current", loginController::getCurrent);
            post("/login", loginController::login);
            post("/logout", loginController::logout);
          });

      // for open api
      path("/api/ams/v1", apiGroup());
    };
  }

  private EndpointGroup apiGroup() {
    return () -> {
      // table apis
      path(
          "/tables",
          () -> {
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/details",
                tableController::getTableDetail);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/hive/details",
                tableController::getHiveTableDetail);
            post(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/upgrade",
                tableController::upgradeHiveTable);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/upgrade/status",
                tableController::getUpgradeStatus);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/optimizing-processes",
                tableController::getOptimizingProcesses);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/optimizing-types",
                tableController::getOptimizingTypes);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/optimizing-processes/{processId}/tasks",
                tableController::getOptimizingProcessTasks);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/snapshots",
                tableController::getTableSnapshots);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/snapshots/{snapshotId}/detail",
                tableController::getSnapshotDetail);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/partitions",
                tableController::getTablePartitions);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/partitions/{partition}/files",
                tableController::getPartitionFileListInfo);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/operations",
                tableController::getTableOperations);
            get("/catalogs/{catalog}/dbs/{db}/tables/{table}/tags", tableController::getTableTags);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/branches",
                tableController::getTableBranches);
            get(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/consumers",
                tableController::getTableConsumerInfos);
            post(
                "/catalogs/{catalog}/dbs/{db}/tables/{table}/optimizing-processes/{processId}/cancel",
                tableController::cancelOptimizingProcess);
          });
      get("/upgrade/properties", tableController::getUpgradeHiveTableProperties);

      // catalog apis
      path(
          "/catalogs",
          () -> {
            get("/{catalog}/databases/{db}/tables", tableController::getTableList);
            get("/{catalog}/databases", tableController::getDatabaseList);
            get("", tableController::getCatalogs);
            post("", catalogController::createCatalog);
            get("metastore/types", catalogController::getCatalogTypeList);
            get("/{catalogName}", catalogController::getCatalogDetail);
            delete("/{catalogName}", catalogController::deleteCatalog);
            put("/{catalogName}", catalogController::updateCatalog);
            get("/{catalogName}/delete/check", catalogController::catalogDeleteCheck);
            get("/{catalogName}/config/{type}/{key}", catalogController::getCatalogConfFileContent);
          });

      // optimizing api
      path(
          "/optimize",
          () -> {
            get("/actions", optimizerGroupController::getActions);
            get(
                "/optimizerGroups/{optimizerGroup}/tables",
                optimizerGroupController::getOptimizerTables);
            get(
                "/optimizerGroups/{optimizerGroup}/optimizers",
                optimizerGroupController::getOptimizers);
            get("/optimizerGroups", optimizerGroupController::getOptimizerGroups);
            get(
                "/optimizerGroups/{optimizerGroup}/info",
                optimizerGroupController::getOptimizerGroupInfo);
            post(
                "/optimizerGroups/{optimizerGroup}/optimizers",
                optimizerGroupController::scaleOutOptimizer);
            post("/optimizers", optimizerController::createOptimizer);
            delete("/optimizers/{jobId}", optimizerController::releaseOptimizer);
            get("/resourceGroups", optimizerGroupController::getResourceGroup);
            post("/resourceGroups", optimizerGroupController::createResourceGroup);
            put("/resourceGroups", optimizerGroupController::updateResourceGroup);
            delete(
                "/resourceGroups/{resourceGroupName}",
                optimizerGroupController::deleteResourceGroup);
            get(
                "/resourceGroups/{resourceGroupName}/delete/check",
                optimizerGroupController::deleteCheckResourceGroup);
            get("/containers/get", optimizerGroupController::getContainers);
          });

      // console apis
      path(
          "/terminal",
          () -> {
            get("/examples", terminalController::getExamples);
            get("/examples/{exampleName}", terminalController::getSqlExamples);
            post("/catalogs/{catalog}/execute", terminalController::executeScript);
            get("/{sessionId}/logs", terminalController::getLogs);
            get("/{sessionId}/result", terminalController::getSqlResult);
            put("/{sessionId}/stop", terminalController::stopSql);
            get("/latestInfos/", terminalController::getLatestInfo);
          });

      // file apis
      path(
          "/files",
          () -> {
            post("", platformFileInfoController::uploadFile);
            get("/{fileId}", platformFileInfoController::downloadFile);
          });

      // setting apis
      path(
          "/settings",
          () -> {
            get("/containers", settingController::getContainerSetting);
            get("/system", settingController::getSystemSetting);
          });

      // health api
      get("/health/status", healthCheckController::healthCheck);

      // version api
      get("/versionInfo", versionController::getVersionInfo);

      // overview apis
      path(
          "/overview",
          () -> {
            get("/summary", overviewController::getSummary);
            get("/resource", overviewController::getResourceUsageHistory);
            get("/optimizing", overviewController::getOptimizingStatus);
            get("/dataSize", overviewController::getDataSizeHistory);
            get("/top", overviewController::getTopTables);
          });

      // api token apis
      path(
          "/api/token",
          () -> {
            post("/create", apiTokenController::createApiToken);
            post("/delete", apiTokenController::deleteApiToken);
            get("/info", apiTokenController::getApiTokens);
            post("/calculate/signature", apiTokenController::calculateSignature);
            post("/calculate/encryptString", apiTokenController::getEncryptStringFromQueryParam);
          });
    };
  }

  public void preHandleRequest(Context ctx) {
    String uriPath = ctx.path();
    if (inWhiteList(uriPath)) {
      return;
    }
    String requestSource = ctx.header(X_REQUEST_SOURCE_HEADER);
    boolean isWebRequest = X_REQUEST_SOURCE_WEB.equalsIgnoreCase(requestSource);

    if (isWebRequest) {
      if (null == ctx.sessionAttribute("user")) {
        throw new ForbiddenException("User session attribute is missed for url: " + uriPath);
      }
      return;
    }
    if (null != basicAuthProvider || null != bearerAuthProvider) {
      Principal authPrincipal;
      if (null != basicAuthProvider) {
        authPrincipal =
            basicAuthProvider.authenticate(
                HttpAuthenticationFactory.getPasswordCredential(ctx, proxyClientIpHeader));
      } else {
        authPrincipal =
            bearerAuthProvider.authenticate(
                HttpAuthenticationFactory.getBearerTokenCredential(ctx, proxyClientIpHeader));
      }
      LOG.info(
          "Authenticated principal: {}, URI: {}",
          authPrincipal != null ? authPrincipal.getName() : "null",
          uriPath);
    } else {
      apiTokenController.checkApiToken(ctx);
    }
  }

  public void handleException(Exception e, Context ctx) {
    if (e instanceof ForbiddenException) {
      // request doesn't start with /ams is  page request. we return index.html
      if (!ctx.req.getRequestURI().startsWith("/api/ams")) {
        ctx.html(getIndexFileContent());
      } else {
        ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "Please login first", ""));
      }
    } else if (e instanceof SignatureCheckException) {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "Signature check failed", ""));
    } else {
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage(), ""));
    }
    LOG.error("An error occurred while processing the url:{}", ctx.url(), e);
  }

  private static final String[] URL_WHITE_LIST = {
    "/api/ams/v1/versionInfo",
    "/api/ams/v1/login",
    "/api/ams/v1/health/status",
    "/api/ams/v1/login/current",
    "/",
    "/overview",
    "/introduce",
    "/tables",
    "/optimizers",
    "/login",
    "/terminal",
    "/hive-tables/upgrade",
    "/hive-tables",
    "/index.html",
    "/favicon.ico",
    "/assets/*",
    "/openapi-ui",
    "/openapi-ui/*",
    "/swagger-docs",
    "/api/ams/v1/api/token/calculate/signature",
    "/api/ams/v1/api/token/calculate/encryptString",
    RestCatalogService.ICEBERG_REST_API_PREFIX + "/*"
  };

  private static boolean inWhiteList(String uri) {
    for (String item : URL_WHITE_LIST) {
      if (item.endsWith("*")) {
        if (uri.startsWith(item.substring(0, item.length() - 1))) {
          return true;
        }
      } else {
        if (uri.equals(item)) {
          return true;
        }
      }
    }
    return false;
  }
}
