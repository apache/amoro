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

import java.util.Objects;
import java.util.Optional;

/**
 * Parses an AWS Secrets Manager reference written in {@code config.yaml}.
 *
 * <p>Only a full ARN is accepted (short names are not supported); the region is extracted directly
 * from the ARN to avoid additional configuration.
 *
 * <p>Syntax:
 *
 * <pre>{@code
 * arn:aws:secretsmanager:<region>:<account>:secret:<name>[-<suffix>]
 * arn:aws:secretsmanager:<region>:<account>:secret:<name>[-<suffix>]#<jsonKey>
 * }</pre>
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod/amoro/admin-AbCdEf} —
 *       the whole secret is the password string.
 *   <li>{@code
 *       arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod/amoro/rds-AbCdEf#password} —
 *       the secret is JSON, and the {@code password} field is extracted.
 * </ul>
 *
 * <p>The delimiter is fixed to {@code #} to avoid the {@code :} that fills ARNs and the {@code /}
 * commonly found in secret names.
 */
final class SecretReference {

  private static final char KEY_DELIMITER = '#';

  /** ARN fixed format: {@code arn:aws:secretsmanager:REGION:ACCOUNT:secret:NAME} — 7 parts. */
  private static final int ARN_PARTS = 7;

  private static final String ARN_PREFIX = "arn:";
  private static final String ARN_SERVICE = "secretsmanager";
  private static final String ARN_RESOURCE_TYPE = "secret";

  private final String secretArn;
  private final String region;
  private final String jsonKey; // nullable

  private SecretReference(String secretArn, String region, String jsonKey) {
    this.secretArn = secretArn;
    this.region = region;
    this.jsonKey = jsonKey;
  }

  static SecretReference parse(String content) {
    Objects.requireNonNull(content, "secret reference content is null");
    String trimmed = content.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("secret reference is blank");
    }

    // Split off the optional '#jsonKey' suffix first.
    String arnPart;
    String jsonKey;
    int hashIdx = trimmed.lastIndexOf(KEY_DELIMITER);
    if (hashIdx < 0) {
      arnPart = trimmed;
      jsonKey = null;
    } else {
      arnPart = trimmed.substring(0, hashIdx).trim();
      String tail = trimmed.substring(hashIdx + 1).trim();
      jsonKey = tail.isEmpty() ? null : tail;
    }

    validateArn(arnPart);
    String region = extractRegion(arnPart);
    return new SecretReference(arnPart, region, jsonKey);
  }

  private static void validateArn(String arn) {
    if (!arn.startsWith(ARN_PREFIX)) {
      throw new IllegalArgumentException(
          "secret reference must be a full ARN (arn:aws:secretsmanager:<region>:<account>:secret:<name>)");
    }
    // limit=ARN_PARTS ensures that an illegal ':' inside the secret name is not over-split.
    String[] parts = arn.split(":", ARN_PARTS);
    if (parts.length != ARN_PARTS
        || !ARN_SERVICE.equals(parts[2])
        || !ARN_RESOURCE_TYPE.equals(parts[5])
        || parts[3].isEmpty()
        || parts[6].isEmpty()) {
      throw new IllegalArgumentException(
          "malformed AWS Secrets Manager ARN (expected arn:aws:secretsmanager:<region>:<account>:secret:<name>)");
    }
  }

  private static String extractRegion(String arn) {
    // At this point the ARN has passed validateArn and is guaranteed to have 7 parts.
    return arn.split(":", ARN_PARTS)[3];
  }

  /** The full ARN — pass it directly to {@code GetSecretValueRequest#secretId}. */
  String secretArn() {
    return secretArn;
  }

  /** The region extracted from the ARN; used to select/create the {@code SecretsManagerClient}. */
  String region() {
    return region;
  }

  Optional<String> jsonKey() {
    return Optional.ofNullable(jsonKey);
  }

  @Override
  public String toString() {
    return jsonKey == null ? secretArn : secretArn + KEY_DELIMITER + jsonKey;
  }
}
