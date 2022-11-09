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
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.controller.response.Response;
import com.netease.arctic.ams.server.model.CatalogSettingInfo;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CatalogControllerTest {
  private final Logger LOG = LoggerFactory.getLogger("CatalogControllerTest");

  @Test
  public void testGetCatalogTypeList() {
    JavalinTest.test((app, client) -> {
      app.get("/", ctx -> CatalogController.getCatalogTypeList(ctx));
      final okhttp3.Response resp = client.get("/", x -> {
      });
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      assert result.getCode() == 200;
      List<Map<String, String>> resBody = (List<Map<String, String>>)result.getResult();
      assert  resBody.size() == 4;
    });
  }

  public void getCatalogDetail(Consumer consumer, HttpClient client) {
    // test get catalog detail
    final okhttp3.Response resp1 = client.get("/unittest", x -> {});
    final OkResponse result1 = JSONObject.parseObject(resp1.body().toString(), OkResponse.class);
    assert result1.getCode() == 200;
    final CatalogSettingInfo info = JSONObject.parseObject(result1.getResult().toString(), CatalogSettingInfo.class);
    assert info != null;
  }

  @Test
  public void testCreateCatalog() {
    String name = "unittest";
    String newAuthUser = "UnitTest";
    JavalinTest.test((app, client) -> {
      // add one catalog
      app.post("/", ctx -> CatalogController.createCatalog(ctx));
      app.get("/{catalogName}", ctx->CatalogController.getCatalogDetail(ctx));
      app.put("/{catalogName}", ctx->CatalogController.updateCatalog(ctx));
      app.delete("/{catalogName}", ctx -> CatalogController.deleteCatalog(ctx));
      String paramString = "{\"name\":\"unittest\",\"type\":\"hadoop\","
              + "\"storageConfig\":{\"storage_config.storage.type\":\"hdfs\","
              + "\"storage_config.core-site\":\"1\","
              + "\"storage_config.hdfs-site\":\"2\","
              + "\"storage_config.hive-site\":\"3\"},"
              + "\"authConfig\":{\"auth_config.type\":\"SIMPLE\",\"auth_config.hadoop_username\":\"arctic\"},"
              + "\"properties\":{},\"tableFormat\":\"hive\"}";
      JSONObject param = JSONObject.parseObject(paramString);
      final okhttp3.Response resp = client.post("/", param);
      Response result = JSONObject.parseObject(resp.body().string(), Response.class);
      assert result.getCode() == 200;

      // test get catalog detail
      final okhttp3.Response resp1 = client.get("/unittest", x -> {});
      final OkResponse result1 = JSONObject.parseObject(resp1.body().string(), OkResponse.class);
      assert result1.getCode() == 200;
      final CatalogSettingInfo info = JSONObject.parseObject(result1.getResult().toString(), CatalogSettingInfo.class);
      assert info != null;
      assert info.getName().equals(name);
      // update catalog
      JSONObject authConfig = new JSONObject();
      authConfig.put("auth_config.type", "SIMPLE");
      authConfig.put("auth_config.hadoop_username", newAuthUser);
      param.put("authConfig", authConfig);
      final okhttp3.Response resp2 = client.put("/unittest", param);
      final OkResponse result2 = JSONObject.parseObject(resp2.body().string(), OkResponse.class);
      assert result2.getCode() == 200;
      // get catalog after update
      final okhttp3.Response resp3 = client.get("/unittest", x -> {});
      final OkResponse result3 = JSONObject.parseObject(resp3.body().string(), OkResponse.class);
      assert result3.getCode() == 200;
      final CatalogSettingInfo infoPut = JSONObject.parseObject(result3.getResult().toString(), CatalogSettingInfo.class);
      assert infoPut != null;
      assert infoPut.getAuthConfig().get("auth_config.hadoop_username").equals(newAuthUser);

      // delete catalog

    });
  }
}
