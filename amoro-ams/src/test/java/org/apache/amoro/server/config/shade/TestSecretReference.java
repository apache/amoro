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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link SecretReference} ARN parsing and validation. */
public class TestSecretReference {

  private static final String DB_SECRET_ARN =
      "arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:prod-amoro-db-mOhyOp";

  @Test
  public void testParsePlainArn() {
    SecretReference ref = SecretReference.parse(DB_SECRET_ARN);
    assertEquals(DB_SECRET_ARN, ref.secretArn());
    assertEquals("ap-northeast-1", ref.region());
    assertFalse(ref.jsonKey().isPresent());
  }

  @Test
  public void testParseArnWithJsonKey() {
    SecretReference ref = SecretReference.parse(DB_SECRET_ARN + "#db.password");
    // The '#jsonKey' suffix must be stripped from the ARN passed to the SDK.
    assertEquals(DB_SECRET_ARN, ref.secretArn());
    assertEquals("ap-northeast-1", ref.region());
    assertTrue(ref.jsonKey().isPresent());
    // A dotted field name is kept intact: '#' is split on lastIndexOf, dots are part of the key.
    assertEquals("db.password", ref.jsonKey().get());
  }

  @Test
  public void testParseTrimsSurroundingWhitespace() {
    SecretReference ref = SecretReference.parse("  " + DB_SECRET_ARN + " # db.password ");
    assertEquals(DB_SECRET_ARN, ref.secretArn());
    assertEquals("db.password", ref.jsonKey().get());
  }

  @Test
  public void testEmptyJsonKeyTreatedAsAbsent() {
    // A trailing '#' with no key should behave like a plain ARN reference.
    SecretReference ref = SecretReference.parse(DB_SECRET_ARN + "#");
    assertEquals(DB_SECRET_ARN, ref.secretArn());
    assertFalse(ref.jsonKey().isPresent());
  }

  @Test
  public void testToStringRoundTrip() {
    assertEquals(DB_SECRET_ARN, SecretReference.parse(DB_SECRET_ARN).toString());
    assertEquals(
        DB_SECRET_ARN + "#db.password",
        SecretReference.parse(DB_SECRET_ARN + "#db.password").toString());
  }

  @Test
  public void testNullContentRejected() {
    assertThrows(NullPointerException.class, () -> SecretReference.parse(null));
  }

  @Test
  public void testBlankContentRejected() {
    assertThrows(IllegalArgumentException.class, () -> SecretReference.parse("   "));
  }

  @Test
  public void testNonArnRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> SecretReference.parse("just-a-plain-secret-name"));
  }

  @Test
  public void testWrongServiceRejected() {
    // Correct ARN shape but not a secretsmanager resource.
    assertThrows(
        IllegalArgumentException.class,
        () -> SecretReference.parse("arn:aws:s3:ap-northeast-1:123456789012:secret:foo"));
  }

  @Test
  public void testWrongResourceTypeRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> SecretReference.parse("arn:aws:secretsmanager:ap-northeast-1:123456789012:key:foo"));
  }

  @Test
  public void testMissingRegionRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> SecretReference.parse("arn:aws:secretsmanager::123456789012:secret:foo"));
  }

  @Test
  public void testMissingNameRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> SecretReference.parse("arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:"));
  }
}
