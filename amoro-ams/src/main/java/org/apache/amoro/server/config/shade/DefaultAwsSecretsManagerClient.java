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

package org.apache.amoro.server.config.shade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The default {@link AwsSecretsManagerClient} implementation, bound to a single region.
 *
 * <ul>
 *   <li>Credentials: {@link DefaultCredentialsProvider} — tries env / Java system props / Web
 *       Identity (EKS IRSA) / ECS / EC2 InstanceProfile / {@code ~/.aws/credentials} in order.
 *   <li>Region: extracted from the ARN by the caller and passed in at construction time.
 *   <li>HTTP: {@link UrlConnectionHttpClient} — no Netty dependency, minimizing the shade artifact.
 *   <li>Cache: an in-process {@link ConcurrentHashMap}; each secret is fetched only once (username
 *       and password usually live in the same JSON and are referenced twice).
 * </ul>
 *
 * <p>On failure it throws {@link IllegalStateException} to make AMS startup fail-fast, avoiding
 * starting the service with a wrong password.
 */
final class DefaultAwsSecretsManagerClient implements AwsSecretsManagerClient {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAwsSecretsManagerClient.class);

  private final SecretsManagerClient delegate;
  private final String region;
  private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();

  private DefaultAwsSecretsManagerClient(SecretsManagerClient delegate, String region) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.region = region;
  }

  static DefaultAwsSecretsManagerClient create(String region) {
    Objects.requireNonNull(region, "region");
    SecretsManagerClient sdkClient =
        SecretsManagerClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.create())
            .build();
    LOG.info("AWS Secrets Manager client initialized (region={})", region);
    return new DefaultAwsSecretsManagerClient(sdkClient, region);
  }

  @Override
  public String getSecretString(String secretId) {
    return cache.computeIfAbsent(secretId, this::fetch);
  }

  private String fetch(String secretId) {
    try {
      GetSecretValueResponse response =
          delegate.getSecretValue(GetSecretValueRequest.builder().secretId(secretId).build());
      String value = response.secretString();
      if (value == null) {
        // Binary secrets are rarely used for passwords; fail clearly if one is encountered.
        throw new IllegalStateException(
            "secret '" + secretId + "' has no SecretString (binary secrets are not supported)");
      }
      LOG.info("Fetched secret '{}' from AWS Secrets Manager (region={})", secretId, region);
      return value;
    } catch (RuntimeException e) {
      // Mask the secret value itself, but keep secretId / region for operational troubleshooting.
      throw new IllegalStateException(
          "Failed to fetch secret '"
              + secretId
              + "' from AWS Secrets Manager (region="
              + region
              + "): "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public void close() {
    try {
      delegate.close();
    } catch (RuntimeException e) {
      LOG.warn("Error closing SecretsManagerClient (region={})", region, e);
    }
  }
}
