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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReflectionUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtils.class);
  private static final Map<String, Class> CLASS_MAP = new HashMap<>();

  public static Object invoke(String className, String methodName)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Class<?> classRef =
        CLASS_MAP.computeIfAbsent(
            className,
            key -> {
              try {
                return Class.forName(className);
              } catch (ClassNotFoundException e) {
                // ignore error
                return null;
              }
            });
    if (Objects.isNull(classRef)) {
      LOG.warn("cannot load class {}, skip execute method {}", className, methodName);
      return null;
    }
    Method method = classRef.getDeclaredMethod(methodName);
    return method.invoke(null);
  }
}
