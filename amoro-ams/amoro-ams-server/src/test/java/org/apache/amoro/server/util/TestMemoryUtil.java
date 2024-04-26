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

package org.apache.amoro.server.util;

import org.apache.amoro.server.utils.MemoryUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMemoryUtil {

  @Test
  public void convertToMegabytes() {
    String[] memoryStrings = {"1024", "1024m", "1024M", "1G", "1024k", "1024K"};
    long[] expected = {1024, 1024, 1024, 1024, 1, 1};

    long[] actual = new long[memoryStrings.length];
    for (int i = 0; i < memoryStrings.length; i++) {
      actual[i] = MemoryUtil.convertToMegabytes(memoryStrings[i]);
    }

    Assertions.assertArrayEquals(expected, actual);

    Assertions.assertThrows(
        IllegalArgumentException.class, () -> MemoryUtil.convertToMegabytes("1024a"));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> MemoryUtil.convertToMegabytes("1023.11"));
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> MemoryUtil.convertToMegabytes("abc"));
  }
}
