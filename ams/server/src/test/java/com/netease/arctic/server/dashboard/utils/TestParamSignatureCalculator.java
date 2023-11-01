/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
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

package com.netease.arctic.server.dashboard.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestParamSignatureCalculator {

  @Test
  public void testGenerateParamStringWithValueList() {
    // Prepare input and mock classes
    Map<String, List<String>> testMap = new HashMap<>();
    testMap.put("name", Collections.singletonList(""));
    testMap.put("value", Collections.singletonList("111"));
    testMap.put("age", Collections.singletonList("11"));
    testMap.put("sex", Collections.singletonList("1"));
    testMap.put("high", Collections.singletonList("180"));
    testMap.put("nick", Collections.singletonList(""));

    // Call the actual method
    String result = ParamSignatureCalculator.generateParamStringWithValueList(testMap);

    // Assert the result
    Assertions.assertEquals("age11high180sex1value111", result);
  }

  @Test
  void generateTablePageToken() {
    Map<String, String> map = new HashMap<>();
    map.put("age", "11");
    map.put("value", "");
    map.put("high", "180");
    map.put("sex", "1");
    map.put("name", "111");

    String result = ParamSignatureCalculator.generateParamStringWithValue(map);

    // The expected string is: age11high180sex1value111
    Assertions.assertEquals("age11high180name111sex1", result);
  }
}
