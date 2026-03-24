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

package org.apache.amoro.server.authorization;

import io.javalin.http.Context;

import java.util.Optional;

public class DashboardPrivilegeMapper {
  public Optional<AuthorizationRequest> resolve(Context ctx) {
    String method = ctx.method();
    String path = ctx.path();

    if (path.startsWith("/api/ams/v1/overview")) {
      return Optional.of(AuthorizationRequest.of(ResourceType.SYSTEM, Privilege.VIEW_SYSTEM));
    }

    if (path.startsWith("/api/ams/v1/catalogs")) {
      if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
        return Optional.of(AuthorizationRequest.of(ResourceType.CATALOG, Privilege.MANAGE_CATALOG));
      }
      return Optional.of(AuthorizationRequest.of(ResourceType.CATALOG, Privilege.VIEW_CATALOG));
    }

    if (path.startsWith("/api/ams/v1/tables") || path.equals("/api/ams/v1/upgrade/properties")) {
      if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
        return Optional.of(AuthorizationRequest.of(ResourceType.TABLE, Privilege.MANAGE_TABLE));
      }
      if (path.equals("/api/ams/v1/upgrade/properties")) {
        return Optional.of(AuthorizationRequest.of(ResourceType.TABLE, Privilege.MANAGE_TABLE));
      }
      return Optional.of(AuthorizationRequest.of(ResourceType.TABLE, Privilege.VIEW_TABLE));
    }

    if (path.startsWith("/api/ams/v1/optimize")) {
      if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
        return Optional.of(
            AuthorizationRequest.of(ResourceType.OPTIMIZER, Privilege.MANAGE_OPTIMIZER));
      }
      return Optional.of(AuthorizationRequest.of(ResourceType.OPTIMIZER, Privilege.VIEW_OPTIMIZER));
    }

    if (path.startsWith("/api/ams/v1/terminal")) {
      if ("POST".equals(method) || "PUT".equals(method)) {
        return Optional.of(AuthorizationRequest.of(ResourceType.TERMINAL, Privilege.EXECUTE_SQL));
      }
      return Optional.of(AuthorizationRequest.of(ResourceType.SYSTEM, Privilege.VIEW_SYSTEM));
    }

    if (path.startsWith("/api/ams/v1/files")
        || path.startsWith("/api/ams/v1/settings")
        || path.startsWith("/api/ams/v1/api/token")) {
      return Optional.of(AuthorizationRequest.of(ResourceType.PLATFORM, Privilege.MANAGE_PLATFORM));
    }

    return Optional.empty();
  }
}
