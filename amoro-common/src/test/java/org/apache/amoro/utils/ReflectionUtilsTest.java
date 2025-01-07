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

package org.apache.amoro.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class ReflectionUtilsTest {

  @Test
  void testInvokeValidClassAndMethod() {
    try {
      // Invoke the static method in TestClass
      Object result = ReflectionUtils.invoke("java.lang.System", "currentTimeMillis");
      assertNotNull(result);
    } catch (Exception e) {
      fail("Invocation failed: " + e.getMessage());
    }
  }

  @Test
  void testInvokeNonExistentClass() {
    try {
      Object result =
          ReflectionUtils.invoke("com.example.nonexistent.NonExistentClass", "testMethod");
      assertNull(result); // The method should return null for non-existent class
    } catch (Exception e) {
      fail("Exception should not be thrown for non-existent class: " + e.getMessage());
    }
  }
}
