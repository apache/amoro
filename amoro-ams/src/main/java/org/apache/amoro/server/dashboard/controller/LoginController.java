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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.authentication.DefaultPasswordCredential;
import org.apache.amoro.server.authentication.HttpAuthenticationFactory;
import org.apache.amoro.server.authorization.Role;
import org.apache.amoro.server.authorization.RoleResolver;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.utils.PreconditionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

/** The controller that handles login requests. */
public class LoginController {
  public static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

  private final PasswdAuthenticationProvider loginAuthProvider;
  private final RoleResolver roleResolver;

  public LoginController(Configurations serviceConfig, RoleResolver roleResolver) {
    this.loginAuthProvider =
        HttpAuthenticationFactory.getPasswordAuthenticationProvider(
            serviceConfig.get(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_PROVIDER), serviceConfig);
    this.roleResolver = roleResolver;
  }

  /** Get current user. */
  public void getCurrent(Context ctx) {
    SessionInfo user = ctx.sessionAttribute("user");
    ctx.json(OkResponse.of(user));
  }

  /** handle login post request. */
  public void login(Context ctx) {
    // ok
    Map<String, String> bodyParams = ctx.bodyAsClass(Map.class);
    String user = bodyParams.get("user");
    String pwd = bodyParams.get("password");
    PreconditionUtils.checkNotNullOrEmpty(user, "user");
    PreconditionUtils.checkNotNullOrEmpty(pwd, "password");
    DefaultPasswordCredential credential = new DefaultPasswordCredential(user, pwd);

    // Step 1: Authenticate the user
    Principal principal;
    try {
      principal = this.loginAuthProvider.authenticate(credential);
    } catch (Exception e) {
      LOG.error("authenticate user {} failed", user, e);
      String causeMessage = e.getMessage() != null ? e.getMessage() : "unknown error";
      throw new RuntimeException("invalid user " + user + " or password! Cause: " + causeMessage);
    }

    // Step 2: Resolve user role (LDAP group lookup)
    String authenticatedUser = principal.getName();
    Role role;
    try {
      role = roleResolver.resolve(authenticatedUser);
    } catch (Exception e) {
      LOG.error(
          "Role resolution failed for user {}. "
              + "Authentication succeeded but LDAP group query failed. "
              + "Check authorization.ldap-role-mapping config (bind-dn/bind-password/admin-group-dn).",
          authenticatedUser,
          e);
      throw new RuntimeException("Login failed due to a server error during role resolution");
    }

    SessionInfo sessionInfo =
        new SessionInfo(authenticatedUser, System.currentTimeMillis() + "", role);
    ctx.sessionAttribute("user", sessionInfo);
    ctx.json(OkResponse.of(sessionInfo));
  }

  /** handle logout post request. */
  public void logout(Context ctx) {
    ctx.removeCookie("JSESSIONID");
    ctx.json(OkResponse.ok());
  }

  /** Session user payload persisted in the server-side HTTP session. */
  public static class SessionInfo implements Serializable {
    String userName;
    String loginTime;
    Role role;

    public SessionInfo(String username, String loginTime, Role role) {
      this.userName = username;
      this.loginTime = loginTime;
      this.role = role;
    }

    public String getUserName() {
      return userName;
    }

    public String getLoginTime() {
      return loginTime;
    }

    public Role getRole() {
      return role;
    }
  }
}
