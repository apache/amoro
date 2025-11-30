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

import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ConfigOptionTest {
  ConfigOption<Duration> durationConfigOption =
      ConfigOptions.key("test.duration.option").durationType().defaultValue(Duration.ZERO);
  ConfigOption<List<String>> listOption =
      ConfigOptions.key("test.list.option")
          .stringType()
          .asList()
          .defaultValues("default1", "default2");
  ConfigOption<Map<String, String>> mapOption =
      ConfigOptions.key("test.map.option")
          .mapType()
          .defaultValue(ImmutableMap.of("key1", "value1", "key2", "value2"));

  @Test
  public void testConfigOption() {
    Map<String, String> configMap =
        ImmutableMap.<String, String>builder()
            .put("test.duration.option", "PT5M")
            .put("test.list.option", "value1;value2;value3")
            .put("test.map.option", "keyA:valueA,keyB:valueB")
            .build();
    Configurations config = Configurations.fromMap(configMap);
    Duration durationValue = config.get(durationConfigOption);
    assert durationValue.equals(Duration.ofMinutes(5));
    assert ConfigHelpers.convertToString(durationValue).equals("PT5M");
    List<String> listValue = config.get(listOption);
    assert listValue.size() == 3;
    assert listValue.get(0).equals("value1");
    assert listValue.get(1).equals("value2");
    assert listValue.get(2).equals("value3");
    assert ConfigHelpers.convertToString(listValue).equals("value1;value2;value3");
    Map<String, String> mapValue = config.get(mapOption);
    assert mapValue.size() == 2;
    assert mapValue.get("keyA").equals("valueA");
    assert mapValue.get("keyB").equals("valueB");
    assert ConfigHelpers.convertToString(mapValue).equals("keyA:valueA,keyB:valueB")
        || ConfigHelpers.convertToString(mapValue).equals("keyB:valueB,keyA:valueA");
  }
}
