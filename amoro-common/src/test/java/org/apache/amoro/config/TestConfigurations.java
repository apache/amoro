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

package org.apache.amoro.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestConfigurations {

  @Test
  public void testDifferentKeySetSizesAreNotEqual() {
    Configurations small = Configurations.fromMap(Map.of("a", "1"));
    Configurations large = Configurations.fromMap(Map.of("a", "1", "b", "2"));

    Assertions.assertNotEquals(small, large);
    Assertions.assertNotEquals(large, small);
  }

  @Test
  public void testDifferentByteArrayKeysAreNotEqual() {
    Configurations left = Configurations.fromObjectMap(Map.of("a", new byte[] {1, 2, 3}));
    Configurations right = Configurations.fromObjectMap(Map.of("b", new byte[] {1, 2, 3}));

    Assertions.assertNotEquals(left, right);
    Assertions.assertNotEquals(right, left);
  }

  @Test
  public void testByteArrayValuesAreComparedByContent() {
    Configurations first = Configurations.fromObjectMap(Map.of("key", new byte[] {1, 2, 3}));
    Configurations second = Configurations.fromObjectMap(Map.of("key", new byte[] {1, 2, 3}));
    Configurations different = Configurations.fromObjectMap(Map.of("key", new byte[] {1, 2, 4}));

    Assertions.assertEquals(first, second);
    Assertions.assertEquals(first.hashCode(), second.hashCode());
    Assertions.assertNotEquals(first, different);
  }
}
