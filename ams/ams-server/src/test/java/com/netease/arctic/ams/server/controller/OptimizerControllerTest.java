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
import com.google.common.collect.Maps;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.controller.response.Response;
import com.netease.arctic.ams.server.model.Container;
import com.netease.arctic.ams.server.model.OptimizeQueueMeta;
import com.netease.arctic.ams.server.model.Optimizer;
import com.netease.arctic.ams.server.model.TableTaskStatus;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.OptimizerService;
import com.netease.arctic.ams.server.util.DerbyTestUtil;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import io.javalin.testtools.JavalinTest;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @Description: OptimizerController Test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
      JDBCSqlSessionFactoryProvider.class,
      ArcticMetaStore.class
})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class OptimizerControllerTest {

  static {
    try {
      deleteDerby();
      mockDerby();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  @Test
  public void testGetOptimizers() {

    OptimizerService optimizerService = new OptimizerService();
    optimizerService.insertOptimizer("test1", 1, "testOptimizeGroup",
            TableTaskStatus.STARTING,
            new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()),
            2, 1024, 1, "test1");
    JavalinTest.test((app, client) -> {
        app.get("/{optimizerGroup}/", OptimizerController::getOptimizers);
        final okhttp3.Response resp = client.get("/all?page=1&pageSize=20", x -> {});
        OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
        assert JSONObject.parseObject(result.getResult().toString()).
                getString("total").equals("1");
        assert result.getCode() == 200;
    });
    optimizerService.deleteOptimizerByName("test1");
  }

  @Test
  public void testGetOptimizersTable() {
    JavalinTest.test((app, client) -> {
      app.get("/{optimizerGroup}/", OptimizerController::getOptimizerTables);
      final okhttp3.Response resp = client.get("/all?page=1&pageSize=20", x -> {});
      Response result = JSONObject.parseObject(resp.body().string(), Response.class);
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetOptimizerGroups() throws Exception {
    OptimizeQueueMeta optimizeQueueMeta = new OptimizeQueueMeta();
    optimizeQueueMeta.name = "testOptimizerGroup";
    optimizeQueueMeta.container = "test1";

    Map<String, String> map = Maps.newHashMap();
    map.put("memory","1024");
    optimizeQueueMeta.properties = map;
    ServiceContainer.getOptimizeQueueService().createQueue(optimizeQueueMeta);
    JavalinTest.test((app, client) -> {
      app.get("/", OptimizerController::getOptimizerGroups);
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      assert JSONObject.parseObject(JSONObject.parseArray(result.getResult().toString()).get(0).toString()).
              getString("optimizerGroupName").equals("testOptimizerGroup");
      assert result.getCode() == 200;
    });
    ServiceContainer.getOptimizeQueueService().removeAllQueue();
  }

  @Test
  public void testGetOptimizerGroupInfo() {
    OptimizerService optimizerService = new OptimizerService();
    optimizerService.insertOptimizer("test1", 1, "testOptimizeGroup",
            TableTaskStatus.STARTING,
            new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()),
            2, 1024, 1, "test1");
    JavalinTest.test((app, client) -> {
      app.get("/{optimizerGroup}", OptimizerController::getOptimizerGroupInfo);
      final okhttp3.Response resp = client.get("/all", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      assert JSONObject.parseObject(result.getResult().toString()).getString("occupationCore").equals("2");
      assert result.getCode() == 200;
    });
    optimizerService.deleteOptimizerByName("test1");
  }

  @Test
  public void testScaleOutOptimizer() throws Exception {
    Container container = new Container();
    container.setName("test1");
    container.setType("local");
    Map<String, String> map = new HashMap<>();
    map.put("hadoop_home", "test");
    container.setProperties(map);
    ServiceContainer.getOptimizeQueueService().insertContainer(container);

    OptimizeQueueMeta optimizeQueueMeta = new OptimizeQueueMeta();
    optimizeQueueMeta.name = "default202206141";
    optimizeQueueMeta.container = "test1";

    Map<String, String> map1 = Maps.newHashMap();
    map1.put("memory","1024");
    optimizeQueueMeta.properties = map1;
    ServiceContainer.getOptimizeQueueService().createQueue(optimizeQueueMeta);
    JavalinTest.test((app, client) -> {
      app.post("/{optimizerGroup}", OptimizerController::scaleOutOptimizer);
      JSONObject  requestJson = new JSONObject();
      requestJson.put("parallelism", 1);
      final okhttp3.Response resp = client.post("/default202206141", requestJson);
      Response result = JSONObject.parseObject(resp.body().string(), Response.class);
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testReleaseOptimizer() throws Exception {
    Container container = new Container();
    container.setName("test2");
    container.setType("local");
    Map<String, String> map = Maps.newHashMap();
    map.put("hadoop_home", "test");
    container.setProperties(map);
    ServiceContainer.getOptimizeQueueService().insertContainer(container);

    OptimizeQueueMeta optimizeQueueMeta = new OptimizeQueueMeta();
    optimizeQueueMeta.name = "testOptimizeGroup2";
    optimizeQueueMeta.container = "test2";

    Map<String, String> map1 = Maps.newHashMap();
    map1.put("memory","1024");
    optimizeQueueMeta.properties = map1;
    ServiceContainer.getOptimizeQueueService().createQueue(optimizeQueueMeta);
    OptimizerService optimizerService = new OptimizerService();
    optimizerService.insertOptimizer("test2", 1, "testOptimizeGroup2",
            TableTaskStatus.STARTING,
            new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()),
            2, 1024, 1, "test2");
    JavalinTest.test((app, client) -> {
      List<Optimizer> optimizers = optimizerService.getOptimizers();
      long jobId = optimizers.get(0).getJobId();
      ServiceContainer.getOptimizeExecuteService().startOptimizer(jobId);
      app.post("/{jobId}", OptimizerController::releaseOptimizer);
      final okhttp3.Response resp = client.post("/" + jobId);
      Response result = JSONObject.parseObject(resp.body().string(), Response.class);
      assert result.getCode() == 200;
    });
  }

  public static void mockDerby() throws Exception {
    mockStatic(JDBCSqlSessionFactoryProvider.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenAnswer((Answer<SqlSessionFactory>) invocation -> DerbyTestUtil.get());
    DerbyTestUtil derbyService = new DerbyTestUtil();
    derbyService.createTestTable();
    mockStatic(ArcticMetaStore.class);
    com.netease.arctic.ams.server.config.Configuration configuration = new com.netease.arctic.ams.server.config.Configuration();
    configuration.setString(ArcticMetaStoreConf.DB_TYPE, "derby");
    ArcticMetaStore.conf = configuration;
  }

  public static void deleteDerby() throws IOException {
    DerbyTestUtil.deleteIfExists(DerbyTestUtil.path + "mydb1");
  }
}