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

package org.apache.amoro.server.dashboard;

import io.javalin.plugin.json.JsonMapper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.module.SimpleModule;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Json mapper to adapt shaded jackson. */
public class JavalinJsonMapper implements JsonMapper {

  private final ObjectMapper objectMapper;

  public static JavalinJsonMapper createDefaultJsonMapper() {
    ObjectMapper om = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(TableFormat.class, new TableFormat.JsonSerializer());
    module.addDeserializer(TableFormat.class, new TableFormat.JsonDeserializer());
    om.registerModule(module);
    return new JavalinJsonMapper(om);
  }

  public JavalinJsonMapper(ObjectMapper shadedMapper) {
    this.objectMapper = shadedMapper;
  }

  @NotNull
  @Override
  public String toJsonString(@NotNull Object obj) {
    if (obj instanceof String) {
      return (String) obj;
    }
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public InputStream toJsonStream(@NotNull Object obj) {
    if (obj instanceof String) {
      String result = (String) obj;
      return new ByteArrayInputStream(result.getBytes());
    } else {
      byte[] string = new byte[0];
      try {
        string = objectMapper.writeValueAsBytes(obj);
        return new ByteArrayInputStream(string);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @NotNull
  @Override
  public <T> T fromJsonString(@NotNull String json, @NotNull Class<T> targetClass) {
    try {
      return objectMapper.readValue(json, targetClass);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public <T> T fromJsonStream(@NotNull InputStream json, @NotNull Class<T> targetClass) {
    try {
      return objectMapper.readValue(json, targetClass);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
