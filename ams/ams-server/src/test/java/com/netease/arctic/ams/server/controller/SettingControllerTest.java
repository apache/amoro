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

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.controller.response.Response;
import io.javalin.testtools.JavalinTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ArcticMetaStore.class})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class SettingControllerTest {
  private final Logger LOG = LoggerFactory.getLogger(SettingController.class);

  @Test
  public void testGetVersion() {
    mockStatic(ArcticMetaStore.class);
    when(ArcticMetaStore.getSystemSettingFromYaml()).thenAnswer((Answer<LinkedHashMap<String,Object>>) x ->
        new LinkedHashMap<String, Object>(){{
          put("arctic.ams.database.type", "derby");
          put("b", true);
          Map<String, String> mapType = new HashMap<>();
          mapType.put("c1", "v1");
          put("c", mapType);
        }});
    JavalinTest.test((app, client) -> {
      app.get("/settings/system", ctx -> SettingController.getSystemSetting(ctx));
      final okhttp3.Response resp = client.get("/settings/system", x -> {
      });
      assert resp.code() == 200;
      Response result = JSONObject.parseObject(resp.body().string(), Response.class);
      assert result.getCode() == 200;
    });
  }
}
