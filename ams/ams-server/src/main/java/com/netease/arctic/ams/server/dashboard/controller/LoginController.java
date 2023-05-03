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

package com.netease.arctic.ams.server.dashboard.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.server.dashboard.response.ErrorResponse;
import com.netease.arctic.ams.server.dashboard.response.OkResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;

import java.io.Serializable;

/**
 * login controller.

 */
public class LoginController {

  /**
   * get current user.
   */
  public void getCurrent(Context ctx) {
    SessionInfo user = ctx.sessionAttribute("user");
    ctx.json(OkResponse.of(user));
  }

  /**
   * handle login post request.
   **/
  public void login(Context ctx) {
    // ok
    JSONObject postBody = ctx.bodyAsClass(JSONObject.class);
    if (postBody.get("user").equals("admin") && (postBody.get("password").equals("admin"))) {
      ctx.sessionAttribute("user", new SessionInfo("admin", System.currentTimeMillis() + ""));
      ctx.json(OkResponse.of("success"));
    } else {
      ctx.json(new ErrorResponse(HttpCode.FORBIDDEN, "bad user " + postBody.get("user") + "or password!",
              null));
    }
  }

  static class SessionInfo implements Serializable {
    String userName;
    String loginTime;

    public SessionInfo(String username, String loginTime) {
      this.userName = username;
      this.loginTime = loginTime;
    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public String getLoginTime() {
      return loginTime;
    }

    public void setLoginTime(String loginTime) {
      this.loginTime = loginTime;
    }
  }
}
