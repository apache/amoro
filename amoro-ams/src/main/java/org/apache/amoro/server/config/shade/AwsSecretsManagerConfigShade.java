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

import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.shade.ConfigShade;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Delegates AMS's sensitive configs (db.username / db.password, etc.) to AWS Secrets Manager
 * through Amoro's {@link ConfigShade} SPI.
 *
 * <p>Usage (in {@code $AMORO_HOME/conf/config.yaml}):
 *
 * <pre>{@code
 * ams:
 *   shade:
 *     identifier: aws-sm
 *     sensitive-keywords: database.username;database.password
 *   database:
 *     # both point to the same JSON secret; '#<field>' selects the value out of it
 *     username: arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod-amoro-db-mOhyOp#db.username
 *     password: arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod-amoro-db-mOhyOp#db.password
 * }</pre>
 *
 * <p>The trailing {@code -mOhyOp} is the six random characters AWS appends to every secret ARN; use
 * the full ARN as shown. The optional {@code #<field>} suffix selects one field when the secret
 * value is JSON, e.g. {@code {"db.username": "amoro", "db.password": "..."}} — the field name is
 * whatever key you stored, taken as-is (dots are part of the key, not a nested path). Omit the
 * suffix when the secret value is the plaintext itself.
 *
 * <p>No explicit region configuration is required — it is resolved from the ARN. All secrets shaded
 * for one AMS instance are expected to live in the same region, so a single client is created
 * (lazily, memoized within the process); a reference resolving to a different region is treated as
 * a misconfiguration and fails fast.
 *
 * <p>AMS startup flow: {@code AmoroServiceContainer.initServiceConfig} → {@code
 * ConfigShadeUtils.decryptConfig} → find this SPI instance with identifier=aws-sm → call {@link
 * #decrypt(String)} for each sensitive key.
 *
 * <p>Failure strategy: always fail-fast (malformed ARN, fetch failure, missing JSON field) so that
 * AMS startup blows up immediately, instead of continuing with a wrong password — errors that
 * surface after the DB receives a wrong password are far from the root cause.
 */
public class AwsSecretsManagerConfigShade implements ConfigShade {

  private static final Logger LOG = LoggerFactory.getLogger(AwsSecretsManagerConfigShade.class);

  public static final String IDENTIFIER = "aws-sm";

  private final ObjectMapper jsonMapper;

  /**
   * A single client, lazily initialized on the first decrypt() call. All secrets shaded for one AMS
   * instance (admin-password, database.password, ...) live in the same region, so one client is
   * enough. Guarded by {@code this} for the double-checked lazy init; the region it was bound to is
   * validated against every subsequent reference so that a misconfigured cross-region ARN fails
   * fast instead of being fetched with the wrong client.
   */
  private volatile AwsSecretsManagerClient client;

  private volatile String boundRegion;

  /**
   * The factory that creates a client; {@link DefaultAwsSecretsManagerClient#create} for
   * production, a mock injected in unit tests.
   */
  private final Function<String, AwsSecretsManagerClient> clientFactory;

  /** No-arg SPI constructor. */
  public AwsSecretsManagerConfigShade() {
    this(new ObjectMapper(), DefaultAwsSecretsManagerClient::create);
  }

  /** For injecting a mock client factory in unit tests. */
  AwsSecretsManagerConfigShade(
      ObjectMapper jsonMapper, Function<String, AwsSecretsManagerClient> clientFactory) {
    this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper");
    this.clientFactory = Objects.requireNonNull(clientFactory, "clientFactory");
  }

  @Override
  public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public void initialize(Configurations serviceConfig) {
    // No SPI-level config to read — region is resolved per ARN; credentials use the AWS default
    // provider chain.
    LOG.info("AwsSecretsManagerConfigShade initialized (region resolved per-secret from ARN)");
  }

  @Override
  public String decrypt(String content) {
    SecretReference ref = SecretReference.parse(content);
    String raw = getOrCreateClient(ref.region()).getSecretString(ref.secretArn());
    return ref.jsonKey().map(key -> extractJsonField(raw, key, ref.secretArn())).orElse(raw);
  }

  /**
   * Returns the shared client, creating it on the first call. All shaded secrets are expected to
   * live in a single region; if a later reference resolves to a different region it is almost
   * certainly a misconfiguration, so we fail fast rather than fetch it with the wrong client.
   */
  private AwsSecretsManagerClient getOrCreateClient(String region) {
    AwsSecretsManagerClient existing = client;
    if (existing == null) {
      synchronized (this) {
        existing = client;
        if (existing == null) {
          existing = clientFactory.apply(region);
          boundRegion = region;
          client = existing;
        }
      }
    }
    if (!Objects.equals(region, boundRegion)) {
      throw new IllegalStateException(
          "all AWS Secrets Manager references must be in the same region; expected '"
              + boundRegion
              + "' but got '"
              + region
              + "'");
    }
    return existing;
  }

  private String extractJsonField(String json, String key, String secretArn) {
    JsonNode root;
    try {
      root = jsonMapper.readTree(json);
    } catch (IOException e) {
      // Do not log the raw JSON; it may contain other sensitive fields.
      throw new IllegalStateException(
          "secret '" + secretArn + "' is not valid JSON; cannot extract field '" + key + "'", e);
    }
    if (root == null || !root.isObject()) {
      throw new IllegalStateException(
          "secret '" + secretArn + "' is not a JSON object; cannot extract field '" + key + "'");
    }
    JsonNode value = root.get(key);
    if (value == null || value.isNull()) {
      throw new IllegalStateException("secret '" + secretArn + "' has no field '" + key + "'");
    }
    // A config value must be a scalar. For an object/array node asText() silently returns "", which
    // would then be handed to the DB as e.g. an empty password — the far-from-root-cause failure
    // this class fails fast to avoid. So reject non-scalar values explicitly.
    if (!value.isValueNode()) {
      throw new IllegalStateException(
          "secret '"
              + secretArn
              + "' field '"
              + key
              + "' must be a scalar value, but is a "
              + value.getNodeType()
              + "; a structured JSON value cannot be used as a config value");
    }
    // asText() returns strings as-is and converts numbers/booleans to strings — a good fit for the
    // password scenario.
    return value.asText();
  }
}
