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

package com.netease.arctic.server.util;

import static com.netease.arctic.server.ArcticManagementConf.ADMIN_PASSWORD;
import static com.netease.arctic.server.ArcticManagementConf.ADMIN_USERNAME;
import static com.netease.arctic.server.ArcticManagementConf.DB_PASSWORD;
import static com.netease.arctic.server.ArcticManagementConf.DB_USER_NAME;
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
    dummyEnv.put("AMS_DATABASE_USERNAME", "amoro");
    dummyEnv.put("AMS_DATABASE_PASSWORD", "1234567");
    dummyEnv.put("AMS_SERVER__EXPOSE__HOST", "127.0.0.1");
    dummyEnv.put("AMS_ADMIN__USERNAME", "admin");
    dummyEnv.put("AMS_ADMIN__PASSWORD", "admin");
    Map<String, Object> result = ConfigurationUtil.convertConfigurationKeys(prefix, dummyEnv);
    Assert.assertNotNull("AMS_DATABASE_USERNAME Convert Failed", result.get(DB_USER_NAME.key()));
    Assert.assertNotNull("AMS_DATABASE_PASSWORD Convert Failed", result.get(DB_PASSWORD.key()));
    Assert.assertNotNull(
        "AMS_SERVER__EXPOSE__HOST Convert Failed", result.get(SERVER_EXPOSE_HOST.key()));
    Assert.assertNotNull("AMS_ADMIN__USERNAME Convert Failed", result.get(ADMIN_USERNAME.key()));
    Assert.assertNotNull("AMS_ADMIN__PASSWORD Convert Failed", result.get(ADMIN_PASSWORD.key()));
  }
}
