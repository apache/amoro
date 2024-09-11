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
import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.staticfiles.StaticFileConfig;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.RestCatalogService;
import org.apache.amoro.server.dashboard.controller.CatalogController;
import org.apache.amoro.server.dashboard.controller.HealthCheckController;
import org.apache.amoro.server.dashboard.controller.LoginController;
import org.apache.amoro.server.dashboard.controller.OptimizerController;
import org.apache.amoro.server.dashboard.controller.OverviewController;
import org.apache.amoro.server.dashboard.controller.PlatformFileInfoController;
import org.apache.amoro.server.dashboard.controller.SettingController;
import org.apache.amoro.server.dashboard.controller.TableController;
import org.apache.amoro.server.dashboard.controller.TerminalController;
import org.apache.amoro.server.dashboard.controller.VersionController;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.dashboard.utils.ParamSignatureCalculator;
import org.apache.amoro.server.exception.ForbiddenException;
import org.apache.amoro.server.exception.SignatureCheckException;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.terminal.TerminalManager;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DashboardServer {

  public static final Logger LOG = LoggerFactory.getLogger(DashboardServer.class);

  private static final String AUTH_TYPE_BASIC = "basic";

  private final CatalogController catalogController;
  private final HealthCheckController healthCheckController;
  private final LoginController loginController;
  private final OptimizerController optimizerController;
  private final PlatformFileInfoController platformFileInfoController;
  private final SettingController settingController;
  private final TableController tableController;
  private final TerminalController terminalController;
  private final VersionController versionController;
  private final OverviewController overviewController;

  private final String authType;
  private final String basicAuthUser;
  private final String basicAuthPassword;

  public DashboardServer(
      Configurations serviceConfig,
      TableService tableService,
      DefaultOptimizingService optimizerManager,
      TerminalManager terminalManager) {
    PlatformFileManager platformFileManager = new PlatformFileManager();
    this.catalogController = new CatalogController(tableService, platformFileManager);
    this.healthCheckController = new HealthCheckController();
    this.loginController = new LoginController(serviceConfig);
    this.optimizerController = new OptimizerController(tableService, optimizerManager);
    this.platformFileInfoController = new PlatformFileInfoController(platformFileManager);
    this.settingController = new SettingController(serviceConfig, optimizerManager);
    ServerTableDescriptor tableDescriptor = new ServerTableDescriptor(tableService, serviceConfig);
    this.tableController = new TableController(tableService, tableDescriptor, serviceConfig);
    this.terminalController = new TerminalController(terminalManager);
    this.versionController = new VersionController();
    this.overviewController = new OverviewController();

    this.authType = serviceConfig.get(AmoroManagementConf.HTTP_SERVER_REST_AUTH_TYPE);
    this.basicAuthUser = serviceConfig.get(AmoroManagementConf.ADMIN_USERNAME);
    this.basicAuthPassword = serviceConfig.get(AmoroManagementConf.ADMIN_PASSWORD);
  }

  private String indexHtml = "";

  // read index.html content
  public String getIndexFileContent() {
    try {
      if ("".equals(indexHtml)) {
        try (InputStream fileName =
            DashboardServer.class.getClassLoader().getResourceAsStream("static/index.html")) {
          Preconditions.checkNotNull(fileName, "Cannot find index file.");
          try (InputStreamReader isr =
                  new InputStreamReader(fileName, StandardCharsets.UTF_8.newDecoder());
              BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
              // process the line
              sb.append(line);
            }
            indexHtml = sb.toString();
          }
        }
      }
      return indexHtml;
    } catch (IOException e) {
      throw new UncheckedIOException("Load index html filed", e);
    }
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
          "/ams/v1",
          () -> {
            // login controller
            get("/login/current", loginController::getCurrent);
            post("/login", loginController::login);
            post("/logout", loginController::logout);
          });
      path("ams/v1", apiGroup());

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
            get(
                "/optimizerGroups/{optimizerGroup}/tables",
                optimizerController::getOptimizerTables);
            get("/optimizerGroups/{optimizerGroup}/optimizers", optimizerController::getOptimizers);
            get("/optimizerGroups", optimizerController::getOptimizerGroups);
            get(
                "/optimizerGroups/{optimizerGroup}/info",
                optimizerController::getOptimizerGroupInfo);
            delete(
                "/optimizerGroups/{optimizerGroup}/optimizers/{jobId}",
                optimizerController::releaseOptimizer);
            post(
                "/optimizerGroups/{optimizerGroup}/optimizers",
                optimizerController::scaleOutOptimizer);
            get("/resourceGroups", optimizerController::getResourceGroup);
            post("/resourceGroups", optimizerController::createResourceGroup);
            put("/resourceGroups", optimizerController::updateResourceGroup);
            delete("/resourceGroups/{resourceGroupName}", optimizerController::deleteResourceGroup);
            get(
                "/resourceGroups/{resourceGroupName}/delete/check",
                optimizerController::deleteCheckResourceGroup);
            get("/containers/get", optimizerController::getContainers);
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
    };
  }

  public void preHandleRequest(Context ctx) {
    String uriPath = ctx.path();
    if (needApiKeyCheck(uriPath)) {
      if (AUTH_TYPE_BASIC.equalsIgnoreCase(authType)) {
        BasicAuthCredentials cred = ctx.basicAuthCredentials();
        if (!(basicAuthUser.equals(cred.component1())
            && basicAuthPassword.equals(cred.component2()))) {
          throw new SignatureCheckException(
              "Failed to authenticate via basic authentication for url:" + uriPath);
        }
      } else {
        checkApiToken(
            ctx.url(), ctx.queryParam("apiKey"), ctx.queryParam("signature"), ctx.queryParamMap());
      }
    } else if (needLoginCheck(uriPath)) {
      if (null == ctx.sessionAttribute("user")) {
        throw new ForbiddenException("User session attribute is missed for url: " + uriPath);
      }
    }
  }

  public void handleException(Exception e, Context ctx) {
    if (e instanceof ForbiddenException) {
      // request doesn't start with /ams is  page request. we return index.html
      if (!ctx.req.getRequestURI().startsWith("/ams")) {
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

  private static final String[] urlWhiteList = {
    "/ams/v1/versionInfo",
    "/ams/v1/login",
    "/ams/v1/health/status",
    "/ams/v1/login/current",
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
    RestCatalogService.ICEBERG_REST_API_PREFIX + "/*"
  };

  private static boolean needLoginCheck(String uri) {
    for (String item : urlWhiteList) {
      if (item.endsWith("*")) {
        if (uri.startsWith(item.substring(0, item.length() - 1))) {
          return false;
        }
      } else {
        if (uri.equals(item)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean needApiKeyCheck(String uri) {
    return uri.startsWith("/api/ams");
  }

  private void checkApiToken(
      String requestUrl, String apiKey, String signature, Map<String, List<String>> params) {
    String plainText;
    String encryptString;
    String signCal;

    APITokenManager apiTokenService = new APITokenManager();
    try {
      String secret = apiTokenService.getSecretByKey(apiKey);

      if (secret == null) {
        throw new SignatureCheckException();
      }

      if (apiKey == null || signature == null) {
        throw new SignatureCheckException();
      }

      params.remove("apiKey");
      params.remove("signature");

      String paramString = ParamSignatureCalculator.generateParamStringWithValueList(params);

      if (StringUtils.isBlank(paramString)) {
        encryptString = ParamSignatureCalculator.SIMPLE_DATE_FORMAT.format(new Date());
      } else {
        encryptString = paramString;
      }

      plainText = String.format("%s%s%s", apiKey, encryptString, secret);
      signCal = ParamSignatureCalculator.getMD5(plainText);
      LOG.debug(
          "Calculated signature for url:{}, plain text:{}, calculated signature:{}, signature in request: {}",
          requestUrl,
          plainText,
          signCal,
          signature);

      if (!signature.equals(signCal)) {
        throw new SignatureCheckException(
            String.format(
                "Check signature for url:%s failed,"
                    + " calculated signature:%s, signature in request:%s",
                requestUrl, signCal, signature));
      }
    } catch (Exception e) {
      throw new SignatureCheckException("Check url signature failed", e);
    }
  }
}
