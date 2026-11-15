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

package org.apache.amoro.utils.map;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;

/**
 * Memory estimates for spill-map payloads, retaining the previous shallow-object and one-level
 * container estimates.
 *
 * <p>Adapted from the Apache-2.0 portions of Apache Lucene 8.11.2 RamUsageEstimator and Constants:
 * https://github.com/apache/lucene-solr/blob/releases/lucene-solr/8.11.2/lucene/core/src/java/org/apache/lucene/util/RamUsageEstimator.java
 *
 * <p>Modified to use only JDK APIs and the types used by Amoro payloads. No Lucene search,
 * Accountable, array-growth or Unicode conversion implementation is included.
 */
final class ObjectSizeEstimator {
  private static final int REFERENCE_BYTES;
  private static final int OBJECT_HEADER_BYTES;
  private static final int ARRAY_HEADER_BYTES;
  private static final int ALIGNMENT;
  private static final long LONG_CACHE_MIN;
  private static final long LONG_CACHE_MAX;
  private static final long LONG_BYTES;
  private static final long STRING_BYTES;

  static {
    String model = System.getProperty("sun.arch.data.model");
    boolean is64Bit =
        model != null ? model.contains("64") : System.getProperty("os.arch", "").contains("64");
    boolean compressed = false;
    int alignment = 8;
    if (is64Bit) {
      try {
        Class<?> beanType = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
        Object bean =
            Class.forName("java.lang.management.ManagementFactory")
                .getMethod("getPlatformMXBean", Class.class)
                .invoke(null, beanType);
        if (bean != null) {
          Method getOption = beanType.getMethod("getVMOption", String.class);
          try {
            Object option = getOption.invoke(bean, "UseCompressedOops");
            compressed =
                Boolean.parseBoolean(
                    option.getClass().getMethod("getValue").invoke(option).toString());
          } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Same conservative fallback as the previous estimator on unsupported JVMs.
          }
          try {
            Object option = getOption.invoke(bean, "ObjectAlignmentInBytes");
            alignment =
                Integer.parseInt(option.getClass().getMethod("getValue").invoke(option).toString());
          } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Keep the default alignment when the VM does not expose this option.
          }
        }
      } catch (ReflectiveOperationException | RuntimeException ignored) {
        // Non-HotSpot JVMs use the same fallback object layout as before.
      }
    }
    ALIGNMENT = alignment;
    REFERENCE_BYTES = is64Bit && !compressed ? 8 : 4;
    OBJECT_HEADER_BYTES = is64Bit ? 8 + REFERENCE_BYTES : 8;
    ARRAY_HEADER_BYTES =
        is64Bit
            ? (int) align(OBJECT_HEADER_BYTES + Integer.BYTES)
            : OBJECT_HEADER_BYTES + Integer.BYTES;
    long min = 0;
    while (min > Long.MIN_VALUE && Long.valueOf(min - 1) == Long.valueOf(min - 1)) {
      min--;
    }
    long max = -1;
    while (max < Long.MAX_VALUE && Long.valueOf(max + 1) == Long.valueOf(max + 1)) {
      max++;
    }
    LONG_CACHE_MIN = min;
    LONG_CACHE_MAX = max;
    LONG_BYTES = shallowInstance(Long.class);
    STRING_BYTES = shallowInstance(String.class);
  }

  private ObjectSizeEstimator() {}

  static long sizeOf(Object value) {
    return sizeOf(value, 0);
  }

  private static long sizeOf(Object value, int depth) {
    if (value == null) {
      return 0;
    }
    if (value instanceof String) {
      // Keep the previous UTF-16 estimate even on VMs using compact strings.
      return align(
          STRING_BYTES + ARRAY_HEADER_BYTES + (long) Character.BYTES * ((String) value).length());
    }
    if (value instanceof Long) {
      long number = (Long) value;
      return number >= LONG_CACHE_MIN && number <= LONG_CACHE_MAX ? 0 : LONG_BYTES;
    }
    if (value instanceof String[]) {
      long size = shallow(value);
      for (String string : (String[]) value) {
        size += sizeOf(string, depth);
      }
      return size;
    }
    if (value instanceof Map) {
      long size = shallow(value);
      if (++depth > 1) {
        return size;
      }
      long entryBytes = -1;
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        if (entryBytes == -1) {
          entryBytes = shallow(entry);
        }
        size += entryBytes + sizeOf(entry.getKey(), depth) + sizeOf(entry.getValue(), depth);
      }
      return align(size);
    }
    if (value instanceof Collection) {
      long size = shallow(value);
      if (++depth > 1) {
        return size;
      }
      Collection<?> collection = (Collection<?>) value;
      size += ARRAY_HEADER_BYTES + collection.size() * REFERENCE_BYTES;
      for (Object element : collection) {
        size += sizeOf(element, depth);
      }
      return align(size);
    }
    return shallow(value);
  }

  private static long shallow(Object value) {
    Class<?> type = value.getClass();
    if (type.isArray()) {
      Class<?> component = type.getComponentType();
      return align(
          ARRAY_HEADER_BYTES
              + (long) Array.getLength(value)
                  * (component.isPrimitive() ? primitiveSize(component) : REFERENCE_BYTES));
    }
    return shallowInstance(type);
  }

  private static long shallowInstance(Class<?> type) {
    long size = OBJECT_HEADER_BYTES;
    for (Class<?> current = type; current != null; current = current.getSuperclass()) {
      Class<?> inspected = current;
      Field[] fields =
          AccessController.doPrivileged((PrivilegedAction<Field[]>) inspected::getDeclaredFields);
      for (Field field : fields) {
        if (!Modifier.isStatic(field.getModifiers())) {
          size += field.getType().isPrimitive() ? primitiveSize(field.getType()) : REFERENCE_BYTES;
        }
      }
    }
    return align(size);
  }

  private static int primitiveSize(Class<?> type) {
    if (type == boolean.class || type == byte.class) {
      return 1;
    }
    if (type == char.class || type == short.class) {
      return 2;
    }
    if (type == int.class || type == float.class) {
      return 4;
    }
    if (type == long.class || type == double.class) {
      return 8;
    }
    throw new IllegalArgumentException("Unsupported primitive type: " + type);
  }

  private static long align(long size) {
    long padded = size + ALIGNMENT - 1L;
    return padded - padded % ALIGNMENT;
  }
}
