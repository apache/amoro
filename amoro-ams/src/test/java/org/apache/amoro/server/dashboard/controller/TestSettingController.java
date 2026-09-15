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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.resource.OptimizerManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class TestSettingController {

  private OptimizerManager optimizerManager;
  private SettingController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    optimizerManager = mock(OptimizerManager.class);
    controller = new SettingController(new Configurations(), optimizerManager);
    ctx = mock(Context.class);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void ldapBindPasswordIsMaskedInSystemSetting() {
    Configurations configurations = new Configurations();
    configurations.set(
        AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_BIND_PASSWORD, "ldap-secret");
    controller = new SettingController(configurations, optimizerManager);
    when(ctx.json(any())).thenReturn(ctx);

    controller.getSystemSetting(ctx);

    ArgumentCaptor<OkResponse> captor = ArgumentCaptor.forClass(OkResponse.class);
    verify(ctx).json(captor.capture());
    java.util.Map<String, String> result =
        (java.util.Map<String, String>) captor.getValue().getResult();
    Assertions.assertEquals(
        "******",
        result.get(AmoroManagementConf.AUTHORIZATION_LDAP_ROLE_MAPPING_BIND_PASSWORD.key()),
        "LDAP bind password must be masked in the system settings response");
  }
}
