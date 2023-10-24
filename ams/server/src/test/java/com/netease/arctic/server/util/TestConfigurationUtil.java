package com.netease.arctic.server.util;

import static com.netease.arctic.server.ArcticManagementConf.DB_PASSWORD;
import static com.netease.arctic.server.ArcticManagementConf.SERVER_EXPOSE_HOST;

import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.utils.ConfigurationUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestConfigurationUtil {

  @Test
  public void testConvertConfigurationKeys() {
    HashMap<String, String> dummyEnv = new HashMap<>(2);
    String prefix = ArcticManagementConf.SYSTEM_CONFIG.toUpperCase();
    dummyEnv.put("AMS_DATABASE_PASSWORD", "1234567");
    dummyEnv.put("AMS_SERVER__EXPOSE__HOST", "127.0.0.1");
    Map<String, Object> result = ConfigurationUtil.convertConfigurationKeys(prefix, dummyEnv);
    Assert.assertNotNull("AMS_DATABASE_PASSWORD Convert Failed", result.get(DB_PASSWORD.key()));
    Assert.assertNotNull(
        "AMS_SERVER__EXPOSE__HOST Convert Failed", result.get(SERVER_EXPOSE_HOST.key()));
  }
}
