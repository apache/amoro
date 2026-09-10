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

/**
 * A minimal abstraction over AWS Secrets Manager. It only exposes a single method for fetching a
 * secret string, which makes it easy to stub in unit tests.
 *
 * <p>Implementations are expected to cache within the process — AMS only calls {@link
 * #getSecretString(String)} during startup, but the same secret may be referenced in several places
 * (for example, the username/password of the same DB living in the same JSON).
 */
interface AwsSecretsManagerClient extends AutoCloseable {

  /**
   * Fetches the {@code SecretString} content of the secret.
   *
   * @param secretId the secret name or ARN
   * @return the plaintext of the secret (either a plain string or JSON)
   * @throws RuntimeException if the fetch fails; the caller decides whether to fail-fast
   */
  String getSecretString(String secretId);

  @Override
  void close();
}
