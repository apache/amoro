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

package com.netease.arctic.server.dashboard;

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.server.DefaultOptimizingService;
import com.netease.arctic.server.IcebergRestCatalogService;
import com.netease.arctic.server.dashboard.controller.CatalogController;
import com.netease.arctic.server.dashboard.controller.HealthCheckController;
import com.netease.arctic.server.dashboard.controller.LoginController;
import com.netease.arctic.server.dashboard.controller.OptimizerController;
import com.netease.arctic.server.dashboard.controller.PlatformFileInfoController;
import com.netease.arctic.server.dashboard.controller.SettingController;
import com.netease.arctic.server.dashboard.controller.TableController;
import com.netease.arctic.server.dashboard.controller.TerminalController;
import com.netease.arctic.server.dashboard.controller.VersionController;
import com.netease.arctic.server.dashboard.response.ErrorResponse;
import com.netease.arctic.server.dashboard.utils.ParamSignatureCalculator;
import com.netease.arctic.server.exception.ForbiddenException;
import com.netease.arctic.server.exception.SignatureCheckException;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.server.terminal.TerminalManager;
import com.netease.arctic.server.utils.Configurations;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.staticfiles.StaticFileConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class DashboardServer {

  public static final Logger LOG = LoggerFactory.getLogger(DashboardServer.class);

  public static final String API_KEY = "apiKey";
  public static final String SIGNATURE = "signature";

  private final CatalogController catalogController;
  private final HealthCheckController healthCheckController;
  private final LoginController loginController;
  private final OptimizerController optimizerController;
  private final PlatformFileInfoController platformFileInfoController;
  private final SettingController settingController;
  private final TableController tableController;
  private final TerminalController terminalController;
  private final VersionController versionController;

  public DashboardServer(
      Configurations serviceConfig, TableService tableService,
      DefaultOptimizingService optimizerManager, TerminalManager terminalManager) {
    PlatformFileManager platformFileManager = new PlatformFileManager();
    this.catalogController = new CatalogController(tableService, platformFileManager);
    this.healthCheckController = new HealthCheckController();
    this.loginController = new LoginController(serviceConfig);
    this.optimizerController = new OptimizerController(tableService, optimizerManager);
    this.platformFileInfoController = new PlatformFileInfoController(platformFileManager);
    this.settingController = new SettingController(serviceConfig, optimizerManager);
    ServerTableDescriptor tableDescriptor = new ServerTableDescriptor(tableService);
    this.tableController = new TableController(tableService, tableDescriptor, serviceConfig);
    this.terminalController = new TerminalController(terminalManager);
    this.versionController = new VersionController();
  }

  private String indexHtml = "";

  // read index.html content
  public String getFileContent() throws IOException {
    if ("".equals(indexHtml)) {
      try (InputStream fileName = DashboardServer.class.getClassLoader().getResourceAsStream("static/index.html")) {
        try (InputStreamReader isr = new InputStreamReader(fileName, StandardCharsets.UTF_8.newDecoder());
            BufferedReader br = new BufferedReader(isr)) {
          StringBuilder sb = new StringBuilder();
          String line;
          while ((line = br.readLine()) != null) {
            //process the line
            sb.append(line);
          }
          indexHtml = sb.toString();
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
      //staticFiles.headers = Map.of(...);
      // headers that will be set for the files
      staticFiles.skipFileFunction = req -> false;
      // you can use this to skip certain files in the dir, based on the HttpServletRequest
    };
  }

  public EndpointGroup endpoints() {
    return () -> {
      /*backend routers*/
      path("", () -> {
        //  /docs/latest can't be located to the index.html, so we add rule to redirect to it.
        get("/docs/latest", ctx -> ctx.redirect("/docs/latest/index.html"));
        // unify all addSinglePageRoot(like /tables, /optimizers etc) configure here
        get("/{page}", ctx -> {
          String fileName = ctx.pathParam("page");
          if (fileName.endsWith("ico")) {
            ctx.contentType(ContentType.IMAGE_ICO);
            ctx.result(DashboardServer.class.getClassLoader().getResourceAsStream("static/" + fileName));
          } else {
            ctx.html(getFileContent());
          }
        });
        get("/hive-tables/upgrade", ctx -> ctx.html(getFileContent()));
      });
      path("/ams/v1", () -> {
        path("/login", () -> {
          get("/current", loginController::getCurrent);
          post("", loginController::login);
        });

        path("/tables", () -> {
          final String path = "/catalogs/{catalog}/dbs/{db}/tables/{table}";

          get(path + "/details", tableController::getTableDetail);
          get(path + "/hive/details", tableController::getHiveTableDetail);
          post(path + "/upgrade", tableController::upgradeHiveTable);
          get(path + "/upgrade/status", tableController::getUpgradeStatus);
          get("/upgrade/properties", tableController::getUpgradeHiveTableProperties);
          get(path + "/optimizing-processes", tableController::getOptimizingProcesses);
          get(path + "/transactions", tableController::getTableTransactions);
          get(path + "/tables/{table}/transactions/{transactionId}/detail", tableController::getTransactionDetail);
          get(path + "/partitions", tableController::getTablePartitions);
          get(path + "/partitions/{partition}/files", tableController::getPartitionFileListInfo);
          get(path + "/operations", tableController::getTableOperations);
        });

        path("/catalogs", () -> {

          get("", catalogController::getCatalogs);
          get("/{catalogName}/dbs", catalogController::getDatabaseList);
          get("/{catalogName}/dbs/{db}/tables", catalogController::getTableList);

          post("", catalogController::createCatalog);
          get("/types", catalogController::getCatalogTypeList);
          get("/metastore/types", catalogController::getCatalogTypeList);
          get("/{catalogName}", catalogController::getCatalogDetail);
          delete("/{catalogName}", catalogController::deleteCatalog);
          put("/{catalogName}", catalogController::updateCatalog);
          get("/{catalogName}/delete/check", catalogController::catalogDeleteCheck);
          get("/{catalogName}/config/{type}/{key}", catalogController::getCatalogConfFileContent);
        });

        path("/optimize", () -> {
          get("/optimizerGroups/{optimizerGroup}/tables", optimizerController::getOptimizerTables);
          get("/optimizerGroups/{optimizerGroup}/optimizers", optimizerController::getOptimizers);
          get("/optimizerGroups", optimizerController::getOptimizerGroups);
          get("/optimizerGroups/{optimizerGroup}/info", optimizerController::getOptimizerGroupInfo);
          delete("/optimizerGroups/{optimizerGroup}/optimizers/{jobId}", optimizerController::releaseOptimizer);
          post("/optimizerGroups/{optimizerGroup}/optimizers", optimizerController::scaleOutOptimizer);
          get("/resourceGroups", optimizerController::getResourceGroup);
          post("/resourceGroups", optimizerController::createResourceGroup);
          put("/resourceGroups", optimizerController::updateResourceGroup);
          delete("/resourceGroups/{resourceGroupName}", optimizerController::deleteResourceGroup);
          get("/resourceGroups/{resourceGroupName}/delete/check", optimizerController::deleteCheckResourceGroup);
          get("/containers/get", optimizerController::getContainers);
        });

        path("/terminal", () -> {
          get("/examples", terminalController::getExamples);
          get("/examples/{exampleName}", terminalController::getSqlExamples);
          post("/catalogs/{catalog}/execute", terminalController::executeScript);
          get("/{sessionId}/logs", terminalController::getLogs);
          get("/{sessionId}/result", terminalController::getSqlResult);
          put("/{sessionId}/stop", terminalController::stopSql);
          get("/latestInfos/", terminalController::getLatestInfo);
        });

        path("/files", () -> {
          post("", platformFileInfoController::uploadFile);
          get("/{fileId}", platformFileInfoController::downloadFile);
        });

        path("/settings", () -> {
          get("/containers", settingController::getContainerSetting);
          get("/system", settingController::getSystemSetting);
        });

        get("/health/status", healthCheckController::healthCheck);

        get("/versionInfo", versionController::getVersionInfo);
      });
      // for open api
      path("/api/ams/v1", () -> {

        // table controller
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/details", tableController::getTableDetail);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/hive/details", tableController::getHiveTableDetail);
        post("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/upgrade", tableController::upgradeHiveTable);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/upgrade/status", tableController::getUpgradeStatus);
        get("/upgrade/properties", tableController::getUpgradeHiveTableProperties);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/optimizing-processes",
            tableController::getOptimizingProcesses);
        get(
            "/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/transactions",
            tableController::getTableTransactions);
        get(
            "/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/transactions/{transactionId}/detail",
            tableController::getTransactionDetail);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/partitions", tableController::getTablePartitions);
        get(
            "/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/partitions/{partition}/files",
            tableController::getPartitionFileListInfo);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/signature", tableController::getTableDetailTabToken);
        get("/catalogs/{catalog}/databases/{db}/tables", catalogController::getTableList);
        get("/catalogs/{catalog}/databases", catalogController::getDatabaseList);
        get("/catalogs", catalogController::getCatalogs);

        // optimize controller
        get("/optimize/optimizerGroups/{optimizerGroup}/tables", optimizerController::getOptimizerTables);
        get("/optimize/optimizerGroups/{optimizerGroup}/optimizers", optimizerController::getOptimizers);
        get("/optimize/optimizerGroups", optimizerController::getOptimizerGroups);
        get("/optimize/optimizerGroups/{optimizerGroup}/info", optimizerController::getOptimizerGroupInfo);
        delete("/optimize/optimizerGroups/{optimizerGroup}/optimizers/{jobId}", optimizerController::releaseOptimizer);
        post("/optimize/optimizerGroups/{optimizerGroup}/optimizers", optimizerController::scaleOutOptimizer);
        get("/optimize/resourceGroups", optimizerController::getResourceGroup);
        post("/optimize/resourceGroups", optimizerController::createResourceGroup);
        put("/optimize/resourceGroups", optimizerController::updateResourceGroup);
        delete("/optimize/resourceGroups/{resourceGroupName}", optimizerController::deleteResourceGroup);
        get("/optimize/resourceGroups/{resourceGroupName}/delete/check", optimizerController::deleteCheckResourceGroup);
        get("/optimize/containers/get", optimizerController::getContainers);

        // console controller
        get("/terminal/examples", terminalController::getExamples);
        get("/terminal/examples/{exampleName}", terminalController::getSqlExamples);
        post("/terminal/catalogs/{catalog}/execute", terminalController::executeScript);
        get("/terminal/{sessionId}/logs", terminalController::getLogs);
        get("/terminal/{sessionId}/result", terminalController::getSqlResult);
        put("/terminal/{sessionId}/stop", terminalController::stopSql);
        get("/terminal/latestInfos/", terminalController::getLatestInfo);

        // health check
        get("/health/status", healthCheckController::healthCheck);

        // version controller
        get("/versionInfo", versionController::getVersionInfo);
      });
    };
  }

  public void preHandleRequest(Context ctx) {
    String uriPath = ctx.path();
    if (needApiKeyCheck(uriPath)) {
      checkApiToken(ctx.method(), ctx.url(), ctx.queryParam(API_KEY),
          ctx.queryParam(SIGNATURE), ctx.queryParamMap());
    } else if (needLoginCheck(uriPath)) {
      if (null == ctx.sessionAttribute("user")) {
        ctx.sessionAttributeMap();
        LOG.info("session info: {}", JSONObject.toJSONString(
            ctx.sessionAttributeMap()));
        throw new ForbiddenException();
      }
    }
  }

  public void handleException(Exception e, Context ctx) {
    if (e instanceof ForbiddenException) {
      try {
        // request doesn't start with /ams is  page request. we return index.html
        if (!ctx.req.getRequestURI().startsWith("/ams")) {
          ctx.html(getFileContent());
        } else {
          ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "need login before request", ""));
        }
      } catch (Exception fe) {
        LOG.error("Failed to handle request", fe);
      }
    } else if (e instanceof SignatureCheckException) {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "Signature Exception  before request", ""));
    } else {
      LOG.error("Failed to handle request", e);
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage(), ""));
    }
  }

  private static final String[] urlWhiteList = {
      "/ams/v1/versionInfo",
      "/ams/v1/login",
      "/ams/v1/health/status",
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
      "/js/*",
      "/img/*",
      "/css/*",
      IcebergRestCatalogService.ICEBERG_REST_API_PREFIX + "/*"
  };

  private static boolean needLoginCheck(String uri) {
    for (String item : urlWhiteList) {
      if (item.endsWith("*")) {
        if (uri.startsWith(item.substring(0, item.length() - 1))) {
          return false;
        }
      } else if (uri.equals(item)) {
        return false;
      }
    }
    return true;
  }

  private boolean needApiKeyCheck(String uri) {
    return uri.startsWith("/api/ams");
  }

  private void checkApiToken(
      String requestMethod, String requestUrl, String apiKey, String signature,
      Map<String, List<String>> params) {
    String plainText;
    String encryptString;
    String signCal;

    LOG.debug("[{}] url: {}, ", requestMethod, requestUrl);

    long startTime = System.currentTimeMillis();
    APITokenManager apiTokenService = new APITokenManager();
    try {
      String secrete = apiTokenService.getSecretByKey(apiKey);

      if (secrete == null || apiKey == null || signature == null) {
        throw new SignatureCheckException();
      }

      params.remove(API_KEY);
      params.remove(SIGNATURE);

      String paramString = ParamSignatureCalculator.generateParamStringWithValueList(params);
      encryptString = StringUtils.isBlank(paramString)
          ? ParamSignatureCalculator.SIMPLE_DATE_FORMAT.format(new Date())
          : paramString;

      plainText = String.format("%s%s%s", apiKey, encryptString, secrete);
      signCal = ParamSignatureCalculator.getMD5(plainText);
      LOG.info("calculate:  plainText:{}, signCal:{}, signFromRequest: {}", plainText, signCal, signature);

      if (!signature.equals(signCal)) {
        LOG.error("Signature Check Failed !, req:{}, cal:{}", signature, signCal);
        throw new SignatureCheckException();
      }
    } catch (Exception e) {
      LOG.error("API checkApiToken error.", e);
      throw new SignatureCheckException();
    } finally {
      LOG.debug("[finish] in {} ms, [{}] {}", System.currentTimeMillis() - startTime, requestMethod, requestUrl);
    }
  }
}
