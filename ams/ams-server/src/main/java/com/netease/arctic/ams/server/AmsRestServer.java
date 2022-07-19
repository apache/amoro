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

package com.netease.arctic.ams.server;

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.server.controller.HealthCheckController;
import com.netease.arctic.ams.server.controller.LoginController;
import com.netease.arctic.ams.server.controller.OptimizerController;
import com.netease.arctic.ams.server.controller.TableController;
import com.netease.arctic.ams.server.controller.TerminalController;
import com.netease.arctic.ams.server.controller.VersionController;
import com.netease.arctic.ams.server.controller.response.ErrorResponse;
import com.netease.arctic.ams.server.exception.ForbiddenException;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import io.javalin.http.staticfiles.Location;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;


public class AmsRestServer {
  public static final Logger LOG = LoggerFactory.getLogger("AmsRestServer");

  public static void startRestServer(Integer port) {
    Javalin app = Javalin.create(config -> {
      config.addStaticFiles(staticFiles -> {
        staticFiles.hostedPath = "/";
        // change to host files on a subpath, like '/assets'
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
      });

      //redirect the static page url to index.html
      config.addSinglePageRoot("/overview", "/static/index.html", Location.CLASSPATH);
      config.addSinglePageRoot("/introduce", "/static/index.html", Location.CLASSPATH);
      config.addSinglePageRoot("/table", "/static/index.html", Location.CLASSPATH);
      config.addSinglePageRoot("/optimize", "/static/index.html", Location.CLASSPATH);
      config.addSinglePageRoot("/login", "/static/index.html", Location.CLASSPATH);
      config.addSinglePageRoot("/terminal", "/static/index.html", Location.CLASSPATH);
      config.addSinglePageRoot("/optimizing", "/static/index.html", Location.CLASSPATH);

      config.sessionHandler(() -> new SessionHandler());
      config.enableCorsForAllOrigins();
    }).start(port);
    LOG.info("Javalin Rest server start at {}!!!", port);

    // before
    app.before(ctx -> {
      String uriPath = ctx.path();
      if (needLoginCheck(uriPath)) {
        if (null == ctx.sessionAttribute("user")) {
          LOG.info("session info: {}", ctx.sessionAttributeMap() == null ? null : JSONObject.toJSONString(
                  ctx.sessionAttributeMap()));
          throw new ForbiddenException();
        }
      }
    });

    app.routes(() -> {
      /*backend routers*/
      path("", () -> {
        //  /docs/latest can't be locationed to the index.html, so we add rule to redict to it.
        get("/docs/latest", ctx -> ctx.redirect("/docs/latest/index.html"));
      });
      path("/ams/v1", () -> {
        /** login controller**/
        get("/login/current", LoginController::getCurrent);
        post("/login", LoginController::login);

        /**  table controller **/
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/details", TableController::getTableDetail);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/optimize", TableController::getOptimizeInfo);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/transactions",
                TableController::getTableTransactions);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/transactions/{transactionId}/detail",
                TableController::getTransactionDetail);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/partitions", TableController::getTablePartitions);
        get("/tables/catalogs/{catalog}/dbs/{db}/tables/{table}/partitions/{partition}/files",
                TableController::getPartitionFileListInfo);
        get("/catalogs/{catalog}/databases/{db}/tables", TableController::getTableList);
        get("/catalogs/{catalog}/databases", TableController::getDatabaseList);
        get("/catalogs", TableController::getCatalogs);

        /** optimize controller **/
        get("/optimize/optimizerGroups/{optimizerGroup}/tables", OptimizerController::getOptimizerTables);
        get("/optimize/optimizerGroups/{optimizerGroup}/optimizers", OptimizerController::getOptimizers);
        get("/optimize/optimizerGroups", OptimizerController::getOptimizerGroups);
        get("/optimize/optimizerGroups/{optimizerGroup}/info", OptimizerController::getOptimizerGroupInfo);
        delete("/optimize/optimizerGroups/{optimizerGroup}/optimizers/{jobId}", OptimizerController::releaseOptimizer);
        post("/optimize/optimizerGroups/{optimizerGroup}/optimizers", OptimizerController::scaleOutOptimizer);

        /** console controller **/
        get("/terminal/examples", TerminalController::getExamples);
        get("/terminal/examples/{exampleName}", TerminalController::getSqlExamples);
        post("/terminal/catalogs/{catalog}/execute", TerminalController::executeSql);
        get("/terminal/{sessionId}/logs", TerminalController::getLogs);
        get("/terminal/{sessionId}/result", TerminalController::getSqlStatus);
        put("/terminal/{sessionId}/stop", TerminalController::stopSql);
        get("/terminal/latestInfos/", TerminalController::getLatestInfo);

        /** health check **/
        get("/health/status", HealthCheckController::healthCheck);

        /** version controller **/
        get("/versionInfo", VersionController::getVersionInfo);
      });
    });

    // after-handler
    app.after(ctx -> {
    });

    // exception-handler
    app.exception(Exception.class, (e, ctx) -> {
      if (e instanceof ForbiddenException) {
        ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "need login! before request", ""));
        return;
      } else {
        LOG.error("Failed to handle request", e);
        JSONObject obj = new JSONObject();
        ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage(), ""));
      }
    });

    // default response handle
    app.error(HttpCode.NOT_FOUND.getStatus(), ctx -> {
      ctx.json(new ErrorResponse(HttpCode.NOT_FOUND, "page not found!", ""));
    });

    app.error(HttpCode.INTERNAL_SERVER_ERROR.getStatus(),ctx -> {
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "internal error!", ""));
    });
  }

  private static final String[] urlWhiteList = {
    "/ams/v1/login",
    "/",
    "/overview",
    "/introduce",
    "/table",
    "/optimize",
    "/login",
    "/terminal",
    "/index.html",
    "/favicon.ico",
    "/js/*",
    "/img/*",
    "/css/*"
  };

  private static final boolean needLoginCheck(String uri) {
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
}
