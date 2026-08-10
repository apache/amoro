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

package org.apache.amoro.server.las;

import bytedance.olap.iam.http.model.AssumeRoleResponse.Credentials;
import org.apache.amoro.config.Configurations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestLasIntegrationContext {

  @Test
  public void testDisabledByDefault() {
    LasIntegrationContext context = LasIntegrationContext.initialize(new Configurations());

    Assertions.assertFalse(context.enabled());
    Assertions.assertThrows(IllegalStateException.class, context::newHmsClientPool);
  }

  @Test
  public void testInitializeKubernetesServiceEndpoints() {
    LasIntegrationContext context = LasIntegrationContext.initialize(validConfigurations());

    Assertions.assertTrue(context.enabled());
    Assertions.assertEquals("thrift://hms-service:9083", context.hmsUri().toString());
    Assertions.assertEquals("https://iam.volcengineapi.com", context.iamEndpoint().toString());
    Assertions.assertEquals("https://emr-serverless", context.emrServerlessEndpoint().toString());
    Assertions.assertEquals("cn-beijing", context.region());
    Assertions.assertNotNull(context.newHmsClientPool());
    Credentials credentials = new Credentials();
    credentials.setAccessKeyId("data-ak");
    credentials.setSecretAccessKey("data-sk");
    credentials.setSessionToken("data-token");
    Assertions.assertEquals(
        "https://tos-cn-beijing.volces.com",
        context.newTosConfiguration(credentials).get("fs.tos.endpoint"));
  }

  @Test
  public void testRejectMissingRequiredEndpoint() {
    Configurations configurations = validConfigurations();
    configurations.setString(LasIntegrationConfig.HMS_URI, "");

    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> LasIntegrationContext.initialize(configurations));
    Assertions.assertTrue(exception.getMessage().contains(LasIntegrationConfig.HMS_URI.key()));
  }

  @Test
  public void testUsesExistingLasServiceCredentialEnvironmentNames() {
    Configurations configurations = validConfigurations();
    configurations.removeConfig(LasIntegrationConfig.IAM_BOOTSTRAP_ACCESS_KEY);
    configurations.removeConfig(LasIntegrationConfig.IAM_BOOTSTRAP_SECRET_KEY);
    Map<String, String> environment = new HashMap<>();
    environment.put(LasIntegrationConfig.LAS_SERVICE_ACCESS_KEY_ENV, "las-service-ak");
    environment.put(LasIntegrationConfig.LAS_SERVICE_SECRET_KEY_ENV, "las-service-sk");

    LasIntegrationContext context = LasIntegrationContext.initialize(configurations, environment);
    bytedance.olap.iam.Credential credential = context.bootstrapCredential();

    Assertions.assertEquals("las-service-ak", credential.getAccessKeyId());
    Assertions.assertEquals("las-service-sk", credential.getSecretAccessKey());
  }

  @Test
  public void testExplicitCredentialConfigurationOverridesEnvironment() {
    Map<String, String> environment = new HashMap<>();
    environment.put(LasIntegrationConfig.LAS_SERVICE_ACCESS_KEY_ENV, "las-service-ak");
    environment.put(LasIntegrationConfig.LAS_SERVICE_SECRET_KEY_ENV, "las-service-sk");

    LasIntegrationContext context =
        LasIntegrationContext.initialize(validConfigurations(), environment);
    bytedance.olap.iam.Credential credential = context.bootstrapCredential();

    Assertions.assertEquals("bootstrap-ak", credential.getAccessKeyId());
    Assertions.assertEquals("bootstrap-sk", credential.getSecretAccessKey());
  }

  @Test
  public void testCrossVpcConfigurationIsAtomic() {
    Configurations configurations = validConfigurations();
    configurations.setBoolean(LasIntegrationConfig.CROSS_VPC_ENABLED, true);

    IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> LasIntegrationContext.initialize(configurations));
    Assertions.assertTrue(
        exception.getMessage().contains(LasIntegrationConfig.CROSS_VPC_ACCOUNT_ID.key()));
  }

  static Configurations validConfigurations() {
    Map<String, String> values = new HashMap<>();
    values.put(LasIntegrationConfig.ENABLED.key(), "true");
    values.put(LasIntegrationConfig.HMS_URI.key(), "thrift://hms-service:9083");
    values.put(LasIntegrationConfig.TOS_ENDPOINT.key(), "https://tos-cn-beijing.volces.com");
    values.put(LasIntegrationConfig.IAM_ENDPOINT.key(), "https://iam.volcengineapi.com");
    values.put(LasIntegrationConfig.EMR_SERVERLESS_ENDPOINT.key(), "https://emr-serverless");
    values.put(LasIntegrationConfig.IAM_BOOTSTRAP_ACCESS_KEY.key(), "bootstrap-ak");
    values.put(LasIntegrationConfig.IAM_BOOTSTRAP_SECRET_KEY.key(), "bootstrap-sk");
    return Configurations.fromMap(values);
  }
}
