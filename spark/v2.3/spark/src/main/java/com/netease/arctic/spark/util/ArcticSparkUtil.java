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

package com.netease.arctic.spark.util;

import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.util.ByteBuffers;
import org.apache.spark.sql.RuntimeConfig;
import org.apache.spark.sql.types.BinaryType;
import org.apache.spark.sql.types.Decimal;
import org.apache.spark.unsafe.types.UTF8String;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

public class ArcticSparkUtil {
  public static final String CATALOG_URL = "arctic.catalog.url";
  public static final String SQL_DELEGATE_HIVE_TABLE = "spark.arctic.sql.delegate.enable";

  public static String catalogUrl(RuntimeConfig conf) {
    String catalogUrl = conf.get(CATALOG_URL, "");
    if (StringUtils.isBlank(catalogUrl)) {
      throw new IllegalArgumentException("arctic.catalog.url is blank");
    }
    return catalogUrl;
  }

  public static boolean delegateHiveTable(RuntimeConfig config) {
    String val = config.get(SQL_DELEGATE_HIVE_TABLE, "true");
    return Boolean.parseBoolean(val);
  }

  public static Object convertConstant(Type type, Object value) {
    if (value == null) {
      return null;
    }

    switch (type.typeId()) {
      case DECIMAL:
        return Decimal.apply((BigDecimal) value);
      case STRING:
        if (value instanceof Utf8) {
          Utf8 utf8 = (Utf8) value;
          return UTF8String.fromBytes(utf8.getBytes(), 0, utf8.getByteLength());
        }
        return UTF8String.fromString(value.toString());
      case FIXED:
        if (value instanceof byte[]) {
          return value;
        } else if (value instanceof GenericData.Fixed) {
          return ((GenericData.Fixed) value).bytes();
        }
        return ByteBuffers.toByteArray((ByteBuffer) value);
      case BINARY:
        return ByteBuffers.toByteArray((ByteBuffer) value);
      default:
    }
    return value;
  }

  public static Object[] convertRowObject(Object[] objects) {
    for (int i = 0; i < objects.length; i++) {
      Object object = objects[i];
      if (object instanceof UTF8String) {
        objects[i] = object.toString();
      } else if (object instanceof Decimal) {
        objects[i] = ((Decimal) object).toJavaBigDecimal();
      } else if (object instanceof BinaryType) {
        objects[i] = ByteBuffer.wrap((byte[]) object);
      }
    }
    return objects;
  }

}
