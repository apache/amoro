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

package org.apache.amoro.table;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StateKey<T> {
  private final String key;
  private final T defaultValue;

  private final Function<T, String> serializer;
  private final Function<String, T> deserializer;

  private final BiFunction<T, T, Boolean> valueComparator;

  public static StateKeyBuilder stateKey(String key) {
    return new StateKeyBuilder(key);
  }

  public StateKey(
      String key,
      T defaultValue,
      Function<T, String> serializer,
      Function<String, T> deserializer) {
    this(key, defaultValue, serializer, deserializer, Objects::equals);
  }

  public StateKey(
      String key,
      T defaultValue,
      Function<T, String> serializer,
      Function<String, T> deserializer,
      BiFunction<T, T, Boolean> valueComparator) {
    Preconditions.checkArgument(
        !StringUtils.isEmpty(key), "TableRuntime state key cannot be null or empty");
    Preconditions.checkNotNull(defaultValue, "TableRuntime state default value cannot be null");
    this.key = key;
    this.defaultValue = defaultValue;
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.valueComparator = valueComparator;
  }

  public String getKey() {
    return key;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public String serialize(T value) {
    Preconditions.checkNotNull(value, "TableRuntime state does not support null value");
    return serializer.apply(value);
  }

  public String serializeDefault() {
    return serializer.apply(defaultValue);
  }

  public T deserialize(String storedValue) {
    if (StringUtils.isEmpty(key)) {
      return defaultValue;
    }
    return deserializer.apply(storedValue);
  }

  public BiFunction<T, T, Boolean> getValueComparator() {
    return valueComparator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && o.getClass() == StateKey.class) {
      StateKey<?> that = (StateKey<?>) o;
      return key.equals(that.key);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(key);
  }

  @Override
  public String toString() {
    return "StateKey[key=" + key + "]";
  }

  public static class StateKeyBuilder {
    static ObjectMapper MAPPER = new ObjectMapper();

    static {
      MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    static TypeReference<Map<String, String>> propertiesMapTypeRef =
        new TypeReference<Map<String, String>>() {};

    private final String key;

    private StateKeyBuilder(String key) {
      this.key = key;
    }

    public TypedKeyBuilder<Long> longType() {
      return new TypedKeyBuilder<>(key, Object::toString, Long::valueOf);
    }

    public TypedKeyBuilder<String> stringType() {
      return new TypedKeyBuilder<>(key, Function.identity(), Function.identity());
    }

    public <T> TypedKeyBuilder<T> jsonType(Class<T> clazz) {
      return new TypedKeyBuilder<>(
          key,
          t -> {
            try {
              return MAPPER.writeValueAsString(t);
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          },
          json -> {
            try {
              return MAPPER.readValue(json, clazz);
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          });
    }

    public StateKey<Map<String, String>> propertiesType() {
      return new StateKey<>(
          key,
          Collections.emptyMap(),
          map -> {
            try {
              return MAPPER.writeValueAsString(map);
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          },
          storedValue -> {
            try {
              return MAPPER.readValue(storedValue, propertiesMapTypeRef);
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          },
          (a, b) -> Maps.difference(a, b).areEqual());
    }
  }

  public static class TypedKeyBuilder<T> {
    private final String key;
    private final Function<T, String> serializer;
    private final Function<String, T> deserializer;

    public TypedKeyBuilder(
        String key, Function<T, String> serializer, Function<String, T> deserializer) {
      this.key = key;
      this.serializer = serializer;
      this.deserializer = deserializer;
    }

    public StateKey<T> defaultValue(T defaultValue) {
      return new StateKey<>(key, defaultValue, serializer, deserializer);
    }
  }
}
