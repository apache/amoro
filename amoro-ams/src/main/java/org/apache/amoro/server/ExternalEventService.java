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

package org.apache.amoro.server;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.amoro.exception.ForbiddenException;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.scheduler.inline.InlineTableExecutors;
import org.apache.amoro.table.TableIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalEventService extends PersistentBase {
  private static final Logger LOG = LoggerFactory.getLogger(ExternalEventService.class);

  public static final String REFRESH_REST_API_PREFIX = "/api/ams/v1/refresh/";

  public ExternalEventService() {}

  public EndpointGroup endpoints() {
    return () -> {
      // for refresh rest api
      path(
          REFRESH_REST_API_PREFIX,
          () -> {
            post(
                "/catalog/{catalog}/database/{database}/table/{table}",
                this::receiveExternalTableRefreshEvent);
          });
    };
  }

  public boolean needHandleException(Context ctx) {
    return ctx.req.getRequestURI().startsWith(REFRESH_REST_API_PREFIX);
  }

  public void handleException(Exception e, Context ctx) {
    if (e instanceof ForbiddenException) {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "Please check authentication", ""));

    } else if (e instanceof SignatureCheckException) {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "Signature check failed", ""));
    } else {
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage(), ""));
    }
    LOG.error("Error when handle refresh event", e);
  }

  /** POST /api/ams/refresh/catalog/{catalog}/database/{database}/table/{table} */
  public void receiveExternalTableRefreshEvent(Context ctx) {
    String catalog = ctx.pathParam("catalog").trim().replaceAll("^\"|\"$", "");
    String database = ctx.pathParam("database").trim().replaceAll("^\"|\"$", "");
    String table = ctx.pathParam("table").trim().replaceAll("^\"|\"$", "");

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, database, table);
    boolean result =
        InlineTableExecutors.getInstance()
            .getTableRefreshingExecutor()
            .addTableToRefresh(tableIdentifier);
    if (result) {
      ctx.json(OkResponse.of("Table added to wait for refreshing"));
    }
    ctx.json(ErrorResponse.of("Table not managed by event trigger"));
  }
}
