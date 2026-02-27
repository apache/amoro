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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JacksonUtilTest {

  @Test
  public void jacksonTest() {

    JsonTestBean testObject = new JsonTestBean();
    testObject.setBoolValue(true);
    testObject.setIntValue(3);
    testObject.setStringValue("abcd");
    Map<String, String> map = new HashMap<>();
    map.put("a", "b");
    testObject.setMapValue(map);

    String jasonString = JacksonUtil.toJSONString(testObject);

    String expectedValue =
        "{\"stringValue\":\"abcd\",\"intValue\":3,\"boolValue\":true,\"mapValue\":{\"a\":\"b\"}}";
    assertEquals(expectedValue, jasonString);

    JsonTestBean objReadFromJson = JacksonUtil.parseObject(jasonString, JsonTestBean.class);
    assertEquals(testObject, objReadFromJson);

    JsonNode jsonNode = JacksonUtil.fromObjects(objReadFromJson);

    Integer expectedInt = 3;
    assertEquals(expectedInt, JacksonUtil.getInteger(jsonNode, "intValue"));
    // return null if key does not exist
    assertNull(JacksonUtil.getInteger(jsonNode, "NoIntValue"));

    String expectedString = "abcd";
    assertEquals(expectedString, JacksonUtil.getString(jsonNode, "stringValue"));
    // return null if key does not exist
    assertNull(JacksonUtil.getString(jsonNode, "NoStringValue"));
    // return default value if key does not exist, and default value passed
    assertEquals("DefaultValue", JacksonUtil.getString(jsonNode, "NoStringValue", "DefaultValue"));

    assertTrue(JacksonUtil.getBoolean(jsonNode, "boolValue"));
    // returns false if key does not exist
    assertFalse(JacksonUtil.getBoolean(jsonNode, "NboolValue"));
    assertTrue(JacksonUtil.getBoolean(jsonNode, "NboolValue", true));

    Map<String, Object> yamlMap = new HashMap<>();
    Map<String, Object> amsNode = new HashMap<>();
    amsNode.put("admin-username", "admin");
    amsNode.put("admin-password", "admin");
    amsNode.put("server-bind-host", "0.0.0.0");
    amsNode.put("server-expose-host", "127.0.0.1");

    yamlMap.put("ams", amsNode);

    Map<String, Object> expireSnapshots = new HashMap<>();
    expireSnapshots.put("enabled", true);
    expireSnapshots.put("thread-count", 10);
    yamlMap.put("expire-snapshots", expireSnapshots);

    List<Map<String, Object>> containersList = new ArrayList<>();
    Map<String, Object> localContainer = new HashMap<>();
    localContainer.put("name", "localContainer");
    localContainer.put("type", "local");
    localContainer.put("properties", Collections.singletonMap("hadoop_home", "/opt/hadoop"));

    containersList.add(localContainer);

    Map<String, Object> flinkContainer = new HashMap<>();
    flinkContainer.put("name", "flinkContainer");
    flinkContainer.put("type", "flink");
    Map<String, String> pro = new HashMap<>();
    pro.put("FLINK_HOME", "/opt/flink/");
    pro.put("HADOOP_CONF_DIR", "/etc/hadoop/conf/");
    pro.put("HADOOP_USER_NAME", "hadoop");
    pro.put("JVM_ARGS", "-Djava.security.krb5.conf=/opt/krb5.conf");
    pro.put("FLINK_CONF_DIR", "/etc/hadoop/conf/");
    flinkContainer.put("properties", pro);
    containersList.add(flinkContainer);

    yamlMap.put("containers", containersList);

    JsonNode yamlNode = JacksonUtil.fromObjects(yamlMap);

    JsonNode expiredSnapshots = yamlNode.get("expire-snapshots");
    assertTrue(JacksonUtil.getBoolean(expiredSnapshots, "enabled"));
    Integer expectedThreadCount = 10;
    assertEquals(expectedThreadCount, JacksonUtil.getInteger(expiredSnapshots, "thread-count"));
    JsonNode containersNode = yamlNode.get("containers");
    assertEquals(2, containersNode.size());
    JsonNode secondNode = containersNode.get(1);
    assertEquals("flink", JacksonUtil.getString(secondNode, "type"));
    assertEquals("flinkContainer", JacksonUtil.getString(secondNode, "name"));
    JsonNode propertyiesNode = secondNode.get("properties");
    assertEquals(5, propertyiesNode.size());

    assertEquals("/opt/flink/", JacksonUtil.getString(propertyiesNode, "FLINK_HOME"));
    assertEquals("/etc/hadoop/conf/", JacksonUtil.getString(propertyiesNode, "HADOOP_CONF_DIR"));
    assertEquals("hadoop", JacksonUtil.getString(propertyiesNode, "HADOOP_USER_NAME"));
    assertEquals(
        "-Djava.security.krb5.conf=/opt/krb5.conf",
        JacksonUtil.getString(propertyiesNode, "JVM_ARGS"));
    assertEquals("/etc/hadoop/conf/", JacksonUtil.getString(propertyiesNode, "FLINK_CONF_DIR"));
  }

  static class JsonTestBean {

    private String stringValue;

    private Integer intValue;

    private Boolean boolValue;

    private Map<String, String> mapValue;

    public String getStringValue() {
      return stringValue;
    }

    public void setStringValue(String stringValue) {
      this.stringValue = stringValue;
    }

    public Integer getIntValue() {
      return intValue;
    }

    public void setIntValue(Integer intValue) {
      this.intValue = intValue;
    }

    public Boolean getBoolValue() {
      return boolValue;
    }

    public void setBoolValue(Boolean boolValue) {
      this.boolValue = boolValue;
    }

    public Map<String, String> getMapValue() {
      return mapValue;
    }

    public void setMapValue(Map<String, String> mapValue) {
      this.mapValue = mapValue;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof JsonTestBean)) {
        return false;
      }

      JsonTestBean that = (JsonTestBean) obj;
      return Objects.equals(intValue, that.intValue)
          && Objects.equals(boolValue, that.boolValue)
          && Objects.equals(stringValue, that.stringValue)
          && Objects.equals(mapValue, that.mapValue);
    }
  }
}
