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

package org.apache.amoro.formats.lance;

import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TestLanceStorageOptionsProvider {

  @Test
  public void testTemporaryCredentialsAreMappedForTos() {
    Configuration configuration = new Configuration(false);
    configuration.set(LanceStorageOptionsProvider.TOS_ENDPOINT, "https://tos.example.com");
    configuration.set(LanceStorageOptionsProvider.VOLC_REGION, "cn-test");
    configuration.set(LanceStorageOptionsProvider.TOS_CREDENTIAL_PROVIDER, "test-provider");
    configuration.setLong(LanceStorageOptionsProvider.TOS_CREDENTIAL_TTL, 3_600L);
    AtomicReference<String> requestedBucket = new AtomicReference<>();

    LanceStorageOptionsProvider provider =
        new LanceStorageOptionsProvider(
            configuration,
            bucket -> {
              requestedBucket.set(bucket);
              return new LanceStorageOptionsProvider.TemporaryCredential("ak", "sk", "token");
            });

    long before = System.currentTimeMillis();
    Map<String, String> options = provider.storageOptions("tos://test-bucket/path/table.lance");

    Assertions.assertEquals("test-bucket", requestedBucket.get());
    Assertions.assertEquals("https://tos.example.com", options.get("endpoint"));
    Assertions.assertEquals("cn-test", options.get("region"));
    Assertions.assertEquals("ak", options.get("access_key_id"));
    Assertions.assertEquals("sk", options.get("secret_access_key"));
    Assertions.assertEquals("token", options.get("security_token"));
    Assertions.assertTrue(Long.parseLong(options.get("expires_at_millis")) > before);
    Assertions.assertTrue(Long.parseLong(options.get("refresh_offset_millis")) > 0);
    Assertions.assertEquals(
        "tos://test-bucket/path/table.lance",
        provider.datasetLocation("tos://test-bucket/path/table.lance"));
  }

  @Test
  public void testStaticCredentialsRemainSupported() {
    Configuration configuration = new Configuration(false);
    configuration.set(LanceStorageOptionsProvider.TOS_ACCESS_KEY, "static-ak");
    configuration.set(LanceStorageOptionsProvider.TOS_SECRET_KEY, "static-sk");

    Map<String, String> options =
        new LanceStorageOptionsProvider(configuration)
            .storageOptions("tos://test-bucket/table.lance");

    Assertions.assertEquals("static-ak", options.get("access_key_id"));
    Assertions.assertEquals("static-sk", options.get("secret_access_key"));
  }

  @Test
  public void testNonTosLocationHasNoOptions() {
    Configuration configuration = new Configuration(false);
    configuration.set(LanceStorageOptionsProvider.TOS_ACCESS_KEY, "ak");
    configuration.set(LanceStorageOptionsProvider.TOS_SECRET_KEY, "sk");

    Assertions.assertTrue(
        new LanceStorageOptionsProvider(configuration)
            .storageOptions("s3://bucket/table.lance")
            .isEmpty());
    Assertions.assertEquals(
        "s3://bucket/table.lance",
        new LanceStorageOptionsProvider(configuration).datasetLocation("s3://bucket/table.lance"));
  }
}
