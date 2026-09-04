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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/** Unit tests for {@link AwsSecretsManagerConfigShade} decryption logic with a mocked client. */
public class TestAwsSecretsManagerConfigShade {

  /**
   * The common case: one secret holds a JSON blob with many key-value pairs, and each config value
   * selects a field with {@code #<field>}. Most tests use this ARN.
   */
  private static final String DB_SECRET_ARN =
      "arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod-amoro-db-mOhyOp";

  /**
   * A secret whose value is a single plaintext string (no {@code #}); exercises the non-JSON
   * branch.
   */
  private static final String PLAINTEXT_SECRET_ARN =
      "arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod-amoro-admin-AbCdEf";

  /** A secret in a DIFFERENT region, used only to drive the cross-region fail-fast test. */
  private static final String OTHER_REGION_SECRET_ARN =
      "arn:aws:secretsmanager:us-east-1:123456789012:secret:prod-amoro-other-XyZ123";

  @Test
  public void testIdentifier() {
    assertEquals("aws-sm", new AwsSecretsManagerConfigShade().getIdentifier());
  }

  @Test
  public void testDecryptPlainSecret() {
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(PLAINTEXT_SECRET_ARN)).thenReturn("super-secret-password");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertEquals("super-secret-password", shade.decrypt(PLAINTEXT_SECRET_ARN));
    verify(client).getSecretString(PLAINTEXT_SECRET_ARN);
  }

  @Test
  public void testDecryptJsonField() {
    // Username and password live in the same JSON secret, selected by dotted field names.
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN))
        .thenReturn("{\"db.username\":\"amoro\",\"db.password\":\"p@ss\"}");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertEquals("amoro", shade.decrypt(DB_SECRET_ARN + "#db.username"));
    assertEquals("p@ss", shade.decrypt(DB_SECRET_ARN + "#db.password"));
  }

  @Test
  public void testDottedFieldNameIsTakenAsIsNotNestedPath() {
    // A '.' in the field name is part of the key, not a nested-path separator: a flat key with dots
    // resolves, while the same dots interpreted as nesting must not.
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN))
        .thenReturn("{\"db.password\":\"flat\",\"db\":{\"password\":\"nested\"}}");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    // Must match the flat key literally, never drill into the nested object.
    assertEquals("flat", shade.decrypt(DB_SECRET_ARN + "#db.password"));
  }

  @Test
  public void testDecryptJsonFieldConvertsNonStringValue() {
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN)).thenReturn("{\"db.port\":5432}");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertEquals("5432", shade.decrypt(DB_SECRET_ARN + "#db.port"));
  }

  @Test
  public void testClientIsMemoized() {
    AtomicInteger created = new AtomicInteger();
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN)).thenReturn("{\"db.password\":\"p@ss\"}");
    Function<String, AwsSecretsManagerClient> factory =
        region -> {
          created.incrementAndGet();
          return client;
        };

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), factory);

    shade.decrypt(DB_SECRET_ARN + "#db.password");
    shade.decrypt(DB_SECRET_ARN + "#db.password");
    // Multiple decrypt calls must reuse a single lazily-created client.
    assertEquals(1, created.get());
    verify(client, times(2)).getSecretString(DB_SECRET_ARN);
  }

  @Test
  public void testCrossRegionReferenceFailsFast() {
    AtomicInteger created = new AtomicInteger();
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(PLAINTEXT_SECRET_ARN)).thenReturn("v1");
    Function<String, AwsSecretsManagerClient> factory =
        region -> {
          created.incrementAndGet();
          return client;
        };

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), factory);

    shade.decrypt(PLAINTEXT_SECRET_ARN); // ap-northeast-1 binds the client
    // A second reference in a different region is treated as a misconfiguration.
    assertThrows(
        IllegalStateException.class, () -> shade.decrypt(OTHER_REGION_SECRET_ARN)); // us-east-1
    // The client is created only once and never rebound to the second region.
    assertEquals(1, created.get());
  }

  @Test
  public void testMissingJsonFieldFailsFast() {
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN)).thenReturn("{\"db.username\":\"amoro\"}");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertThrows(IllegalStateException.class, () -> shade.decrypt(DB_SECRET_ARN + "#db.password"));
  }

  @Test
  public void testJsonFieldPointingToObjectFailsFast() {
    // '#db' selects an object node. Jackson's asText() would silently return "" for it, so without
    // the scalar check the DB would receive an empty value; the shade must fail fast instead.
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN))
        .thenReturn("{\"db\":{\"username\":\"amoro\",\"password\":\"p@ss\"}}");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertThrows(IllegalStateException.class, () -> shade.decrypt(DB_SECRET_ARN + "#db"));
  }

  @Test
  public void testJsonFieldPointingToArrayFailsFast() {
    // Same trap as an object node: '#hosts' selects an array, for which asText() returns "".
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN)).thenReturn("{\"hosts\":[\"a\",\"b\"]}");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertThrows(IllegalStateException.class, () -> shade.decrypt(DB_SECRET_ARN + "#hosts"));
  }

  @Test
  public void testInvalidJsonFailsFast() {
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN)).thenReturn("not-a-json");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertThrows(IllegalStateException.class, () -> shade.decrypt(DB_SECRET_ARN + "#db.password"));
  }

  @Test
  public void testNonObjectJsonFailsFast() {
    AwsSecretsManagerClient client = mock(AwsSecretsManagerClient.class);
    when(client.getSecretString(DB_SECRET_ARN)).thenReturn("[1,2,3]");

    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(new ObjectMapper(), region -> client);

    assertThrows(IllegalStateException.class, () -> shade.decrypt(DB_SECRET_ARN + "#db.password"));
  }

  @Test
  public void testMalformedReferenceFailsFast() {
    AwsSecretsManagerConfigShade shade =
        new AwsSecretsManagerConfigShade(
            new ObjectMapper(), region -> mock(AwsSecretsManagerClient.class));

    assertThrows(IllegalArgumentException.class, () -> shade.decrypt("not-an-arn"));
  }
}
