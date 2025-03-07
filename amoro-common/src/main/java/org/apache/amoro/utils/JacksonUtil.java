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

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Util class for Jackson. */
public class JacksonUtil {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final ObjectMapper INDENT_OBJECT_MAPPER = new ObjectMapper();

  private static final String SERIALIZE_ERROR = "serialize object error";

  private static final String DESERIALIZE_ERROR = "deserialize to object error";

  static {
    OBJECT_MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    INDENT_OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

  private JacksonUtil() {
    // do nothing
  }

  public static ObjectNode createEmptyObjectNode() {
    return OBJECT_MAPPER.createObjectNode();
  }

  public static String toJSONString(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(SERIALIZE_ERROR, e);
    }
  }

  /**
   * Deserialize a json object from string which serialized using {@link #toJSONString(Object)} }.
   */
  public static <T> T parseObject(String jsonString, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(jsonString, clazz);
    } catch (IOException e) {
      throw new IllegalStateException(DESERIALIZE_ERROR, e);
    }
  }

  /** Convert an java object(usually java bean) to JsonNode. */
  public static <T> JsonNode fromObjects(T object) {
    return OBJECT_MAPPER.valueToTree(object);
  }

  public static Integer getInteger(JsonNode jsonNode, String fieldName) {
    Preconditions.checkNotNull(jsonNode, "JsonNode should not be null.");

    JsonNode valueNode = jsonNode.get(fieldName);
    if (valueNode == null) {
      return null;
    }

    if (valueNode.isInt()) {
      return valueNode.intValue();
    }
    throw new ClassCastException("can not cast to int, value : " + valueNode);
  }

  public static Boolean getBoolean(JsonNode jsonNode, String fieldName, boolean defaultValue) {
    Preconditions.checkNotNull(jsonNode, "JsonNode should not be null.");
    JsonNode value = jsonNode.get(fieldName);
    if (value == null) {
      return defaultValue;
    }

    if (value.isBoolean()) {
      return value.booleanValue();
    }
    throw new ClassCastException("can not cast to bool, value : " + value);
  }

  public static Boolean getBoolean(JsonNode jsonNode, String fieldName) {
    return getBoolean(jsonNode, fieldName, false);
  }

  public static String getString(JsonNode jsonNode, String fieldName) {
    return getString(jsonNode, fieldName, null);
  }

  public static String getString(JsonNode jsonNode, String fieldName, String defaultValue) {
    Preconditions.checkNotNull(jsonNode, "JsonNode should not be null");
    JsonNode textNode = jsonNode.get(fieldName);

    if (textNode == null) {
      return defaultValue;
    }
    return textNode.asText();
  }

  /** Retrieve the given map from a JsonNode, and return empty map if the field doesn't exist. */
  public static <K, V> Map<K, V> getMap(
      JsonNode jsonNode, String fieldName, TypeReference<Map<K, V>> mapType) {
    Preconditions.checkNotNull(jsonNode, "JsonNode should not be null");

    JsonNode mapNode = jsonNode.get(fieldName);
    if (mapNode == null) {
      return new HashMap<>();
    }

    return OBJECT_MAPPER.convertValue(mapNode, mapType);
  }
}
