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

package com.netease.arctic.utils;

import static org.junit.Assert.*;

import org.apache.paimon.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.*;

public class JacksonUtilsTest {

  @Test
  public void jacksonTest() {

    JsonTestBean testObject = new JsonTestBean();
    testObject.setBoolValue(true);
    testObject.setIntValue(3);
    testObject.setStringValue("abcd");
    Map<String, String> map = new HashMap<>();
    map.put("a", "b");
    testObject.setMapValue(map);

    String jasonString = JacksonUtils.toJSONString(testObject);

    String expectedValue =
        "{\"stringValue\":\"abcd\",\"intValue\":3,\"boolValue\":true,\"mapValue\":{\"a\":\"b\"}}";
    assertEquals(expectedValue, jasonString);

    JsonTestBean objReadFromJson = JacksonUtils.parseObject(jasonString, JsonTestBean.class);
    assertEquals(testObject, objReadFromJson);

    JsonNode jsonNode = JacksonUtils.fromObjects(objReadFromJson);

    Integer expectedInt = 3;
    assertEquals(expectedInt, JacksonUtils.getInteger(jsonNode, "intValue"));
    // return null if key does not exist
    assertNull(JacksonUtils.getInteger(jsonNode, "NoIntValue"));

    String expectedString = "abcd";
    assertEquals(expectedString, JacksonUtils.getString(jsonNode, "stringValue"));
    // return null if key does not exist
    assertNull(JacksonUtils.getString(jsonNode, "NoStringValue"));
    // return default value if key does not exist, and default value passed
    assertEquals("DefaultValue", JacksonUtils.getString(jsonNode, "NoStringValue", "DefaultValue"));

    assertTrue(JacksonUtils.getBoolean(jsonNode, "boolValue"));
    // returns false if key does not exist
    assertFalse(JacksonUtils.getBoolean(jsonNode, "NboolValue"));
    assertTrue(JacksonUtils.getBoolean(jsonNode, "NboolValue", true));

    Map<String, Object> yamlMap = new HashMap<>();
    Map<String, Object> amsNode = new HashMap<>();
    amsNode.put("arctic.ams.server-host.prefix", "127.");
    amsNode.put("arctic.ams.thrift.port", 1260);
    amsNode.put("arctic.ams.http.port", 1630);
    amsNode.put("arctic.ams.optimize.check.thread.pool-size", 10);
    amsNode.put("arctic.ams.optimize.commit.thread.pool-size", 10);
    amsNode.put("arctic.ams.expire.thread.pool-size", 10);
    amsNode.put("arctic.ams.orphan.clean.thread.pool-size", 10);
    amsNode.put("arctic.ams.file.sync.thread.pool-size", 10);
    amsNode.put(
        "arctic.ams.mybatis.ConnectionDriverClassName", "org.apache.derby.jdbc.EmbeddedDriver");
    amsNode.put("arctic.ams.mybatis.ConnectionURL", "jdbc:derby:/tmp/arctic/derby;create=true");
    amsNode.put("arctic.ams.database.type", "derby");
    amsNode.put("arctic.ams.ha.enabled", true);
    yamlMap.put("ams", amsNode);

    yamlMap.put("extension_properties", null);

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

    JsonNode yamlNode = JacksonUtils.fromObjects(yamlMap);

    Integer expectedPort = 1260;
    JsonNode systemNode = yamlNode.get("ams");
    assertEquals(expectedPort, JacksonUtils.getInteger(systemNode, "arctic.ams.thrift.port"));
    assertEquals(
        "jdbc:derby:/tmp/arctic/derby;create=true",
        JacksonUtils.getString(systemNode, "arctic.ams.mybatis.ConnectionURL"));
    assertTrue(JacksonUtils.getBoolean(systemNode, "arctic.ams.ha.enabled"));

    JsonNode containersNode = yamlNode.get("containers");
    assertEquals(2, containersNode.size());
    JsonNode secondNode = containersNode.get(1);
    assertEquals("flink", JacksonUtils.getString(secondNode, "type"));
    assertEquals("flinkContainer", JacksonUtils.getString(secondNode, "name"));
    JsonNode propertyiesNode = secondNode.get("properties");
    assertEquals(5, propertyiesNode.size());

    assertEquals("/opt/flink/", JacksonUtils.getString(propertyiesNode, "FLINK_HOME"));
    assertEquals("/etc/hadoop/conf/", JacksonUtils.getString(propertyiesNode, "HADOOP_CONF_DIR"));
    assertEquals("hadoop", JacksonUtils.getString(propertyiesNode, "HADOOP_USER_NAME"));
    assertEquals(
        "-Djava.security.krb5.conf=/opt/krb5.conf",
        JacksonUtils.getString(propertyiesNode, "JVM_ARGS"));
    assertEquals("/etc/hadoop/conf/", JacksonUtils.getString(propertyiesNode, "FLINK_CONF_DIR"));
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
