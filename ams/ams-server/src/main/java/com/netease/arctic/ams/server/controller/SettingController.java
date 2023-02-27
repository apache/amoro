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

package com.netease.arctic.ams.server.controller;

import com.alibaba.fastjson.JSON;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import io.javalin.http.Context;

import java.util.LinkedHashMap;

import static com.netease.arctic.ams.server.config.ArcticMetaStoreConf.LOGIN_PASSWORD;
import static com.netease.arctic.ams.server.config.ArcticMetaStoreConf.LOGIN_USERNAME;
import static com.netease.arctic.ams.server.config.ArcticMetaStoreConf.MYBATIS_CONNECTION_PASSWORD;
import static com.netease.arctic.ams.server.config.ArcticMetaStoreConf.MYBATIS_CONNECTION_USER_NAME;

public class SettingController extends RestBaseController {
  private static String MASK_STRING = "******";
  
  /**
   * get systemSetting
   *
   * @param ctx
   */
  public static void getSystemSetting(Context ctx) {
    try {
      LinkedHashMap<String, Object> config = ArcticMetaStore.getSystemSettingFromYaml();
      // hidden password and username
      config.replace(MYBATIS_CONNECTION_PASSWORD.key(), MASK_STRING);
      config.replace(MYBATIS_CONNECTION_USER_NAME.key(), MASK_STRING);
      config.replace(LOGIN_USERNAME.key(), MASK_STRING);
      config.replace(LOGIN_PASSWORD.key(), MASK_STRING);
      LinkedHashMap<String, Object> result = new LinkedHashMap<>();
      config.forEach((k, v) -> {
        result.put(k, JSON.toJSONString(v));
      });
      ctx.json(OkResponse.of(result));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
