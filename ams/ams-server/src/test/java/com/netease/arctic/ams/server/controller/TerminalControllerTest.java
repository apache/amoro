package com.netease.arctic.ams.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.AmsClientPools;
import com.netease.arctic.CatalogMetaTestUtil;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import io.javalin.testtools.JavalinTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class TerminalControllerTest {
  private final Logger LOG = LoggerFactory.getLogger("TerminalControllerTest");

  protected static final Object ANY = new Object();

  protected static File testBaseDir = new File("unit_test_base_tmp");
  protected static File testSparkDir = new File(testBaseDir, "spark-warehouse");
  protected static File testArcticDir = new File(testBaseDir, "arctic");

  protected static MockArcticMetastoreServer ams = new MockArcticMetastoreServer();

  protected static String amsUrl;
  protected static String catalogName;

  final static ConcurrentHashMap<String, ArcticCatalog> catalogs = new ConcurrentHashMap<>();

  public static ArcticCatalog catalog(String name) {
    return catalogs.computeIfAbsent(name, n -> CatalogLoader.load(amsUrl + "/" + n));
  }

  @Before
  public void startMetastore() throws Exception {
    FileUtils.deleteQuietly(testBaseDir);
    testBaseDir.mkdirs();

    AmsClientPools.cleanAll();
    if (!ams.isStarted()) {
      ams.start();
    }
    amsUrl = "thrift://127.0.0.1:" + ams.port();

    CatalogMeta arctic = CatalogMetaTestUtil.createArcticCatalog(testArcticDir);
    catalogName = arctic.getCatalogName();
    ams.handler().createCatalog(arctic);
  }

  @After
  public void stopMetastore() {
    // ams.stopAndCleanUp();
    ams.handler().cleanUp();
    AmsClientPools.cleanAll();
  }

  @Test
  public void testGetExamples() {
    JavalinTest.test((app, client) -> {
      app.get("/", ctx -> TerminalController.getExamples(ctx));
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testGetSqlExamples() {
    JavalinTest.test((app, client) -> {
      app.get("/{exampleName}/", ctx -> TerminalController.getSqlExamples(ctx));
      final okhttp3.Response resp = client.get("/CreateTable/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

  @Test
  public void testTerminal() {
    // test execute sql
    JavalinTest.test((app, client) -> {
      JSONObject requestJson = new JSONObject();
      requestJson.put("sql", "create database arctic_test;");
      app.post("/{catalog}/", ctx -> TerminalController.executeSql(ctx));
      final okhttp3.Response resp1 = client.post("/" + catalogName + "/", requestJson, x -> {
      });
      OkResponse result = JSONObject.parseObject(resp1.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });

      // test get latest info
    JavalinTest.test((app, client) -> {
      app.get("/", ctx -> TerminalController.getLatestInfo(ctx));
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });

      // test get sql running logs
    JavalinTest.test((app, client) -> {
      app.get("/{sessionId}/", ctx -> TerminalController.getLogs(ctx));
      final okhttp3.Response resp3 = client.get("/1/", x -> {});
      OkResponse result = JSONObject.parseObject(resp3.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });

    // test get sql status and result
    JavalinTest.test((app, client) -> {
      app.get("/{sessionId}/", ctx -> TerminalController.getSqlStatus(ctx));
      final okhttp3.Response resp4 = client.get("/1/", x -> {});
      OkResponse result = JSONObject.parseObject(resp4.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });

      // test stop sql
    JavalinTest.test((app, client) -> {
      app.put("/{sessionId}/", ctx -> TerminalController.stopSql(ctx));
      final okhttp3.Response resp5 = client.put("/1/", new JSONObject(), x -> {});
      OkResponse result = JSONObject.parseObject(resp5.body().string(), OkResponse.class);
      LOG.info("xxx: {}", JSONObject.toJSONString(result));
      assert result.getCode() == 200;
    });
  }

}
